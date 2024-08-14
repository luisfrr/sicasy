package gob.yucatan.sicasy.utils.strings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonStringConverter {

    public static String convertToString(Object data) {
        // Convertir bitacoraCambiosList a Text Json
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = "";
        try {
            jsonStr = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("No se pudo convertir a JSON String", e);
        }
        return jsonStr;
    }

    public static <T> List<T> convertToList(String data, TypeReference<List<T>> typeRef) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, typeRef);
        } catch (Exception e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("No se pudo convertir el JSON a Lista", e);
            // Puedes devolver una lista vacía o lanzar una excepción personalizada, según tu lógica de negocio
            return List.of(); // Retornar una lista vacía
        }
    }

    public static List<BitacoraCambios> getBitacoraCambios(String cambios) {
        List<BitacoraCambios> cambiosList = new ArrayList<>();
        if(cambios != null) {
            TypeReference<List<BitacoraCambios>> typeRef = new TypeReference<>() {};
            cambiosList = JsonStringConverter.convertToList(cambios, typeRef);
        }
        return cambiosList;
    }

}
