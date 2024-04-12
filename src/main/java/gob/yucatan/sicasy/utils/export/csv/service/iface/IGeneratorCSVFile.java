package gob.yucatan.sicasy.utils.export.csv.service.iface;

import edu.umm.platform.utils.export.ExportFile;
import edu.umm.platform.utils.export.csv.models.CsvData;

import java.io.IOException;

public interface IGeneratorCSVFile {

    ExportFile createCsvFile(CsvData csvData) throws IOException;

}
