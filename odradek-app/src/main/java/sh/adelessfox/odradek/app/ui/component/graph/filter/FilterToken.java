package sh.adelessfox.odradek.app.ui.component.graph.filter;

sealed interface FilterToken {
    int offset();

    default String toDisplayString() {
        return switch (this) {
            case Name(var value, _) -> "'" + value + "'";
            case Number(var value, _) -> Integer.toString(value);
            case Open _ -> "'('";
            case Close _ -> "')'";
            case Colon _ -> "':'";
            case Not _ -> "'not'";
            case And _ -> "'and'";
            case Or _ -> "'or'";
            case End _ -> "<end of input>";
        };
    }

    record Name(String value, int offset) implements FilterToken {
    }

    record Number(int value, int offset) implements FilterToken {
    }

    record Open(int offset) implements FilterToken {
    }

    record Close(int offset) implements FilterToken {
    }

    record Colon(int offset) implements FilterToken {
    }

    record Not(int offset) implements FilterToken {
    }

    record And(int offset) implements FilterToken {
    }

    record Or(int offset) implements FilterToken {
    }

    record End(int offset) implements FilterToken {
    }
}
