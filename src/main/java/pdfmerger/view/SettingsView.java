package pdfmerger.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SettingsView extends BorderPane {

    private final ObjectProperty<SettingsRecord> settings = new SimpleObjectProperty<>(
            new SettingsRecord(getFormattedDate(), false)
    );
    @FXML
    private Spinner<Integer> entriesPerPageSpinner;

    @FXML
    private TextField documentName;

    @FXML
    private Button mergeDocumentsButton;

    @FXML
    private CheckBox startButtonOnEachPage;

    public SettingsView() {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("SettingsView.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        // Create the settings property and bind it to the spinner value
        mergeDocumentsButton.setOnAction(e -> settings.set(new SettingsRecord(
                documentName.textProperty().getValue(),
                startButtonOnEachPage.selectedProperty().getValue()
        )));
        documentName.setText(getFormattedDate());
    }

    @FXML
    private void mergeDocuments() {

    }

    private static String getFormattedDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return currentDate.format(formatter);
    }

    public ObjectProperty<SettingsRecord> settingsProperty() {
        return settings;
    }

}
