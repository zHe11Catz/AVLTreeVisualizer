package io.github.zhe11catz.avltreevisualizer.model.operation.step;

/**
 * Highlights a node during search, delete, rotation, or traversal, tagged
 * with a semantic kind so the view layer can render an appropriate color.
 */
public record HighlightStep(int key, HighlightKind kind) implements TreeStep {

    /**
     * Semantic meaning of a highlight step, used by TreeCanvas to pick a color.
     */
    public enum HighlightKind {
        /** Node currently being compared while walking down the tree. */
        COMPARE,
        /** Search target was found. */
        FOUND,
        /** Search target was not found; marks the last node compared. */
        NOT_FOUND,
        /** Node targeted for deletion, or its inorder successor. */
        DELETE_TARGET,
        /** Pivot node of an AVL rotation. */
        ROTATE_PIVOT,
        /** Node visited during a tree traversal. */
        VISIT,
        /** Node that was just structurally inserted into the tree. */
        NEW_NODE
    }
}