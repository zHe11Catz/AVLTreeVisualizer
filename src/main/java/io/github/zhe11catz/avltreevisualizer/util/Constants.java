package io.github.zhe11catz.avltreevisualizer.util;

/**
 * Global application constants aligned with SRS constraints.
 */
public final class Constants {

    public static final int MIN_NODE_VALUE = -9999;
    public static final int MAX_NODE_VALUE = 9999;
    public static final int MAX_NODE_COUNT = 127;

    public static final String STATE_FILE_NAME = "avl_state.json";

    public static final long DEFAULT_ANIMATION_DURATION_MS = 1000L;

    private Constants() {
    }
}
