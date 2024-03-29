package dk.kb.util;

/**
 * The much missed Pair class. There have been much discussion about Pairs. Here it is, do not use
 * it if you do not like it.
 *
 * This implementation is immutable, ie, there are no setter methods. Equals and hashcode have
 * been overridden to delegate the work the the contained elements.
 */
public class Pair<L,R> {

    final private L left;
    final private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }
    public L getKey() {
        return left;
    }

    public R getRight() {
        return right;
    }
    public R getValue() {
        return right;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (left != null ? !left.equals(pair.left) : pair.left != null) {
            return false;
        }
        if (right != null ? !right.equals(pair.right) : pair.right != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
