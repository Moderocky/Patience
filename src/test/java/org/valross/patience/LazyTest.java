package org.valross.patience;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.*;

public class LazyTest extends SlowTest {

    @Test
    public void task() throws Throwable {
        final Lazy lazy = Lazy.task();
        assert !lazy.isComplete();
        final var future = SlowTest.executor.submit(lazy::complete);
        final boolean await = lazy.await();
        assert await;
        assert lazy.isComplete();
        assert future.isDone();
    }

    @Test
    public void alreadyComplete() {
        final Lazy lazy = Lazy.alreadyComplete();
        assert lazy.isComplete();
        final boolean await = lazy.await();
        assert await;
        assert lazy.isComplete();
    }

    @Test
    public void complete() {
        final Lazy lazy = Lazy.task();
        assert !lazy.isComplete();
        final AtomicBoolean completed = new AtomicBoolean();
        SlowTest.executor.submit(() -> {
            completed.set(true);
            lazy.complete();
        });
        final boolean await = lazy.await();
        assert await;
        assert lazy.isComplete();
        assert completed.get();
    }

    @Test
    public void wake() {
        final Lazy lazy = Lazy.task();
        assert !lazy.isComplete();
        final AtomicBoolean completed = new AtomicBoolean();
        SlowTest.executor.submit(() -> {
            completed.set(true);
            lazy.wake();
        });
        final boolean await = lazy.await();
        assert !await;
        assert !lazy.isComplete();
        assert completed.get();
    }

    @Test
    public void isComplete() {
        final Lazy lazy = Lazy.task();
        assert !lazy.isComplete();
        final AtomicBoolean completed = new AtomicBoolean();
        SlowTest.executor.submit(() -> {
            completed.set(true);
            lazy.complete();
        });
        final boolean await = lazy.await();
        assert await;
        assert lazy.isComplete();
        assert completed.get();
    }

    @Test
    public void await() {
        final Lazy lazy = Lazy.task();
        assert !lazy.isComplete();
        final AtomicBoolean completed = new AtomicBoolean();
        SlowTest.executor.submit(() -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
            completed.set(true);
            lazy.complete();
        });
        final boolean await = lazy.await(3, TimeUnit.MILLISECONDS);
        assert !await;
        assert !lazy.isComplete();
        assert !completed.get();
        final boolean second = lazy.await(500, TimeUnit.MILLISECONDS);
        assert second;
        assert lazy.isComplete();
        assert completed.get();
    }

}
