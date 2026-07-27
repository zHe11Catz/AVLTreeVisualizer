package io.github.zhe11catz.avltreevisualizer.model.persistence;

/**
 * Persisted animation settings. Stored as a plain string for the
 * speed enum so the JSON stays human-readable and resilient to enum
 * reordering.
 */
public class SettingsDto {

    private boolean animationEnabled;
    private String animationSpeed;

    public SettingsDto() {
    }

    public SettingsDto(boolean animationEnabled, String animationSpeed) {
        this.animationEnabled = animationEnabled;
        this.animationSpeed = animationSpeed;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public String getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(String animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
}