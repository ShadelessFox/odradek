package sh.adelessfox.odradek.rtti;

import java.util.List;

public interface EnumValueInfo {
    String name();

    int value();

    List<String> aliases();
}
