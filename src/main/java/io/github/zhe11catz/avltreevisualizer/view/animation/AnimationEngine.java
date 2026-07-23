package io.github.zhe11catz.avltreevisualizer.view.animation;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.CompareStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.HighlightStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.RotateStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.StructuralChangeStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;
import io.github.zhe11catz.avltreevisualizer.view.canvas.TreeCanvas;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.List;

/**
 * Plays back recorded {@link TreeStep}s on the {@link TreeCanvas} as a
 * sequence of highlight pauses, one step at a time.
 * <p>
 * Node-movement animation (see {@code MoveStep}) is not implemented yet;
 * RotateStep is currently visualized as a brief pivot-node highlight only.
 * Structural changes (a node actually appearing/disappearing, or the tree
 * being rebalanced) are applied exactly when a {@link StructuralChangeStep}
 * is reached in playback, so the canvas stays in sync with the highlights
 * that lead up to that change instead of jumping to the final state early.
 */
public class AnimationEngine {

    /** How long a completed highlight lingers before auto-clearing (REQ-3.4). */
    private static final Duration HIGHLIGHT_LINGER = Duration.seconds(2);

    private final TreeCanvas treeCanvas;
    private final AnimationConfig animationConfig;

    // Tracks the currently pending step delay so stop() can cancel it,
    // and whether playback has been cancelled so playFrom() stops recursing.
    private PauseTransition currentPause;
    private boolean cancelled;

    public AnimationEngine(TreeCanvas treeCanvas, AnimationConfig animationConfig) {
        this.treeCanvas = treeCanvas;
        this.animationConfig = animationConfig;
    }

    /**
     * Plays the given steps sequentially, then invokes onFinished. Highlights
     * left on the canvas are cleared automatically a short delay afterward.
     */
    public void playSteps(List<TreeStep> steps, Runnable onFinished) {
        treeCanvas.clearHighlights();
        cancelled = false;

        if (!animationConfig.isAnimationEnabled()) {
            // REQ-7.1: apply every step instantly (structural changes still
            // need to happen even with animation off), then finish with no
            // lingering highlights.
            for (TreeStep step : steps) {
                applyStep(step, Duration.ZERO);
            }
            treeCanvas.clearHighlights();
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        if (steps.isEmpty()) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        Duration stepDuration = Duration.millis(animationConfig.getStepDurationMs());
        playFrom(steps, 0, stepDuration, () -> {
            if (onFinished != null) {
                onFinished.run();
            }
            scheduleHighlightClear();
        });
    }

    private void playFrom(List<TreeStep> steps, int index, Duration stepDuration, Runnable onAllFinished) {
        if (cancelled) {
            return;
        }
        if (index >= steps.size()) {
            onAllFinished.run();
            return;
        }

        applyStep(steps.get(index), stepDuration);

        currentPause = new PauseTransition(stepDuration);
        currentPause.setOnFinished(event -> playFrom(steps, index + 1, stepDuration, onAllFinished));
        currentPause.play();
    }

    /**
     * Cancels any in-progress playback immediately. The onFinished callback
     * originally passed to playSteps() will NOT be invoked after this.
     * Safe to call even if nothing is currently playing.
     */
    public void stop() {
        cancelled = true;
        if (currentPause != null) {
            currentPause.stop();
        }
        treeCanvas.clearHighlights();
    }

    private void applyStep(TreeStep step, Duration stepDuration) {
        if (step instanceof CompareStep compareStep) {
            treeCanvas.setHighlight(compareStep.currentKey(), HighlightStep.HighlightKind.COMPARE);
        } else if (step instanceof StructuralChangeStep structuralChangeStep) {
            // Tween node positions to the new structure over the same duration
            // as this step's pause, so movement and pacing stay in sync.
            treeCanvas.animateToRoot(structuralChangeStep.newRoot(), stepDuration, null);
        } else if (step instanceof HighlightStep highlightStep) {
            treeCanvas.setHighlight(highlightStep.key(), highlightStep.kind());
        } else if (step instanceof RotateStep rotateStep) {
            // Structure already animated by the StructuralChangeStep that
            // preceded this one; just highlight the pivot for this step's pause.
            treeCanvas.setHighlight(rotateStep.pivotKey(), HighlightStep.HighlightKind.ROTATE_PIVOT);
        }
    }

    private void scheduleHighlightClear() {
        PauseTransition lingerThenClear = new PauseTransition(HIGHLIGHT_LINGER);
        lingerThenClear.setOnFinished(event -> treeCanvas.clearHighlights());
        lingerThenClear.play();
    }
}