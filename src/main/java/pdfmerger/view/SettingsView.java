package pdfmerger.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SettingsView extends BorderPane {

    public static final int ENTRIES_PER_PAGE_DEFAULT = 42;
    private final ObjectProperty<SettingsRecord> settings = new SimpleObjectProperty<>(
            new SettingsRecord(getFormattedDate(), ENTRIES_PER_PAGE_DEFAULT)
    );
    @FXML
    private Spinner<Integer> entriesPerPageSpinner;

    @FXML
    private TextField documentName;

    @FXML
    private Button mergeDocumentsButton;

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
        // Limit the spinner values to a range of 1 to 1000
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000);
        valueFactory.setValue(ENTRIES_PER_PAGE_DEFAULT);
        entriesPerPageSpinner.setValueFactory(valueFactory);

        entriesPerPageSpinner.setRepeatDelay(Duration.INDEFINITE);

        // Create the settings property and bind it to the spinner value

        mergeDocumentsButton.setOnAction(e -> settings.set(new SettingsRecord(
                documentName.textProperty().getValue(),
                entriesPerPageSpinner.getValue()
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
