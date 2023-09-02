package pdfmerger;

import org.junit.jupiter.api.Test;
import pdfmerger.tableofcontents.Toc;
import pdfmerger.tableofcontents.TocBuilder;
import pdfmerger.tableofcontents.TocEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TocBuilderTest {

    @Test
    public void entriesShouldBeDividedIntoSectionsAcrossMultiplePages1() {
        // Create a TocBuilder2 instance with a maximum of 1 entries per page
        TocBuilder builder = new TocBuilder();

        // Add entries starting with A
        builder.addEntry(new TocEntry("Apple", 0));
        builder.addEntry(new TocEntry("Artichoke", 4));

        // Add entries starting with B
        builder.addEntry(new TocEntry("Banana", 5));

        // Add entries starting with C
        builder.addEntry(new TocEntry("Cherry", 6));
        builder.addEntry(new TocEntry("Coconut", 7));
        builder.addEntry(new TocEntry("Carrot", 8));

        // Build the Toc
        Toc toc = builder.build();

        assertEquals("""
                        -A
                        --Apple
                        --Artichoke
                        -B
                        --Banana
                        -C
                        --Cherry
                        --Coconut
                        --Carrot""",
                toc.getAsciiVisualization());

        System.out.println(toc.getAsciiVisualization());
    }

    @Test
    public void entriesShouldBeDividedIntoSectionsAcrossMultiplePages2() {
        // Create a TocBuilder2 instance with a maximum of 2 entries per page
        TocBuilder builder = new TocBuilder("");

        // Add entries starting with A
        builder.addEntry(new TocEntry("Apple", 0));
        builder.addEntry(new TocEntry("Apricot", 1));
        builder.addEntry(new TocEntry("Avocado", 2));
        builder.addEntry(new TocEntry("Almond", 3));
        builder.addEntry(new TocEntry("Artichoke", 4));

        // Add entries starting with B
        builder.addEntry(new TocEntry("Banana", 5));

        // Add entries starting with C
        builder.addEntry(new TocEntry("Cherry", 6));
        builder.addEntry(new TocEntry("Coconut", 7));
        builder.addEntry(new TocEntry("Carrot", 8));

        // Build the Toc
        Toc toc = builder.build();

        assertEquals("""
                        -A
                        --Apple
                        --Apricot
                        --Avocado
                        --Almond
                        --Artichoke
                        -B
                        --Banana
                        -C
                        --Cherry
                        --Coconut
                        --Carrot""",
                toc.getAsciiVisualization());
    }

    @Test
    public void testBuildWithNoEntries() {
        TocBuilder TocBuilder = new TocBuilder("");

        Toc toc = TocBuilder.build();

        assertEquals(0, toc.tocSections().size());
    }

    @Test
    public void sectionsShouldBeCreatedFromEntries() {
        // Create a TocBuilder2 instance
        TocBuilder builder = new TocBuilder();

        // Add entries starting with A
        builder.addEntry(new TocEntry("Apple", 0));
        builder.addEntry(new TocEntry("Apricot", 1));
        builder.addEntry(new TocEntry("Avocado", 2));

        // Add entries starting with B
        builder.addEntry(new TocEntry("Banana", 3));

        // Add entries starting with C
        builder.addEntry(new TocEntry("Cherry", 4));
        builder.addEntry(new TocEntry("Cranberry", 5));

        // Build the Toc
        Toc toc = builder.build();

        // Verify the sections
        assertEquals(3, toc.tocSections().size());
    }

    @Test
    public void shouldIgnoreCase() {
        TocBuilder TocBuilder = new TocBuilder();

        TocBuilder.addAll(List.of(
                new TocEntry("A", 0),
                new TocEntry("a", 1),
                new TocEntry("b", 2),
                new TocEntry("B", 3),
                new TocEntry("C", 4),
                new TocEntry("d", 5)
        ));

        Toc toc = TocBuilder.build();
        assertEquals("""
                        -A
                        --A
                        --a
                        -B
                        --b
                        --B
                        -C
                        --C
                        -D
                        --d""",
                toc.getAsciiVisualization());

    }


}
