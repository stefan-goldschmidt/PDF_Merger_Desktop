package pdfmerger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Disabled("Manual")
public class DummyPdfGenerator {

    public static void main(String[] args) {

        List<DummyFile> files = List.of(
                new DummyFile("Quantitative Analysis of Apple Pectin Isolated from Different Varieties_Version1", 1),
                new DummyFile("Effect of Banana Peel Extract on Oxidative Stress in Rat Liver_Version2", 3),
                new DummyFile("Chemical_Characterization_of_Anthocyanins_from_Cherry_Fruit", 1),
                new DummyFile("antioxidant Activity of Date Palm Pollen Extracts_Version1", 3),
                new DummyFile("Bioactive Compounds in Elderberry and Their Health Benefits_Version2", 3),
                new DummyFile("A_Study_of_the_Nutritional_Value_of_Fig_Fruit_Version1", 3),
                new DummyFile("Grape Seed Extract as a Potential Therapeutic Agent for Cancer_Version2", 1),
                new DummyFile("Honeydew_Melon_Peptides_and_Their_Antioxidant_Properties", 3),
                new DummyFile("Indian_gooseberry_extract_and_its_potential_as_a_natural_preservative_Version1", 3),
                new DummyFile("A Comprehensive Review of Jackfruit as a Functional Food_Version2", 1),
                new DummyFile("Kiwi_Fruit_and_Its_Health_Benefits:_A_Review", 2),
                new DummyFile("lemon_Essential_Oil_and_Its_Anti-inflammatory_Properties_Version1", 3),
                new DummyFile("Mango Peel Extract and Its Potential in Diabetes Management_Version2", 3),
                new DummyFile("Nectarine as a Source of Bioactive Compounds: A Systematic Review_Version1", 3),
                new DummyFile("Orange_Juice_and_Its_Effect_on_Cardiovascular_Health", 1),
                new DummyFile("Papaya_Leaf_Extract_as_a_Natural_Antimicrobial_Agent_Version2", 5),
                new DummyFile("pharmacological_Properties_of_Quince_Fruit_Extracts_Version1", 3),
                new DummyFile("Raspberry_Ketones_and_Their_Metabolic_Effects", 3),
                new DummyFile("Strawberry_Phenolics_and_Their_Antioxidant_Activity_Version2", 3),
                new DummyFile("Tomato_Lycopene_and_Its_Potential_in_Cancer_Prevention_Version1", 3),
                new DummyFile("Ugli_fruit_extract_and_its_potential_in_cosmeceuticals_Version2", 3),
                new DummyFile("Vanilla_Bean_Extract_and_Its_Aromatic_Properties_Version1", 3),
                new DummyFile("Watermelon_Rind_Extract_and_Its_Health_Benefits", 3),
                new DummyFile("Xigua_Seed_Oil_and_Its_Anti-inflammatory_Activity_Version2", 3),
                new DummyFile("Yellow_Passionfruit_Juice_and_Its_Nutritional_Value_Version1", 2)
        );

        createPdfFiles(files);
    }

    private static void createPdfFiles(List<DummyFile> files) {
        for (DummyFile file : files) {
            try (PDDocument document = new PDDocument()) {
                for (int i = 0; i < file.pages(); i++) {
                    PDPage page = new PDPage();
                    document.addPage(page);

                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        PDType1Font font = PDType1Font.HELVETICA_BOLD;
                        int fontSize = 12;
                        float textWidth = font.getStringWidth(file.name()) / 1000 * fontSize;
                        float startX = (page.getMediaBox().getWidth() - textWidth) / 2;
                        float startY = page.getMediaBox().getHeight() / 2;

                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);
                        contentStream.newLineAtOffset(startX, startY);
                        contentStream.showText(file.name());
                        contentStream.endText();

                    }
                }
                String fileName = file.name() + "_Pages_" + file.pages() + ".pdf";
                document.save(new File("src/test/resources/pdf/" + fileName));
                System.out.println("Created PDF: " + fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
