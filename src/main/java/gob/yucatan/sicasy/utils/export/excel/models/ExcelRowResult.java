package gob.yucatan.sicasy.utils.export.excel.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExcelRowResult {
    private Object result;
    private List<ExcelCell> cells;
    private Integer rowHeight;
}
