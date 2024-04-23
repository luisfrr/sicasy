package gob.yucatan.sicasy.utils.imports.excel;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
public class SaveFile {

    public static String saveFileToPath(byte[] content, String fileName, String path) throws IOException {
        // path ejemplo pa windows : C:\Users\Isra\Downloads\
        log.info("guardando archivo en "+ path+fileName);
        File file = new File(path+fileName);
        return Files.write(file.toPath(), content).toString();
    }

    public static void saveFileWithInputStream(InputStream inputStream, String originalFile, String path) throws IOException {
        File file = new File(path + originalFile);

        try {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        }catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }

}
