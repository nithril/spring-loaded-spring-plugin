package org.nigajuan.springloaded.service.missingDeps;

import javax.annotation.PostConstruct;

import org.nigajuan.springloaded.StateHolder;
import org.springframework.stereotype.Service;

/**
 * Created by nigajuan on 08/02/14.
 */
public class MissingDepsServiceA implements MissingDepsA {

    public Integer version = 1;

    @PostConstruct
    public void init(){
    }


    public void sayHello() {
        StateHolder.BLOCKING_QUEUE.offer(STEP1);
    }
}
