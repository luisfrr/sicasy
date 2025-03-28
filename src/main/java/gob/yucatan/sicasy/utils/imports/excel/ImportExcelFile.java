package gob.yucatan.sicasy.utils.imports.excel;

import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.utils.date.DateFormatUtil;
import gob.yucatan.sicasy.utils.numbers.DoubleUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportExcelFile<T> {

    public List<T> processExcelFile(byte[] fileContent, Class<T> type, List<ConfigHeaderExcelModel> headers) {
        List<T> entityList = new ArrayList<>();
        try (InputStream is = new ByteArrayInputStream(fileContent)) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0); // Suponiendo que los datos están en la primera hoja

            // Verificar si las cabeceras del archivo coinciden con las cabeceras esperadas
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BadRequestException("El archivo Excel no tiene cabeceras.");
            }
            int cellCount = headerRow.getPhysicalNumberOfCells();
            if (cellCount != headers.size()) {
                throw new BadRequestException("El número de cabeceras en el archivo Excel no coincide con el número esperado.");
            }
            for (int i = 0; i < cellCount; i++) {
                Cell cell = headerRow.getCell(i);
                String headerCellValue = cell.getStringCellValue().trim();
                if (!headerCellValue.equals(headers.get(i).getHeader())) {
                    throw new BadRequestException("Cabecera no válida en la posición " + (i + 1) + ": " + headerCellValue);
                }
            }

            // Procesar los datos del archivo Excel
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    // La fila está vacía o la primera celda está vacía, esto indica que no hay más datos
                    break;
                }

                // Crear una instancia de la entidad
                T entity;
                try {
                    entity = type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("No se pudo instanciar la entidad.", e);
                }

                // Llenar la entidad con los datos de la fila
                fillEntityFromRow(entity, row, headers);

                // Agregar la entidad a la lista
                entityList.add(entity);
            }
        } catch (IOException | ParseException | NoSuchFieldException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            throw new BadRequestException("Error al procesar el archivo Excel.", e);
        }

        return entityList;
    }

    private void fillEntityFromRow(T entity, Row row, List<ConfigHeaderExcelModel> headers) throws ParseException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, BadRequestException {
        for (ConfigHeaderExcelModel header : headers) {
            int columnIndex = header.getColumnIndex(); // Obtener el índice de la columna de la configuración

            // Obtener el valor de la celda de la fila según el índice de la columna
            Cell cell = row.getCell(columnIndex);

            // Verificar si la celda no es nula y su tipo no es en blanco
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                // Obtener el nombre del campo asociado con la cabecera
                String fieldName = header.getFieldName();

                // Usar reflexión para establecer el valor del campo en la entidad
                // Obtener el tipo de dato del campo
                Class<?> fieldType = getFieldClass(entity, fieldName);

                // Convertir el valor de la celda al tipo de dato apropiado
                Object cellValue = getCellValue(header.getHeader(), cell, fieldType, header.getDateFormat());

                // Obtener el método setter del campo correspondiente
                Method setterMethod = entity.getClass().getMethod("set" + StringUtils.capitalize(fieldName), fieldType);

                // Llamar al método setter para establecer el valor del campo
                setterMethod.invoke(entity, cellValue);
            }
        }
    }

    private Class<?> getFieldClass(Object entity, String fieldName) throws NoSuchFieldException {
        // Obtener el tipo de dato del campo usando reflexión
        Field field = entity.getClass().getDeclaredField(fieldName);
        return field.getType();
    }

    private Object getCellValue(String header, Cell cell, Class<?> fieldType, String dateFormat) throws BadRequestException {
        // Convertir el valor de la celda al tipo de dato apropiado
        if (fieldType == String.class) {
            try {
                return cell.getStringCellValue().trim();
            }catch (Exception e) {
                DecimalFormat df = new DecimalFormat("#.##"); // No muestra decimales si son ceros
                return df.format(cell.getNumericCellValue());
            }
        } else if (fieldType == Integer.class) {
            try {
                return DoubleUtil.parse(String.valueOf(cell.getNumericCellValue())).intValue();
            } catch (Exception e) {
                throw new BadRequestException("Se espera un campo númerico entero. Columna: " + header);
            }
        } else if (fieldType == Double.class) {
            try {
                return DoubleUtil.parse(String.valueOf(cell.getNumericCellValue()));
            } catch (Exception e) {
                throw new BadRequestException("Se espera un campo númerico decimal. Columna: " + header);
            }
        } else if (fieldType == Date.class) {
            try {
                if(cell.getDateCellValue() != null)
                    return cell.getDateCellValue();
                else
                    return DateFormatUtil.convertToDate(cell.getStringCellValue(), dateFormat);
            } catch (Exception e) {
                throw new BadRequestException("Formato de fecha no válido o la columna no corresponde a una fecha. Columna: " + header);
            }
        } else if (fieldType == Boolean.class) {
            try {
                return cell.getBooleanCellValue();
            } catch (Exception e) {
                throw new BadRequestException("Formato del campo no válido. Columna: " + header);
            }
        } else {
            // Otros tipos de datos podrían manejarse de manera similar
            throw new BadRequestException("Tipo de dato no compatible. Columna: " + header);
        }
    }

}
