package gob.yucatan.sicasy.utils.export.zip.services.impl;

import edu.umm.platform.utils.export.ExportFile;
import edu.umm.platform.utils.export.ExportFileType;
import edu.umm.platform.utils.export.zip.models.ZipDataModel;
import edu.umm.platform.utils.export.zip.models.ZipFolder;
import edu.umm.platform.utils.export.zip.services.iface.IGeneratorZipFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class GeneratorZipFileImpl implements IGeneratorZipFile {

    @Override
    public ExportFile createZipFile(ZipDataModel zipDataModel) throws IOException {

        Objects.requireNonNull(zipDataModel, "zipDataModel");
        Objects.requireNonNull(zipDataModel.getMainZipFolder(), "mainZipFolder");

        ZipFolder mainFolder = zipDataModel.getMainZipFolder();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(stream);

        addFilesToZip(mainFolder, "", zos);

        zos.flush();
        zos.close();

        return ExportFile.builder()
                .fileName(zipDataModel.getFilename())
                .exportFileType(ExportFileType.ZIP)
                .stream(stream)
                .build();
    }

    private void addFilesToZip(ZipFolder folder, String parentFolderPath, ZipOutputStream zos) throws IOException {
        String folderPath = parentFolderPath + folder.getFolderName() + "/";

        // Agregar archivos de la carpeta actual
        if(folder.getFileList() != null) {
            for (ExportFile file : folder.getFileList()) {
                String filePath = folderPath + file.getFileName();
                ZipEntry zipEntry = new ZipEntry(filePath);
                zos.putNextEntry(zipEntry);
                zos.write(file.getStream().toByteArray());
                zos.closeEntry();
            }
        }

        // Agregar archivos de subcarpetas (recursivamente)
        if(folder.getSubFolderList() != null) {
            for (ZipFolder subFolder : folder.getSubFolderList()) {
                addFilesToZip(subFolder, folderPath, zos);
            }
        }
    }

}
