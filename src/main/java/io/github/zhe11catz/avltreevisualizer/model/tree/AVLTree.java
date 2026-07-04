package io.github.zhe11catz.avltreevisualizer.model.tree;

import io.github.zhe11catz.avltreevisualizer.model.operation.DeleteResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.InsertResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.SearchResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalType;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.CompareStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.HighlightStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.RotateStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;

import java.util.ArrayList;
import java.util.List;

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
        List<TreeStep> steps = new ArrayList<>();
        root = insertRecursive(root, key, steps);
        return new InsertResult(true, root, steps);
    }

    /**
     * Deletes a key following standard BST deletion rules, then rebalances
     * the AVL tree. Records comparison/highlight/rotation steps for visualization.
     */
    public DeleteResult delete(int key) {
        boolean existedBefore = contains(key);
        List<TreeStep> steps = new ArrayList<>();
        root = deleteRecursive(root, key, steps);
        return new DeleteResult(existedBefore, root, steps);
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

    // ── Insert helpers ───────────────────────────────────────────────────────

    private AVLNode insertRecursive(AVLNode node, int key, List<TreeStep> steps) {
        if (node == null) {
            return new AVLNode(key);
        }

        boolean goLeft = key < node.getKey();
        steps.add(new CompareStep(node.getKey(), key, goLeft));

        if (goLeft) {
            node.setLeft(insertRecursive(node.getLeft(), key, steps));
        } else if (key > node.getKey()) {
            node.setRight(insertRecursive(node.getRight(), key, steps));
        } else {
            // Duplicate key: the controller already checks contains() before
            // calling insert(), so this branch is just a defensive no-op.
            return node;
        }

        updateHeight(node);
        return rebalance(node, key, steps);
    }

    private AVLNode rebalance(AVLNode node, int insertedKey, List<TreeStep> steps) {
        int balance = balanceFactor(node);

        // Left-heavy
        if (balance > 1) {
            if (insertedKey < node.getLeft().getKey()) {
                // Left-Left case
                steps.add(new RotateStep(RotationType.RIGHT, node.getKey()));
                return rotateRight(node);
            } else {
                // Left-Right case
                steps.add(new RotateStep(RotationType.LEFT_RIGHT, node.getKey()));
                node.setLeft(rotateLeft(node.getLeft()));
                return rotateRight(node);
            }
        }

        // Right-heavy
        if (balance < -1) {
            if (insertedKey > node.getRight().getKey()) {
                // Right-Right case
                steps.add(new RotateStep(RotationType.LEFT, node.getKey()));
                return rotateLeft(node);
            } else {
                // Right-Left case
                steps.add(new RotateStep(RotationType.RIGHT_LEFT, node.getKey()));
                node.setRight(rotateRight(node.getRight()));
                return rotateLeft(node);
            }
        }

        return node;
    }

    // ── Delete helpers ───────────────────────────────────────────────────────

    private AVLNode deleteRecursive(AVLNode node, int key, List<TreeStep> steps) {
        if (node == null) {
            // Key not found on this path; nothing to record beyond the
            // comparison steps already added by the caller.
            return null;
        }

        if (key < node.getKey()) {
            steps.add(new CompareStep(node.getKey(), key, true));
            node.setLeft(deleteRecursive(node.getLeft(), key, steps));
        } else if (key > node.getKey()) {
            steps.add(new CompareStep(node.getKey(), key, false));
            node.setRight(deleteRecursive(node.getRight(), key, steps));
        } else {
            // Found the node to delete.
            steps.add(new HighlightStep(node.getKey()));

            if (node.getLeft() == null || node.getRight() == null) {
                // Case: leaf node or single-child node.
                AVLNode child = (node.getLeft() != null) ? node.getLeft() : node.getRight();
                node = child;
            } else {
                // Case: two children -> replace with inorder successor
                // (smallest key in the right subtree).
                AVLNode successor = findMin(node.getRight(), steps);
                steps.add(new HighlightStep(successor.getKey()));

                AVLNode newRight = deleteRecursive(node.getRight(), successor.getKey(), steps);

                // Key is final on AVLNode, so we rebuild this position with a
                // fresh node carrying the successor's key instead of mutating it.
                AVLNode replacement = new AVLNode(successor.getKey());
                replacement.setLeft(node.getLeft());
                replacement.setRight(newRight);
                node = replacement;
            }
        }

        if (node == null) {
            return null;
        }

        updateHeight(node);
        return rebalanceAfterDelete(node, steps);
    }

    /**
     * Walks to the leftmost node of the given subtree (inorder successor
     * source), recording a highlight step at each visited node.
     */
    private AVLNode findMin(AVLNode node, List<TreeStep> steps) {
        steps.add(new HighlightStep(node.getKey()));
        while (node.getLeft() != null) {
            node = node.getLeft();
            steps.add(new HighlightStep(node.getKey()));
        }
        return node;
    }

    private AVLNode rebalanceAfterDelete(AVLNode node, List<TreeStep> steps) {
        int balance = balanceFactor(node);

        // Left-heavy
        if (balance > 1) {
            if (balanceFactor(node.getLeft()) >= 0) {
                // Left-Left case
                steps.add(new RotateStep(RotationType.RIGHT, node.getKey()));
                return rotateRight(node);
            } else {
                // Left-Right case
                steps.add(new RotateStep(RotationType.LEFT_RIGHT, node.getKey()));
                node.setLeft(rotateLeft(node.getLeft()));
                return rotateRight(node);
            }
        }

        // Right-heavy
        if (balance < -1) {
            if (balanceFactor(node.getRight()) <= 0) {
                // Right-Right case
                steps.add(new RotateStep(RotationType.LEFT, node.getKey()));
                return rotateLeft(node);
            } else {
                // Right-Left case
                steps.add(new RotateStep(RotationType.RIGHT_LEFT, node.getKey()));
                node.setRight(rotateRight(node.getRight()));
                return rotateLeft(node);
            }
        }

        return node;
    }

    // ── Rotation primitives ──────────────────────────────────────────────────

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.getLeft();
        AVLNode transferred = x.getRight();

        x.setRight(y);
        y.setLeft(transferred);

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.getRight();
        AVLNode transferred = y.getLeft();

        y.setLeft(x);
        x.setRight(transferred);

        updateHeight(x);
        updateHeight(y);

        return y;
    }

    // ── Height / balance helpers ─────────────────────────────────────────────

    private int height(AVLNode node) {
        return node == null ? 0 : node.getHeight();
    }

    private int balanceFactor(AVLNode node) {
        return node == null ? 0 : height(node.getLeft()) - height(node.getRight());
    }

    private void updateHeight(AVLNode node) {
        node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
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