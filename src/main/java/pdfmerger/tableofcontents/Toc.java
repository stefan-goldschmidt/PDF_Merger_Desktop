package pdfmerger.tableofcontents;

import java.util.List;
import java.util.Map;

public record Toc(String documentTitle, Map<String, List<TocEntry>> tocSections) {
    public String getAsciiVisualization() {

        StringBuilder s = new StringBuilder();

        s.append(documentTitle).append("\n");
        for (Map.Entry<String, List<TocEntry>> mapEntry : tocSections.entrySet()) {
            s.append("-").append(mapEntry.getKey()).append("\n");
            for (TocEntry entry : mapEntry.getValue()) {
                s.append("--").append(entry.name()).append("\n");
            }
            //s.append("\n");
        }
        return s.toString().trim();
    }
}
