package io.github.zhe11catz.avltreevisualizer.view.animation;

import io.github.zhe11catz.avltreevisualizer.model.settings.AppSettings;

/**
 * Maps animation settings to concrete timing values.
 */
public class AnimationConfig {

    private final AppSettings settings;

    public AnimationConfig(AppSettings settings) {
        this.settings = settings;
    }

    public boolean isAnimationEnabled() {
        return settings.isAnimationEnabled();
    }

    public long getStepDurationMs() {
        return settings.getAnimationDurationMs();
    }
}
