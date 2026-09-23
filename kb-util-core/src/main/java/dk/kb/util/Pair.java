package dk.kb.util;

/**
 * <p>The much missed Pair class. There have been much discussion about Pairs. Here it is, do not use
 * it if you do not like it.</p>
 *
 * <p>This implementation is immutable, i.e., there are no setter methods.
 * Equals and hashcode delegate the work to the contained elements.</p>
 */
public record Pair<L, R>(L left, R right) {

    public L getKey() {
        return left;
    }

    public R getValue() {
        return right;
    }

}
