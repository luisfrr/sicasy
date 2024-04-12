package gob.yucatan.sicasy.utils.date;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateFormatUtil {

    /**
     * Método que devuelve una fecha convertida a una cadena de texto con un formato dado.
     * @param date Una fecha, no puede ser nulo.
     * @param pattern Patrón de fecha a convertir. Si es nulo el patrón por defecto es: yyyy-MM-dd HH:mm:ss.SSS
     * @return fecha en cadena de texto
     */
    public static String convertToFormat(Date date, String pattern) {
        if(pattern == null || pattern.isEmpty())
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static Date convertToDate(String date, String pattern) {
        if(date == null || date.isEmpty())
            return null;

        if(pattern == null || pattern.isEmpty())
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";

        DateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            return dateFormat.parse(date);
        } catch (Exception e) {
            log.warn("Could not convert date {} to {}", date, pattern);
            return null;
        }
    }

}
