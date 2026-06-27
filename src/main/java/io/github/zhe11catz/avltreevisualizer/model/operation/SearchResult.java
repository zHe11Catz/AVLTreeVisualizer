package io.github.zhe11catz.avltreevisualizer.model.operation;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;

import java.util.Collections;
import java.util.List;

/**
 * Result of a search operation.
 */
public class SearchResult implements OperationResult {

    private final boolean found;
    private final AVLNode targetNode;
    private final List<TreeStep> steps;

    public SearchResult(boolean found, AVLNode targetNode, List<TreeStep> steps) {
        this.found = found;
        this.targetNode = targetNode;
        this.steps = steps == null ? List.of() : List.copyOf(steps);
    }

    @Override
    public boolean isSuccess() {
        return found;
    }

    public AVLNode getTargetNode() {
        return targetNode;
    }

    @Override
    public List<TreeStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }
}
