package org.pdfmerger;

import java.util.List;

public record ContentSection(String sectionName, List<ContentEntry> contentEntries) {
}
