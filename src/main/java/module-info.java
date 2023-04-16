module pdfmerger {
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    exports org.pdfmerger to javafx.graphics, javafx.fxml;
    opens org.pdfmerger to javafx.graphics, javafx.fxml;
}