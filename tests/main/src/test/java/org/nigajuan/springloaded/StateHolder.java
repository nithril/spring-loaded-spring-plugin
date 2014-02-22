package org.nigajuan.springloaded;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nigajuan on 13/02/14.
 */
public class StateHolder {


    public static final AtomicInteger INTEGER = new AtomicInteger();

    public static final BlockingQueue<String> BLOCKING_QUEUE = new ArrayBlockingQueue<>(10);

    public static final String STEP1 = "STEP1";
    public static final String STEP2 = "STEP2";
}
