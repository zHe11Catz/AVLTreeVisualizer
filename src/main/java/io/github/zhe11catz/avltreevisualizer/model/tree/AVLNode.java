package io.github.zhe11catz.avltreevisualizer.model.tree;

/**
 * A single node in the AVL tree.
 */
public class AVLNode {

    private final int key;
    private int height;
    private AVLNode left;
    private AVLNode right;

    public AVLNode(int key) {
        this.key = key;
        this.height = 1;
    }

    public int getKey() {
        return key;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public AVLNode getLeft() {
        return left;
    }

    public void setLeft(AVLNode left) {
        this.left = left;
    }

    public AVLNode getRight() {
        return right;
    }

    public void setRight(AVLNode right) {
        this.right = right;
    }
}
