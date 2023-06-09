open module pdfmerger {
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.materialdesign2;
    exports pdfmerger.tableofcontents;
    exports pdfmerger;
    exports pdfmerger.pdf;
    exports pdfmerger.view;
}
