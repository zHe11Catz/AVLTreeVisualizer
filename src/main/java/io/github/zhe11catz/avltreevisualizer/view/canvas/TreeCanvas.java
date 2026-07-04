package io.github.zhe11catz.avltreevisualizer.view.canvas;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.HighlightStep;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Canvas component responsible for rendering the AVL tree.
 */
public class TreeCanvas extends Canvas {

    private static final double NODE_RADIUS = 20.0;
    private static final Color DEFAULT_FILL = Color.web("#4dabf7");
    private static final Color DEFAULT_STROKE = Color.web("#1c7ed6");

    private final TreeLayout treeLayout = new TreeLayout();
    private final Map<Integer, HighlightStep.HighlightKind> highlights = new HashMap<>();

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
     * Sets (or overwrites) the highlight kind for a single node and redraws.
     * Used by AnimationEngine to visualize CompareStep / HighlightStep.
     */
    public void setHighlight(int key, HighlightStep.HighlightKind kind) {
        highlights.put(key, kind);
        redraw();
    }

    /**
     * Clears all active highlights (REQ-3.4: highlights fade after animation ends).
     */
    public void clearHighlights() {
        highlights.clear();
        redraw();
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
        HighlightStep.HighlightKind kind = highlights.get(node.getKey());

        gc.setFill(fillColorFor(kind));
        gc.fillOval(pos.x() - NODE_RADIUS, pos.y() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        gc.setStroke(strokeColorFor(kind));
        gc.setLineWidth(kind == null ? 2 : 3);
        gc.strokeOval(pos.x() - NODE_RADIUS, pos.y() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(node.getKey()), pos.x(), pos.y());

        drawNodes(gc, node.getLeft(), positions);
        drawNodes(gc, node.getRight(), positions);
    }

    private Color fillColorFor(HighlightStep.HighlightKind kind) {
        if (kind == null) {
            return DEFAULT_FILL;
        }
        return switch (kind) {
            case COMPARE -> Color.web("#ffd43b");       // yellow: node currently being compared
            case FOUND -> Color.web("#51cf66");         // green: search target found
            case NOT_FOUND -> Color.web("#ff6b6b");     // red: last node compared, not found
            case DELETE_TARGET -> Color.web("#ff922b"); // orange: node being deleted / successor
            case ROTATE_PIVOT -> Color.web("#20c997");  // teal: pivot of a rotation
            case VISIT -> Color.web("#845ef7");         // purple: node visited during traversal
            case NEW_NODE -> Color.web("#22b8cf");      // cyan: node just inserted
        };
    }

    private Color strokeColorFor(HighlightStep.HighlightKind kind) {
        if (kind == null) {
            return DEFAULT_STROKE;
        }
        return switch (kind) {
            case COMPARE -> Color.web("#f08c00");
            case FOUND -> Color.web("#2f9e44");
            case NOT_FOUND -> Color.web("#c92a2a");
            case DELETE_TARGET -> Color.web("#e8590c");
            case ROTATE_PIVOT -> Color.web("#0ca678");
            case VISIT -> Color.web("#5f3dc4");
            case NEW_NODE -> Color.web("#1098ad");
        };
    }
}