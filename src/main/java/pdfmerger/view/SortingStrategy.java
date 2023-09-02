package pdfmerger.view;

import javafx.scene.Node;

import java.util.Comparator;
import java.util.function.Supplier;

public record SortingStrategy<T>(String displayName, Supplier<Node> icon, Comparator<T> comparator) {
}