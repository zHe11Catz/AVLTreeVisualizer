package io.github.zhe11catz.avltreevisualizer.model.persistence;

/**
 * Serializable representation of the AVL tree for JSON persistence.
 */
public class TreeStateDto {

    private Integer rootKey;
    private TreeNodeDto root;

    public TreeStateDto() {
    }

    public TreeStateDto(TreeNodeDto root) {
        this.root = root;
        this.rootKey = root == null ? null : root.getKey();
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
