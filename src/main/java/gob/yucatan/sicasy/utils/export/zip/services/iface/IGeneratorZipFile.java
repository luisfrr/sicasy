package gob.yucatan.sicasy.utils.export.zip.services.iface;

import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.export.zip.models.ZipDataModel;

import java.io.IOException;

public interface IGeneratorZipFile {

    ExportFile createZipFile(ZipDataModel zipDataModel) throws IOException;

}
