package org.pdfmerger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfViewer extends StackPane {

    public ObjectProperty<File> documentObjectProperty() {
        return documentObject;
    }

    private final ObjectProperty<File> documentObject = new SimpleObjectProperty<>(null);
    private final IntegerProperty lastPage = new SimpleIntegerProperty(0);

    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);

    public PdfViewer() {
        Button prevButton = new Button("Previous Page");
        prevButton.setOnAction(e -> previousPage());

        Button nextButton = new Button("Next Page");
        nextButton.setOnAction(e -> nextPage());

        nextButton.disableProperty().bind(Bindings.createBooleanBinding(() -> currentPage.get() >= lastPage.get() - 1, currentPage, lastPage));
        prevButton.disableProperty().bind(Bindings.createBooleanBinding(() -> currentPage.get() <= 0, currentPage, lastPage));

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(Bindings.createStringBinding(() -> (lastPage.get() == 0 ? 0 : (currentPage.get() + 1)) + " of " + lastPage.get(), currentPage, lastPage));

        BorderPane borderPane = new BorderPane();
        ScrollPane root = new ScrollPane();
        ImageView imageView = new ImageView();
        imageView.setSmooth(false);
        imageView.setPreserveRatio(true);
        root.setContent(imageView);

        borderPane.setCenter(root);
        borderPane.setBottom(new HBox(prevButton, pageLabel, nextButton));
        this.getChildren().add(borderPane);

        lastPage.bind(Bindings.createIntegerBinding(() -> getLastPage(documentObject.get()), documentObject));

        ObjectBinding<Image> renderedImage =
                Bindings.createObjectBinding(() -> renderDocumentToImage(documentObject.get(), currentPage.get()), currentPage, documentObject);


        imageView.imageProperty().bind(renderedImage);
    }

    private Integer getLastPage(File file) {
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

    private void nextPage() {
        if (documentObject.get() == null) {
            return;
        }
        if (lastPage.get() == 0) {
            currentPage.set(0);
        } else if (currentPage.get() < lastPage.get() - 1) {
            currentPage.set(currentPage.get() + 1);
        }
    }

    private void previousPage() {
        if (documentObject.get() == null) {
            return;
        }
        if (lastPage.get() == 0) {
            currentPage.set(0);
        } else if (currentPage.get() > 0) {
            currentPage.set(currentPage.get() - 1);
        }
    }

    private static Image renderDocumentToImage(File file, int page) {
        if (file == null) return null;
        try (PDDocument document = PDDocument.load(file)) {
            if (document == null || document.getNumberOfPages() < page) {
                return null;
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = null;
            image = pdfRenderer.renderImage(page);
            return SwingFXUtils.toFXImage(image, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
