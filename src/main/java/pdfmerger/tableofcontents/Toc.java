package pdfmerger.tableofcontents;

import java.util.List;

public record Toc(List<TocPage> tocPages) {
    public String getAsciiVisualization() {

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < tocPages.size(); i++) {

            TocPage page = tocPages.get(i);
            s.append(i).append(" ").append(page.heading()).append("\n");
            for (TocSection section : page.sections()) {
                s.append("-").append(section.sectionName()).append(":").append("\n");
                for (TocEntry entry : section.contentEntries()) {
                    s.append("--").append(entry.name()).append("\n");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
