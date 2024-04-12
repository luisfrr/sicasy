package gob.yucatan.sicasy.utils.export.csv.service.iface;

import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.export.csv.models.CsvData;

import java.io.IOException;

public interface IGeneratorCSVFile {

    ExportFile createCsvFile(CsvData csvData) throws IOException;

}
