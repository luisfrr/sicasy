package gob.yucatan.sicasy.utils.export.excel.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExcelDataSheet {
    private List<?> data;
    private List<ExcelCell> cells;
    private String title;
    private String subtitle;
    private String sheetName;
    private String filename;
    private ExcelRowResult rowResult;
    private boolean agregarFechaGeneracion;
    private boolean autoFilter;
    private String appName;
}
