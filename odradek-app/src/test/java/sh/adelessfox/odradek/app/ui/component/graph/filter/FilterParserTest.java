package sh.adelessfox.odradek.app.ui.component.graph.filter;

import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.util.Result;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterParserTest {
    @Test
    void parserTest() {
        var result = Filter.parse("!(type:Texture OR type:EnumFact) AND has:subgroups");
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
                new FilterToken.Open(1),
                new FilterToken.Name("type", 2),
                new FilterToken.Colon(6),
                new FilterToken.Name("Texture", 7),
                new FilterToken.Or(15),
                new FilterToken.Name("type", 18),
                new FilterToken.Colon(22),
                new FilterToken.Name("EnumFact", 23),
                new FilterToken.Close(31),
                new FilterToken.And(33),
                new FilterToken.Name("has", 37),
                new FilterToken.Colon(40),
                new FilterToken.Name("subgroups", 41),
                new FilterToken.End(50)
            )),
            FilterParser.tokenize("!(type:Texture OR type:EnumFact) AND has:subgroups")
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
