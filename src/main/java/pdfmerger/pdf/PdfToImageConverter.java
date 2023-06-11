package pdfmerger.pdf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfToImageConverter {
    public static Image renderDocumentToImage(File file, int page) {
        if (file == null) return null;
        try (PDDocument document = PDDocument.load(file)) {
            if (document == null || document.getNumberOfPages() < page) {
                return null;
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImage(page);
            return SwingFXUtils.toFXImage(image, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
