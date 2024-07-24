package gob.yucatan.sicasy.utils.strings;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ReservedWordsReplace {

    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            // Java reserved words
            "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized",
            "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw",
            "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
            "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void",
            "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while",
            // PostgreSQL reserved words
            "ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHORIZATION", "BINARY", "BOTH",
            "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG",
            "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT", "DEFERRABLE", "DESC",
            "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "FALSE", "FETCH", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT",
            "GROUP", "HAVING", "ILIKE", "IN", "INITIALLY", "INNER", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "LATERAL",
            "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NATURAL", "NOT", "NULL", "OFFSET", "ON",
            "ONLY", "OR", "ORDER", "OUTER", "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT",
            "SESSION_USER", "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "THEN", "TO", "TRAILING", "TRUE", "UNION", "UNIQUE",
            "USER", "USING", "VARIADIC", "VERBOSE", "WHEN", "WHERE", "WINDOW", "WITH", "CHECKPOINT", "CLOSE", "CLUSTER", "COMMENT",
            "COPY", "DISCARD", "EXECUTE", "EXPLAIN", "LISTEN", "LOAD", "LOCK", "MOVE", "NOTIFY", "PREPARE", "REASSIGN", "REINDEX",
            "RESET", "REVOKE", "RULE", "SAVEPOINT", "SECURITY", "SET", "SHOW", "START", "TRUNCATE", "UNLISTEN", "VACUUM",
            // Programming symbols
            "+", "-", "*", "/", "%", "=", "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>",
            "++", "--", "->", "::", "?", ":", ".", ",", ";", "(", ")", "{", "}", "[", "]"
    ));

    public static void processEntity(Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(entity);
                    if (value != null) {
                        String newValue = replaceReservedWords(value);
                        field.set(entity, newValue);
                    } else {
                        log.info("El campo " + field.getName() + " es nulo.");
                    }
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static String replaceReservedWords(String value) {
        for (String reservedWord : RESERVED_WORDS) {
            if (value.contains(reservedWord)) {
                value = value.replaceAll("\\b" + reservedWord + "\\b", "");
            }
        }
        return value;
    }

}
