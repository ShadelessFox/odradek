package sh.adelessfox.odradek.app.ui.component.graph.filter;

import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.util.StringSource;
import sh.adelessfox.odradek.util.Result;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterParserTest {
    @Test
    void parserTest() {
        assertEquals(
            Result.ok(
                new Filter.And(
                    new Filter.Not(
                        new Filter.Or(
                            new Filter.GroupType("Texture"),
                            new Filter.GroupType("EnumFact")
                        )
                    ),
                    new Filter.GroupHasSubgroups()
                )
            ),
            Filter.parse("not (type:Texture or type:EnumFact) and has:subgroups")
        );

        assertEquals(
            Result.ok(
                new Filter.Not(
                    new Filter.GroupHasRoots()
                )
            ),
            Filter.parse("not has:roots")
        );

        // not has:roots and has:subgroups
        assertEquals(
            Result.ok(
                new Filter.And(
                    new Filter.Not(
                        new Filter.GroupHasRoots()
                    ),
                    new Filter.GroupHasSubgroups()
                )
            ),
            Filter.parse("not has:roots and has:subgroups")
        );
    }

    @Test
    void tokenizerMustEndWithEndTokenTest() {
        assertEquals(
            Result.ok(List.of(new FilterToken.End(new Location(0, 0)))),
            tokenize("")
        );
        assertEquals(
            Result.ok(List.of(
                new FilterToken.Name("type", new Location(0, 0)),
                new FilterToken.Colon(new Location(0, 4)),
                new FilterToken.Name("Texture", new Location(0, 5)),
                new FilterToken.End(new Location(0, 12))
            )),
            tokenize("type:Texture")
        );
    }

    @Test
    void tokenizerComplexTest() {
        assertEquals(
            Result.ok(List.of(
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
            )),
            tokenize("not (type:Texture or type:EnumFact) and has:subgroups")
        );
    }

    @Test
    void tokenizerErrorTest() {
        assertEquals(
            Result.error(new FilterError("For input string: \"99999999999999999999\"", new Location(0, 0))),
            tokenize("99999999999999999999")
        );
        assertEquals(
            Result.error(new FilterError("Unexpected character '@'", new Location(0, 3))),
            tokenize("abc@def")
        );
    }

    private static Result<List<FilterToken>, FilterError> tokenize(String input) {
        var lexer = new FilterLexer(new StringSource(input));
        var tokens = new ArrayList<FilterToken>();
        while (true) {
            var next = lexer.next();
            if (next.isError()) {
                return next.map(_ -> null);
            }
            var token = next.unwrap();
            tokens.add(token);
            if (token instanceof FilterToken.End) {
                return Result.ok(List.copyOf(tokens));
            }
        }
    }
}
