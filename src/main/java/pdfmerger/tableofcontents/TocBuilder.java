package pdfmerger.tableofcontents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TocBuilder {

    private final List<TocEntry> tocEntries = new ArrayList<>();
    private final String documentTitle;

    public TocBuilder() {
        this.documentTitle = "";
    }

    public TocBuilder(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public void addEntry(TocEntry tocEntry) {
        tocEntries.add(tocEntry);
    }


    public Map<String, List<TocEntry>> getSectionsMap() {
        LinkedHashMap<String, List<TocEntry>> map = new LinkedHashMap<>();
        for (TocEntry entry : tocEntries) {
            String sectionName = String.valueOf(entry.name().toUpperCase().charAt(0));
            map.putIfAbsent(sectionName, new ArrayList<>());
            map.get(sectionName).add(entry);
        }
        return map;
    }

    public Toc build() {
        return new Toc(documentTitle, getSectionsMap());
    }

    public void addAll(List<TocEntry> tocEntries) {
        this.tocEntries.addAll(tocEntries);
    }
}
