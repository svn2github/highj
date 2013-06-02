package org.highj.data.collection.maybe;

import org.highj._;
import org.highj.data.collection.Maybe;
import org.highj.typeclass1.functor.Functor;

import java.util.function.Function;

import static org.highj.data.collection.Maybe.narrow;

public class MaybeFunctor implements Functor<Maybe.µ> {

    public <A, B> Maybe<B> map(Function<A, B> fn, _<Maybe.µ, A> nestedA) {
        return narrow(nestedA).map(fn);
    }

}
