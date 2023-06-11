package pdfmerger.tableofcontents;

import java.util.List;

public record TocSection(String sectionName, List<TocEntry> contentEntries) {
}
