package pdfmerger.tableofcontents;

import java.util.List;

public record TocPage(String heading, List<TocSection> sections) {
}
