package org.highj.data.tuple;

import org.highj._;
import org.highj.data.tuple.t1.*;
import org.highj.typeclass0.group.Group;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass0.group.Semigroup;
import org.highj.typeclass1.comonad.Comonad;
import org.highj.typeclass1.monad.Monad;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.highj.data.tuple.Tuple.of;

/**
 * A tuple of arity 1, a.k.a. "cell" or "Id".
 */
public abstract class T1<A> implements  _<T1.µ, A>, Supplier<A> {
    public static class µ {}

    @Override
    public A get() {
        return _1();
    }

    public abstract A _1();

    @Override
    public String toString() {
        return String.format("(%s)", _1());
    }

    public <B> T1<B> map(Function<? super A, ? extends B> fn) {
        return of(fn.apply(_1()));
    }

    public <B> T1<B> ap(T1<Function<A, B>> nestedFn) {
        return map(Tuple.narrow(nestedFn)._1());
    }

    @Override
    public int hashCode() {
        return _1().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof T1) {
            T1<?> that = (T1) o;
            return this._1().equals(that._1());
        }
        return false;
    }

    public static final Monad<µ> monad = new T1Monad();
    public static final Comonad<µ> comonad = new T1Comonad();

    public static <A> Semigroup<T1<A>> semigroup(Semigroup<A> semigroupA) {
        return T1Semigroup.from(semigroupA);
    }

    public static <A> Monoid<T1<A>> monoid(Monoid<A> monoidA) {
        return T1Monoid.from(monoidA);
    }

    public static <A> Group<T1<A>> group(Group<A> groupA) {
        return T1Group.from(groupA);
    }

}
