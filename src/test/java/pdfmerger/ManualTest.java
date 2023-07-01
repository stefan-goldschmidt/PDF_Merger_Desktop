package pdfmerger;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

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

                // Optionally, perform additional tests or interactions with the UI

                List<String> fileNameList = List.of(
                        "A Comprehensive Review of Jackfruit as a Functional Food_Version2_Pages_1.pdf",
                                "A_Study_of_the_Nutritional_Value_of_Fig_Fruit_Version1_Pages_3.pdf",
                                "antioxidant Activity of Date Palm Pollen Extracts_Version1_Pages_3.pdf",
                                "Bioactive Compounds in Elderberry and Their Health Benefits_Version2_Pages_3.pdf",
                                "Chemical_Characterization_of_Anthocyanins_from_Cherry_Fruit_Pages_1.pdf",
                                "Effect of Banana Peel Extract on Oxidative Stress in Rat Liver_Version2_Pages_3.pdf",
                                "Grape Seed Extract as a Potential Therapeutic Agent for Cancer_Version2_Pages_1.pdf",
                                "Honeydew_Melon_Peptides_and_Their_Antioxidant_Properties_Pages_3.pdf",
                                "Indian_gooseberry_extract_and_its_potential_as_a_natural_preservative_Version1_Pages_3.pdf",
                                "Kiwi_Fruit_and_Its_Health_Benefits:_A_Review_Pages_2.pdf",
                                "lemon_Essential_Oil_and_Its_Anti-inflammatory_Properties_Version1_Pages_3.pdf",
                                "Mango Peel Extract and Its Potential in Diabetes Management_Version2_Pages_3.pdf",
                                "Nectarine as a Source of Bioactive Compounds: A Systematic Review_Version1_Pages_3.pdf",
                                "Orange_Juice_and_Its_Effect_on_Cardiovascular_Health_Pages_1.pdf",
                                "Papaya_Leaf_Extract_as_a_Natural_Antimicrobial_Agent_Version2_Pages_5.pdf",
                                "pharmacological_Properties_of_Quince_Fruit_Extracts_Version1_Pages_3.pdf",
                                "Quantitative Analysis of Apple Pectin Isolated from Different Varieties_Version1_Pages_1.pdf",
                                "Raspberry_Ketones_and_Their_Metabolic_Effects_Pages_3.pdf",
                                "Strawberry_Phenolics_and_Their_Antioxidant_Activity_Version2_Pages_3.pdf",
                                "Tomato_Lycopene_and_Its_Potential_in_Cancer_Prevention_Version1_Pages_3.pdf",
                                "Ugli_fruit_extract_and_its_potential_in_cosmeceuticals_Version2_Pages_3.pdf",
                                "Vanilla_Bean_Extract_and_Its_Aromatic_Properties_Version1_Pages_3.pdf",
                                "Watermelon_Rind_Extract_and_Its_Health_Benefits_Pages_3.pdf",
                                "Xigua_Seed_Oil_and_Its_Anti-inflammatory_Activity_Version2_Pages_3.pdf",
                                "Yellow_Passionfruit_Juice_and_Its_Nutritional_Value_Version1_Pages_2.pdf"
                );

                List<File> files = fileNameList.stream()
                        .map(fn -> getClass().getResource("/pdf/").getPath() + fn)
                        .map(File::new)
                        .toList();

                controller.fileList.setAll(files);
                // Close the stage
                //  stage.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(60_000_000);
    }
}
