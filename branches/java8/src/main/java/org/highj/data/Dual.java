package org.highj.data;

import org.highj._;
import org.highj.__;
import org.highj.typeclass.arrow.Category;
import org.highj.typeclass.arrow.CategoryAbstract;

// This code works somehow, but I don't dare thinking about it,
// as I'm not sure whether this path leads to madness or enlightenment.
public class Dual<M, A, B> extends __<_<Dual.µ, M>, A, B> {

    private static final µ hidden = new µ();

    public static class µ {
        private µ() {
        }
    }

    public static class µµ<M> extends _<µ, M> {
        private µµ() {
            super(hidden);
        }
    }

    private final __<M, B, A> value;

    public Dual(__<M, B, A> value) {
        super(new µµ<M>());
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <M, A, B> Dual<M, A, B> narrow(__<_<Dual.µ, M>, A, B> value) {
        return (Dual) value;
    }

    public __<M, B, A> get() {
        return value;
    }

    public static <M> Category<_<µ, M>> category(final Category<M> categoryM) {
        return new CategoryAbstract<_<µ, M>>() {
            @Override
            public <A> __<_<µ, M>, A, A> identity() {
                return new Dual<M, A, A>(categoryM.<A>identity());
            }

            @Override
            public <A, B, C> __<_<µ, M>, A, C> dot(__<_<µ, M>, B, C> bc, __<_<µ, M>, A, B> ab) {
                Dual<M, B, C> bcDual = narrow(bc);
                Dual<M, A, B> abDual = narrow(ab);
                return new Dual<M, A, C>(categoryM.dot(abDual.get(), bcDual.get()));
            }
        };
    }

}
