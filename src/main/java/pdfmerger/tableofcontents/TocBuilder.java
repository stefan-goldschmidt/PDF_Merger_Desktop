package pdfmerger.tableofcontents;

import java.util.ArrayList;
import java.util.Collection;
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
            String sectionName = String.valueOf(entry.name().toUpperCase().charAt(0));
            if (currentPage == null || currentSection == null || pageEntryCounter >= partitionSize) {
                // Create a new content page and section
                currentSection = new TocSection(sectionName, new ArrayList<>());
                currentPage = new TocPage(documentTitle, new ArrayList<>());
                currentPage.sections().add(currentSection);
                tocPages.add(currentPage);
                pageEntryCounter = 0;
            }

            // Check if a new content section needs to be created
            if (!sectionName.equals(currentSection.sectionName().toUpperCase())) {
                // Create a new content section
                currentSection = new TocSection(sectionName, new ArrayList<>());
                currentPage.sections().add(currentSection);
            }

            // Add the entry to the current section
            currentSection.contentEntries().add(new TocEntry(entry.name(), entry.referencedPage()));
            pageEntryCounter++;
        }
        return tocPages;
    }

    public void addAll(Collection<TocEntry> entries) {
        tocEntries.addAll(entries);
    }
}
