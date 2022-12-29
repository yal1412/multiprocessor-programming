import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

public class Tree {
    private Node root;
    private static ThreadLocal<Stack<Lock>> nodesLocks = ThreadLocal.withInitial(Stack::new);

    public Tree(int initialVal) {
        this.root = new Node();
        this.root.setKey(initialVal);
        this.root.setLeaf(true);
    }

    private List<Node> traversal(int v) {
        while (true) {
            Node gPrev = new Node();
            Node prev = new Node();
            Node curr = this.root;

            while (Objects.nonNull(curr)) {
                if (curr.getKey() == v) {
                    break;
                } else {
                    gPrev = prev;
                    prev = curr;
                    if (curr.getKey() > v) {
                        curr = curr.getLeftChild();
                    } else {
                        curr = curr.getRightChild();
                    }
                }

                if (checkDeleted(gPrev)) break;
                if (checkDeleted(prev)) break;
                if (checkDeleted(curr)) break;
            }

            if (checkDeleted(gPrev)) continue;
            if (checkDeleted(prev)) continue;
            if (checkDeleted(curr)) continue;

            return Arrays.asList(gPrev, prev, curr);
        }
    }

    private boolean checkDeleted(Node node) {
        return Objects.nonNull(node) && node.isDeleted().get();
    }

    public boolean contains(int v) {
        Node curr = traversal(v).get(2);
        return Objects.nonNull(curr) && curr.isLeaf();
    }

    public boolean insert(int v) {
        while (true) {
            try {
                List<Node> traversal = traversal(v);
                Node prev = traversal.get(1);
                Node curr = traversal.get(2);

                if (Objects.nonNull(curr)) {
                    if (curr.isLeaf()) {
                        return false;
                    }
                    curr.tryWriteLockState(false, nodesLocks);
                    curr.setLeaf(true);
                } else {
                    insertNewNode(v, prev);
                }
                return true;
            } catch (Exception ignored) {

            } finally {
                releaseAllLocks();
            }
        }
    }

    private void insertNewNode(int v, Node prev) {
        Node newNode = new Node();
        newNode.setKey(v);
        newNode.setLeaf(true);
        if (prev.getKey() > v) {
            prev.tryReadLockState(nodesLocks);
            prev.tryLockLeftEdgeRef(null, nodesLocks);
            prev.setLeftChild(newNode);
        } else {
            prev.tryReadLockState(nodesLocks);
            prev.tryLockRightEdgeRef(null, nodesLocks);
            prev.setRightChild(newNode);
        }
    }

    private void releaseAllLocks() {
        while (!nodesLocks.get().empty()) {
            nodesLocks.get().pop().unlock();
        }
    }

    public boolean delete(int v) {
        while (true) {
            try {
                List<Node> traversal = traversal(v);
                Node gPrev = traversal.get(0);
                Node prev = traversal.get(1);
                Node curr = traversal.get(2);

                if (Objects.isNull(curr) || !curr.isLeaf()) {
                    return false;
                }

                if (Objects.nonNull(curr.getLeftChild()) && Objects.nonNull(curr.getRightChild())) {

                    deleteNodeWithTwoChildren(curr);

                } else if (Objects.nonNull(curr.getLeftChild()) || Objects.nonNull(curr.getRightChild())) {

                    deleteNodeWithOneChild(prev, curr);

                } else {
                    if (prev.isLeaf()) {

                        deleteNodeWithLeafParent(v, prev, curr);

                    } else {

                        deleteNodeWithRoutingParent(v, gPrev, prev, curr);

                    }
                }
                return true;
            } catch (Exception ignored) {

            } finally {
                releaseAllLocks();
            }
        }
    }

    private void deleteNodeWithRoutingParent(int v, Node gPrev, Node prev, Node curr) {
        Node child;
        if (curr.getKey() < prev.getKey()) {
            child = prev.getRightChild();
        } else {
            child = prev.getLeftChild();
        }

        if (Objects.nonNull(gPrev.getLeftChild()) && prev == gPrev.getLeftChild()) {
            gPrev.tryLockEdgeRef(prev, nodesLocks);
            prev.tryWriteLockState(false, nodesLocks);
            prev.tryLockEdgeRef(child, nodesLocks);

            curr = lockLeaf(v, prev, curr);

            prev.isDeleted().set(true);
            curr.isDeleted().set(true);
            gPrev.setLeftChild(child);
        } else if (Objects.nonNull(gPrev.getRightChild()) && prev == gPrev.getRightChild()) {
            gPrev.tryLockEdgeRef(prev, nodesLocks);
            prev.tryWriteLockState(false, nodesLocks);
            prev.tryLockEdgeRef(child, nodesLocks);

            curr = lockLeaf(v, prev, curr);

            prev.isDeleted().set(true);
            curr.isDeleted().set(true);
            gPrev.setRightChild(child);
        }
    }

    private void deleteNodeWithLeafParent(int v, Node prev, Node curr) {
        if (curr.getKey() < prev.getKey()) {
            prev.tryReadLockState(true, nodesLocks);

            curr = lockLeaf(v, prev, curr);

            curr.isDeleted().set(true);
            prev.setLeftChild(null);
        } else {
            prev.tryReadLockState(true, nodesLocks);

            curr = lockLeaf(v, prev, curr);

            curr.isDeleted().set(true);
            prev.setRightChild(null);
        }
    }

    private void deleteNodeWithTwoChildren(Node curr) {
        curr.tryWriteLockState(true, nodesLocks);

        if (Objects.isNull(curr.getLeftChild()) || Objects.isNull(curr.getRightChild())) {
            throw new RuntimeException("curr does not have 2 children");
        }
        curr.setLeaf(false);
    }

    private void deleteNodeWithOneChild(Node prev, Node curr) {
        Node child;
        if (Objects.nonNull(curr.getLeftChild())) {
            child = curr.getLeftChild();
        } else {
            child = curr.getRightChild();
        }

        if (curr.getKey() < prev.getKey()) {
            lockVertexWithOneChild(prev, curr, child);
            curr.isDeleted().set(true);
            prev.setLeftChild(child);
        } else {
            lockVertexWithOneChild(prev, curr, child);
            curr.isDeleted().set(true);
            prev.setRightChild(child);
        }
    }

    private Node lockLeaf(int v, Node prev, Node curr) {
        prev.tryLockEdgeVal(curr, nodesLocks);

        if (v < prev.getKey()) {
            curr = prev.getLeftChild();
        } else {
            curr = prev.getRightChild();
        }

        curr.tryWriteLockState(true, nodesLocks);

        if (Objects.nonNull(curr.getLeftChild()) || Objects.nonNull(curr.getRightChild())) {
            throw new RuntimeException("curr in not a leaf");
        }
        return curr;
    }

    private void lockVertexWithOneChild(Node prev, Node curr, Node child) {
        prev.tryLockEdgeRef(curr, nodesLocks);
        curr.tryWriteLockState(true, nodesLocks);

        if (Objects.nonNull(curr.getLeftChild()) && Objects.nonNull(curr.getRightChild())) {
            throw new RuntimeException("curr has 2 children");
        }

        if (Objects.isNull(curr.getLeftChild()) && Objects.isNull(curr.getRightChild())) {
            throw new RuntimeException("curr has 0 children");
        }

        curr.tryLockEdgeRef(child, nodesLocks);
    }

    public CopyOnWriteArrayList<Integer> inorderTraversal() {
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        Stack<Node> stack = new Stack<>();
        Node curr = root;
        while (curr != null || !stack.empty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeftChild();
            }
            curr = stack.pop();
            if (curr.isLeaf() && !curr.isDeleted().get()) {
                list.add(curr.getKey());
            }
            curr = curr.getRightChild();
        }
        return list;
    }
}
