package sh.adelessfox.odradek.game;

public record ObjectId(int groupId, int objectIndex) {
    public static ObjectId valueOf(String value) {
        var parts = value.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected object id to be in form of groupId:objectIndex");
        }
        return new ObjectId(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1])
        );
    }

    @Override
    public String toString() {
        return groupId + ":" + objectIndex;
    }
}
