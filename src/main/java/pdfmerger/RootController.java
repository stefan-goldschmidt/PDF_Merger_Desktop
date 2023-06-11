package pdfmerger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RootController implements Initializable {


    public ListProperty<File> fileList = new SimpleListProperty<>();
    @FXML
    public FileSelectionView fileListView;
    @FXML
    public HBox menuBar;
    public ObjectProperty<File> saveDirectory = new SimpleObjectProperty<>(new File(""));
    public ObjectProperty<File> mergedDocument = new SimpleObjectProperty<>(null);
    @FXML
    public PdfViewer pdfViewer;
    @FXML
    public Button saveFileButton;

    private final PdfHandler pdfHandler = new PdfHandler();

    // private WebView webView;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileList.bind(fileListView.itemsProperty());

        saveFileButton.disableProperty().bind(pdfHandler.canSaveProperty().not());
        pdfHandler.filesProperty().bind(fileList);
        pdfViewer.documentObjectProperty().bind(pdfHandler.outputDocumentProperty());
    }

    public void saveFileButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("MergedFiles.pdf");

        File saveFile = saveDirectory.get();
        if (saveFile != null && saveFile.exists()) {
            fileChooser.setInitialDirectory(saveFile);
        }

        File outputFile = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (outputFile == null) {
            throw new RuntimeException("output file is null");
        }

        pdfHandler.saveFileToDisk(outputFile);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("File has been saved");
        alert.showAndWait();
    }
}
