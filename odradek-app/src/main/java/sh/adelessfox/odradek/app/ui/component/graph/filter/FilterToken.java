package sh.adelessfox.odradek.app.ui.component.graph.filter;

import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.Token;

sealed interface FilterToken extends Token {
    sealed interface Prefix extends FilterToken {
        int precedence();
    }

    sealed interface Infix extends FilterToken {
        int precedence();
    }

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

    record Name(String value, Location location) implements FilterToken {
    }

    record Number(int value, Location location) implements FilterToken {
    }

    record Open(Location location) implements FilterToken {
    }

    record Close(Location location) implements FilterToken {
    }

    record Colon(Location location) implements FilterToken {
    }

    record End(Location location) implements FilterToken {
    }

    record Not(Location location) implements Prefix {
        @Override
        public int precedence() {
            return 30;
        }
    }

    record And(Location location) implements Infix {
        @Override
        public int precedence() {
            return 20;
        }
    }

    record Or(Location location) implements Infix {
        @Override
        public int precedence() {
            return 10;
        }
    }
}
