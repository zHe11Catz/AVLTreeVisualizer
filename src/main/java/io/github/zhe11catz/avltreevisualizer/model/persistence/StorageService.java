package io.github.zhe11catz.avltreevisualizer.model.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * Handles reading and writing tree state to avl_state.json.
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
     * Loads tree state from disk if the file exists and is valid.
     */
    public Optional<AVLTree> loadTree() {
        if (!Files.exists(stateFilePath)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(stateFilePath)) {
            TreeStateDto dto = gson.fromJson(reader, TreeStateDto.class);
            if (dto == null || dto.getRoot() == null) {
                return Optional.empty();
            }
            AVLTree tree = new AVLTree();
            tree.setRoot(fromDto(dto.getRoot()));
            return Optional.of(tree);
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Failed to load tree state from " + stateFilePath, ex);
            return Optional.empty();
        }
    }

    /**
     * Persists the current tree state to disk.
     */
    public void saveTree(AVLTree tree) throws IOException {
        TreeStateDto dto = new TreeStateDto(toDto(tree.getRoot()));
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
