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
}
