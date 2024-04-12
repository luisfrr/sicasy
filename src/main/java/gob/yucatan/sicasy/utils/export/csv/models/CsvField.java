package gob.yucatan.sicasy.utils.export.csv.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CsvField {
    /**
     * Nombre de la cabecera
     */
    private String fieldName;

    /**
     * Expresión que permite obtener un valor de un objeto
     */
    private String propertyExpression;

    /**
     * Se le puede aplicar un formato a una fecha o un double o float
     */
    private String dataFormat;
}
