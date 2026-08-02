package io.github.zhe11catz.avltreevisualizer.model.settings;

import io.github.zhe11catz.avltreevisualizer.util.Constants;

/**
 * In-memory application settings for the current session.
 */
public class AppSettings {

    public enum AnimationSpeed {
        SLOW(2000L),
        NORMAL(Constants.DEFAULT_ANIMATION_DURATION_MS),
        FAST(400L);

        private final long durationMs;

        AnimationSpeed(long durationMs) {
            this.durationMs = durationMs;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }

    private boolean animationEnabled = true;
    private AnimationSpeed animationSpeed = AnimationSpeed.NORMAL;

    // Persistence preferences: whether closing the app should write the tree structure
    // and/or the animation settings to avl_state.json. These two flags themselves are always persisted,
    // regardless of their own value, so the user's choice survives restarts.
    private boolean saveTreeStateEnabled = true;
    private boolean saveSettingsEnabled = true;

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public AnimationSpeed getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(AnimationSpeed animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public long getAnimationDurationMs() {
        return animationSpeed.getDurationMs();
    }

    public boolean isSaveTreeStateEnabled() {
        return saveTreeStateEnabled;
    }

    public void setSaveTreeStateEnabled(boolean saveTreeStateEnabled) {
        this.saveTreeStateEnabled = saveTreeStateEnabled;
    }

    public boolean isSaveSettingsEnabled() {
        return saveSettingsEnabled;
    }

    public void setSaveSettingsEnabled(boolean saveSettingsEnabled) {
        this.saveSettingsEnabled = saveSettingsEnabled;
    }
}