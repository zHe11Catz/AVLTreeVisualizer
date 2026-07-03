package io.github.zhe11catz.avltreevisualizer.view.canvas;

import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.Map;

/**
 * Canvas component responsible for rendering the AVL tree.
 */
public class TreeCanvas extends Canvas {

    private static final double NODE_RADIUS = 20.0;

    private final TreeLayout treeLayout = new TreeLayout();

    private AVLNode root;

    public TreeCanvas(double width, double height) {
        super(width, height);
        widthProperty().addListener(e -> redraw());
        heightProperty().addListener(e -> redraw());
    }

    // ── Resizability ──────────────────────────────────────────────────────────

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
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("Cây đang trống", getWidth() / 2, getHeight() / 2);
            return;
        }

        Map<Integer, TreeLayout.NodePosition> positions =
                treeLayout.calculateLayout(root, getWidth(), getHeight());

        drawEdges(gc, root, positions);
        drawNodes(gc, root, positions);
    }

    private void drawEdges(GraphicsContext gc, AVLNode node, Map<Integer, TreeLayout.NodePosition> positions) {
        if (node == null) {
            return;
        }

        TreeLayout.NodePosition parentPos = positions.get(node.getKey());
        gc.setStroke(Color.web("#adb5bd"));
        gc.setLineWidth(1.5);

        if (node.getLeft() != null) {
            TreeLayout.NodePosition childPos = positions.get(node.getLeft().getKey());
            gc.strokeLine(parentPos.x(), parentPos.y(), childPos.x(), childPos.y());
            drawEdges(gc, node.getLeft(), positions);
        }
        if (node.getRight() != null) {
            TreeLayout.NodePosition childPos = positions.get(node.getRight().getKey());
            gc.strokeLine(parentPos.x(), parentPos.y(), childPos.x(), childPos.y());
            drawEdges(gc, node.getRight(), positions);
        }
    }

    private void drawNodes(GraphicsContext gc, AVLNode node, Map<Integer, TreeLayout.NodePosition> positions) {
        if (node == null) {
            return;
        }

        TreeLayout.NodePosition pos = positions.get(node.getKey());

        gc.setFill(Color.web("#4dabf7"));
        gc.fillOval(pos.x() - NODE_RADIUS, pos.y() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        gc.setStroke(Color.web("#1c7ed6"));
        gc.setLineWidth(2);
        gc.strokeOval(pos.x() - NODE_RADIUS, pos.y() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(node.getKey()), pos.x(), pos.y());

        drawNodes(gc, node.getLeft(), positions);
        drawNodes(gc, node.getRight(), positions);
    }
}