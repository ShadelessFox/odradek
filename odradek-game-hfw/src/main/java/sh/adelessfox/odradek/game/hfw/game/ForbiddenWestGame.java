package sh.adelessfox.odradek.game.hfw.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeFactory;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.game.hfw.storage.ObjectStreamingSystem;
import sh.adelessfox.odradek.game.hfw.storage.StorageReadDevice;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.util.ProductVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ForbiddenWestGame implements Game {
    private static final Logger log = LoggerFactory.getLogger(ForbiddenWestGame.class);

    private final StreamingGraphResource streamingGraph;
    private final StorageReadDevice storageDevice;
    private final ObjectStreamingSystem streamingSystem;
    private final StreamingObjectReader streamingReader;

    public ForbiddenWestGame(Path source, EPlatform platform) throws IOException {
        var version = Optional.of(source.resolve("HorizonForbiddenWest.exe"))
            .filter(Files::exists)
            .flatMap(ProductVersion::probe)
            .map(ProductVersion::toString)
            .orElse("Unknown");

        log.debug("[GAME] Source:   {}", source);
        log.debug("[GAME] Platform: {}", platform);
        log.debug("[GAME] Version:  {}", version);

        var fileSystem = new ForbiddenWestFileSystem(source, platform);

        log.debug("Loading type factory");
        var typeFactory = new HFWTypeFactory();

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

    public byte[] readDataSource(HorizonForbiddenWest.StreamingDataSource dataSource) throws IOException {
        if (dataSource.length() == 0) {
            return new byte[0];
        }
        return getStreamingSystem().getDataSourceData(dataSource);
    }

    public byte[] readDataSourceUnchecked(HorizonForbiddenWest.StreamingDataSource dataSource) {
        try {
            return readDataSource(dataSource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TypedObject readObject(int groupId, int objectIndex) throws IOException {
        synchronized (streamingReader) {
            return streamingReader.readGroup(groupId).objects().get(objectIndex).object();
        }
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

    @Override
    public void close() throws IOException {
        storageDevice.close();
    }

    private static StreamingGraphResource readStreamingGraph(ForbiddenWestFileSystem fileSystem, TypeFactory typeFactory) throws IOException {
        try (var reader = BinaryReader.open(fileSystem.resolve("cache:package/streaming_graph.core"))) {
            var result = new HFWTypeReader().readObject(reader, typeFactory);
            var graph = (HorizonForbiddenWest.StreamingGraphResource) result.object();
            return new StreamingGraphResource(graph, typeFactory);
        }
    }
}
