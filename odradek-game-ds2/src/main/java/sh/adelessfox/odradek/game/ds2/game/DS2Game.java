package sh.adelessfox.odradek.game.ds2.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.ELanguage;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.EPlatform;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.StreamingDataSource;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeFactory;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.game.ds2.storage.ObjectStreamingSystem;
import sh.adelessfox.odradek.game.ds2.storage.StorageReadDevice;
import sh.adelessfox.odradek.game.ds2.storage.StreamingGraphResource;
import sh.adelessfox.odradek.game.ds2.storage.StreamingObjectReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.util.system.ProductVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class DS2Game implements Game {
    public static final class Provider implements Game.Provider {
        @Override
        public boolean supports(Path path) {
            return Files.exists(path.resolve("DS2.exe"));
        }

        @Override
        public Game load(Path path) throws IOException {
            return new DS2Game(path, EPlatform.WinGame);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DS2Game.class);

    private final StreamingGraphResource streamingGraph;
    private final StorageReadDevice storageDevice;
    private final ObjectStreamingSystem streamingSystem;
    private final StreamingObjectReader streamingReader;
    private final DS2FileSystem fileSystem;

    // NOTE: Add customization later
    private final ELanguage writtenLanguage = ELanguage.English;
    private final ELanguage spokenLanguage = ELanguage.English;

    public DS2Game(Path source, EPlatform platform) throws IOException {
        var version = Optional.of(source.resolve("DS2.exe"))
            .filter(Files::exists)
            .flatMap(ProductVersion::probe)
            .map(ProductVersion::toString)
            .orElse("Unknown");

        log.debug("[GAME] Source:   {}", source);
        log.debug("[GAME] Platform: {}", platform);
        log.debug("[GAME] Version:  {}", version);

        fileSystem = new DS2FileSystem(source, platform);

        log.debug("Loading type factory");
        var typeFactory = new DS2TypeFactory();

        log.debug("Loading streaming graph");
        streamingGraph = readStreamingGraph(fileSystem, typeFactory);

        log.debug("Loading storage files");
        storageDevice = new StorageReadDevice(fileSystem);

        for (String file : streamingGraph.files()) {
            storageDevice.mount(file);
        }

        streamingSystem = new ObjectStreamingSystem(storageDevice, streamingGraph);
        streamingReader = new StreamingObjectReader(streamingSystem, typeFactory);
    }

    public byte[] readDataSource(StreamingDataSource dataSource) {
        try {
            return getStreamingSystem().getDataSourceData(dataSource);
        } catch (IOException e) {
            // FIXME: Throwing unchecked exceptions is not ideal. Think about proper exception handling
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TypedObject readObject(int groupId, int objectIndex) throws IOException {
        synchronized (streamingReader) {
            return streamingReader.readGroup(groupId).objects().get(objectIndex);
        }
    }

    @Override
    public Path resolvePath(String path) {
        return fileSystem.resolve(path);
    }

    public StreamingGraphResource getStreamingGraph() {
        return streamingGraph;
    }

    public ObjectStreamingSystem getStreamingSystem() {
        return streamingSystem;
    }

    public StreamingObjectReader getStreamingReader() {
        return streamingReader;
    }

    public ELanguage getWrittenLanguage() {
        return writtenLanguage;
    }

    public ELanguage getSpokenLanguage() {
        return spokenLanguage;
    }

    @Override
    public void close() throws IOException {
        storageDevice.close();
    }

    private static StreamingGraphResource readStreamingGraph(
        DS2FileSystem fileSystem,
        TypeFactory typeFactory
    ) throws IOException {
        try (var reader = BinaryReader.open(fileSystem.resolve("cache:package/streaming_graph.core"))) {
            var result = new DS2TypeReader().readObject(reader, typeFactory);
            var graph = (DS2.StreamingGraphResource) result;
            return new StreamingGraphResource(graph, typeFactory);
        }
    }
}
