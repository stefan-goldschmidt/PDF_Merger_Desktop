package org.pdfmerger;

import javafx.scene.Node;

import java.io.File;
import java.util.Comparator;
import java.util.function.Supplier;

public record SortingStrategy(String displayName, Supplier<Node> icon, Comparator<File> comparator) {
}