package org.valross.patience;

import org.valross.patience.error.Interruption;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A flag-only implementation of a {@link Slow} object.
 * This is designed to be used as a marker for testing (and awaiting) the completeness of a task.
 */
public abstract class Lazy implements Slow {

    protected transient final Semaphore sleeper = new Semaphore(0);
    protected volatile boolean complete = false;

    /**
     * @return an incomplete simple lazy task
     */
    public static Lazy task() {
        return new Simple(false);
    }

    /**
     * @return a simple lazy task with an initial completeness value
     */
    public static Lazy task(boolean complete) {
        return new Simple(complete);
    }

    /**
     * @return a simple lazy task that has already completed
     */
    public static Lazy alreadyComplete() {
        return new Simple(true);
    }

    /**
     * Marks this task as having been completed and wakes anything
     * pending its resolution.
     */
    public void complete() {
        this.complete = true;
        this.wake();
    }

    /**
     * Anything waiting for the resolution of this task will stop waiting.
     * Nothing will wait for this task subsequently.
     */
    protected void wake() {
        this.sleeper.release(Short.MAX_VALUE);
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public boolean await(boolean suppressInterruption) throws Interruption {
        if (complete) return true;
        if (suppressInterruption)
            this.sleeper.acquireUninterruptibly();
        else try {
            this.sleeper.acquire();
        } catch (InterruptedException e) {
            this.consumeError(e);
            throw new Interruption(e);
        }
        return complete;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit, boolean suppressInterruption)
        throws Interruption {
        if (complete) return true;
        if (suppressInterruption) {
            final long end = System.currentTimeMillis() + unit.toMillis(timeout);
            do try {
                return this.sleeper.tryAcquire(end - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    && complete;
            } catch (InterruptedException _) {
            } while (System.currentTimeMillis() < end);
        } else try {
            return this.sleeper.tryAcquire(timeout, unit) && complete;
        } catch (InterruptedException e) {
            this.consumeError(e);
            throw new Interruption(e);
        }
        return complete;
    }

    protected void consumeError(Throwable throwable) {
    }

    private static class Simple extends Lazy {

        public Simple(boolean complete) {
            if (complete) this.complete();
        }

    }

}
