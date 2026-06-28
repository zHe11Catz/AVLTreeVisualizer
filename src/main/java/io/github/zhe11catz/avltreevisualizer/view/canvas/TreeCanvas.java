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
        widthProperty().addListener(e -> redraw());
        heightProperty().addListener(e -> redraw());
    }

    // ── Resizability ──────────────────────────────────────────────────────────

    /**
     * Returning true lets the parent (StackPane in BorderPane center) call
     * resize(w, h) on this node during its layout pass, so the canvas fills
     * the center region without creating circular preferred-size feedback.
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    @Override
    public double minWidth(double height) {
        return 0;
    }

    @Override
    public double minHeight(double width) {
        return 0;
    }

    /**
     * Called by the layout system when the parent decides how large this canvas should be.
     * The width/height listeners above will trigger redraw() automatically.
     */
    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    // ── Tree rendering ────────────────────────────────────────────────────────

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