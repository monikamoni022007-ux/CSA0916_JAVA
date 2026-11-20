import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class BufferedLineCounter {
    private static final int BUFFER_SIZE = 8192;

    public static long countLinesWithBufferedStream(Path filePath) throws IOException {
        long lineCount = 0;
        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(filePath.toFile()), BUFFER_SIZE)) {
            int byteRead;
            while ((byteRead = bis.read()) != -1) {
                if (byteRead == '\n') {
                    lineCount++;
                }
            }
        }
        return lineCount;
    }

    public static long countLinesWithByteArray(Path filePath) throws IOException {
        long lineCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (buffer[i] == (byte) '\n') {
                        lineCount++;
                    }
                }
            }
        }
        return lineCount;
    }
}
