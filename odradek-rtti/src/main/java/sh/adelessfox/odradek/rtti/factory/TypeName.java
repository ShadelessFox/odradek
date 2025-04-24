package sh.adelessfox.odradek.rtti.factory;

public sealed interface TypeName extends Comparable<TypeName> {
    static TypeName of(String name) {
        return new Simple(name);
    }

    static TypeName of(String name, TypeName argument) {
        return new Parameterized(name, argument);
    }

    static TypeName parse(String name) {
        int start = name.indexOf('<');
        if (start < 0) {
            return of(name);
        }
        int end = name.lastIndexOf('>');
        if (start == 0 || end < start + 1) {
            throw new IllegalArgumentException("Invalid parameterized name: '" + name + "'");
        }
        String rawType = name.substring(0, start);
        String argumentType = name.substring(start + 1, end);
        return of(rawType, parse(argumentType));
    }

    String fullName();

    @Override
    default int compareTo(TypeName o) {
        return fullName().compareTo(o.fullName());
    }

    record Simple(String name) implements TypeName {
        @Override
        public String fullName() {
            return name;
        }

        @Override
        public String toString() {
            return fullName();
        }
    }

    record Parameterized(String name, TypeName argument) implements TypeName {
        @Override
        public String fullName() {
            return name + '<' + argument.fullName() + '>';
        }

        @Override
        public String toString() {
            return fullName();
        }
    }
}
