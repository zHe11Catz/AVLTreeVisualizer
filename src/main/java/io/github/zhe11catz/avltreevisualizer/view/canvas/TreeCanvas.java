package io.github.zhe11catz.avltreevisualizer.view.canvas;

import io.github.zhe11catz.avltreevisualizer.model.operation.step.HighlightStep;
import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
        widthProperty().addListener(e -> onResize());
        heightProperty().addListener(e -> onResize());
    }

    private void onResize() {
        if (positionTween != null) {
            positionTween.stop();
            positionTween = null;
        }
        currentPositions = treeLayout.calculateLayout(root, getWidth(), getHeight());
        redraw();
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

        // Fall back to a fresh layout if positions haven't been computed yet
        // (e.g. first paint before setRoot/animateToRoot has run).
        if (currentPositions.isEmpty()) {
            currentPositions = treeLayout.calculateLayout(root, getWidth(), getHeight());
        }

        drawEdges(gc, root, currentPositions);
        drawNodes(gc, root, currentPositions);
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

    // ── Position animation state ────────────────────────────────────────────

    /** Positions actually rendered right now (may be mid-tween). */
    private Map<Integer, TreeLayout.NodePosition> currentPositions = new HashMap<>();

    /** The tween currently running, if any — so a new one can cancel it. */
    private Timeline positionTween;

    /**
     * Immediately snaps to the given root with no animation. Used for initial
     * load, animation-disabled mode, and rollback on cancelled operations.
     */
    public void setRoot(AVLNode root) {
        if (positionTween != null) {
            positionTween.stop();
            positionTween = null;
        }
        this.root = root;
        this.currentPositions = treeLayout.calculateLayout(root, getWidth(), getHeight());
        redraw();
    }

    /**
     * Animates from the currently displayed structure to {@code newRoot} over
     * {@code duration}, tweening each node's on-screen position. Nodes that
     * exist in both the old and new tree slide from their old spot to their
     * new spot (this is what makes a rotation visually readable). Brand-new
     * nodes fade in at their final position's parent (or their own final
     * position if they have no parent yet), and removed nodes simply vanish
     * once the tween completes.
     */
    public void animateToRoot(AVLNode newRoot, Duration duration, Runnable onFinished) {
        if (positionTween != null) {
            positionTween.stop();
        }

        Map<Integer, TreeLayout.NodePosition> targetPositions =
                treeLayout.calculateLayout(newRoot, getWidth(), getHeight());
        Map<Integer, TreeLayout.NodePosition> startPositions = new HashMap<>(currentPositions);

        // For nodes that are new (didn't exist before this structural change),
        // start them at their own target position's nearest still-existing
        // ancestor so they visibly "grow" from where they were inserted rather
        // than materializing out of nowhere or sliding in from the corner.
        for (Integer key : targetPositions.keySet()) {
            if (!startPositions.containsKey(key)) {
                TreeLayout.NodePosition fallback = findNearestKnownAncestorPosition(newRoot, key, startPositions);
                startPositions.put(key, fallback != null ? fallback : targetPositions.get(key));
            }
        }

        this.root = newRoot;

        DoubleProperty progress = new SimpleDoubleProperty(0);
        progress.addListener((obs, oldVal, newVal) -> {
            double t = newVal.doubleValue();
            Map<Integer, TreeLayout.NodePosition> interpolated = new HashMap<>();
            for (Integer key : targetPositions.keySet()) {
                TreeLayout.NodePosition from = startPositions.get(key);
                TreeLayout.NodePosition to = targetPositions.get(key);
                interpolated.put(key, new TreeLayout.NodePosition(
                        lerp(from.x(), to.x(), t),
                        lerp(from.y(), to.y(), t)
                ));
            }
            this.currentPositions = interpolated;
            redraw();
        });

        positionTween = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
                new KeyFrame(duration, new KeyValue(progress, 1))
        );
        positionTween.setOnFinished(event -> {
            this.currentPositions = targetPositions;
            positionTween = null;
            redraw();
            if (onFinished != null) {
                onFinished.run();
            }
        });
        positionTween.play();
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Walks up from {@code key}'s position in {@code newRoot} toward the root,
     * returning the position of the first ancestor that already had a known
     * on-screen position before this structural change.
     */
    private TreeLayout.NodePosition findNearestKnownAncestorPosition(
            AVLNode node, int key, Map<Integer, TreeLayout.NodePosition> known) {
        List<AVLNode> path = new ArrayList<>();
        if (!collectPathTo(node, key, path)) {
            return null;
        }
        for (int i = path.size() - 2; i >= 0; i--) {
            TreeLayout.NodePosition pos = known.get(path.get(i).getKey());
            if (pos != null) {
                return pos;
            }
        }
        return null;
    }

    private boolean collectPathTo(AVLNode node, int key, List<AVLNode> path) {
        if (node == null) {
            return false;
        }
        path.add(node);
        if (node.getKey() == key) {
            return true;
        }
        boolean found = key < node.getKey()
                ? collectPathTo(node.getLeft(), key, path)
                : collectPathTo(node.getRight(), key, path);
        if (!found) {
            path.remove(path.size() - 1);
        }
        return found;
    }
}