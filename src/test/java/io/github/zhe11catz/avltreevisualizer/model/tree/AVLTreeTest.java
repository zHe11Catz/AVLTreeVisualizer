package io.github.zhe11catz.avltreevisualizer.model.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the AVL tree model layer.
 */
class AVLTreeTest {

    private AVLTree tree;

    @BeforeEach
    void setUp() {
        tree = new AVLTree();
    }

    @Test
    void newTreeShouldBeEmpty() {
        assertTrue(tree.isEmpty());
        assertFalse(tree.contains(10));
    }
}
