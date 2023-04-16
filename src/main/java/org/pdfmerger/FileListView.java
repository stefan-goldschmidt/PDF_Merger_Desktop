package org.pdfmerger;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.File;

public class FileListView extends ListView<File> {

    public FileListView() {
        this.setCellFactory(param -> new AttachmentListCell());
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
}
