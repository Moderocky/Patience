package org.valross.patience;

import org.valross.patience.error.Interruption;

import java.util.concurrent.TimeUnit;

/**
 * A patient thing is a thing that waits for resolution.
 *
 * @param <Type>
 */
public class Patient<Type> extends Lazy implements Slow, SafeFuture<Type> {

    protected volatile boolean cancelled = false;
    protected volatile Type value;
    protected volatile Throwable error;

    public Patient() {
    }

    public Patient(Type value) {
        this();
        this.value = value;
    }

    public void cancel() {
        this.cancelled = true;
        this.wake();
    }

    /**
     * Stores the given value in this reference.
     * Marks this task as having been completed and wakes anything
     * pending its resolution.
     */
    public void complete(Type value) {
        this.value = value;
        this.complete();
    }

    @Override
    public void complete() {
        this.cancelled = false;
        super.complete();
    }

    /**
     * If something caused this task to complete exceptionally, the causing error will be available here.
     * This may be different from what (if anything) is thrown during a get or await method.
     *
     * @param <Issue> the error parameter scope (for exception checking)
     * @return the error
     */
    public <Issue extends Throwable> Issue error() {
        //noinspection unchecked
        return (Issue) error;
    }

    /**
     * Throws the error associated with this patient task.
     *
     * @param <Issue> the error parameter scope (for exception checking)
     * @throws Issue the expected error candidate type
     */
    public <Issue extends Throwable> void throwError() throws Issue {
        final Issue issue = this.error();
        if (issue != null) throw issue;
    }

    /**
     * Patient objects do not support a concept of 'failure' by default:
     * they are either complete or pending (or cancelled). Failure would be considered complete
     * if a value was resolved, or cancelled.
     *
     * @return whether this failed to complete.
     */
    public boolean failed() {
        return this.isCancelled();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean await(boolean suppressInterruption) throws Interruption {
        if (cancelled) return false;
        return super.await(suppressInterruption);
    }

    @Override
    public boolean await(long timeout, TimeUnit unit, boolean suppressInterruption)
        throws Interruption {
        if (cancelled) return false;
        return super.await(timeout, unit, suppressInterruption);
    }

    @Override
    public Type get(long timeout, TimeUnit unit) throws Interruption {
        try {
            this.await(timeout, unit);
        } catch (Interruption interruption) {
            throw interruption;
        } catch (Throwable throwable) {
            this.error = throwable;
            this.throwError();
        }
        return value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.cancel();
        return this.isCancelled();
    }

    @Override
    public final boolean isDone() {
        return this.isComplete();
    }

    @Override
    public Type get() throws Interruption {
        return this.get(false);
    }

    public Type get(boolean suppressInterruption) throws Interruption {
        try {
            this.await(suppressInterruption);
        } catch (Interruption interruption) {
            throw interruption;
        } catch (Throwable throwable) {
            this.error = throwable;
            this.throwError();
        }
        return value;
    }

    @Override
    public State state() {
        if (this.isComplete()) return State.SUCCESS;
        if (this.isCancelled()) return State.CANCELLED;
        return State.RUNNING;
    }

    @Override
    public Type resultNow() {
        return value;
    }

    public static Patient<?> empty() {
        return new Patient<>();
    }

    public static <Thing> Patient<Thing> expecting(Class<Thing> type) {
        return new Patient<>();
    }

    public static <Thing> Patient<Thing> withInitial(Thing value) {
        return new Patient<>(value);
    }

    public static <Thing> Patient<Thing> expectingFailSafe(Class<Thing> type) {
        return new FailurePermissivePatient<>();
    }

    public static <Thing> Patient<Thing> withInitialFailSafe(Thing value) {
        return new FailurePermissivePatient<>(value);
    }

}

class FailurePermissivePatient<Type> extends Patient<Type> {

    public FailurePermissivePatient() {
    }

    public FailurePermissivePatient(Type value) {
        super(value);
    }

    @Override
    public boolean failed() {
        return error != null;
    }

    @Override
    public State state() {
        if (this.isComplete()) return State.SUCCESS;
        if (this.isCancelled()) return State.CANCELLED;
        if (this.failed()) return State.FAILED;
        return State.RUNNING;
    }

}
