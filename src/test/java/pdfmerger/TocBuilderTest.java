package pdfmerger;

import org.junit.jupiter.api.Test;
import pdfmerger.tableofcontents.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TocBuilderTest {

    @Test
    public void entriesShouldBeDividedIntoSectionsAcrossMultiplePages1() {
        // Create a TocBuilder instance with a maximum of 1 entries per page
        TocBuilder builder = new TocBuilder(1);

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
                        0 A
                        -A:
                        --Apple
                                                
                        1 A
                        -A:
                        --Artichoke
                                                
                        2 B
                        -B:
                        --Banana
                                                
                        3 C
                        -C:
                        --Cherry
                                                
                        4 C
                        -C:
                        --Coconut
                                                
                        5 C
                        -C:
                        --Carrot
                                                
                        """,
                toc.getAsciiVisualization());

        System.out.println(toc.getAsciiVisualization());
    }

    @Test
    public void entriesShouldBeDividedIntoSectionsAcrossMultiplePages2() {
        // Create a TocBuilder instance with a maximum of 2 entries per page
        TocBuilder builder = new TocBuilder(2);

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
                        0 A
                        -A:
                        --Apple
                        --Apricot
                                                
                        1 A
                        -A:
                        --Avocado
                        --Almond
                                                
                        2 A
                        -A:
                        --Artichoke
                        -B:
                        --Banana
                                                
                        3 C
                        -C:
                        --Cherry
                        --Coconut
                                                
                        4 C
                        -C:
                        --Carrot
                                                
                        """,
                toc.getAsciiVisualization());
    }

    @Test
    public void testBuildWithNoEntries() {
        TocBuilder tocBuilder = new TocBuilder(1000);

        Toc toc = tocBuilder.build();
        List<TocPage> tocPages = toc.tocPages();

        assertEquals(0, tocPages.size());
    }

    @Test
    public void sectionsShouldBeCreatedFromEntries() {
        // Create a TocBuilder instance
        TocBuilder builder = new TocBuilder(1000);

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
        assertEquals(1, toc.tocPages().size());

        TocPage page = toc.tocPages().get(0);
        assertEquals(3, page.sections().size());

        TocSection sectionA = page.sections().get(0);
        assertEquals("A", sectionA.sectionName());
        assertEquals(3, sectionA.contentEntries().size());

        TocSection sectionB = page.sections().get(1);
        assertEquals("B", sectionB.sectionName());
        assertEquals(1, sectionB.contentEntries().size());

        TocSection sectionC = page.sections().get(2);
        assertEquals("C", sectionC.sectionName());
        assertEquals(2, sectionC.contentEntries().size());
    }

    @Test
    public void testBuildWithEntries() {
        TocBuilder tocBuilder = new TocBuilder(1000);

        TocEntry entryA = new TocEntry("A", 0);
        TocEntry entryAa = new TocEntry("Aa", 1);
        TocEntry entryB = new TocEntry("B", 2);
        TocEntry entryC = new TocEntry("C", 3);

        tocBuilder.addEntry(entryA);
        tocBuilder.addEntry(entryAa);
        tocBuilder.addEntry(entryB);
        tocBuilder.addEntry(entryC);

        Toc toc = tocBuilder.build();
        assertEquals("""
                        0 A
                        -A:
                        --A
                        --Aa
                        -B:
                        --B
                        -C:
                        --C
                        
                        """,
                toc.getAsciiVisualization());

    }
}
