package io.github.zhe11catz.avltreevisualizer.model.tree;

import io.github.zhe11catz.avltreevisualizer.model.operation.DeleteResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.InsertResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.SearchResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalType;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.CompareStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.HighlightStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.RotateStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.StructuralChangeStep;
import io.github.zhe11catz.avltreevisualizer.model.operation.step.TreeStep;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Core AVL tree engine containing all tree algorithms.
 * This class must not depend on JavaFX.
 */
public class AVLTree {

    private AVLNode root;

    public AVLNode getRoot() {
        return root;
    }

    public void setRoot(AVLNode root) {
        this.root = root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return countNodes(root);
    }

    public void clear() {
        root = null;
    }

    public boolean contains(int key) {
        return searchNode(root, key) != null;
    }

    /**
     * Inserts a key and records algorithm steps for visualization.
     * <p>
     * Steps are split into THREE phases so the view can stay in sync:
     * (1) a read-only walk over the CURRENT tree recording a CompareStep at
     * each ancestor visited; (2) a raw BST insertion with no rebalancing yet,
     * captured as an intermediate StructuralChangeStep so the view shows the
     * node landing in its "natural" spot before any rotation; (3) rebalancing
     * on top of that same structure, recording a RotateStep plus a second,
     * final StructuralChangeStep only if a rotation actually happened.
     */
    public InsertResult insert(int key) {
        List<TreeStep> steps = new ArrayList<>();

        AVLNode current = root;
        while (current != null && key != current.getKey()) {
            boolean goLeft = key < current.getKey();
            steps.add(new CompareStep(current.getKey(), key, goLeft));
            current = goLeft ? current.getLeft() : current.getRight();
        }

        // Phase 1: plain BST insertion, no rebalancing yet. This snapshot is
        // what the view animates to right after the compare steps, so the new
        // node visibly lands at its raw insertion point first.
        AVLNode unbalancedRoot = insertBstOnly(deepCopy(root), key);
        steps.add(new StructuralChangeStep(unbalancedRoot));
        steps.add(new HighlightStep(key, HighlightStep.HighlightKind.NEW_NODE));

        // Phase 2: rebalance starting from that same unbalanced structure.
        List<TreeStep> rebalanceSteps = new ArrayList<>();
        AVLNode newRoot = rebalanceAfterInsert(deepCopy(unbalancedRoot), key, rebalanceSteps);

        if (!rebalanceSteps.isEmpty()) {
            // Only add a second structural change if a rotation actually
            // happened; otherwise unbalancedRoot == final structure already.
            steps.addAll(rebalanceSteps);
            steps.add(new StructuralChangeStep(newRoot));
        }

        root = newRoot;
        return new InsertResult(true, newRoot, steps);
    }

    /**
     * Pure BST insertion with no AVL rebalancing. Used to capture the
     * intermediate "just inserted, not yet rotated" snapshot for animation.
     */
    private AVLNode insertBstOnly(AVLNode node, int key) {
        if (node == null) {
            return new AVLNode(key);
        }
        if (key < node.getKey()) {
            node.setLeft(insertBstOnly(node.getLeft(), key));
        } else if (key > node.getKey()) {
            node.setRight(insertBstOnly(node.getRight(), key));
        }
        updateHeight(node);
        return node;
    }

    /**
     * Walks back up an already-inserted (but not yet rebalanced) tree along
     * {@code insertedKey}'s path, fixing heights and applying rotation(s) at
     * the first unbalanced ancestor found, same as the old insertRecursive's
     * second half.
     */
    private AVLNode rebalanceAfterInsert(AVLNode node, int insertedKey, List<TreeStep> steps) {
        if (node == null) {
            return null;
        }
        if (insertedKey < node.getKey()) {
            node.setLeft(rebalanceAfterInsert(node.getLeft(), insertedKey, steps));
        } else if (insertedKey > node.getKey()) {
            node.setRight(rebalanceAfterInsert(node.getRight(), insertedKey, steps));
        }
        updateHeight(node);
        return rebalance(node, insertedKey, steps);
    }

    /**
     * Deletes a key following standard BST deletion rules, then rebalances
     * the AVL tree. Steps are split into THREE phases, mirroring insert():
     * (1) a read-only search phase (CompareStep + DELETE_TARGET highlights
     * on the CURRENT tree); (2) a raw BST deletion with no rebalancing yet,
     * captured as an intermediate StructuralChangeStep so the view shows the
     * node actually leaving the tree before any rotation; (3) rebalancing on
     * top of that same structure, recording RotateStep(s) plus a second,
     * final StructuralChangeStep only if a rotation actually happened.
     */
    public DeleteResult delete(int key) {
        List<TreeStep> steps = new ArrayList<>();

        AVLNode current = root;
        AVLNode lastVisited = null;
        while (current != null && key != current.getKey()) {
            lastVisited = current;
            boolean goLeft = key < current.getKey();
            steps.add(new CompareStep(current.getKey(), key, goLeft));
            current = goLeft ? current.getLeft() : current.getRight();
        }

        if (current == null) {
            if (lastVisited != null) {
                steps.add(new HighlightStep(lastVisited.getKey(), HighlightStep.HighlightKind.NOT_FOUND));
            }
            return new DeleteResult(false, root, steps);
        }

        steps.add(new HighlightStep(current.getKey(), HighlightStep.HighlightKind.DELETE_TARGET));
        if (current.getLeft() != null && current.getRight() != null) {
            AVLNode successor = current.getRight();
            steps.add(new HighlightStep(successor.getKey(), HighlightStep.HighlightKind.DELETE_TARGET));
            while (successor.getLeft() != null) {
                successor = successor.getLeft();
                steps.add(new HighlightStep(successor.getKey(), HighlightStep.HighlightKind.DELETE_TARGET));
            }
        }

        // Phase 1: plain BST deletion, no rebalancing yet. This snapshot is
        // what the view animates to right after the search/target highlights,
        // so the node visibly leaves the tree (and, for the two-children case,
        // gets replaced by its inorder successor) before any rotation.
        AVLNode unbalancedRoot = deleteBstOnly(deepCopy(root), key);
        steps.add(new StructuralChangeStep(unbalancedRoot));

        // Phase 2: rebalance starting from that same unbalanced structure.
        // Unlike insert (which only ever unbalances one ancestor), delete can
        // require rotations at MULTIPLE ancestors on the way back up, so this
        // walks the whole remaining tree height, same as before.
        List<TreeStep> rebalanceSteps = new ArrayList<>();
        AVLNode newRoot = rebalanceAfterDeleteWholeTree(deepCopy(unbalancedRoot), rebalanceSteps);

        if (!rebalanceSteps.isEmpty()) {
            steps.addAll(rebalanceSteps);
            steps.add(new StructuralChangeStep(newRoot));
        }

        root = newRoot;
        return new DeleteResult(true, newRoot, steps);
    }

    /**
     * Pure BST deletion with no AVL rebalancing. Handles all three classic
     * cases (leaf, single child, two children via inorder successor) exactly
     * like the old deleteRecursive() did, but stops short of touching height/
     * balance — used to capture the intermediate "just deleted, not yet
     * rotated" snapshot for animation.
     */
    private AVLNode deleteBstOnly(AVLNode node, int key) {
        if (node == null) {
            return null;
        }

        if (key < node.getKey()) {
            node.setLeft(deleteBstOnly(node.getLeft(), key));
        } else if (key > node.getKey()) {
            node.setRight(deleteBstOnly(node.getRight(), key));
        } else {
            if (node.getLeft() == null || node.getRight() == null) {
                AVLNode child = (node.getLeft() != null) ? node.getLeft() : node.getRight();
                node = child;
            } else {
                AVLNode successor = findMin(node.getRight());
                AVLNode newRight = deleteBstOnly(node.getRight(), successor.getKey());

                // Key is final on AVLNode, so rebuild this position with a
                // fresh node carrying the successor's key instead of mutating it.
                AVLNode replacement = new AVLNode(successor.getKey());
                replacement.setLeft(node.getLeft());
                replacement.setRight(newRight);
                node = replacement;
            }
        }

        if (node != null) {
            updateHeight(node);
        }
        return node;
    }

    /**
     * Walks the already-deleted (but not yet rebalanced) tree bottom-up,
     * fixing heights and applying rotations at every ancestor that's out of
     * balance — same logic as the old deleteRecursive()'s rebalancing half,
     * just decoupled from the deletion itself so it can run as its own phase
     * on top of the deleteBstOnly() snapshot.
     */
    private AVLNode rebalanceAfterDeleteWholeTree(AVLNode node, List<TreeStep> steps) {
        if (node == null) {
            return null;
        }
        // No key to compare against here since the whole subtree may need
        // re-checking after deletion (unlike insert, which only unbalances
        // nodes on a single root-to-inserted-key path).
        node.setLeft(rebalanceAfterDeleteWholeTree(node.getLeft(), steps));
        node.setRight(rebalanceAfterDeleteWholeTree(node.getRight(), steps));
        updateHeight(node);
        return rebalanceAfterDelete(node, steps);
    }

    /**
     * Searches for a key, recording a CompareStep at every node visited on
     * the way down, and a terminal HighlightStep (FOUND or NOT_FOUND) so the
     * view layer can animate the search path (REQ-3.1 to REQ-3.3).
     */
    public SearchResult search(int key) {
        List<TreeStep> steps = new ArrayList<>();
        AVLNode current = root;
        AVLNode lastVisited = null;

        while (current != null) {
            lastVisited = current;

            if (key == current.getKey()) {
                steps.add(new HighlightStep(current.getKey(), HighlightStep.HighlightKind.FOUND));
                return new SearchResult(true, current, steps);
            }

            boolean goLeft = key < current.getKey();
            steps.add(new CompareStep(current.getKey(), key, goLeft));
            current = goLeft ? current.getLeft() : current.getRight();
        }

        if (lastVisited != null) {
            // REQ-3.3: highlight the last node compared before concluding "not found".
            steps.add(new HighlightStep(lastVisited.getKey(), HighlightStep.HighlightKind.NOT_FOUND));
        }
        return new SearchResult(false, null, steps);
    }

    /**
     * Traverses the tree in the given order, recording a VISIT HighlightStep
     * for each node in the order it's visited (REQ-4.1 to REQ-4.4).
     * <p>
     * All four traversal orders — INORDER, PREORDER, POSTORDER, LEVEL_ORDER —
     * are implemented.
     */
    public TraversalResult traverse(TraversalType type) {
        List<Integer> values = new ArrayList<>();
        List<TreeStep> steps = new ArrayList<>();

        if (root == null) {
            // REQ-4.4: empty tree -> no steps, no animation.
            return new TraversalResult(type, values, steps);
        }

        switch (type) {
            case INORDER -> inorderTraverse(root, values, steps);
            case PREORDER -> preorderTraverse(root, values, steps);
            case POSTORDER -> postorderTraverse(root, values, steps);
            case LEVEL_ORDER -> levelOrderTraverse(root, values, steps);
        }

        return new TraversalResult(type, values, steps);
    }

    // ── Traversal helpers ────────────────────────────────────────────────────

    /**
     * Left -> Root -> Right. Produces keys in ascending order for a valid
     * AVL/BST (REQ-4.1). Each visited node gets a VISIT HighlightStep in
     * visitation order, and its key is appended to the result sequence
     * (REQ-4.3 groundwork - the view currently applies these after playback
     * finishes rather than incrementally; that refinement comes later).
     */
    private void inorderTraverse(AVLNode node, List<Integer> values, List<TreeStep> steps) {
        if (node == null) {
            return;
        }
        inorderTraverse(node.getLeft(), values, steps);
        steps.add(new HighlightStep(node.getKey(), HighlightStep.HighlightKind.VISIT));
        values.add(node.getKey());
        inorderTraverse(node.getRight(), values, steps);
    }

    /**
     * Root -> Left -> Right. Visits and highlights the root before either
     * subtree, unlike inorder (REQ-4.1). Same VISIT HighlightStep + value
     * accumulation pattern as inorderTraverse().
     */
    private void preorderTraverse(AVLNode node, List<Integer> values, List<TreeStep> steps) {
        if (node == null) {
            return;
        }
        steps.add(new HighlightStep(node.getKey(), HighlightStep.HighlightKind.VISIT));
        values.add(node.getKey());
        preorderTraverse(node.getLeft(), values, steps);
        preorderTraverse(node.getRight(), values, steps);
    }

    /**
     * Left -> Right -> Root. Visits and highlights the root last, after both
     * subtrees (REQ-4.1). Same VISIT HighlightStep + value accumulation
     * pattern as inorderTraverse() / preorderTraverse().
     */
    private void postorderTraverse(AVLNode node, List<Integer> values, List<TreeStep> steps) {
        if (node == null) {
            return;
        }
        postorderTraverse(node.getLeft(), values, steps);
        postorderTraverse(node.getRight(), values, steps);
        steps.add(new HighlightStep(node.getKey(), HighlightStep.HighlightKind.VISIT));
        values.add(node.getKey());
    }

    /**
     * Breadth-first, level by level, left to right within each level (REQ-4.1).
     * Implemented iteratively with a FIFO queue rather than recursion, since
     * level-order is inherently a queue-driven traversal. Same VISIT
     * HighlightStep + value accumulation pattern as the other traversal
     * helpers, so playback order matches the queue's dequeue order exactly.
     */
    private void levelOrderTraverse(AVLNode root, List<Integer> values, List<TreeStep> steps) {
        Queue<AVLNode> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            AVLNode node = queue.poll();
            steps.add(new HighlightStep(node.getKey(), HighlightStep.HighlightKind.VISIT));
            values.add(node.getKey());

            if (node.getLeft() != null) {
                queue.add(node.getLeft());
            }
            if (node.getRight() != null) {
                queue.add(node.getRight());
            }
        }
    }

    // ── Insert helpers ───────────────────────────────────────────────────────

    /**
     * Mutates the tree to insert {@code key} and rebalances on the way back
     * up. Only records RotateStep entries; comparison highlights are recorded
     * separately by insert()'s read-only phase before this runs.
     */

    private AVLNode rebalance(AVLNode node, int insertedKey, List<TreeStep> steps) {
        int balance = balanceFactor(node);

        // Left-heavy
        if (balance > 1) {
            if (insertedKey < node.getLeft().getKey()) {
                // Left-Left case
                steps.add(new RotateStep(RotationType.RIGHT, node.getKey()));
                return rotateRight(node);
            } else {
                // Left-Right case
                steps.add(new RotateStep(RotationType.LEFT_RIGHT, node.getKey()));
                node.setLeft(rotateLeft(node.getLeft()));
                return rotateRight(node);
            }
        }

        // Right-heavy
        if (balance < -1) {
            if (insertedKey > node.getRight().getKey()) {
                // Right-Right case
                steps.add(new RotateStep(RotationType.LEFT, node.getKey()));
                return rotateLeft(node);
            } else {
                // Right-Left case
                steps.add(new RotateStep(RotationType.RIGHT_LEFT, node.getKey()));
                node.setRight(rotateRight(node.getRight()));
                return rotateLeft(node);
            }
        }

        return node;
    }

    // ── Delete helpers ───────────────────────────────────────────────────────

    /**
     * @return the leftmost (smallest-key) node of the given subtree.
     */
    private AVLNode findMin(AVLNode node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    private AVLNode rebalanceAfterDelete(AVLNode node, List<TreeStep> steps) {
        int balance = balanceFactor(node);

        // Left-heavy
        if (balance > 1) {
            if (balanceFactor(node.getLeft()) >= 0) {
                // Left-Left case
                steps.add(new RotateStep(RotationType.RIGHT, node.getKey()));
                return rotateRight(node);
            } else {
                // Left-Right case
                steps.add(new RotateStep(RotationType.LEFT_RIGHT, node.getKey()));
                node.setLeft(rotateLeft(node.getLeft()));
                return rotateRight(node);
            }
        }

        // Right-heavy
        if (balance < -1) {
            if (balanceFactor(node.getRight()) <= 0) {
                // Right-Right case
                steps.add(new RotateStep(RotationType.LEFT, node.getKey()));
                return rotateLeft(node);
            } else {
                // Right-Left case
                steps.add(new RotateStep(RotationType.RIGHT_LEFT, node.getKey()));
                node.setRight(rotateRight(node.getRight()));
                return rotateLeft(node);
            }
        }

        return node;
    }

    // ── Rotation primitives ──────────────────────────────────────────────────

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.getLeft();
        AVLNode transferred = x.getRight();

        x.setRight(y);
        y.setLeft(transferred);

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.getRight();
        AVLNode transferred = y.getLeft();

        y.setLeft(x);
        x.setRight(transferred);

        updateHeight(x);
        updateHeight(y);

        return y;
    }

    // ── Height / balance helpers ─────────────────────────────────────────────

    private int height(AVLNode node) {
        return node == null ? 0 : node.getHeight();
    }

    private int balanceFactor(AVLNode node) {
        return node == null ? 0 : height(node.getLeft()) - height(node.getRight());
    }

    private void updateHeight(AVLNode node) {
        node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
    }

    private AVLNode searchNode(AVLNode node, int key) {
        if (node == null) {
            return null;
        }
        if (key == node.getKey()) {
            return node;
        }
        return key < node.getKey()
                ? searchNode(node.getLeft(), key)
                : searchNode(node.getRight(), key);
    }

    private int countNodes(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }

    // ── Copy helper ──────────────────────────────────────────────────────────

    /**
     * Deep-copies the subtree rooted at {@code node}. Used before mutating
     * algorithms (insert/delete) run, so the pre-existing node graph — which
     * the view layer (TreeCanvas) may still be rendering mid-animation —
     * is left completely untouched until a StructuralChangeStep explicitly
     * hands over the new (copied) structure.
     */
    private AVLNode deepCopy(AVLNode node) {
        if (node == null) {
            return null;
        }
        AVLNode copy = new AVLNode(node.getKey());
        copy.setHeight(node.getHeight());
        copy.setLeft(deepCopy(node.getLeft()));
        copy.setRight(deepCopy(node.getRight()));
        return copy;
    }
}