package io.github.zhe11catz.avltreevisualizer.model.operation.step;

import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;

/**
 * Marks the point in a step sequence where the underlying tree structure
 * actually changes (a node is inserted/removed and/or rebalanced).
 * <p>
 * The model computes the operation synchronously and to completion before
 * any animation plays, so without this marker the view would have to choose
 * between showing the OLD structure for the whole animation (nodes appear/
 * disappear at the wrong time) or the NEW structure for the whole animation
 * (same problem, reversed). By recording this step at the exact moment the
 * structural change conceptually happens - after the search-path highlights
 * that lead up to it, before any post-rebalance rotation highlights - the
 * view can swap to {@link #newRoot()} at the right time in playback.
 */
public record StructuralChangeStep(AVLNode newRoot) implements TreeStep {
}