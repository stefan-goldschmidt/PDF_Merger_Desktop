package pdfmerger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import pdfmerger.pdf.PdfHandler;
import pdfmerger.view.FileSelectionView;
import pdfmerger.view.PdfViewer;
import pdfmerger.view.SettingsView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RootController implements Initializable {

    private final PdfHandler pdfHandler = new PdfHandler();
    @FXML
    private SettingsView settingsView;
    @FXML
    private HBox menuBar;
    private final ObjectProperty<File> saveDirectory = new SimpleObjectProperty<>(new File(""));
    @FXML
    private PdfViewer pdfViewer;
    @FXML
    private Button saveFileButton;
    @FXML
    private FileSelectionView fileListView;

    public FileSelectionView getFileListView() {
        return fileListView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pdfHandler.settingsProperty().bind(settingsView.settingsProperty());

        saveFileButton.disableProperty().bind(pdfHandler.canSaveProperty().not());
        pdfHandler.filesProperty().bind(fileListView.itemsProperty());
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
