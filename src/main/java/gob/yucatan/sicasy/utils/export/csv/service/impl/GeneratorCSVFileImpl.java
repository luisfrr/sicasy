package gob.yucatan.sicasy.utils.export.csv.service.impl;

import edu.umm.platform.utils.export.ExportFile;
import edu.umm.platform.utils.export.ExportFileType;
import edu.umm.platform.utils.export.csv.models.CsvData;
import edu.umm.platform.utils.export.csv.models.CsvField;
import edu.umm.platform.utils.export.csv.service.iface.IGeneratorCSVFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class GeneratorCSVFileImpl implements IGeneratorCSVFile {

    @Override
    public ExportFile createCsvFile(CsvData csvData) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);

        if(csvData.isPrintHeaders()) {
            // Se agregan las cabeceras
            for (CsvField field : csvData.getFields()) {
                printer.print(field.getFieldName());
            }
            // Salto de línea
            printer.println();
        }

        for (Object item : csvData.getData()) {

            for (CsvField field : csvData.getFields()) {
                Object value;
                // Se obtiene la expresión
                SpelExpressionParser parser = new SpelExpressionParser();
                Expression expression = parser.parseExpression(field.getPropertyExpression());
                // Se obtiene el valor a partir de la expresion
                value = expression.getValue(item);

                // Válida y convierte el valor a cadena con un determinado formato
                String valueStr = convertToValidData(value, field.getDataFormat());

                // Se printa en el CSV
                printer.print(valueStr);
            }

            // Salto de línea
            printer.println();
        }

        printer.flush();

        return ExportFile.builder()
                .stream(stream)
                .exportFileType(ExportFileType.CSV)
                .fileName(csvData.getFilename())
                .build();
    }

    private String convertToValidData(Object value, String dataFormat) {

        String valueStr;

        if(value == null) {
            valueStr = Strings.EMPTY;
        } else if (value instanceof Double) {
            if(dataFormat != null) {
                Double doubleValue = (Double) value;
                valueStr = String.format(dataFormat, doubleValue);
            } else {
                valueStr = value.toString();
            }
        } else if (value instanceof Float) {
            if(dataFormat != null) {
                Float floatValue = (Float) value;
                valueStr = String.format(dataFormat, floatValue);
            } else {
                valueStr = value.toString();
            }
        } else if (value instanceof Date) {
            if(dataFormat != null) {
                Date dateValue = (Date) value;
                DateFormat df = new SimpleDateFormat(dataFormat);
                valueStr = df.format(dateValue);
            } else {
                valueStr = value.toString();
            }
        } else {
            valueStr = value.toString();
        }

        return valueStr;
    }

}
