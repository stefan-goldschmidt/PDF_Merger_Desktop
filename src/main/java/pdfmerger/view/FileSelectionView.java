package pdfmerger.view;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignO;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FileSelectionView extends BorderPane {
    @FXML
    public ComboBox<SortingStrategy<File>> sortingChoiceBox;
    @FXML
    private ListView<File> listView;
    @FXML
    public Button addFilesButton;
    private ObservableValue<Comparator<File>> sortingComparator;

    public ObservableList<File> getUnsortedFileList() {
        return unsortedFileList;
    }

    private ObservableList<File> unsortedFileList = FXCollections.observableArrayList();


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
        sortingChoiceBox.setButtonCell(new SortingStrategyListCell());
        sortingChoiceBox.setCellFactory(param -> new SortingStrategyListCell());
        sortingChoiceBox.getItems().addAll(List.of(
                new SortingStrategy<>("Alphabetical", () -> FontIcon.of(MaterialDesignO.ORDER_ALPHABETICAL_ASCENDING, 12), Comparator.comparing(file -> file.getName().toLowerCase())),
                new SortingStrategy<>("Alphabetical", () -> FontIcon.of(MaterialDesignO.ORDER_ALPHABETICAL_DESCENDING, 12), Comparator.comparing((File file1) -> file1.getName().toLowerCase()).reversed()),
                new SortingStrategy<>("Custom (None)", () -> FontIcon.of(MaterialDesignO.ORDER_BOOL_DESCENDING_VARIANT, 12), Comparator.comparingInt(f -> 0)) // No sorting
        ));
        sortingChoiceBox.getSelectionModel().selectFirst();

        sortingComparator = sortingChoiceBox.getSelectionModel().selectedItemProperty().map(SortingStrategy::comparator);

        SortedList<File> sortedList = new SortedList<>(unsortedFileList);
        sortedList.comparatorProperty().bind(sortingComparator);
        listView.setItems(sortedList);

        listView.setCellFactory(param -> new FileListCell(fileToDelete -> unsortedFileList.remove(fileToDelete)));
        addFilesButton.setOnAction(e -> addFilesAction());
    }

    public ReadOnlyObjectProperty<ObservableList<File>> itemsProperty() {
        return listView.itemsProperty();
    }

    public void setSortingComparator(Comparator<File> comparator) {
        //sortingComparator.set(comparator);
    }


    private static class FileListCell extends ListCell<File> {
        HBox hbox = new HBox();
        Label label = new Label();
        Pane pane = new Pane();
        Button button = new Button("✕");
        File lastItem;

        public FileListCell(Consumer<File> deleteAction) {
            super();
            label.setWrapText(true);
            hbox.getChildren().addAll(label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);
            button.setOnAction(event -> deleteAction.accept(lastItem));
        }

        @Override
        protected void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                lastItem = null;
                setGraphic(null);
                setText(null);
                return;
            }

            lastItem = item;
            setText(item.getName());
            setGraphic(hbox);
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
        unsortedFileList.addAll(list);
    }

    private static class SortingStrategyListCell extends ListCell<SortingStrategy<File>> {
        @Override
        protected void updateItem(SortingStrategy<File> item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(item.icon().get());
                setText(item.displayName());
            }
        }
    }
}
