package io.github.zhe11catz.avltreevisualizer.model.persistence;

/**
 * Serializable representation of the AVL tree AND app settings for JSON
 * persistence. Both are stored in the same avl_state.json file
 * so there is a single source of truth for "what to restore on startup".
 */
public class TreeStateDto {

    private Integer rootKey;
    private TreeNodeDto root;
    private SettingsDto settings;

    public TreeStateDto() {
    }

    public TreeStateDto(TreeNodeDto root, SettingsDto settings) {
        this.root = root;
        this.rootKey = root == null ? null : root.getKey();
        this.settings = settings;
    }

    public Integer getRootKey() {
        return rootKey;
    }

    public void setRootKey(Integer rootKey) {
        this.rootKey = rootKey;
    }

    public TreeNodeDto getRoot() {
        return root;
    }

    public void setRoot(TreeNodeDto root) {
        this.root = root;
        this.rootKey = root == null ? null : root.getKey();
    }

    public SettingsDto getSettings() {
        return settings;
    }

    public void setSettings(SettingsDto settings) {
        this.settings = settings;
    }

    /**
     * Nested DTO for a single tree node.
     */
    public static class TreeNodeDto {

        private int key;
        private TreeNodeDto left;
        private TreeNodeDto right;

        public TreeNodeDto() {
        }

        public TreeNodeDto(int key, TreeNodeDto left, TreeNodeDto right) {
            this.key = key;
            this.left = left;
            this.right = right;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public TreeNodeDto getLeft() {
            return left;
        }

        public void setLeft(TreeNodeDto left) {
            this.left = left;
        }

        public TreeNodeDto getRight() {
            return right;
        }

        public void setRight(TreeNodeDto right) {
            this.right = right;
        }
    }
}