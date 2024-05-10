package org.valross.patience;

import org.valross.patience.error.Interruption;

import java.util.concurrent.TimeUnit;

/**
 * Something that takes time and may not complete immediately.
 * Typically, a task occurring on another process.
 */
public interface Slow {

    /**
     * Blocks the current thread indefinitely until the resolution of the task.
     * This will throw any interruptions as an unexpected error.
     *
     * @return true if the task completed successfully
     * @see #await(boolean)
     */
    default boolean await() throws Interruption {
        return this.await(false);
    }

    /**
     * Blocks the current thread indefinitely until the resolution of the task.
     * @param suppressInterruption whether an interruption should be suppressed:
     *                             if so, this will continue to wait after being interrupted
     * @return true if the task completed successfully
     * @throws Interruption any unforced error that causes this to be interrupted
     */
    boolean await(boolean suppressInterruption) throws Interruption;

    /**
     * Blocks the current thread for the specified amount of time, or until the resolution of the task.
     *
     * @return true if the task completed successfully
     * @see #await(long, TimeUnit, boolean)
     */
    default boolean await(long timeout, TimeUnit unit) throws Interruption {
        return this.await(timeout, unit, false);
    }

    /**
     * Blocks the current thread for the specified amount of time, or until the resolution of the task.
     *
     * @param timeout the amount of time after which to time out
     * @param unit the units of time
     * @param suppressInterruption whether an interruption should be suppressed:
     *                             if so, this will continue to wait after being interrupted
     * @return true if the task completed successfully
     * @throws Interruption any unforced error that causes this to be interrupted
     */
    boolean await(long timeout, TimeUnit unit, boolean suppressInterruption) throws Interruption;

}
