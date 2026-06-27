package io.github.zhe11catz.avltreevisualizer.view.component;

import io.github.zhe11catz.avltreevisualizer.model.settings.AppSettings;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

/**
 * Settings sidebar for animation options.
 */
public class SettingsPanel extends VBox {

    private final AppSettings settings;
    private final CheckBox animationToggle;
    private final ComboBox<AppSettings.AnimationSpeed> speedSelector;

    public SettingsPanel(AppSettings settings) {
        this.settings = settings;
        getStyleClass().add("settings-panel");

        animationToggle = new CheckBox("Bật animation");
        animationToggle.setSelected(settings.isAnimationEnabled());
        animationToggle.selectedProperty().addListener((obs, oldVal, newVal) ->
                settings.setAnimationEnabled(newVal));

        speedSelector = new ComboBox<>();
        speedSelector.getItems().addAll(AppSettings.AnimationSpeed.values());
        speedSelector.setValue(settings.getAnimationSpeed());
        speedSelector.setPromptText("Tốc độ animation");
        speedSelector.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(AppSettings.AnimationSpeed speed) {
                if (speed == null) {
                    return "";
                }
                return switch (speed) {
                    case SLOW -> "Chậm";
                    case NORMAL -> "Bình thường";
                    case FAST -> "Nhanh";
                };
            }

            @Override
            public AppSettings.AnimationSpeed fromString(String label) {
                return switch (label) {
                    case "Chậm" -> AppSettings.AnimationSpeed.SLOW;
                    case "Nhanh" -> AppSettings.AnimationSpeed.FAST;
                    default -> AppSettings.AnimationSpeed.NORMAL;
                };
            }
        });
        speedSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settings.setAnimationSpeed(newVal);
            }
        });

        getChildren().addAll(animationToggle, speedSelector);
    }

    public CheckBox getAnimationToggle() {
        return animationToggle;
    }

    public ComboBox<AppSettings.AnimationSpeed> getSpeedSelector() {
        return speedSelector;
    }
}
