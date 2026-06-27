package io.github.zhe11catz.avltreevisualizer.model.operation;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;

import java.util.Collections;
import java.util.List;

/**
 * Result of a tree traversal operation.
 */
public class TraversalResult implements OperationResult {

    private final TraversalType type;
    private final List<Integer> values;
    private final List<TreeStep> steps;

    public TraversalResult(TraversalType type, List<Integer> values, List<TreeStep> steps) {
        this.type = type;
        this.values = values == null ? List.of() : List.copyOf(values);
        this.steps = steps == null ? List.of() : List.copyOf(steps);
    }

    @Override
    public boolean isSuccess() {
        return !values.isEmpty() || !steps.isEmpty();
    }

    public TraversalType getType() {
        return type;
    }

    public List<Integer> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public List<TreeStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }
}
