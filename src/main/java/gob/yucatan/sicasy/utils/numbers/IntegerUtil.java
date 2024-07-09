package gob.yucatan.sicasy.utils.numbers;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class IntegerUtil {

    /**
     * Convierte una cadena a numero si no devuelve null
     */
    public static Integer parse(String str) {
        return Optional.ofNullable(str)
                .filter(numStr -> !numStr.isEmpty())
                .map(numStr -> {
                    try {
                        return Integer.valueOf(numStr);
                    } catch (NumberFormatException e) {
                        log.warn("{} is not a valid integer", numStr);
                        return null; // Valor predeterminado en caso de error
                    }
                })
                .orElse(null);
    }

    public static Integer fromDouble(Double d) {
        return Optional.ofNullable(d)
                .map(value -> {
                    try {
                        return value.intValue();
                    } catch (NumberFormatException e) {
                        log.warn("{} is not a valid double to convert to integer", value);
                        return null; // Valor predeterminado en caso de error
                    }
                })
                .orElse(null);
    }

}
