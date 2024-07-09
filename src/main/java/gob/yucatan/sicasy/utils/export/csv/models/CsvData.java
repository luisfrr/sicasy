package gob.yucatan.sicasy.utils.export.csv.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CsvData {
    private List<?> data;
    private List<CsvField> fields;
    private String filename;
    private boolean printHeaders;
}
