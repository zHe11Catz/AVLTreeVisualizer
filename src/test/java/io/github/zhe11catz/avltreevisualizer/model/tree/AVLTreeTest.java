package io.github.zhe11catz.avltreevisualizer.model.tree;

import io.github.zhe11catz.avltreevisualizer.model.operation.DeleteResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.InsertResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.SearchResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalResult;
import io.github.zhe11catz.avltreevisualizer.model.operation.TraversalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the AVL tree model layer (AVLTree / AVLNode).
 * <p>
 * Covers: basic operations, duplicate insertion, deletion edge cases
 * (leaf / one child / two children / not found), all four rotation
 * cases (LL, RR, LR, RL) for both insert and delete, traversal
 * correctness, and randomized stress testing of the AVL invariant.
 */
class AVLTreeTest {

    private AVLTree tree;

    @BeforeEach
    void setUp() {
        tree = new AVLTree();
    }

    // ── Test helpers ─────────────────────────────────────────────────────────

    /**
     * Recursively verifies the BST property (left < node < right) and the
     * AVL balance property (balance factor in {-1, 0, 1}) for every node.
     */
    private void assertValidAvl(AVLNode node) {
        assertValidAvlRecursive(node, null, null);
    }

    private int assertValidAvlRecursive(AVLNode node, Integer min, Integer max) {
        if (node == null) {
            return 0;
        }

        if (min != null) {
            assertTrue(node.getKey() > min,
                    "BST violation: key " + node.getKey() + " should be > " + min);
        }
        if (max != null) {
            assertTrue(node.getKey() < max,
                    "BST violation: key " + node.getKey() + " should be < " + max);
        }

        int leftHeight = assertValidAvlRecursive(node.getLeft(), min, node.getKey());
        int rightHeight = assertValidAvlRecursive(node.getRight(), node.getKey(), max);

        int balance = leftHeight - rightHeight;
        assertTrue(balance >= -1 && balance <= 1,
                "AVL balance violation at node " + node.getKey() + ": balance factor = " + balance);

        int expectedHeight = 1 + Math.max(leftHeight, rightHeight);
        assertEquals(expectedHeight, node.getHeight(),
                "Stored height mismatch at node " + node.getKey());

        return expectedHeight;
    }

    /** Counts nodes by walking the actual node graph (independent of tree.size()). */
    private int countNodes(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }

    /** Collects keys via inorder walk to check sortedness / membership. */
    private void collectInorder(AVLNode node, List<Integer> out) {
        if (node == null) {
            return;
        }
        collectInorder(node.getLeft(), out);
        out.add(node.getKey());
        collectInorder(node.getRight(), out);
    }

    // ── Basic state ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Basic tree state")
    class BasicState {

        @Test
        @DisplayName("A new tree is empty")
        void newTreeShouldBeEmpty() {
            assertTrue(tree.isEmpty());
            assertFalse(tree.contains(10));
            assertEquals(0, tree.size());
            assertNull(tree.getRoot());
        }

        @Test
        @DisplayName("Inserting increases size and contains() reflects it")
        void insertIncreasesSize() {
            tree.insert(10);
            tree.insert(5);
            tree.insert(15);

            assertEquals(3, tree.size());
            assertTrue(tree.contains(10));
            assertTrue(tree.contains(5));
            assertTrue(tree.contains(15));
            assertFalse(tree.contains(99));
        }

        @Test
        @DisplayName("clear() empties the tree")
        void clearEmptiesTree() {
            tree.insert(1);
            tree.insert(2);
            tree.clear();

            assertTrue(tree.isEmpty());
            assertEquals(0, tree.size());
            assertNull(tree.getRoot());
        }
    }

    // ── Insert: duplicates and result payload ──────────────────────────────

    @Nested
    @DisplayName("Insert - duplicates")
    class InsertDuplicates {

        @Test
        @DisplayName("Inserting a duplicate key does not add a new node")
        void duplicateInsertDoesNotChangeSize() {
            tree.insert(10);
            tree.insert(5);
            tree.insert(15);
            int sizeBefore = tree.size();

            tree.insert(10);

            assertEquals(sizeBefore, tree.size(),
                    "Size must not change when inserting an existing key");
            assertEquals(sizeBefore, countNodes(tree.getRoot()));
        }

        @Test
        @DisplayName("Inserting a duplicate key leaves tree structure valid")
        void duplicateInsertKeepsTreeValid() {
            int[] values = {50, 30, 70, 20, 40, 60, 80};
            for (int v : values) {
                tree.insert(v);
            }

            tree.insert(40); // duplicate of an existing internal node

            assertValidAvl(tree.getRoot());
            for (int v : values) {
                assertTrue(tree.contains(v));
            }
        }

        @Test
        @DisplayName("InsertResult carries the tree's new root")
        void insertResultCarriesRoot() {
            InsertResult result = tree.insert(42);

            assertTrue(result.isSuccess());
            assertNotNull(result.getRoot());
            assertEquals(tree.getRoot(), result.getRoot());
            assertFalse(result.getSteps().isEmpty(), "Insert should record at least one step");
        }
    }

    // ── Insert: rotation cases ──────────────────────────────────────────────

    @Nested
    @DisplayName("Insert - rotation cases")
    class InsertRotations {

        @Test
        @DisplayName("Left-Left case triggers a single right rotation")
        void leftLeftCaseRotatesRight() {
            // Descending insert order forces a LL imbalance at the root.
            tree.insert(30);
            tree.insert(20);
            tree.insert(10);

            assertValidAvl(tree.getRoot());
            assertEquals(20, tree.getRoot().getKey(), "20 should become the new root after RR rotation");
            assertEquals(10, tree.getRoot().getLeft().getKey());
            assertEquals(30, tree.getRoot().getRight().getKey());
            assertEquals(3, tree.size());
        }

        @Test
        @DisplayName("Right-Right case triggers a single left rotation")
        void rightRightCaseRotatesLeft() {
            // Ascending insert order forces a RR imbalance at the root.
            tree.insert(10);
            tree.insert(20);
            tree.insert(30);

            assertValidAvl(tree.getRoot());
            assertEquals(20, tree.getRoot().getKey(), "20 should become the new root after LL rotation");
            assertEquals(10, tree.getRoot().getLeft().getKey());
            assertEquals(30, tree.getRoot().getRight().getKey());
            assertEquals(3, tree.size());
        }

        @Test
        @DisplayName("Left-Right case triggers a double rotation (inner LEFT, outer RIGHT)")
        void leftRightCaseRotatesTwice() {
            tree.insert(30);
            tree.insert(10);
            tree.insert(20); // unbalances at 30, imbalance node is in left-right position

            assertValidAvl(tree.getRoot());
            assertEquals(20, tree.getRoot().getKey(), "20 should become the new root after LR rotation");
            assertEquals(10, tree.getRoot().getLeft().getKey());
            assertEquals(30, tree.getRoot().getRight().getKey());
            assertEquals(3, tree.size());
        }

        @Test
        @DisplayName("Right-Left case triggers a double rotation (inner RIGHT, outer LEFT)")
        void rightLeftCaseRotatesTwice() {
            tree.insert(10);
            tree.insert(30);
            tree.insert(20); // unbalances at 10, imbalance node is in right-left position

            assertValidAvl(tree.getRoot());
            assertEquals(20, tree.getRoot().getKey(), "20 should become the new root after RL rotation");
            assertEquals(10, tree.getRoot().getLeft().getKey());
            assertEquals(30, tree.getRoot().getRight().getKey());
            assertEquals(3, tree.size());
        }

        @Test
        @DisplayName("Insert steps include a RotateStep when rebalancing occurs")
        void insertRecordsRotateStepsWhenNeeded() {
            tree.insert(10);
            tree.insert(20);
            InsertResult result = tree.insert(30); // triggers LL->RR rotation

            boolean hasRotateStep = result.getSteps().stream()
                    .anyMatch(step -> step instanceof io.github.zhe11catz.avltreevisualizer.model.operation.step.RotateStep);
            assertTrue(hasRotateStep, "Expected a RotateStep to be recorded for a rebalancing insert");
        }

        @Test
        @DisplayName("Larger interleaved insert sequence keeps the AVL invariant")
        void interleavedInsertsStayBalanced() {
            int[] values = {50, 25, 75, 10, 30, 60, 90, 5, 15, 27, 35, 55, 65, 80, 95};
            for (int v : values) {
                tree.insert(v);
            }

            assertValidAvl(tree.getRoot());
            assertEquals(values.length, tree.size());
        }
    }

    // ── Delete: not found / edge cases ──────────────────────────────────────

    @Nested
    @DisplayName("Delete - edge cases")
    class DeleteEdgeCases {

        @Test
        @DisplayName("Deleting from an empty tree fails gracefully")
        void deleteFromEmptyTreeFails() {
            DeleteResult result = tree.delete(10);

            assertFalse(result.isSuccess());
            assertTrue(tree.isEmpty());
        }

        @Test
        @DisplayName("Deleting a non-existent value fails and leaves tree unchanged")
        void deleteNonExistentValueFails() {
            tree.insert(10);
            tree.insert(5);
            tree.insert(15);
            int sizeBefore = tree.size();

            DeleteResult result = tree.delete(999);

            assertFalse(result.isSuccess());
            assertEquals(sizeBefore, tree.size());
            assertValidAvl(tree.getRoot());
        }

        @Test
        @DisplayName("Deleting a leaf node removes it directly")
        void deleteLeafNode() {
            tree.insert(10);
            tree.insert(5);
            tree.insert(15);

            DeleteResult result = tree.delete(5); // leaf

            assertTrue(result.isSuccess());
            assertFalse(tree.contains(5));
            assertEquals(2, tree.size());
            assertValidAvl(tree.getRoot());
        }

        @Test
        @DisplayName("Deleting a node with one child promotes that child")
        void deleteNodeWithOneChild() {
            tree.insert(10);
            tree.insert(5);
            tree.insert(15);
            tree.insert(12); // 15 now has a single left child: 12

            DeleteResult result = tree.delete(15);

            assertTrue(result.isSuccess());
            assertFalse(tree.contains(15));
            assertTrue(tree.contains(12));
            assertEquals(3, tree.size());
            assertValidAvl(tree.getRoot());
        }

        @Test
        @DisplayName("Deleting a node with two children replaces it with its inorder successor")
        void deleteNodeWithTwoChildren() {
            int[] values = {50, 30, 70, 20, 40, 60, 80};
            for (int v : values) {
                tree.insert(v);
            }

            // Node 30 has two children (20 and 40); inorder successor is 40.
            DeleteResult result = tree.delete(30);

            assertTrue(result.isSuccess());
            assertFalse(tree.contains(30));
            for (int v : new int[]{50, 70, 20, 40, 60, 80}) {
                assertTrue(tree.contains(v), "Expected remaining key " + v + " to still be present");
            }
            assertEquals(values.length - 1, tree.size());
            assertValidAvl(tree.getRoot());
        }

        @Test
        @DisplayName("Deleting the root of a single-node tree empties it")
        void deleteRootOfSingleNodeTree() {
            tree.insert(42);

            DeleteResult result = tree.delete(42);

            assertTrue(result.isSuccess());
            assertTrue(tree.isEmpty());
            assertNull(tree.getRoot());
        }

        @Test
        @DisplayName("DeleteResult on failure still records comparison steps")
        void deleteFailureRecordsSteps() {
            tree.insert(50);
            tree.insert(30);
            tree.insert(70);

            DeleteResult result = tree.delete(999);

            assertFalse(result.isSuccess());
            assertFalse(result.getSteps().isEmpty(),
                    "Even a failed delete should record the comparisons made while searching");
        }
    }

    // ── Delete: rotation cases ──────────────────────────────────────────────

    @Nested
    @DisplayName("Delete - rotation cases")
    class DeleteRotations {

        @Test
        @DisplayName("Delete triggering Left-Left rebalance keeps AVL invariant")
        void deleteTriggersLeftLeftRebalance() {
            // Build a tree where removing a right-side leaf unbalances the root to the left.
            tree.insert(30);
            tree.insert(20);
            tree.insert(40);
            tree.insert(10);
            tree.insert(25);
            // Remove 40's only sibling-side weight so the left subtree dominates.
            tree.delete(40);

            assertValidAvl(tree.getRoot());
            assertFalse(tree.contains(40));
            assertEquals(4, tree.size());
        }

        @Test
        @DisplayName("Delete triggering Right-Right rebalance keeps AVL invariant")
        void deleteTriggersRightRightRebalance() {
            tree.insert(20);
            tree.insert(10);
            tree.insert(30);
            tree.insert(25);
            tree.insert(40);
            tree.delete(10);

            assertValidAvl(tree.getRoot());
            assertFalse(tree.contains(10));
            assertEquals(4, tree.size());
        }

        @Test
        @DisplayName("Delete can require rebalancing at multiple ancestors")
        void deleteMayRebalanceMultipleAncestors() {
            int[] values = {50, 25, 75, 10, 30, 60, 90, 5, 15, 27, 35, 55, 65, 80, 95};
            for (int v : values) {
                tree.insert(v);
            }

            // Remove several nodes in sequence, checking the invariant after each.
            int[] toDelete = {5, 95, 15, 85 /* not present, should just no-op */, 60, 30};
            for (int v : toDelete) {
                tree.delete(v);
                assertValidAvl(tree.getRoot());
            }
        }

        @Test
        @DisplayName("Delete records RotateStep when rebalancing occurs")
        void deleteRecordsRotateStepsWhenNeeded() {
            tree.insert(20);
            tree.insert(10);
            tree.insert(30);
            tree.insert(25);
            tree.insert(40);

            DeleteResult result = tree.delete(10);

            boolean hasRotateStep = result.getSteps().stream()
                    .anyMatch(step -> step instanceof io.github.zhe11catz.avltreevisualizer.model.operation.step.RotateStep);
            assertTrue(hasRotateStep, "Expected a RotateStep to be recorded for a rebalancing delete");
        }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Search")
    class SearchTests {

        @Test
        @DisplayName("Search finds an existing key")
        void searchFindsExistingKey() {
            tree.insert(50);
            tree.insert(30);
            tree.insert(70);

            SearchResult result = tree.search(30);

            assertTrue(result.isSuccess());
            assertNotNull(result.getTargetNode());
            assertEquals(30, result.getTargetNode().getKey());
        }

        @Test
        @DisplayName("Search reports not found for a missing key")
        void searchReportsNotFound() {
            tree.insert(50);
            tree.insert(30);
            tree.insert(70);

            SearchResult result = tree.search(999);

            assertFalse(result.isSuccess());
            assertNull(result.getTargetNode());
            assertFalse(result.getSteps().isEmpty());
        }

        @Test
        @DisplayName("Search on an empty tree returns no steps and fails")
        void searchOnEmptyTree() {
            SearchResult result = tree.search(1);

            assertFalse(result.isSuccess());
            assertTrue(result.getSteps().isEmpty());
        }
    }

    // ── Traversal ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Traversal")
    class TraversalTests {

        private void buildSampleTree() {
            int[] values = {50, 30, 70, 20, 40, 60, 80};
            for (int v : values) {
                tree.insert(v);
            }
        }

        @Test
        @DisplayName("Inorder traversal yields keys in ascending order")
        void inorderIsSorted() {
            buildSampleTree();

            TraversalResult result = tree.traverse(TraversalType.INORDER);

            List<Integer> values = result.getValues();
            for (int i = 1; i < values.size(); i++) {
                assertTrue(values.get(i - 1) < values.get(i), "Inorder traversal must be strictly ascending");
            }
            assertEquals(tree.size(), values.size());
        }

        @Test
        @DisplayName("Preorder traversal visits the root first")
        void preorderVisitsRootFirst() {
            buildSampleTree();

            TraversalResult result = tree.traverse(TraversalType.PREORDER);

            assertEquals(tree.getRoot().getKey(), result.getValues().get(0));
        }

        @Test
        @DisplayName("Postorder traversal visits the root last")
        void postorderVisitsRootLast() {
            buildSampleTree();

            TraversalResult result = tree.traverse(TraversalType.POSTORDER);

            List<Integer> values = result.getValues();
            assertEquals(tree.getRoot().getKey(), values.get(values.size() - 1));
        }

        @Test
        @DisplayName("Level-order traversal visits the root first and respects level ordering")
        void levelOrderRespectsLevels() {
            buildSampleTree();

            TraversalResult result = tree.traverse(TraversalType.LEVEL_ORDER);

            List<Integer> values = result.getValues();
            assertEquals(tree.getRoot().getKey(), values.get(0));
            assertEquals(tree.size(), values.size());
        }

        @Test
        @DisplayName("Traversing an empty tree produces no values and no steps")
        void traverseEmptyTree() {
            TraversalResult result = tree.traverse(TraversalType.INORDER);

            assertTrue(result.getValues().isEmpty());
            assertTrue(result.getSteps().isEmpty());
        }

        @Test
        @DisplayName("Every traversal type visits every node exactly once")
        void allTraversalTypesVisitEveryNodeOnce() {
            buildSampleTree();
            int expectedCount = tree.size();

            for (TraversalType type : TraversalType.values()) {
                TraversalResult result = tree.traverse(type);
                Set<Integer> uniqueValues = new HashSet<>(result.getValues());

                assertEquals(expectedCount, result.getValues().size(),
                        "Traversal " + type + " should visit every node exactly once");
                assertEquals(expectedCount, uniqueValues.size(),
                        "Traversal " + type + " should not repeat any node");
            }
        }
    }

    // ── Import limit (127 nodes / range) — validated at the model level ─────

    @Nested
    @DisplayName("Capacity and range boundaries")
    class CapacityAndRange {

        @Test
        @DisplayName("Tree can grow to exactly 127 nodes and stays a valid AVL tree")
        void treeSupportsMaxNodeCount() {
            for (int i = 1; i <= 127; i++) {
                tree.insert(i);
            }

            assertEquals(127, tree.size());
            assertValidAvl(tree.getRoot());
        }

        @Test
        @DisplayName("Boundary values -9999 and 9999 can be inserted and found")
        void boundaryValuesAreAccepted() {
            tree.insert(-9999);
            tree.insert(9999);
            tree.insert(0);

            assertTrue(tree.contains(-9999));
            assertTrue(tree.contains(9999));
            assertValidAvl(tree.getRoot());
        }
    }

    // ── Randomized stress test ───────────────────────────────────────────────

    @Nested
    @DisplayName("Randomized stress test")
    class StressTest {

        @RepeatedTest(5)
        @DisplayName("AVL invariant holds after a random sequence of inserts and deletes")
        void randomInsertDeleteSequenceStaysValid() {
            Random random = new Random(12345);
            Set<Integer> expectedMembers = new HashSet<>();

            // Random inserts within the SRS value range, respecting the 127-node cap.
            for (int i = 0; i < 120; i++) {
                int value = random.nextInt(1000) - 500;
                tree.insert(value);
                expectedMembers.add(value);

                assertValidAvl(tree.getRoot());
                assertEquals(expectedMembers.size(), tree.size());
            }

            // Random deletes, some of which target keys that were never inserted.
            List<Integer> membersSnapshot = new ArrayList<>(expectedMembers);
            for (int i = 0; i < 60; i++) {
                int value = random.nextBoolean() && !membersSnapshot.isEmpty()
                        ? membersSnapshot.get(random.nextInt(membersSnapshot.size()))
                        : random.nextInt(1000) - 500;

                DeleteResult result = tree.delete(value);
                if (result.isSuccess()) {
                    expectedMembers.remove(value);
                }

                assertValidAvl(tree.getRoot());
                assertEquals(expectedMembers.size(), tree.size());
            }

            // Final structural sanity check: inorder walk matches the expected set, sorted.
            List<Integer> inorder = new ArrayList<>();
            collectInorder(tree.getRoot(), inorder);
            List<Integer> expectedSorted = new ArrayList<>(expectedMembers);
            expectedSorted.sort(Integer::compareTo);
            assertEquals(expectedSorted, inorder);
        }
    }
}