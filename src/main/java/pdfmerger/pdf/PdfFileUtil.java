package pdfmerger.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

public class PdfFileUtil {
    public static Integer getLastPage(File file) {
        if (file == null) return 0;
        try (PDDocument document = PDDocument.load(file)) {
            if (document == null) {
                return 0;
            }
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
