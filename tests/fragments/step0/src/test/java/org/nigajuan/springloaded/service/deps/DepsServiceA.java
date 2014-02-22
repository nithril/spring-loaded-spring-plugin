package org.nigajuan.springloaded.service.deps;

import javax.annotation.PostConstruct;

import org.nigajuan.springloaded.StateHolder;
import org.springframework.stereotype.Service;

/**
 * Created by nigajuan on 08/02/14.
 */
@Service
public class DepsServiceA implements Deps {

    public Integer version = 1;

    @PostConstruct
    public void init(){
    }


    public void sayHello() {
        StateHolder.BLOCKING_QUEUE.offer(STEP1);
    }
}
