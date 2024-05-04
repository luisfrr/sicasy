package gob.yucatan.sicasy.utils.imports.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.UUID;

@Slf4j
public class SaveFile {

    public static String saveFileToPath(byte[] content, String fileName, String path) throws IOException {
        // path ejemplo pa windows : C:\Users\Isra\Downloads\
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")){
            log.info("Sistema operativo basado en Windows, download folder: {}", System.getProperty("user.home"));
        }
        else if (os.contains("osx")){
            log.info("Sistema operativo basado en MACOs");
        }
        else if (os.contains("nix") || os.contains("aix") || os.contains("nux")){
            log.info("Sistema operativo basado en Linux");
        }

        log.info("guardando archivo en {}{}", path, fileName);
        File file = new File(System.getProperty("user.home")+path+
                Normalizer.normalize(fileName, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", ""));
        return Files.write(file.toPath(), content).toString();
    }

    public static String importFileToPath(byte[] content, String fileName, String path) throws IOException {
        UUID uuid = UUID.randomUUID();
        String uniqueFileName = uuid + "." + FileNameUtils.getExtension(fileName);
        String fullPath = path + uniqueFileName;

        File file = new File(fullPath);
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
