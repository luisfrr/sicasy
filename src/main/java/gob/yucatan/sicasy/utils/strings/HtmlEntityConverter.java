package gob.yucatan.sicasy.utils.strings;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class HtmlEntityConverter {

    private static final Set<String> RESERVED_WORDS = Set.of(
            "create", "insert", "update", "delete", "drop", "alter", "select", "union",
            "grant", "truncate", "between", "or", "jquery", "javascript"
    );

    private static final Map<String, String> SYMBOLS_TO_HTML = Map.ofEntries(
            Map.entry("+", "&plus;"),
            Map.entry("-", "&minus;"),
            Map.entry("*", "&ast;"),
            Map.entry("/", "&sol;"),
            Map.entry("%", "&percnt;"),
            Map.entry("=", "&equals;"),
            Map.entry("==", "&equals;&equals;"),
            Map.entry("!=", "&excl;&equals;"),
            Map.entry("<", "&lt;"),
            Map.entry(">", "&gt;"),
            Map.entry("<=", "&lt;&equals;"),
            Map.entry(">=", "&gt;&equals;"),
            Map.entry("&&", "&amp;&amp;"),
            Map.entry("||", "&vert;&vert;"),
            Map.entry("!", "&excl;"),
            Map.entry("&", "&amp;"),
            Map.entry("|", "&vert;"),
            Map.entry("^", "&circ;"),
            Map.entry("~", "&tilde;"),
            Map.entry("<<", "&lt;&lt;"),
            Map.entry(">>", "&gt;&gt;"),
            Map.entry(">>>", "&gt;&gt;&gt;"),
            Map.entry("++", "&plus;&plus;"),
            Map.entry("--", "&minus;&minus;"),
            Map.entry("->", "&minus;&gt;"),
            Map.entry("::", "&colon;&colon;"),
            Map.entry("?", "&quest;"),
            Map.entry(":", "&colon;"),
            Map.entry(".", "&period;"),
            Map.entry(",", "&comma;"),
            Map.entry(";", "&semi;"),
            Map.entry("(", "&lpar;"),
            Map.entry(")", "&rpar;"),
            Map.entry("{", "&lcub;"),
            Map.entry("}", "&rcub;"),
            Map.entry("[", "&lsqb;"),
            Map.entry("]", "&rsqb;")
    );

    private static final Map<String, String> HTML_TO_SYMBOLS = new HashMap<>();
    static {
        SYMBOLS_TO_HTML.forEach((key, value) -> HTML_TO_SYMBOLS.put(value, key));
    }

    public static String convertSymbolsAndReservedWordsToHtmlEntities(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            return replaceSymbols(replaceReservedWords(value));
        } catch (RuntimeException e) {
            log.warn("Error converting symbols and reserved words to HTML entities. value: {}", value);
            return value;
        }
    }

    public static String convertHtmlEntitiesToSymbols(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            return replaceHtmlEntities(value);
        } catch (RuntimeException e) {
            log.error("Error converting HTML entities to symbols. value: {}", value);
            return value;
        }
    }

    private static String replaceReservedWords(String value) {
        StringBuilder result = new StringBuilder(value);
        for (String reservedWord : RESERVED_WORDS) {
            int index;
            while ((index = result.indexOf(reservedWord)) != -1) {
                result.replace(index, index + reservedWord.length(), "");
            }
        }
        return result.toString();
    }

    private static String replaceSymbols(String value) {
        StringBuilder result = new StringBuilder(value);
        SYMBOLS_TO_HTML.forEach((symbol, htmlEntity) -> {
            int index;
            while ((index = result.indexOf(symbol)) != -1) {
                result.replace(index, index + symbol.length(), htmlEntity);
            }
        });
        return result.toString();
    }

    private static String replaceHtmlEntities(String value) {
        StringBuilder result = new StringBuilder(value);
        HTML_TO_SYMBOLS.forEach((htmlEntity, symbol) -> {
            int index;
            while ((index = result.indexOf(htmlEntity)) != -1) {
                result.replace(index, index + htmlEntity.length(), symbol);
            }
        });
        return result.toString();
    }
}

