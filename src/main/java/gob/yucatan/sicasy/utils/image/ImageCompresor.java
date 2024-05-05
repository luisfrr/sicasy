package gob.yucatan.sicasy.utils.image;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageCompresor {

    public static byte[] compressImage(byte[] imageBytes, String format) throws IOException {
        // Convertir el byte array en un flujo de entrada
        var inputStream = new ByteArrayInputStream(imageBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Definir una escala antes de obtener el BufferedImage
        BufferedImage bufferedImage = Thumbnails.of(inputStream)
                .scale(1) // Aquí se define la escala para evitar el error
                .asBufferedImage(); // Ahora se puede obtener el BufferedImage sin errores

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int minDim = Math.min(width, height);

        Thumbnails.of(bufferedImage)
                .sourceRegion(Positions.CENTER, minDim, minDim) // Recortar al centro
                .size(800, 800) // Redimensionar a 800x800
                .outputFormat(format) // Establecer formato de salida
                .outputQuality(0.60) // Comprimir con calidad al 60%
                .toOutputStream(outputStream);

        return outputStream.toByteArray(); // Devolver el resultado
    }

}
