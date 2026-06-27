package io.github.zhe11catz.avltreevisualizer.model.operation.step;

/**
 * Represents a node movement step before the layout is finalized.
 */
public record MoveStep(int key, double fromX, double fromY, double toX, double toY) implements TreeStep {
}
