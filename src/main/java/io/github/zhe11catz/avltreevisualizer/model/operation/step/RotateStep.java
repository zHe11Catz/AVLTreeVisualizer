package io.github.zhe11catz.avltreevisualizer.model.operation.step;

import io.github.zhe11catz.avltreevisualizer.model.tree.RotationType;

/**
 * Represents a rotation step during AVL rebalancing.
 */
public record RotateStep(RotationType rotationType, int pivotKey) implements TreeStep {
}
