package pdfmerger;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManualTest {

    @Test
    public void testStartApplication() throws InterruptedException {
        Platform.setImplicitExit(false);
        Platform.startup(() -> {
        });

        Platform.runLater(() -> {
            try {
                // Load the root FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Root.fxml"));
                Parent root = loader.load();

                // Create a new stage and set the root
                Stage stage = new Stage();
                stage.setScene(new Scene(root, 1400, 1000));

                // Get the controller from the loader
                RootController controller = loader.getController();

                // Perform any assertions or additional setup
                assertNotNull(controller);

                // Show the stage
                stage.initStyle(StageStyle.DECORATED);
                stage.show();

                File[] files = new File(getClass().getResource("/dummyfiles/").getPath()).listFiles();
                controller.getFileListView().getUnsortedFileList().setAll(files);
                System.out.println("");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread.sleep(60_000_000);
    }
}
