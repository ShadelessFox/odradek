package sh.adelessfox.odradek.app.cli.data;

import picocli.CommandLine;

public final class ObjectIdConverter implements CommandLine.ITypeConverter<ObjectId> {
    @Override
    public ObjectId convert(String value) {
        var parts = value.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected object id to be in form of groupId:objectIndex");
        }
        return new ObjectId(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1])
        );
    }
}
