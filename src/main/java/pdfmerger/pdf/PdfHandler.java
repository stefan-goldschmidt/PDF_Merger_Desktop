package pdfmerger.pdf;

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
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.util.Matrix;
import pdfmerger.tableofcontents.TocBuilder;
import pdfmerger.tableofcontents.TocEntry;
import pdfmerger.view.SettingsRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PdfHandler {

    private final ObjectProperty<SettingsRecord> settings = new SimpleObjectProperty<>();
    private final Path tempDirWithPrefix;
    private final BooleanProperty canSave = new SimpleBooleanProperty(false);
    private final ObjectProperty<File> outputDocument = new SimpleObjectProperty<>(null);
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

    private static PDAnnotationLink getPdAnnotationLink(PDPage page) {
        PDPageDestination dest = new PDPageFitWidthDestination();
        dest.setPage(page);
        PDActionGoTo action = new PDActionGoTo();
        action.setDestination(dest);
        PDAnnotationLink link = new PDAnnotationLink();
        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        link.setBorderStyle(borderStyle);
        link.setDestination(dest);
        link.setAction(action);
        return link;
    }

    private static PDAnnotationLink getPdAnnotationLink(PDDocument document, int entry) {
        PDPageDestination dest = new PDPageFitWidthDestination();
        dest.setPage(document.getPage(entry));
        PDActionGoTo action = new PDActionGoTo();
        action.setDestination(dest);
        PDAnnotationLink link = new PDAnnotationLink();
        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        link.setBorderStyle(borderStyle);
        link.setDestination(dest);
        link.setAction(action);
        return link;
    }

    private static void addStartButtonToEachPage(File file) {
        try (PDDocument doc = PDDocument.load(file)) {
            Iterator<PDPage> iterator = doc.getPages().iterator();
            if (iterator.hasNext()) {
                // skip first page
                iterator.next();
            }
            iterator.forEachRemaining(page -> {
                try (PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.PREPEND, false)) {
                    int FONT_SIZE = 12;

                    // Page 1
                    PDFont font = PDType1Font.HELVETICA;

                    // Get the non-justified string width in text space units.
                    String message = "Start";
                    float stringWidth = font.getStringWidth(message) * FONT_SIZE;

                    // Get the string height in text space units.
                    float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * FONT_SIZE;

                    PDRectangle pageSize = page.getMediaBox();
                    stream.beginText();
                    stream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
                    stream.setTextMatrix(Matrix.getTranslateInstance(stringWidth * 0.1f / 1000f, pageSize.getUpperRightY() - stringHeight / 1000f));
                    stream.showText(message);
                    stream.endText();
                    // Create a link annotation for the entry
                    PDAnnotationLink link = getPdAnnotationLink(doc, 0);

                    PDRectangle rectangle = new PDRectangle(0, page.getMediaBox().getUpperRightY() - FONT_SIZE * 2, stringWidth * 1.3f / 1000f, FONT_SIZE * 2);
                    link.setRectangle(rectangle);
                    page.getAnnotations().add(link);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            doc.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectProperty<SettingsRecord> settingsProperty() {
        return settings;
    }

    public BooleanProperty canSaveProperty() {
        return canSave;
    }

    public ObjectProperty<File> outputDocumentProperty() {
        return outputDocument;
    }

    public ListProperty<File> filesProperty() {
        return filesList;
    }

    private void updateDocument() {
        createMergedDocumentWithLoadingDialog(filesList, settings.get());
    }

    public void createMergedDocumentWithLoadingDialog(List<File> filesList, SettingsRecord settings) {

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

    private void insertTableOfContents(PDDocument document, Map<String, List<TocEntry>> toc, SettingsRecord settings) {
        if (document.getNumberOfPages() == 0) return;

        // Get the first page of the document
        PDPage lastAddedPage = document.getPage(0);

        int fontSize = 12;
        PDType1Font entryFont = PDType1Font.HELVETICA;
        PDType1Font sectionFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
        int titleFontSize = 18;

        PDPage currentPage = new PDPage(PDRectangle.A4);
        boolean currentPageHasBeenAdded = false;
        boolean isPageFull = false;
        boolean showSection;
        boolean showDocumentTitle = true;

        float pageBorderPadding = 50;
        float y = currentPage.getMediaBox().getHeight() - pageBorderPadding;
        float titleHeight = titleFont.getFontDescriptor().getFontBoundingBox().getHeight() * titleFontSize;
        float lineHeight = entryFont.getFontDescriptor().getFontBoundingBox().getHeight() * fontSize;
        int pageJumpOffset = 0;

        for (Map.Entry<String, List<TocEntry>> section : toc.entrySet()) {
            if (section.getValue().isEmpty()) {
                continue;
            }
            showSection = true;
            PDRectangle pageSize = currentPage.getMediaBox();

            for (TocEntry entry : section.getValue()) {
                if (isPageFull) {
                    document.getPages().insertBefore(currentPage, lastAddedPage);
                    currentPage = new PDPage(PDRectangle.A4);
                    pageJumpOffset++;
                    showSection = true;
                    showDocumentTitle = true;
                    y = currentPage.getMediaBox().getHeight() - pageBorderPadding;
                }
                try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, false)) {

                    if (showDocumentTitle) {
                        contentStream.setFont(titleFont, titleFontSize);
                        contentStream.beginText();
                        contentStream.setTextMatrix(Matrix.getTranslateInstance(pageBorderPadding, y));
                        contentStream.showText(settings.documentName());
                        contentStream.endText();
                        showDocumentTitle = false;
                    }


                    // |                                                                              |
                    // |  pageBorderPadding  section  showDocumentTitle pageNumber  pageBorderPadding |
                    // |                                                                              |
                    y -= lineHeight / 1000f;

                    if (showSection) {
                        contentStream.setFont(sectionFont, fontSize);
                        contentStream.beginText();
                        contentStream.setTextMatrix(Matrix.getTranslateInstance(pageBorderPadding, y));
                        contentStream.showText(section.getKey());
                        contentStream.endText();
                        showSection = false;
                    }

                    contentStream.beginText();
                    contentStream.setFont(entryFont, fontSize);

                    // ENTRY
                    float seperatorPadding = 20;
                    float entryX = pageBorderPadding + seperatorPadding;
                    contentStream.setTextMatrix(Matrix.getTranslateInstance(entryX, y));
                    String fileName = entry.name();

                    // TODO: this cutoff is not flexible enough for many use cases
                    // an ideal solution needs to calculate the actual width of the text
                    // and should use some kind of wrapping like "..."
                    contentStream.showText(fileName.substring(0, Math.min(fileName.length(), 75)));

                    // PAGE NUMBER
                    contentStream.setTextMatrix(Matrix.getTranslateInstance(pageSize.getWidth() - pageBorderPadding, y));
                    contentStream.showText(String.valueOf(entry.referencedPage() + 1));
                    System.out.println("y: " + y + " " + fileName);

                    isPageFull = y - pageBorderPadding < 0;
                    contentStream.endText();

                    // ANNOTATION
                    // Create a link annotation for the entry
                    PDAnnotationLink link = getPdAnnotationLink(document.getPage(entry.referencedPage() + pageJumpOffset));

                    // Calculate the position and size of the link annotation
                    float width = pageSize.getWidth() - entryX - pageBorderPadding - seperatorPadding;

                    // Add the link annotation to the page
                    int linkPadding = 2;
                    PDRectangle rectangle = new PDRectangle(entryX - linkPadding, y - linkPadding, width + linkPadding, (float) fontSize + linkPadding);
                    link.setRectangle(rectangle);
                    currentPage.getAnnotations().add(link);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


        }

        if (!currentPageHasBeenAdded) {
            document.getPages().insertBefore(currentPage, lastAddedPage);
        }
    }

    public File createMergedDocument(List<File> pdfFiles, SettingsRecord settings, Consumer<Integer> progressUpdateCallable) {
        if (pdfFiles.size() == 0) return null;
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        int currentPage = 0;
        TocBuilder tocBuilder = new TocBuilder(settings.documentName());
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
            insertTableOfContents(doc, tocBuilder.getSectionsMap(), settings);
            doc.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (settings.startButtonOnEachPage()) {
            addStartButtonToEachPage(file);
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
