package gob.yucatan.sicasy.utils.strings;

import java.text.Normalizer;

public class FilenameNormalizer {

    public static String normalizeFilename(String filename) {
        // Reemplaza espacios en blanco por guiones bajos
        String normalized = filename.trim().replaceAll("\\s+", "_");

        // Convierte a forma de normalización NFD (descompone caracteres acentuados)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);

        // Elimina caracteres no ASCII, conservando solo letras y números
        normalized = normalized.replaceAll("[^\\p{ASCII}]", "");

        // Elimina caracteres especiales, dejando solo letras, números, guiones bajos y puntos
        normalized = normalized.replaceAll("[^\\w.]", "_");

        return normalized;
    }

}
