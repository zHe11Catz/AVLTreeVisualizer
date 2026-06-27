package io.github.zhe11catz.avltreevisualizer.view.canvas;

import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Canvas component responsible for rendering the AVL tree.
 */
public class TreeCanvas extends Canvas {

    private AVLNode root;

    public TreeCanvas(double width, double height) {
        super(width, height);
    }

    public void setRoot(AVLNode root) {
        this.root = root;
        redraw();
    }

    public AVLNode getRoot() {
        return root;
    }

    /**
     * Redraws the entire tree on the canvas.
     */
    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (root == null) {
            gc.setFill(Color.GRAY);
            gc.fillText("Cây đang trống", getWidth() / 2 - 40, getHeight() / 2);
            return;
        }

        // TODO: use TreeLayout to compute positions and draw nodes/edges
    }
}
