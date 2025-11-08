package sh.adelessfox.odradek.app.cli.data;

public record ObjectId(int groupId, int objectIndex) {
    @Override
    public String toString() {
        return groupId + ":" + objectIndex;
    }
}
