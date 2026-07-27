package io.github.zhe11catz.avltreevisualizer.model.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.zhe11catz.avltreevisualizer.model.settings.AppSettings;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLTree;
import io.github.zhe11catz.avltreevisualizer.util.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles reading and writing app state (tree + settings) to avl_state.json.
 */
public class StorageService {

    private static final Logger LOGGER = Logger.getLogger(StorageService.class.getName());

    private final Gson gson;
    private final Path stateFilePath;

    public StorageService(Path stateFilePath) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.stateFilePath = stateFilePath;
    }

    public StorageService() {
        this(Path.of(Constants.STATE_FILE_NAME));
    }

    /**
     * Bundles the two independent pieces of state restored from disk, since
     * the tree and the settings live in the same file but are otherwise
     * unrelated to each other.
     */
    public record LoadedState(AVLTree tree, AppSettings settings) {
    }

    /**
     * Loads app state from disk if the file exists and is valid. If the tree
     * portion is missing/corrupt but settings are present (or vice versa),
     * whichever part is valid is still returned rather than discarding both.
     */
    public Optional<LoadedState> loadState() {
        if (!Files.exists(stateFilePath)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(stateFilePath)) {
            TreeStateDto dto = gson.fromJson(reader, TreeStateDto.class);
            if (dto == null) {
                return Optional.empty();
            }

            AVLTree tree = new AVLTree();
            if (dto.getRoot() != null) {
                tree.setRoot(fromDto(dto.getRoot()));
            }

            AppSettings settings = fromSettingsDto(dto.getSettings());

            return Optional.of(new LoadedState(tree, settings));
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Failed to load app state from " + stateFilePath, ex);
            return Optional.empty();
        }
    }

    /**
     * Persists both the current tree and current settings to disk in one
     * combined JSON document (REQ-6.1, REQ-7.4).
     */
    public void saveState(AVLTree tree, AppSettings settings) throws IOException {
        TreeStateDto dto = new TreeStateDto(toDto(tree.getRoot()), toSettingsDto(settings));
        try (Writer writer = Files.newBufferedWriter(stateFilePath)) {
            gson.toJson(dto, writer);
        }
    }

    /**
     * Removes the persisted state file if it exists.
     */
    public void clearSavedState() throws IOException {
        Files.deleteIfExists(stateFilePath);
    }

    private SettingsDto toSettingsDto(AppSettings settings) {
        return new SettingsDto(
                settings.isAnimationEnabled(),
                settings.getAnimationSpeed().name()
        );
    }

    private AppSettings fromSettingsDto(SettingsDto dto) {
        AppSettings settings = new AppSettings();
        if (dto == null) {
            return settings;
        }
        settings.setAnimationEnabled(dto.isAnimationEnabled());
        try {
            settings.setAnimationSpeed(AppSettings.AnimationSpeed.valueOf(dto.getAnimationSpeed()));
        } catch (IllegalArgumentException | NullPointerException ex) {
            LOGGER.log(Level.WARNING, "Unknown animation speed in state file: " + dto.getAnimationSpeed());
        }
        return settings;
    }

    private TreeStateDto.TreeNodeDto toDto(AVLNode node) {
        if (node == null) {
            return null;
        }
        return new TreeStateDto.TreeNodeDto(
                node.getKey(),
                toDto(node.getLeft()),
                toDto(node.getRight())
        );
    }

    private AVLNode fromDto(TreeStateDto.TreeNodeDto dto) {
        if (dto == null) {
            return null;
        }
        AVLNode node = new AVLNode(dto.getKey());
        node.setLeft(fromDto(dto.getLeft()));
        node.setRight(fromDto(dto.getRight()));
        return node;
    }
}