package io.github.zhe11catz.avltreevisualizer.view.canvas;

import io.github.zhe11catz.avltreevisualizer.model.tree.AVLNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Computes node coordinates for tree visualization.
 */
public class TreeLayout {

    public record NodePosition(double x, double y) {
    }

    /**
     * Calculates layout positions for all nodes in the tree.
     */
    public Map<Integer, NodePosition> calculateLayout(AVLNode root, double canvasWidth, double canvasHeight) {
        Map<Integer, NodePosition> positions = new HashMap<>();
        if (root != null) {
            double margin = 30.0;
            assignPositions(root, 0, margin, canvasWidth - margin, positions);
        }
        return positions;
    }

    private void assignPositions(AVLNode node, int depth, double leftBound, double rightBound,
                                 Map<Integer, NodePosition> positions) {
        if (node == null) {
            return;
        }

        double x = (leftBound + rightBound) / 2.0;
        double y = depth * 80.0 + 40.0;
        positions.put(node.getKey(), new NodePosition(x, y));

        assignPositions(node.getLeft(), depth + 1, leftBound, x, positions);
        assignPositions(node.getRight(), depth + 1, x, rightBound, positions);
    }
}
