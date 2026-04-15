package sh.adelessfox.odradek.game.hfw.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeFactory;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphImpl;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphStorage;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.util.system.ProductVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class ForbiddenWestGame implements DecimaGame {
    public static final class Provider implements Game.Provider {
        @Override
        public boolean supports(Path path) {
            return Files.exists(path.resolve("HorizonForbiddenWest.exe"));
        }

        @Override
        public Game load(Path path) throws IOException {
            return new ForbiddenWestGame(path, EPlatform.WinGame);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ForbiddenWestGame.class);

    private final StreamingGraphImpl streamingGraphWrapper;
    private final StreamingGraphStorage storage;
    private final StreamingObjectReader streamingReader;
    private final FileSystem fileSystem;

    // NOTE: Add customization later
    private final ELanguage writtenLanguage = ELanguage.English;
    private final ELanguage spokenLanguage = ELanguage.English;

    public ForbiddenWestGame(Path source, EPlatform platform) throws IOException {
        var version = Optional.of(source.resolve("HorizonForbiddenWest.exe"))
            .filter(Files::exists)
            .flatMap(ProductVersion::probe)
            .map(ProductVersion::toString)
            .orElse("Unknown");

        log.debug("[GAME] Source:   {}", source);
        log.debug("[GAME] Platform: {}", platform);
        log.debug("[GAME] Version:  {}", version);

        fileSystem = new FileSystem(source, platform);

        log.debug("Loading type factory");
        var typeFactory = new HFWTypeFactory();

        log.debug("Loading streaming graph");
        var graph = readStreamingGraph(fileSystem, typeFactory);

        log.debug("Loading storage files");
        storage = new StreamingGraphStorage(this);

        for (String file : graph.files()) {
            storage.mount(file);
        }

        streamingGraphWrapper = new StreamingGraphImpl(graph, storage, typeFactory);
        streamingReader = new StreamingObjectReader(this, typeFactory);
    }

    public byte[] readDataSource(HorizonForbiddenWest.StreamingDataSource dataSource) {
        try {
            return getDataSourceData(dataSource);
        } catch (IOException e) {
            // FIXME: Throwing unchecked exceptions is not ideal. Think about proper exception handling
            throw new UncheckedIOException(e);
        }
    }

    public byte[] getDataSourceData(HorizonForbiddenWest.StreamingDataSource dataSource) throws IOException {
        return getDataSourceData(dataSource, dataSource.offset(), dataSource.length());
    }

    public byte[] getDataSourceData(HorizonForbiddenWest.StreamingDataSource dataSource, int offset, int length) throws IOException {
        return getFileData(dataSource.fileId(), (long) dataSource.fileOffset() + offset, length);
    }

    private byte[] getFileData(int fileId, long offset, long length) throws IOException {
        return readFile(streamingGraphWrapper.files().get(fileId), offset, length);
    }

    @Override
    public List<TypedObject> readGroup(int groupId, boolean readSubgroups) throws IOException {
        synchronized (streamingReader) {
            return streamingReader.readGroup(groupId, readSubgroups).objects();
        }
    }

    @Override
    public byte[] readFile(String file, long offset, long length) throws IOException {
        return storage.read(file, offset, length);
    }

    @Override
    public Path resolvePath(String path) {
        return fileSystem.resolve(path);
    }

    @Override
    public StreamingGraph streamingGraph() {
        return streamingGraphWrapper;
    }

    public ELanguage getWrittenLanguage() {
        return writtenLanguage;
    }

    public ELanguage getSpokenLanguage() {
        return spokenLanguage;
    }

    @Override
    public void close() throws IOException {
        storage.close();
    }

    private static HorizonForbiddenWest.StreamingGraphResource readStreamingGraph(FileSystem fileSystem, TypeFactory typeFactory) throws IOException {
        try (var reader = BinaryReader.open(fileSystem.resolve("cache:package/streaming_graph.core"))) {
            var result = new HFWTypeReader().readObject(reader, typeFactory);
            return (HorizonForbiddenWest.StreamingGraphResource) result;
        }
    }

    private record FileSystem(Path source, EPlatform platform) {
        public Path resolve(String path) {
            String[] parts = path.split(":", 2);
            return switch (parts[0]) {
                case "source" -> source.resolve(parts[1]);
                case "cache" -> resolve("source:LocalCache" + platform).resolve(parts[1]);
                case "tools" -> resolve("source:tools").resolve(parts[1]);
                default -> throw new IllegalArgumentException("Unknown device path: " + path);
            };
        }
    }
}
