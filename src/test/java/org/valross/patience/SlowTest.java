package org.valross.patience;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SlowTest {

    protected static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

}
