package org.pdfmerger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PdfHandler {

    private final Path tempDirWithPrefix;

    public BooleanProperty canSaveProperty() {
        return canSave;
    }

    private final BooleanProperty canSave = new SimpleBooleanProperty(false);

    public ObjectProperty<File> outputDocumentProperty() {
        return outputDocument;
    }

    private final ObjectProperty<File> outputDocument = new SimpleObjectProperty<>(null);
    private final ObjectProperty<PDPage> coverPage = new SimpleObjectProperty<>();

    public ListProperty<File> filesProperty() {
        return filesList;
    }

    private final ListProperty<File> filesList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public PdfHandler() {
        filesList.addListener((ListChangeListener<File>) c -> outputDocument.setValue(createMergedDocument(filesList)));
        canSave.bind(Bindings.createBooleanBinding(() -> outputDocument.get() != null && !filesList.isEmpty(), outputDocument, filesList));

        try {
            tempDirWithPrefix = Files.createTempDirectory("pdfmerger_temp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PDPage createCoverPage(PDDocument document, List<ContentEntry> entries, PDPage coverPage) {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, coverPage)) {
            contentStream.beginText();

            // make title
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Merged PDF Files");

            // make content list
            int fontSize = 12;
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(-80, -20);

            for (ContentEntry entry : entries) {
                String text = "- " + entry.name();
                contentStream.showText(text);
                contentStream.newLineAtOffset(0, -20);

                /*PDPageDestination dest = new PDPageFitWidthDestination();
                dest.setPage(entry.firstPage());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                PDAnnotationLink link = new PDAnnotationLink();
                link.setBorderStyle(new PDBorderStyleDictionary());
                link.setDestination(dest);
                link.setAction(action);

                // Calculate the position and size of the link annotation
                float x = 100;
                float y = 100;//contentStream.getCurrentPosition().getY() - 6;
                float width = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * fontSize;
                float height = fontSize;

                // Add the link annotation to the cover page
                link.setRectangle(new PDRectangle(x, y, width, height));
                coverPage.getAnnotations().add(link);
*/

                //contentStream.endText();

               /* PDPageDestination dest = new PDPageFitWidthDestination();
                dest.setPage(mergedDocument.getPage(0));
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                PDAnnotationLink link = new PDAnnotationLink();
                link.setBorderStyle(new PDBorderStyleDictionary());
                link.setDestination(dest);
                link.setAction(action);*/

                // Calculate the position and size of the link annotation
                // float x = 100;
                // float y = contentStream.getCurrentPosition().getY() - 6;
                // float width = PDType1Font.HELVETICA.getStringWidth(pdfFile.getName()) / 1000 * 12;
                // float height = 12;

                // Add the link annotation to the cover page
                // link.setRectangle(new PDRectangle(x, y, width, height));
                //coverPage.getAnnotations().add(link);
            }
            contentStream.endText();
            // contentStream.close();
            return coverPage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record ContentEntry(String name, PDPage firstPage) {

    }

    public File createMergedDocument(List<File> pdfFiles) {
        if (pdfFiles.size() == 0) return null;
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        try (PDDocument outputDocument = new PDDocument()) {

            PDPage coverPage = new PDPage();
            outputDocument.addPage(coverPage);

            List<ContentEntry> contentEntries = new ArrayList<>();
            for (File pdfFile : pdfFiles) {
                try (PDDocument document = PDDocument.load(pdfFile)) {
                    pdfMerger.appendDocument(outputDocument, document);
                    contentEntries.add(new ContentEntry(pdfFile.getName(), document.getPage(0)));
                }
            }

            createCoverPage(outputDocument, contentEntries, coverPage);

            pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            File file = new File(tempDirWithPrefix + "tmpfile.pdf");
            outputDocument.save(file);
            outputDocument.close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFileToDisk(File outputFile) {
        if (outputFile == null) return;
        try {
            Files.copy(outputDocument.get().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
