package pdfmerger.tableofcontents;

import java.util.ArrayList;
import java.util.List;

public class TocBuilder {

    private final List<TocEntry> tocEntries = new ArrayList<>();
    private final int maxEntriesPerPage;
    private final String documentTitle;

    public TocBuilder(int maxEntriesPerPage) {
        this.documentTitle = "";
        this.maxEntriesPerPage = maxEntriesPerPage;
    }

    public TocBuilder(String documentTitle, int maxEntriesPerPage) {
        this.documentTitle = documentTitle;
        this.maxEntriesPerPage = maxEntriesPerPage;
    }

    public void addEntry(TocEntry tocEntry) {
        tocEntries.add(tocEntry);
    }

    public Toc build() {
        List<TocPage> pagePartitionedEntries = partitionEntries(tocEntries, maxEntriesPerPage);
        return new Toc(pagePartitionedEntries);
    }

    private List<TocPage> partitionEntries(List<TocEntry> contentEntries, int partitionSize) {
        List<TocPage> tocPages = new ArrayList<>();

        TocPage currentPage = null;
        TocSection currentSection = null;
        int pageEntryCounter = 0;

        // Set the desired limit for entries per content page
        for (TocEntry entry : contentEntries) {
            // Check if a new content page needs to be created
            if (currentPage == null || currentSection == null || pageEntryCounter >= partitionSize) {
                // Create a new content page and section
                currentSection = new TocSection(String.valueOf(entry.name().charAt(0)), new ArrayList<>());
                currentPage = new TocPage(documentTitle, new ArrayList<>());
                currentPage.sections().add(currentSection);
                tocPages.add(currentPage);
                pageEntryCounter = 0;
            }

            // Check if a new content section needs to be created
            if (!String.valueOf(entry.name().charAt(0)).equals(currentSection.sectionName())) {
                // Create a new content section
                currentSection = new TocSection(String.valueOf(entry.name().charAt(0)), new ArrayList<>());
                currentPage.sections().add(currentSection);
            }

            // Add the entry to the current section
            currentSection.contentEntries().add(new TocEntry(entry.name(), entry.referencedPage() + tocPages.size() - 1));
            pageEntryCounter++;
        }

        return tocPages;

    }
}
