package io.github.zhe11catz.avltreevisualizer.model.operation.step;

/**
 * Represents a comparison step during insert or search.
 */
public record CompareStep(int currentKey, int targetKey, boolean goLeft) implements TreeStep {
}
