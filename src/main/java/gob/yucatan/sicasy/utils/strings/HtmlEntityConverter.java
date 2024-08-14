package gob.yucatan.sicasy.utils.strings;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class HtmlEntityConverter {

    private static final Set<String> RESERVED_WORDS = Set.of(
            "create", "insert", "update", "delete", "drop", "alter", "select", "union", "from",
            "grant", "truncate", "between", "or", "jquery", "javascript"
    );

    private static final Map<String, String> SYMBOLS_TO_HTML = Map.ofEntries(
            Map.entry("<", "&lt;"),
            Map.entry(">", "&gt;"),
            Map.entry("%", "&percnt;"),
            Map.entry("$", "&#36;"),
            Map.entry("(", "&lpar;"),
            Map.entry(")", "&rpar;"),
            Map.entry("\"", "&quot;"),
            Map.entry("'", "&#39;")
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

