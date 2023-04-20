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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;

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


    static class StreamWrapper implements AutoCloseable {

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        private int x;
        private int y;

        private PDPageContentStream contentStream;

        public StreamWrapper(PDDocument document, PDPage sourcePage) throws IOException {
            contentStream = new PDPageContentStream(document, sourcePage);
            //x = (int) sourcePage.getMediaBox().getLowerLeftX();
            //y = (int) sourcePage.getMediaBox().getUpperRightY();
            x = 0;
            y = 0;
        }

        public PDPageContentStream getContentStream() {
            return contentStream;
        }

        @Override
        public void close() throws Exception {
            contentStream.close();
        }


        public void newLineAtOffset(int tx, int ty) throws IOException {
            x += tx;
            y += ty;
            contentStream.newLineAtOffset(tx, ty);
        }
    }

    private PDPage createCoverPage(PDDocument document, List<ContentEntry> entries, PDPage coverPage) {
        try (StreamWrapper wrapper = new StreamWrapper(document, coverPage)) {
            wrapper.getContentStream().beginText();
            int marginLeft = 30;
            int marginTop = (int) (coverPage.getMediaBox().getUpperRightY() - 100);

            // make title
            wrapper.getContentStream().setFont(PDType1Font.HELVETICA_BOLD, 14);
            wrapper.setY(18); // FIXME: where does this offset come from?
            wrapper.newLineAtOffset(marginLeft, marginTop);
            wrapper.getContentStream().showText("Merged PDF Files");

            // make content list
            int fontSize = 12;
            wrapper.getContentStream().setFont(PDType1Font.HELVETICA, fontSize);
            wrapper.newLineAtOffset(0, -20);

            for (ContentEntry entry : entries) {
                String text = (entry.firstPage + 2) + " " + entry.name();
                wrapper.getContentStream().showText(text);
                wrapper.newLineAtOffset(0, -20);

                PDPageDestination dest = new PDPageFitWidthDestination();
                dest.setPage(document.getPage(entry.firstPage));
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                PDAnnotationLink link = new PDAnnotationLink();
                link.setBorderStyle(new PDBorderStyleDictionary());
                link.setDestination(dest);
                link.setAction(action);

                // Calculate the position and size of the link annotation
                float width = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * fontSize;

                // Add the link annotation to the cover page
                link.setRectangle(new PDRectangle(wrapper.getX(), wrapper.getY(), width, (float) fontSize));
                coverPage.getAnnotations().add(link);
            }
            wrapper.getContentStream().endText();
            return coverPage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private record ContentEntry(String name, int firstPage) {

    }

    public File createMergedDocument(List<File> pdfFiles) {
        if (pdfFiles.size() == 0) return null;
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        int currentPage = 0;
        List<ContentEntry> contentEntries = new ArrayList<>();
        File file = new File(tempDirWithPrefix + "tmpfile.pdf");

        // Initial run: merge documents
        try (PDDocument doc = new PDDocument()) {

            for (File pdfFile : pdfFiles) {
                try (PDDocument document = PDDocument.load(pdfFile)) {
                    pdfMerger.appendDocument(doc, document);
                    contentEntries.add(new ContentEntry(pdfFile.getName(), currentPage));
                    currentPage += document.getNumberOfPages();
                }
            }

            pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            doc.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (PDDocument doc = PDDocument.load(file)) {
            //PDPage coverPage = new PDPage();
            PDPage coverPage = createCoverPage(doc, contentEntries, new PDPage());
            doc.getPages().insertBefore(coverPage, doc.getPage(0));

            doc.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file;
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
