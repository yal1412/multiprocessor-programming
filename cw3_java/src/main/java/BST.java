import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BST {

    private class Node {
        volatile Node leftChild, rightChild;
        int key;
        volatile boolean isLeaf = true;
        volatile boolean marked = false;
        final Lock lock = new ReentrantLock();

        public Node(int key) {
            this.key = key;
            this.leftChild = this.rightChild = null;
        }
    }

    private volatile Node root = null;
    private final Node sentinel = new Node(0);

    public BST() {
    }

    private Node[] findPlaceToInsert(final int var) {

        Node parentFirstIter = null;
        while (true) {

            if (this.root == null) {
                return new Node[]{sentinel, null};
            }

            Node currNode = this.root, parentNode = null;

            while (!currNode.isLeaf) {

                parentNode = currNode;
                if (var < currNode.key) {
                    currNode = currNode.leftChild;
                } else if (var >= currNode.key) {
                    currNode = currNode.rightChild;
                } else if (currNode.marked) {
                    break;
                }
                if (currNode == null) {
                    break;
                }
            }

            if (currNode == null) {
                continue;
            }

            if (currNode.marked) {
                continue;
            }

            if (currNode.isLeaf && var == currNode.key && !currNode.marked) {
                return new Node[]{parentNode, currNode};
            }
            if (currNode.isLeaf && parentNode == null && parentFirstIter == null) {
                return new Node[]{parentNode, currNode};
            }
            if (currNode.isLeaf && parentFirstIter != null && parentFirstIter == parentNode) {
                return new Node[]{parentNode, currNode};
            } else {
                parentFirstIter = parentNode;
            }
        }
    }

    public final boolean contains(final int var) {
        Node[] parent_curr = findPlaceToInsert(var);
        if (parent_curr[1] != null) {
            if (!parent_curr[1].marked && parent_curr[1].key == var) {
                return true;
            }
        }
        return false;
    }

    public final boolean insert(final int var) {
        while (true) {
            Node currNode = this.root, parentNode = null, gparentNode = null;

            if (this.root == null) {
                sentinel.lock.lock();
                try {
                    if (this.root == null) {
                        this.root = new Node(var);
                        return true;
                    } else {
                        currNode = this.root;
                    }
                } finally {
                    sentinel.lock.unlock();
                }
            }

            Node[] parentCurr = findPlaceToInsert(var);

            if (parentCurr[1].key == var) {
                return false;
            }
            if (parentCurr[0] == sentinel) {
                continue;
            }

            gparentNode = parentCurr[0];
            parentNode = parentCurr[1];
            int intermediateParentKey = parentNode.key;

            if (gparentNode != null) {
                gparentNode.lock.lock();
            }
            parentNode.lock.lock();
            try {
                if (parentNode.marked == false && intermediateParentKey == parentNode.key) {

                    if (var < parentNode.key) {
                        // делаем левого ребенка
                        parentNode.isLeaf = false;
                        parentNode.leftChild = new Node(var);
                        parentNode.rightChild = new Node(parentNode.key);
                        return true;

                    } else if (var > parentNode.key) {
                        // делаем правого ребенка, в текущем меняем значение
                        parentNode.isLeaf = false;
                        parentNode.leftChild = new Node(parentNode.key);
                        parentNode.rightChild = new Node(var);
                        parentNode.key = var;
                        return true;
                    } else {
                        return false;
                    }
                }
            } finally {
                parentNode.lock.unlock();
                if (gparentNode != null) {
                    gparentNode.lock.unlock();
                }
            }
        }
    }

    private Node[] findPlaceToRemove(final int var) {

        Node parentFirstIter = null;
        while (true) {

            if (this.root == null) {
                return new Node[]{sentinel, null};
            }

            Node currNode = this.root, parentNode = null, gparentNode = null;

            while (!currNode.isLeaf) {

                gparentNode = parentNode;
                parentNode = currNode;
                if (var < currNode.key) {
                    currNode = currNode.leftChild;
                } else if (var >= currNode.key) {
                    currNode = currNode.rightChild;
                } else if (currNode.marked) {
                    break;
                }
                if (currNode == null) {
                    break;
                }
            }

            if (currNode == null) {
                continue;
            }

            if (currNode.marked) {
                continue;
            }

            if (currNode.isLeaf && var == currNode.key && !currNode.marked) {
                return new Node[]{gparentNode, parentNode, currNode};
            }
            if (currNode.isLeaf && parentNode == null && parentFirstIter == null) {
                return new Node[]{gparentNode, parentNode, currNode};
            }
            if (currNode.isLeaf && parentFirstIter != null && parentFirstIter == parentNode) {
                return new Node[]{gparentNode, parentNode, currNode};
            } else {
                parentFirstIter = parentNode;
            }
        }
    }

    public final boolean remove(final int var) {
        while (true) {
            Node currNode = this.root, childNode = null, parentNode = null, gparentNode = null;

            if (this.root == null) {
                sentinel.lock.lock();
                try {
                    if (this.root == null) {
                        return false;
                    } else {
                        currNode = this.root;
                    }
                } finally {
                    sentinel.lock.unlock();
                }
            }

            Node[] parentCurr = findPlaceToRemove(var);

            if (parentCurr[2].key != var) {
                return false;
            }
            if (parentCurr[1] == sentinel) {
                return false;
            }
            if (parentCurr[1] == null) {
                try {
                    sentinel.lock.lock();
                    root = null;
                    return true;
                } finally {
                    sentinel.lock.unlock();
                }

            }

            gparentNode = parentCurr[0];
            parentNode = parentCurr[1];
            childNode = parentCurr[2];

            if (gparentNode == null) {
                try {
                    if (!childNode.marked) {
                        parentNode.lock.lock();
                        childNode.lock.lock();
                        sentinel.lock.lock();
                        if (var == parentNode.leftChild.key && var == childNode.key) {
                            childNode.marked = true;
                            root = parentNode.rightChild;
                            return true;
                        } else if (var == parentNode.rightChild.key && var == childNode.key) {
                            childNode.marked = true;
                            root = parentNode.leftChild;
                            return true;
                        } else {
                            return false;
                        }
                    }
                } finally {
                    sentinel.lock.unlock();
                    parentNode.lock.unlock();
                    childNode.lock.unlock();
                }
            }

            int intermediateParentKey = childNode.key;

            if (gparentNode != null) {
                gparentNode.lock.lock();
            }
            parentNode.lock.lock();
            childNode.lock.lock();

            try {
                if (childNode.marked == false && intermediateParentKey == childNode.key) {

                    if (var == parentNode.leftChild.key) {
                        // удаляем левого ребенка
                        childNode.marked = true;
                        childNode.lock.unlock();
                        if (gparentNode.rightChild == parentNode) {
                            gparentNode.rightChild = parentNode.rightChild;
                        } else {
                            gparentNode.leftChild = parentNode.rightChild;
                        }
                        return true;

                    } else if (var == parentNode.rightChild.key) {
                        // удаляем правого ребенка
                        childNode.marked = true;
                        childNode.lock.unlock();
                        if (gparentNode.rightChild == parentNode) {
                            gparentNode.rightChild = parentNode.leftChild;
                        } else {
                            gparentNode.leftChild = parentNode.leftChild;
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            } finally {
                parentNode.lock.unlock();
                if (gparentNode != null) {
                    gparentNode.lock.unlock();
                }
            }
        }
    }
}