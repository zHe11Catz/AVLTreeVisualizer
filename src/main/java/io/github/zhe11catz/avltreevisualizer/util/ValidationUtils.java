package io.github.zhe11catz.avltreevisualizer.util;

/**
 * Validates user input values before tree operations.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * @return true if the value is within the supported integer range
     */
    public static boolean isValidNodeValue(int value) {
        return value >= Constants.MIN_NODE_VALUE && value <= Constants.MAX_NODE_VALUE;
    }

    /**
     * @return true if the tree can accept another node
     */
    public static boolean canAcceptMoreNodes(int currentNodeCount) {
        return currentNodeCount < Constants.MAX_NODE_COUNT;
    }
}
