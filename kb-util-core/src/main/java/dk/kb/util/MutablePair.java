package dk.kb.util;

import java.util.Objects;

public class MutablePair<L,R> {

    L left;
    R right;

    public MutablePair(L left, R right) {
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

    public void setLeft(L left) {
        this.left = left;
    }
    public void setKey(L key) {
        this.left = key;
    }

    public void setRight(R right) {
        this.right = right;
    }
    public void setValue(R value) {
        this.right = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MutablePair)) {
            return false;
        }

        MutablePair<?, ?> pair = (MutablePair<?, ?>) o;

        if (! Objects.equals(left, pair.left)) {
            return false;
        }
        return Objects.equals(right, pair.right);

    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
