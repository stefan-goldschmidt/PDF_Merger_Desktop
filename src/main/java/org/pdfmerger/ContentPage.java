package org.pdfmerger;

import java.util.List;

public record ContentPage(String heading, List<ContentSection> sections) {
}
