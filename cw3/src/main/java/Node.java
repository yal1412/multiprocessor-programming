import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Node {
    private int key;
    private Node leftChild;
    private Node rightChild;
    private boolean isLeaf;

    private final AtomicBoolean isDeleted = new AtomicBoolean();
    private final ReentrantReadWriteLock leftChildLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock rightChildLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock sLock = new ReentrantReadWriteLock();

    void tryLockLeftEdgeRef(Node expRef, ThreadLocal<Stack<Lock>> localLocks) {

        this.leftChildLock.writeLock().lock();
        localLocks.get().push(this.leftChildLock.writeLock());

        if (this.isDeleted.get() || this.leftChild != expRef) {
            throw new RuntimeException("Can't do lock");
        }
    }

    void tryLockRightEdgeRef(Node expRef, ThreadLocal<Stack<Lock>> localLocks) {

        this.rightChildLock.writeLock().lock();
        localLocks.get().push(this.rightChildLock.writeLock());

        if (this.isDeleted.get() || this.rightChild != expRef) {
            throw new RuntimeException("Can't do lock");
        }
    }

    void tryLockEdgeRef(Node exp, ThreadLocal<Stack<Lock>> localLocks) {
        if (key < exp.getKey()) {
            tryLockRightEdgeRef(exp, localLocks);
        } else {
            tryLockLeftEdgeRef(exp, localLocks);
        }
    }

    void tryLockLeftEdgeVal(int expVal, ThreadLocal<Stack<Lock>> localLocks) {

        this.leftChildLock.writeLock().lock();
        localLocks.get().push(this.leftChildLock.writeLock());

        if (this.isDeleted.get() || Objects.isNull(leftChild) || this.leftChild.getKey() != expVal) {
            throw new RuntimeException("Can't do lock");
        }
    }

    void tryLockRightEdgeVal(int expVal, ThreadLocal<Stack<Lock>> localLocks) {

        this.rightChildLock.writeLock().lock();
        localLocks.get().push(this.rightChildLock.writeLock());

        if (this.isDeleted.get() || Objects.isNull(rightChild) || this.rightChild.getKey() != expVal) {
            throw new RuntimeException("Can't do lock");
        }
    }

    void tryLockEdgeVal(Node exp, ThreadLocal<Stack<Lock>> localLocks) {
        if (key < exp.getKey()) {
            tryLockRightEdgeVal(exp.getKey(), localLocks);
        } else {
            tryLockLeftEdgeVal(exp.getKey(), localLocks);
        }
    }

    void tryReadLockState(ThreadLocal<Stack<Lock>> localLocks) {

        sLock.readLock().lock();
        localLocks.get().push(sLock.readLock());

        if (isDeleted.get()) {
            throw new RuntimeException("Can't do lock");
        }
    }

    void tryReadLockState(boolean expState, ThreadLocal<Stack<Lock>> localLocks) {

        sLock.readLock().lock();
        localLocks.get().push(sLock.readLock());

        if (isDeleted.get() || expState != isLeaf) {
            throw new RuntimeException("Can't do lock");
        }
    }

    void tryWriteLockState(boolean expectedState, ThreadLocal<Stack<Lock>> localLocks) {

        sLock.writeLock().lock();
        localLocks.get().push(sLock.writeLock());

        if (isDeleted.get() || expectedState != isLeaf) {
            throw new RuntimeException("Can't do lock");
        }
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public AtomicBoolean isDeleted() {
        return isDeleted;
    }
}