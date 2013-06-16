package org.highj.data.tuple.t2;

import org.highj.__;
import org.highj.data.tuple.T2;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.monad.Applicative;

public interface T2Applicative<S> extends T2Apply<S>, Applicative<__.µ<T2.µ, S>> {

    @Override
    public Monoid<S> getS();

    @Override
    public default <A> T2<S, A> pure(A a) {
        return T2.of(getS().identity(), a);
    }
}
