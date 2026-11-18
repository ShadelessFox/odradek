package sh.adelessfox.odradek.parsing;

public class SyntaxException extends RuntimeException {
    private final Location location;

    public SyntaxException(String message, Location location) {
        super(message);
        this.location = location;
    }

    public SyntaxException(String message, Throwable cause, Location location) {
        super(message, cause);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at " + location;
    }
}
