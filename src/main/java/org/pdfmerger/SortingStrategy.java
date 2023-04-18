package org.pdfmerger;

import javafx.util.StringConverter;

import java.io.File;
import java.util.Comparator;

public record SortingStrategy(String displayName, Comparator<File> comparator) {
    static StringConverter<SortingStrategy> getSortingStrategyStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SortingStrategy object) {
                return object.displayName();
            }

            @Override
            public SortingStrategy fromString(String string) {
                return null;
            }
        };
    }
}