package gob.yucatan.sicasy.utils.export.zip.services.iface;

import edu.umm.platform.utils.export.ExportFile;
import edu.umm.platform.utils.export.zip.models.ZipDataModel;

import java.io.IOException;

public interface IGeneratorZipFile {

    ExportFile createZipFile(ZipDataModel zipDataModel) throws IOException;

}
