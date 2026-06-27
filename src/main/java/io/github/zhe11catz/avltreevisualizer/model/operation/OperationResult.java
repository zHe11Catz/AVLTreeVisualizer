package io.github.zhe11catz.avltreevisualizer.model.operation;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;

import java.util.List;

/**
 * Common contract for tree operation results that may include animation steps.
 */
public interface OperationResult {

    /**
     * @return true if the operation changed tree state or found a target node
     */
    boolean isSuccess();

    /**
     * @return ordered steps produced by the model for the view layer to animate
     */
    List<TreeStep> getSteps();
}
