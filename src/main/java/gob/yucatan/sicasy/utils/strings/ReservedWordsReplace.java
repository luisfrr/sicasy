package gob.yucatan.sicasy.utils.strings;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class ReservedWordsReplace {

    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "create", "insert", "update", "delete", "drop", "alter", "select", "union",
            "grant", "truncate", "between", "or", "jquery", "javascript"
    ));

    private static final Map<String, String> SYMBOLS_TO_HTML = new HashMap<>();
    static {
        SYMBOLS_TO_HTML.put("+", "&plus;");
        SYMBOLS_TO_HTML.put("-", "&minus;");
        SYMBOLS_TO_HTML.put("*", "&ast;");
        SYMBOLS_TO_HTML.put("/", "&sol;");
        SYMBOLS_TO_HTML.put("%", "&percnt;");
        SYMBOLS_TO_HTML.put("=", "&equals;");
        SYMBOLS_TO_HTML.put("==", "&equals;&equals;");
        SYMBOLS_TO_HTML.put("!=", "&excl;&equals;");
        SYMBOLS_TO_HTML.put("<", "&lt;");
        SYMBOLS_TO_HTML.put(">", "&gt;");
        SYMBOLS_TO_HTML.put("<=", "&lt;&equals;");
        SYMBOLS_TO_HTML.put(">=", "&gt;&equals;");
        SYMBOLS_TO_HTML.put("&&", "&amp;&amp;");
        SYMBOLS_TO_HTML.put("||", "&vert;&vert;");
        SYMBOLS_TO_HTML.put("!", "&excl;");
        SYMBOLS_TO_HTML.put("&", "&amp;");
        SYMBOLS_TO_HTML.put("|", "&vert;");
        SYMBOLS_TO_HTML.put("^", "&circ;");
        SYMBOLS_TO_HTML.put("~", "&tilde;");
        SYMBOLS_TO_HTML.put("<<", "&lt;&lt;");
        SYMBOLS_TO_HTML.put(">>", "&gt;&gt;");
        SYMBOLS_TO_HTML.put(">>>", "&gt;&gt;&gt;");
        SYMBOLS_TO_HTML.put("++", "&plus;&plus;");
        SYMBOLS_TO_HTML.put("--", "&minus;&minus;");
        SYMBOLS_TO_HTML.put("->", "&minus;&gt;");
        SYMBOLS_TO_HTML.put("::", "&colon;&colon;");
        SYMBOLS_TO_HTML.put("?", "&quest;");
        SYMBOLS_TO_HTML.put(":", "&colon;");
        SYMBOLS_TO_HTML.put(".", "&period;");
        SYMBOLS_TO_HTML.put(",", "&comma;");
        SYMBOLS_TO_HTML.put(";", "&semi;");
        SYMBOLS_TO_HTML.put("(", "&lpar;");
        SYMBOLS_TO_HTML.put(")", "&rpar;");
        SYMBOLS_TO_HTML.put("{", "&lcub;");
        SYMBOLS_TO_HTML.put("}", "&rcub;");
        SYMBOLS_TO_HTML.put("[", "&lsqb;");
        SYMBOLS_TO_HTML.put("]", "&rsqb;");
    }

    public static void processEntity(Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(entity);
                    if (value != null) {
                        String newValue = replaceReservedWordsAndSymbols(value);
                        field.set(entity, newValue);
                    } else {
                        System.out.println("El campo " + field.getName() + " es nulo.");
                    }
                } catch (IllegalAccessException e) {
                    log.warn("No se pudo remplazar el campo " + field.getName(), e);
                }
            }
        }
    }

    private static String replaceReservedWordsAndSymbols(String value) {
        for (String reservedWord : RESERVED_WORDS) {
            if (value.contains(reservedWord)) {
                value = value.replaceAll("\\b" + reservedWord + "\\b", "");
            }
        }
        for (Map.Entry<String, String> entry : SYMBOLS_TO_HTML.entrySet()) {
            value = value.replace(entry.getKey(), entry.getValue());
        }
        return value;
    }

}
