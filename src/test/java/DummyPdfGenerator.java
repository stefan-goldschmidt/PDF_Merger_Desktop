import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DummyPdfGenerator {

    public static void main(String[] args) {

        List<String> fileNames = List.of(
                "Quantitative Analysis of Apple Pectin Isolated from Different Varieties_Version1",
                "Effect of Banana Peel Extract on Oxidative Stress in Rat Liver_Version2",
                "Chemical_Characterization_of_Anthocyanins_from_Cherry_Fruit",
                "Antioxidant Activity of Date Palm Pollen Extracts_Version1",
                "Bioactive Compounds in Elderberry and Their Health Benefits_Version2",
                "A_Study_of_the_Nutritional_Value_of_Fig_Fruit_Version1",
                "Grape Seed Extract as a Potential Therapeutic Agent for Cancer_Version2",
                "Honeydew_Melon_Peptides_and_Their_Antioxidant_Properties",
                "Indian_gooseberry_extract_and_its_potential_as_a_natural_preservative_Version1",
                "A Comprehensive Review of Jackfruit as a Functional Food_Version2",
                "Kiwi_Fruit_and_Its_Health_Benefits:_A_Review",
                "Lemon_Essential_Oil_and_Its_Anti-inflammatory_Properties_Version1",
                "Mango Peel Extract and Its Potential in Diabetes Management_Version2",
                "Nectarine as a Source of Bioactive Compounds: A Systematic Review_Version1",
                "Orange_Juice_and_Its_Effect_on_Cardiovascular_Health",
                "Papaya_Leaf_Extract_as_a_Natural_Antimicrobial_Agent_Version2",
                "Pharmacological_Properties_of_Quince_Fruit_Extracts_Version1",
                "Raspberry_Ketones_and_Their_Metabolic_Effects",
                "Strawberry_Phenolics_and_Their_Antioxidant_Activity_Version2",
                "Tomato_Lycopene_and_Its_Potential_in_Cancer_Prevention_Version1",
                "Ugli_fruit_extract_and_its_potential_in_cosmeceuticals_Version2",
                "Vanilla_Bean_Extract_and_Its_Aromatic_Properties_Version1",
                "Watermelon_Rind_Extract_and_Its_Health_Benefits",
                "Xigua_Seed_Oil_and_Its_Anti-inflammatory_Activity_Version2",
                "Yellow_Passionfruit_Juice_and_Its_Nutritional_Value_Version1"
        );

        createPdfFiles(fileNames);
    }

    private static void createPdfFiles(List<String> strings) {
        for (String text : strings) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    PDType1Font font = PDType1Font.HELVETICA_BOLD;
                    int fontSize = 12;
                    float textWidth = font.getStringWidth(text) / 1000 * fontSize;
                    float startX = (page.getMediaBox().getWidth() - textWidth) / 2;
                    float startY = page.getMediaBox().getHeight() / 2;

                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(startX, startY);
                    contentStream.showText(text);
                    contentStream.endText();

                    String fileName = text + ".pdf";
                    contentStream.close();
                    document.save(new File("src/test/resources/pdf/"+fileName));
                    System.out.println("Created PDF: " + fileName);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
