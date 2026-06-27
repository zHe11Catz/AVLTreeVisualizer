package io.github.zhe11catz.avltreevisualizer.model.operation;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;

import java.util.Collections;
import java.util.List;

/**
 * Result of a delete operation.
 */
public class DeleteResult implements OperationResult {

    private final boolean success;
    private final AVLNode root;
    private final List<TreeStep> steps;

    public DeleteResult(boolean success, AVLNode root, List<TreeStep> steps) {
        this.success = success;
        this.root = root;
        this.steps = steps == null ? List.of() : List.copyOf(steps);
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public AVLNode getRoot() {
        return root;
    }

    @Override
    public List<TreeStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }
}
