package pdfmerger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
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
import pdfmerger.tableofcontents.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Consumer;

public class PdfHandler {

    private final ObjectProperty<SettingsView.SettingsRecord> settings = new SimpleObjectProperty<>();

    public ObjectProperty<SettingsView.SettingsRecord> settingsProperty() {
        return settings;
    }

    private final Path tempDirWithPrefix;

    public BooleanProperty canSaveProperty() {
        return canSave;
    }

    private final BooleanProperty canSave = new SimpleBooleanProperty(false);

    public ObjectProperty<File> outputDocumentProperty() {
        return outputDocument;
    }

    private final ObjectProperty<File> outputDocument = new SimpleObjectProperty<>(null);

    public ListProperty<File> filesProperty() {
        return filesList;
    }

    private final ListProperty<File> filesList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public PdfHandler() {
        settings.addListener(observable -> updateDocument());
        filesList.addListener((ListChangeListener<File>) c -> updateDocument());
        canSave.bind(Bindings.createBooleanBinding(() -> outputDocument.get() != null && !filesList.isEmpty(), outputDocument, filesList));

        try {
            tempDirWithPrefix = Files.createTempDirectory("pdfmerger_temp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDocument() {
        createMergedDocumentWithLoadingDialog(filesList, settings.get());
    }

    public void createMergedDocumentWithLoadingDialog(List<File> filesList, SettingsView.SettingsRecord settings) {

        if (filesList.isEmpty()) return;
        Task<File> task = new Task<>() {
            @Override
            protected File call() {
                return createMergedDocument(filesList, settings, v -> this.updateProgress(v, filesList.size()));
            }
        };

        // Set up the loading dialog
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.WINDOW_MODAL);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().clear();
        ProgressIndicator pIndicator = new ProgressIndicator();
        pIndicator.progressProperty().bind(task.progressProperty());
        alert.setGraphic(pIndicator);
        alert.setHeaderText("Merging files");

        // Show the loading dialog
        alert.show();
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();

        task.setOnSucceeded(event -> {
            // Close the loading dialog
            alert.setResult(ButtonType.CLOSE);
            alert.close();

            // Retrieve the result of the task
            File mergedDocument = task.getValue();

            // Process the merged document as needed
            outputDocument.setValue(mergedDocument);
        });

        task.setOnFailed(event -> {
            // Close the loading dialog
            alert.close();

            // Handle any exception that occurred during the task execution
            Throwable exception = task.getException();
            exception.printStackTrace();

            // Show an error message to the user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("An error occurred");
            errorAlert.setContentText("Failed to create merged document. Please try again.");
            errorAlert.showAndWait();
        });
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

    private void insertTableOfContents(PDDocument document, Toc toc, SettingsView.SettingsRecord settings) {
        // Get the first page of the document
        PDPage currentPage = document.getPage(0);

        // Iterate through each TOC page
        for (TocPage tocPage : toc.tocPages()) {
            // Create a new page for the TOC
            final PDPage page = new PDPage();
            try (StreamWrapper wrapper = new StreamWrapper(document, page)) {
                wrapper.getContentStream().beginText();

                int marginLeft = 30;
                int marginTop = (int) (page.getMediaBox().getUpperRightY() - 50);

                // Make the title
                int headerFontSize = 14;
                wrapper.getContentStream().setFont(PDType1Font.HELVETICA_BOLD, headerFontSize);
                wrapper.setY(headerFontSize);
                wrapper.newLineAtOffset(marginLeft, marginTop);
                wrapper.getContentStream().showText(tocPage.heading());

                // Make the content list
                int fontSize = 12;
                wrapper.getContentStream().setFont(PDType1Font.HELVETICA, fontSize);
                int lineYOffset = -16;
                wrapper.newLineAtOffset(0, lineYOffset);

                // Iterate through each section in the TOC page
                for (TocSection section : tocPage.sections()) {
                    wrapper.getContentStream().showText(section.sectionName());
                    wrapper.newLineAtOffset(0, lineYOffset);

                    // Iterate through each entry in the section
                    for (TocEntry entry : section.contentEntries()) {
                        String text = (entry.referencedPage() + 2) + " " + entry.name();
                        wrapper.getContentStream().showText(text);
                        wrapper.newLineAtOffset(0, lineYOffset);

                        // Create a link annotation for the entry
                        PDPageDestination dest = new PDPageFitWidthDestination();
                        dest.setPage(document.getPage(entry.referencedPage()));
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        PDAnnotationLink link = new PDAnnotationLink();
                        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
                        link.setBorderStyle(borderStyle);
                        link.setDestination(dest);
                        link.setAction(action);

                        // Calculate the position and size of the link annotation
                        float width = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * fontSize;

                        // Add the link annotation to the page
                        int padding = 2;
                        PDRectangle rectangle = new PDRectangle(wrapper.getX() - padding, wrapper.getY() - padding, width + padding * 2, (float) fontSize + padding);
                        link.setRectangle(rectangle);
                        page.getAnnotations().add(link);
                    }
                }

                wrapper.getContentStream().endText();

                // Insert the TOC page before the current page
                document.getPages().insertBefore(page, currentPage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public File createMergedDocument(List<File> pdfFiles, SettingsView.SettingsRecord settings, Consumer<Integer> progressUpdateCallable) {
        if (pdfFiles.size() == 0) return null;
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        int currentPage = 0;
        TocBuilder tocBuilder = new TocBuilder(settings.documentName(), settings.entriesPerPage());
        File file = new File(tempDirWithPrefix + "tmpfile.pdf");

        // Initial run: merge documents
        try (PDDocument doc = new PDDocument()) {
            int currentFile = 0;
            for (File pdfFile : pdfFiles) {
                try (PDDocument document = PDDocument.load(pdfFile)) {
                    pdfMerger.appendDocument(doc, document);
                    tocBuilder.addEntry(new TocEntry(pdfFile.getName(), currentPage));
                    currentPage += document.getNumberOfPages();
                }
                progressUpdateCallable.accept(currentFile++);
            }

            pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            doc.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (PDDocument doc = PDDocument.load(file)) {
            Toc toc = tocBuilder.build();
            insertTableOfContents(doc, toc, settings);
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
