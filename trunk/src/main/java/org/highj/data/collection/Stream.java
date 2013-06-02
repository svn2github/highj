package org.highj.data.collection;

import org.highj._;
import org.highj.data.collection.stream.StreamMonad;
import org.highj.data.functions.Strings;
import org.highj.data.tuple.T2;
import org.highj.data.tuple.T3;
import org.highj.data.tuple.T4;
import org.highj.data.tuple.Tuple;
import org.highj.typeclass1.monad.Monad;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/*
 * An infinite list.
 */
public abstract class Stream<A> implements _<Stream.µ, A>, Iterable<A>, Function<Integer, A> {

    public static final class µ {
    }

    @SuppressWarnings("unchecked")
    public static <A> Stream<A> narrow(_<µ, A> value) {
        return (Stream) value;
    }

    private Stream() {
    }

    public abstract A head();

    public abstract Stream<A> tail();

    public A apply(Integer index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Negative index " + index);
        }
        Stream<A> current = this;
        while (index-- > 0) {
            current = current.tail();
        }
        return current.head();
    }

    public static <A> Stream<A> repeat(final A a) {
        return new Stream<A>() {

            @Override
            public A head() {
                return a;
            }

            @Override
            public Stream<A> tail() {
                return this;
            }
        };
    }

    public static <A> Stream<A> unfold(final Function<A, A> fn, final A a) {
        return new Stream<A>() {

            @Override
            public A head() {
                return a;
            }

            @Override
            public Stream<A> tail() {
                return unfold(fn, fn.apply(a));
            }
        };
    }

    public static <A> Stream<A> Cons(final A a, final Stream<A> stream) {
        return new Stream<A>() {

            @Override
            public A head() {
                return a;
            }

            @Override
            public Stream<A> tail() {
                return stream;
            }
        };
    }

    public static <A> Stream<A> Cons(final A a, final Supplier<Stream<A>> thunk) {
        return new Stream<A>() {

            @Override
            public A head() {
                return a;
            }

            @Override
            public Stream<A> tail() {
                return thunk.get();
            }
        };
    }

    //assuming that the iterator doesn't stop
    public static <A> Stream<A> fromIterator(final Iterator<A> iterator) throws NoSuchElementException {
        return Cons(iterator.next(), () -> fromIterator(iterator));
    }

    //returns iterator values wrapped in JustLazy, and Nothing when the iterator gets empty
    public static <A> Stream<Maybe<A>> maybeFromIterator(final Iterator<A> iterator) {
        return Cons(iterator.hasNext()
                ? Maybe.Just(iterator.next())
                : Maybe.<A>Nothing(),
                () -> maybeFromIterator(iterator));
    }

    public Stream<A> filter(final Function<A, Boolean> predicate) {
        final Stream<A> result = dropWhile(a -> !predicate.apply(a));
        return Cons(result.head(), () -> result.tail().filter(predicate));
    }

    public String toString(int n) {
        return Strings.mkString("Stream(", ",", "...)", this.take(n));
    }

    @Override
    public String toString() {
        return toString(10);
    }

    public List<A> take(final int n) {
        return n <= 0 ? List.<A>nil() : List.consLazy(head(), () -> tail().take(n - 1));
    }

    public List<A> takeWhile(final Function<A, Boolean> predicate) {
        return !predicate.apply(head()) ? List.<A>nil() : List.consLazy(head(), () -> tail().takeWhile(predicate));
    }

    public List<A> takeWhile(final Predicate<A> predicate) {
        return !predicate.test(head()) ? List.<A>nil() : List.consLazy(head(), () -> tail().takeWhile(predicate));
    }

    public Stream<A> drop(int n) {
        Stream<A> result = this;
        while (n-- > 0) {
            result = result.tail();
        }
        return result;
    }

    public Stream<A> dropWhile(Function<A, Boolean> predicate) {
        Stream<A> result = this;
        while (predicate.apply(result.head())) {
            result = result.tail();
        }
        return result;
    }

    public Stream<A> dropWhile(Predicate<A> predicate) {
        Stream<A> result = this;
        while (predicate.test(result.head())) {
            result = result.tail();
        }
        return result;
    }

    public Stream<List<A>> inits() {
        //inits xs = Cons [] (fmap (head xs :) (inits (tail xs)))
        return Cons(List.empty(), () -> tail().inits().map(xs -> List.cons(head(),xs)));
    }

    public Stream<Stream<A>> tails() {
        //tails xs = Cons xs (tails (tail xs))
        return Cons(this, () -> tail().tails());
    }

    public Stream<A> intersperse(A a) {
        //intersperse y ~(Cons x xs) = Cons x (Cons y (intersperse y xs))
        return Cons(head(), Cons(a, () -> tail().intersperse(a)));
    }

    public <B> Stream<B> map(final Function<? super A, ? extends B> fn) {
        return Cons(fn.apply(head()), () -> tail().map(fn));
    }

    public static Stream<Integer> range(final int from, final int step) {
        return unfold(x -> x + step, from);
    }

    public static Stream<Integer> range(final int from) {
        return range(from, 1);
    }

    @SafeVarargs
    public static <A> Stream<A> cycle(A... as) {
        if (as.length == 1) {
            return repeat(as[0]);
        } else {
            Stream<A> result = Cons(as[as.length - 1], () -> cycle(as));
            for (int i = as.length - 1; i > 0; i--) {
                result = Cons(as[i - 1], result);
            }
            return result;
        }
    }

    public static <A> Stream<A> append(_<List.µ, A> list, _<µ, A> stream) {
        final List<A> listOne = List.narrow(list);
        final Stream<A> streamTwo = narrow(stream);
        return listOne.isEmpty() ? streamTwo : Cons(listOne.head(), () -> append(listOne.tail(), streamTwo));
    }

    public static <A> Stream<A> interleave(_<µ, A> one, _<µ, A> two) {
       //interleave ~(Cons x xs) ys = Cons x (interleave ys xs)
        return Cons(narrow(one).head(), () -> interleave(two, narrow(one).tail()));
    }


    public static <A, B> Stream<T2<A, B>> zip(_<µ, A> streamA, _<µ, B> streamB) {
        return zipWith((A a) -> (B b) -> Tuple.<A, B>of(a, b), streamA, streamB);
    }

    public static <A, B, C> Stream<T3<A, B, C>> zip(_<µ, A> streamA, _<µ, B> streamB, _<µ, C> streamC) {
        return zipWith((A a) -> (B b) -> (C c) -> Tuple.<A, B, C>of(a, b, c), streamA, streamB, streamC);
    }

    public static <A, B, C, D> Stream<T4<A, B, C, D>> zip(_<µ, A> streamA, _<µ, B> streamB, _<µ, C> streamC, _<µ, D> streamD) {
        return zipWith((A a) -> (B b) -> (C c) -> (D d) -> Tuple.<A, B, C, D>of(a, b, c, d), streamA, streamB, streamC, streamD);
    }

    public static <A, B, C> Stream<C> zipWith(final Function<A, Function<B, C>> fn, _<µ, A> streamA, _<µ, B> streamB) {
        final Stream<A> sA = narrow(streamA);
        final Stream<B> sB = narrow(streamB);
        return Cons(fn.apply(sA.head()).apply(sB.head()), () -> zipWith(fn, sA.tail(), sB.tail()));
    }

    public static <A, B, C, D> Stream<D> zipWith(final Function<A, Function<B, Function<C, D>>> fn, _<µ, A> streamA, _<µ, B> streamB, _<µ, C> streamC) {
        final Stream<A> sA = narrow(streamA);
        final Stream<B> sB = narrow(streamB);
        final Stream<C> sC = narrow(streamC);
        return Cons(fn.apply(sA.head()).apply(sB.head()).apply(sC.head()), () -> zipWith(fn, sA.tail(), sB.tail(), sC.tail()));
    }

    public static <A, B, C, D, E> Stream<E> zipWith(final Function<A, Function<B, Function<C, Function<D, E>>>> fn, _<µ, A> streamA, _<µ, B> streamB, _<µ, C> streamC, _<µ, D> streamD) {
        final Stream<A> sA = narrow(streamA);
        final Stream<B> sB = narrow(streamB);
        final Stream<C> sC = narrow(streamC);
        final Stream<D> sD = narrow(streamD);

        return Cons(fn.apply(sA.head()).apply(sB.head()).apply(sC.head()).apply(sD.head()), () ->
                zipWith(fn, sA.tail(), sB.tail(), sC.tail(), sD.tail()));
    }

    public static <A, B> T2<Stream<A>, Stream<B>> unzip(Stream<T2<A, B>> streamAB) {
        return Tuple.of(streamAB.map(t -> t._1()), streamAB.map(t -> t._2()));
    }

    public static <A, B, C> T3<Stream<A>, Stream<B>, Stream<C>> unzip3(Stream<T3<A, B, C>> streamABC) {
        return Tuple.of(streamABC.map(t -> t._1()), streamABC.map(t -> t._2()), streamABC.map(t -> t._3()));
    }

    public static <A, B, C, D> T4<Stream<A>, Stream<B>, Stream<C>, Stream<D>> unzip4(Stream<T4<A, B, C, D>> streamABCD) {
        return Tuple.of(streamABCD.map(t -> t._1()), streamABCD.map(t -> t._2()), streamABCD.map(t -> t._3()), streamABCD.map(t -> t._4()));
    }

    public static final Monad<µ> monad = new StreamMonad();

    @Override
    public Iterator<A> iterator() {
        return new Iterator<A>() {

            private Stream<A> stream = Stream.this;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public A next() {
                A a = stream.head();
                stream = stream.tail();
                return a;
            }
        };
    }
}