package io.github.zhe11catz.avltreevisualizer.view.animation;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;
import io.github.zhe11catz.avltreevisualizer.view.canvas.TreeCanvas;

import java.util.List;

/**
 * Executes tree operation steps as JavaFX animations.
 */
public class AnimationEngine {

    private final TreeCanvas treeCanvas;
    private final AnimationConfig animationConfig;

    public AnimationEngine(TreeCanvas treeCanvas, AnimationConfig animationConfig) {
        this.treeCanvas = treeCanvas;
        this.animationConfig = animationConfig;
    }

    /**
     * Plays the given steps sequentially, then invokes onFinished.
     */
    public void playSteps(List<TreeStep> steps, Runnable onFinished) {
        if (!animationConfig.isAnimationEnabled() || steps.isEmpty()) {
            treeCanvas.redraw();
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        // TODO: implement Timeline/Transition playback for each TreeStep type
        treeCanvas.redraw();
        if (onFinished != null) {
            onFinished.run();
        }
    }
}
