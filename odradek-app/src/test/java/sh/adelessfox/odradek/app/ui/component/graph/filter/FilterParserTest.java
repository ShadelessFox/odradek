package sh.adelessfox.odradek.app.ui.component.graph.filter;

import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.util.Result;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterParserTest {
    @Test
    void parserTest() {
        var result = Filter.parse("not (type:Texture or type:EnumFact) and has:subgroups");
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
            result
        );
    }

    @Test
    void tokenizerMustEndWithEndTokenTest() {
        assertEquals(
            Result.ok(List.of(new FilterToken.End(0))),
            FilterParser.tokenize("")
        );
        assertEquals(
            Result.ok(List.of(
                new FilterToken.Name("type", 0),
                new FilterToken.Colon(4),
                new FilterToken.Name("Texture", 5),
                new FilterToken.End(12)
            )),
            FilterParser.tokenize("type:Texture")
        );
    }

    @Test
    void tokenizerComplexTest() {
        assertEquals(
            Result.ok(List.of(
                new FilterToken.Not(0),
                new FilterToken.Open(4),
                new FilterToken.Name("type", 5),
                new FilterToken.Colon(9),
                new FilterToken.Name("Texture", 10),
                new FilterToken.Or(18),
                new FilterToken.Name("type", 21),
                new FilterToken.Colon(25),
                new FilterToken.Name("EnumFact", 26),
                new FilterToken.Close(34),
                new FilterToken.And(36),
                new FilterToken.Name("has", 40),
                new FilterToken.Colon(43),
                new FilterToken.Name("subgroups", 44),
                new FilterToken.End(53)
            )),
            FilterParser.tokenize("not (type:Texture or type:EnumFact) and has:subgroups")
        );
    }

    @Test
    void tokenizerErrorTest() {
        assertEquals(
            Result.error(new FilterError("For input string: \"99999999999999999999\"", 0)),
            FilterParser.tokenize("99999999999999999999")
        );
        assertEquals(
            Result.error(new FilterError("Unexpected character '@'", 3)),
            FilterParser.tokenize("abc@def")
        );
    }
}
