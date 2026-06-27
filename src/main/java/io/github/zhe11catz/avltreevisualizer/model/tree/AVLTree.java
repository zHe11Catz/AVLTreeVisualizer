package io.github.zhe11catz.avltreevisualizer.model.tree;

import io.github.zhe11catz.avltreevisualizer.model.operation.DeleteResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.InsertResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.SearchResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalType;

/**
 * Core AVL tree engine containing all tree algorithms.
 * This class must not depend on JavaFX.
 */
public class AVLTree {

    private AVLNode root;

    public AVLNode getRoot() {
        return root;
    }

    public void setRoot(AVLNode root) {
        this.root = root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return countNodes(root);
    }

    public void clear() {
        root = null;
    }

    public boolean contains(int key) {
        return searchNode(root, key) != null;
    }

    /**
     * Inserts a key and records algorithm steps for visualization.
     */
    public InsertResult insert(int key) {
        // TODO: implement insert with step recording
        return new InsertResult(false, root, null);
    }

    /**
     * Deletes a key and records algorithm steps for visualization.
     */
    public DeleteResult delete(int key) {
        // TODO: implement delete with step recording
        return new DeleteResult(false, root, null);
    }

    /**
     * Searches for a key and records the traversal path for visualization.
     */
    public SearchResult search(int key) {
        AVLNode found = searchNode(root, key);
        // TODO: record compare/highlight steps during search
        return new SearchResult(found != null, found, null);
    }

    /**
     * Traverses the tree in the given order.
     */
    public TraversalResult traverse(TraversalType type) {
        // TODO: implement traversal with step recording
        return new TraversalResult(type, null, null);
    }

    private AVLNode searchNode(AVLNode node, int key) {
        if (node == null) {
            return null;
        }
        if (key == node.getKey()) {
            return node;
        }
        return key < node.getKey()
                ? searchNode(node.getLeft(), key)
                : searchNode(node.getRight(), key);
    }

    private int countNodes(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }
}
