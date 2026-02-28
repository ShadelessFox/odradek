package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.data.TypePath;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface LinkProvider extends Closeable {
    record Link(int groupId, int objectIndex, TypePath path) {
    }

    List<Link> getIncomingLinks(ObjectId target) throws IOException;

    List<Link> getOutgoingLinks(ObjectId source) throws IOException;
}
