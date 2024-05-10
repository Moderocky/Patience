package org.valross.patience.error;

import javax.naming.InterruptedNamingException;
import java.io.InterruptedIOException;
import java.nio.channels.InterruptedByTimeoutException;

/**
 * An interruption in a sleeping or busy-waiting process. Usually caused by an
 * {@link InterruptedException}.
 * <br>
 * <br>
 * <h3>Error Semantics</h3>
 * This was converted to an {@link Error} because the error semantics fit better here:
 * If a program is being interrupted <em>and that is not expected to happen</em> then
 * this interruption is an abnormal condition outside the reasonable expected bounds
 * of execution. <em>Id est</em>, it is an error rather than an exception.
 * <br>
 * <br>
 * <h3>Thread Death</h3>
 * This was specifically converted to an error to cover the removal of {@link ThreadDeath}.
 * Previously, a thread could be terminated by inserting an error on top of the stack which,
 * unless caught, would propagate downwards and exit each section to the root, cleaning any
 * resources in `finally` blocks.
 * <p>
 * Java elected to remove thread death and offered no viable replacement.
 * Many programs catch blanket {@link Exception}s (for a number of good reasons) and so
 * the {@link InterruptedException} that could cover the absent behaviour has already been
 * rendered useless: it is very likely to be silently quashed within a program and
 * leave said program in an unexpected, broken state.
 * <p>
 * To fill this gap, an interruption is an error (and an unusual, difficult-to-catch one at that)
 * which is more likely to propagate down the thread and terminate it appropriately.
 * <br>
 * <br>
 * <h3>Catching Interruptions</h3>
 * Interruptions may be caught and consumed but, if they are not caught, they are
 * treated as unrecoverable errors.
 */
@SuppressWarnings("removal")
public class Interruption extends Error {

    private final boolean wasInterrupted;

    public Interruption(Throwable cause) {
        super(cause);
        this.wasInterrupted = cause instanceof InterruptedException
            || cause instanceof InterruptedIOException
            || cause instanceof InterruptedByTimeoutException
            || cause instanceof InterruptedNamingException
            || cause instanceof ThreadDeath;
    }

    public Interruption(String message) {
        super(message);
        this.wasInterrupted = true;
    }

    public Interruption(String message, Throwable cause) {
        super(message, cause);
        this.wasInterrupted = cause instanceof InterruptedException
            || cause instanceof InterruptedIOException
            || cause instanceof InterruptedByTimeoutException
            || cause instanceof InterruptedNamingException
            || cause instanceof ThreadDeath;
    }

    public Interruption() {
        super();
        this.wasInterrupted = true;
    }

    /**
     * Most interruptions are actual interruptions (i.e. this will be true).
     * Some are also caused by unforced errors that inadvertently cause the
     * interruption of the program (e.g. a null-pointer causes a cascade failure which
     * interrupts this in order to terminate the thread), in which case this
     * will be false.
     *
     * @return whether this was triggered by an explicit interruption
     */
    public boolean wasInterrupted() {
        return wasInterrupted;
    }

}
