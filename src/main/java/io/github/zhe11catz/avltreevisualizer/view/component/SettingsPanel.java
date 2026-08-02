package io.github.zhe11catz.avltreevisualizer.view.component;

import io.github.zhe11catz.avltreevisualizer.model.settings.AppSettings;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

/**
 * Settings sidebar, organized into two sections: animation playback options,
 * and persistence preferences (what gets written to avl_state.json).
 */
public class SettingsPanel extends VBox {

    private final AppSettings settings;

    private final CheckBox animationToggle;
    private final ComboBox<AppSettings.AnimationSpeed> speedSelector;
    private final CheckBox saveTreeStateToggle;
    private final CheckBox saveSettingsToggle;

    public SettingsPanel(AppSettings settings) {
        this.settings = settings;
        getStyleClass().add("settings-panel");

        // ── Section: Hiệu ứng ────────────────────────────────────────────
        Label effectsTitle = new Label("Hiệu ứng");
        effectsTitle.getStyleClass().add("settings-section-title");

        animationToggle = new CheckBox("Bật hiệu ứng");
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

        // ── Section: Lưu và khôi phục ────────────────────────────────────
        Label persistenceTitle = new Label("Lưu và khôi phục");
        persistenceTitle.getStyleClass().add("settings-section-title");

        saveTreeStateToggle = new CheckBox("Lưu trạng thái cây");
        saveTreeStateToggle.setSelected(settings.isSaveTreeStateEnabled());
        saveTreeStateToggle.selectedProperty().addListener((obs, oldVal, newVal) ->
                settings.setSaveTreeStateEnabled(newVal));

        saveSettingsToggle = new CheckBox("Lưu cài đặt");
        saveSettingsToggle.setSelected(settings.isSaveSettingsEnabled());
        saveSettingsToggle.selectedProperty().addListener((obs, oldVal, newVal) ->
                settings.setSaveSettingsEnabled(newVal));

        getChildren().addAll(
                effectsTitle,
                animationToggle,
                speedSelector,
                new Separator(),
                persistenceTitle,
                saveTreeStateToggle,
                saveSettingsToggle
        );
    }

    public CheckBox getAnimationToggle() {
        return animationToggle;
    }

    public ComboBox<AppSettings.AnimationSpeed> getSpeedSelector() {
        return speedSelector;
    }

    public CheckBox getSaveTreeStateToggle() {
        return saveTreeStateToggle;
    }

    public CheckBox getSaveSettingsToggle() {
        return saveSettingsToggle;
    }
}