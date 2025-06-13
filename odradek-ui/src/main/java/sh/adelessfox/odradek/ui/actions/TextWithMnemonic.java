package sh.adelessfox.odradek.ui.actions;

record TextWithMnemonic(String text, int mnemonicIndex) {
    public static TextWithMnemonic parse(String name) {
        int index = name.indexOf('&');
        if (index >= 0 && name.length() > index + 1 && name.charAt(index + 1) != '&') {
            return new TextWithMnemonic(name.substring(0, index) + name.substring(index + 1), index);
        } else {
            return new TextWithMnemonic(name, -1);
        }
    }

    public boolean hasMnemonic() {
        return mnemonicIndex >= 0;
    }

    public int mnemonicChar() {
        return hasMnemonic() ? text.charAt(mnemonicIndex) : 0;
    }
}
