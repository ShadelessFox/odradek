package sh.adelessfox.odradek.app.ui.tools.graph.filter;

import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.SyntaxException;
import sh.adelessfox.odradek.parsing.util.StringSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterParserTest {
    @Test
    void parserTest() {
        assertEquals(
            new Filter.And(
                new Filter.Not(
                    new Filter.Or(
                        new Filter.GroupType("Texture"),
                        new Filter.GroupType("EnumFact")
                    )
                ),
                new Filter.GroupHasSubgroups()
            ),
            Filter.parse("not (type:Texture or type:EnumFact) and has:subgroups")
        );

        assertEquals(
            new Filter.Not(
                new Filter.GroupHasRoots()
            ),
            Filter.parse("not has:roots")
        );

        // not has:roots and has:subgroups
        assertEquals(
            new Filter.And(
                new Filter.Not(
                    new Filter.GroupHasRoots()
                ),
                new Filter.GroupHasSubgroups()
            ),
            Filter.parse("not has:roots and has:subgroups")
        );
    }

    @Test
    void tokenizerMustEndWithEndTokenTest() {
        assertEquals(
            List.of(new FilterToken.End(new Location(0, 0))),
            tokenize("")
        );
        assertEquals(
            List.of(
                new FilterToken.Name("type", new Location(0, 0)),
                new FilterToken.Colon(new Location(0, 4)),
                new FilterToken.Name("Texture", new Location(0, 5)),
                new FilterToken.End(new Location(0, 12))
            ),
            tokenize("type:Texture")
        );
    }

    @Test
    void tokenizerComplexTest() {
        assertEquals(
            List.of(
                new FilterToken.Not(new Location(0, 0)),
                new FilterToken.Open(new Location(0, 4)),
                new FilterToken.Name("type", new Location(0, 5)),
                new FilterToken.Colon(new Location(0, 9)),
                new FilterToken.Name("Texture", new Location(0, 10)),
                new FilterToken.Or(new Location(0, 18)),
                new FilterToken.Name("type", new Location(0, 21)),
                new FilterToken.Colon(new Location(0, 25)),
                new FilterToken.Name("EnumFact", new Location(0, 26)),
                new FilterToken.Close(new Location(0, 34)),
                new FilterToken.And(new Location(0, 36)),
                new FilterToken.Name("has", new Location(0, 40)),
                new FilterToken.Colon(new Location(0, 43)),
                new FilterToken.Name("subgroups", new Location(0, 44)),
                new FilterToken.End(new Location(0, 53))
            ),
            tokenize("not (type:Texture or type:EnumFact) and has:subgroups")
        );
    }

    @Test
    void tokenizerErrorTest() {
        var overflow = assertThrows(SyntaxException.class, () -> tokenize("99999999999999999999"));
        assertEquals("For input string: \"99999999999999999999\" at 1:1", overflow.getMessage());
        assertEquals(new Location(0, 0), overflow.getLocation());

        var unexpected = assertThrows(SyntaxException.class, () -> tokenize("abc@def"));
        assertEquals("Unexpected character '@' at 1:4", unexpected.getMessage());
        assertEquals(new Location(0, 3), unexpected.getLocation());
    }

    private static List<FilterToken> tokenize(String input) {
        var lexer = new FilterLexer(new StringSource(input));
        var tokens = new ArrayList<FilterToken>();
        while (true) {
            var token = lexer.next();
            tokens.add(token);
            if (token instanceof FilterToken.End) {
                return List.copyOf(tokens);
            }
        }
    }
}
