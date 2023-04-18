package org.pdfmerger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class FileSelectionView extends BorderPane {
    @FXML
    public ChoiceBox<SortingStrategy> sortingChoiceBox;
    @FXML
    public ListView<File> listView;

    @FXML
    public Button addFilesButton;

    public List<SortingStrategy> sortingStrategies = List.of(
            new SortingStrategy("Alphabetical (A-Z)", Comparator.comparing(File::getName)),
            new SortingStrategy("Alphabetical (Z-A)", Comparator.comparing(File::getName).reversed()),
            new SortingStrategy("Custom", Comparator.comparingInt(f -> 0)) // No sorting
    );

    public FileSelectionView() {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("FileSelectionView.fxml"));
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
        // Set up the sortingChoiceBox
        sortingChoiceBox.getItems().addAll(sortingStrategies);
        sortingChoiceBox.setConverter(SortingStrategy.getSortingStrategyStringConverter());
        sortingChoiceBox.getSelectionModel().selectFirst();

        ObservableValue<Comparator<File>> sortingComparator = sortingChoiceBox.getSelectionModel().selectedItemProperty().map(SortingStrategy::comparator);
        sortingComparator.addListener((observable, oldValue, newValue) -> listView.getItems().sort(newValue));

        listView.setCellFactory(param -> new AttachmentListCell());
        addFilesButton.setOnAction(e -> addFilesAction());
    }

    public ObjectProperty<ObservableList<File>> itemsProperty() {
        return listView.itemsProperty();
    }

    private static class AttachmentListCell extends ListCell<File> {
        @Override
        public void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                setText(item.getName());
            }
        }
    }

    @FXML
    public void addFilesAction() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        List<File> list = fileChooser.showOpenMultipleDialog(addFilesButton.getScene().getWindow());
        if (list == null || list.isEmpty()) {
            return;
        }
        listView.getItems().addAll(list);
    }

}
