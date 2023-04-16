module pdfmerger {
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    opens org.pdfmerger;
    exports org.pdfmerger;
}