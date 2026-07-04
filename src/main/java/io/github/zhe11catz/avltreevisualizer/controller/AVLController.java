package io.github.zhe11catz.avltreevisualizer.controller;

import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalType;
import io.github.zhe11catz.avltreevisualizer.model.persistence.StorageService;
import io.github.zhe11catz.avltreevisualizer.model.settings.AppSettings;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLTree;
import io.github.zhe11catz.avltreevisualizer.util.Constants;
import io.github.zhe11catz.avltreevisualizer.util.FileImportParser;
import io.github.zhe11catz.avltreevisualizer.util.ValidationUtils;
import io.github.zhe11catz.avltreevisualizer.view.animation.AnimationConfig;
import io.github.zhe11catz.avltreevisualizer.view.animation.AnimationEngine;
import io.github.zhe11catz.avltreevisualizer.view.canvas.TreeCanvas;
import io.github.zhe11catz.avltreevisualizer.view.component.SettingsPanel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main controller bridging user actions, model operations, and view updates.
 */
public class AVLController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AVLController.class.getName());

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane canvasContainer;

    @FXML
    private TextField valueField;

    @FXML
    private Button insertButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button importButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button settingsButton;

    @FXML
    private ComboBox<TraversalType> traversalSelector;

    @FXML
    private Button traverseButton;

    @FXML
    private Label statusLabel;

    @FXML
    private StackPane settingsOverlay;

    private SettingsPanel settingsPanel;

    private AVLTree tree;
    private AppSettings settings;
    private StorageService storageService;
    private TreeCanvas treeCanvas;
    private AnimationEngine animationEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        traversalSelector.getItems().addAll(TraversalType.values());
        traversalSelector.setValue(TraversalType.INORDER);
        traversalSelector.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(TraversalType type) {
                if (type == null) {
                    return "";
                }
                return switch (type) {
                    case INORDER -> "Inorder (Trung tự)";
                    case PREORDER -> "Preorder (Tiền tự)";
                    case POSTORDER -> "Postorder (Hậu tự)";
                    case LEVEL_ORDER -> "Level-order (Theo mức)";
                };
            }

            @Override
            public TraversalType fromString(String label) {
                if (label == null) {
                    return TraversalType.INORDER;
                }
                if (label.startsWith("Preorder")) {
                    return TraversalType.PREORDER;
                }
                if (label.startsWith("Postorder")) {
                    return TraversalType.POSTORDER;
                }
                if (label.startsWith("Level-order")) {
                    return TraversalType.LEVEL_ORDER;
                }
                return TraversalType.INORDER;
            }
        });

        insertButton.setOnAction(event -> onInsert());
        deleteButton.setOnAction(event -> onDelete());
        searchButton.setOnAction(event -> onSearch());
        importButton.setOnAction(event -> onImportFile());
        resetButton.setOnAction(event -> onReset());
        traverseButton.setOnAction(event -> onTraverse());
        settingsButton.setOnAction(event -> toggleSettingsSidebar());
    }

    /**
     * Called after FXML injection to wire model, view, and persistence.
     */
    public void initializeView() {
        settings = new AppSettings();
        storageService = new StorageService();
        tree = storageService.loadTree().orElseGet(AVLTree::new);

        treeCanvas = new TreeCanvas(900, 500);
        treeCanvas.setRoot(tree.getRoot());
        canvasContainer.getChildren().setAll(treeCanvas);

        AnimationConfig animationConfig = new AnimationConfig(settings);
        animationEngine = new AnimationEngine(treeCanvas, animationConfig);

        settingsPanel = new SettingsPanel(settings);
        settingsOverlay.getChildren().setAll(settingsPanel);
        settingsOverlay.setVisible(false);
        settingsOverlay.setManaged(false);

        setStatus("Sẵn sàng.");
        Platform.runLater(treeCanvas::redraw);

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnCloseRequest(event -> saveStateOnExit());
                    }
                });
            }
        });
    }

    private void onInsert() {
        Integer value = parseInputValue();
        if (value == null) {
            return;
        }
        if (!ValidationUtils.canAcceptMoreNodes(tree.size())) {
            showWarning("Giới hạn nút", "Cây đã đạt tối đa 127 nút.");
            return;
        }
        if (tree.contains(value)) {
            showWarning("Giá trị trùng", "Giá trị " + value + " đã tồn tại trong cây.");
            return;
        }

        var result = tree.insert(value);
        tree.setRoot(result.getRoot());
        animationEngine.playSteps(result.getSteps(), () -> {
            treeCanvas.setRoot(tree.getRoot());
            setStatus("Đã thêm nút " + value + ".");
        });
    }

    private void onDelete() {
        Integer value = parseInputValue();
        if (value == null) {
            return;
        }

        var result = tree.delete(value);
        tree.setRoot(result.getRoot());
        animationEngine.playSteps(result.getSteps(), () -> {
            treeCanvas.setRoot(tree.getRoot());
            setStatus(result.isSuccess()
                    ? "Đã xóa nút " + value + "."
                    : "Không tìm thấy nút " + value + " để xóa.");
        });
    }

    private void onSearch() {
        Integer value = parseInputValue();
        if (value == null) {
            return;
        }

        var result = tree.search(value);
        animationEngine.playSteps(result.getSteps(), () -> setStatus(result.isSuccess()
                ? "Đã tìm thấy nút " + value + "."
                : "Không tìm thấy nút " + value + "."));
    }

    private void onTraverse() {
        TraversalType type = traversalSelector.getValue();
        if (type == null) {
            showWarning("Thiếu lựa chọn", "Vui lòng chọn kiểu duyệt cây.");
            return;
        }

        var result = tree.traverse(type);
        animationEngine.playSteps(result.getSteps(), () -> setStatus("Duyệt " + type + ": " + result.getValues()));
    }

    private void onImportFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn tệp dữ liệu");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file == null) {
            return;
        }

        List<Integer> rawValues;
        try {
            rawValues = FileImportParser.parseFile(file.toPath());
        } catch (Exception ex) {
            // REQ-5.4: unreadable file -> warn and leave current tree state untouched.
            showWarning("Lỗi tệp", "Không thể đọc tệp đã chọn.");
            LOGGER.log(Level.WARNING, "Import failed", ex);
            return;
        }

        importValues(rawValues);
    }

    /**
     * Classifies parsed values (range, duplicate, capacity), then inserts the
     * accepted subset into the tree one at a time via the animation engine.
     */
    private void importValues(List<Integer> rawValues) {
        if (rawValues.isEmpty()) {
            setStatus("Tệp không chứa giá trị hợp lệ nào.");
            return;
        }

        Set<Integer> seenInFile = new HashSet<>();
        List<Integer> validUnique = new ArrayList<>();
        int skippedRange = 0;
        int skippedDuplicate = 0;

        for (int value : rawValues) {
            if (!ValidationUtils.isValidNodeValue(value)) {
                // REQ-5.1 / business rule: values outside [-9999, 9999] are ignored.
                skippedRange++;
                continue;
            }
            if (tree.contains(value) || !seenInFile.add(value)) {
                // Business rule "Tính duy nhất khoá": skip values already in the
                // tree or repeated within the file itself.
                skippedDuplicate++;
                continue;
            }
            validUnique.add(value);
        }

        int remainingCapacity = Math.max(0, Constants.MAX_NODE_COUNT - tree.size());
        List<Integer> toInsert = validUnique.size() > remainingCapacity
                ? validUnique.subList(0, remainingCapacity)
                : validUnique;
        int skippedLimit = validUnique.size() - toInsert.size();

        if (toInsert.isEmpty()) {
            setStatus(buildImportSummary(0, rawValues.size(), skippedRange, skippedDuplicate, skippedLimit));
            return;
        }

        setControlsDisabled(true);
        insertNextFromImport(toInsert, 0, rawValues.size(), skippedRange, skippedDuplicate, skippedLimit);
    }

    /**
     * Inserts values one by one, chaining through the animation engine's
     * completion callback so each node is animated in sequence.
     */
    private void insertNextFromImport(List<Integer> toInsert, int index, int totalRaw,
                                      int skippedRange, int skippedDuplicate, int skippedLimit) {
        if (index >= toInsert.size()) {
            setControlsDisabled(false);
            setStatus(buildImportSummary(toInsert.size(), totalRaw, skippedRange, skippedDuplicate, skippedLimit));
            return;
        }

        int value = toInsert.get(index);
        var result = tree.insert(value);
        tree.setRoot(result.getRoot());
        animationEngine.playSteps(result.getSteps(), () -> {
            treeCanvas.setRoot(tree.getRoot());
            insertNextFromImport(toInsert, index + 1, totalRaw, skippedRange, skippedDuplicate, skippedLimit);
        });
    }

    private String buildImportSummary(int inserted, int totalRaw, int skippedRange, int skippedDuplicate, int skippedLimit) {
        StringBuilder message = new StringBuilder();
        message.append("Đã chèn ").append(inserted).append("/").append(totalRaw).append(" giá trị từ tệp.");

        List<String> reasons = new ArrayList<>();
        if (skippedRange > 0) {
            reasons.add(skippedRange + " ngoài phạm vi [-9999, 9999]");
        }
        if (skippedDuplicate > 0) {
            reasons.add(skippedDuplicate + " trùng lặp");
        }
        if (skippedLimit > 0) {
            reasons.add(skippedLimit + " vượt giới hạn 127 nút");
        }
        if (!reasons.isEmpty()) {
            message.append(" Bỏ qua: ").append(String.join(", ", reasons)).append(".");
        }
        return message.toString();
    }

    private void onReset() {
        tree.clear();
        treeCanvas.setRoot(null);
        try {
            storageService.clearSavedState();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to clear saved state", ex);
        }
        setStatus("Đã đặt lại cây.");
    }

    private void toggleSettingsSidebar() {
        boolean visible = !settingsOverlay.isVisible();
        settingsOverlay.setVisible(visible);
        settingsOverlay.setManaged(visible);
    }

    private void setControlsDisabled(boolean disabled) {
        insertButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
        searchButton.setDisable(disabled);
        importButton.setDisable(disabled);
        resetButton.setDisable(disabled);
        traverseButton.setDisable(disabled);
    }

    private Integer parseInputValue() {
        String text = valueField.getText();
        if (text == null || text.isBlank()) {
            showWarning("Thiếu dữ liệu", "Vui lòng nhập một giá trị nguyên.");
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (!ValidationUtils.isValidNodeValue(value)) {
                showWarning("Giá trị không hợp lệ", "Giá trị phải nằm trong khoảng -9999 đến 9999.");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            showWarning("Giá trị không hợp lệ", "Vui lòng nhập số nguyên hợp lệ.");
            return null;
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void saveStateOnExit() {
        try {
            if (tree.isEmpty()) {
                storageService.clearSavedState();
            } else {
                storageService.saveTree(tree);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to save tree state on exit", ex);
        }
    }
}