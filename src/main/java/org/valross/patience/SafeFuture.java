package org.valross.patience;

import org.valross.patience.error.Interruption;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A safe kind of future that consumes its termination or interruption.
 */
public interface SafeFuture<Result> extends Future<Result>, Slow {

    @Override
    Result get(long timeout, TimeUnit unit) throws Interruption, TimeoutException;

    @Override
    Result get() throws Interruption;

    @Override
    default boolean await(boolean suppressInterruption) throws Interruption {
        do try {
            if (suppressInterruption) try {
                this.get();
                return true;
            } catch (Interruption _) {
            } else {
                this.get();
                return true;
            }
        } catch (CancellationException ce) {
            return false;
        } while (true);
    }

    @Override
    default boolean await(long timeout, TimeUnit unit, boolean suppressInterruption) throws Interruption {
        final long end = System.currentTimeMillis() + unit.toMillis(timeout);
        do try {
            if (suppressInterruption) try {
                this.get(end - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                return true;
            } catch (Interruption _) {
            } else {
                this.get(timeout, unit);
                return true;
            }
        } catch (CancellationException | TimeoutException ce) {
            return false;
        } while (System.currentTimeMillis() < end);
        return false;
    }

}
