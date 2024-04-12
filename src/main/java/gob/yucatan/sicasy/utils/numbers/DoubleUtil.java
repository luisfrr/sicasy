package gob.yucatan.sicasy.utils.numbers;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class DoubleUtil {

    /**
     * Convierte una cadena a numero si no devuelve null
     */
    public static Double parse(String str) {
        return Optional.ofNullable(str)
                .filter(numStr -> !numStr.isEmpty())
                .map(numStr -> {
                    try {
                        return Double.valueOf(numStr);
                    } catch (NumberFormatException e) {
                        log.warn("{} is not a valid integer", numStr);
                        return null; // Valor predeterminado en caso de error
                    }
                })
                .orElse(null);
    }

}
