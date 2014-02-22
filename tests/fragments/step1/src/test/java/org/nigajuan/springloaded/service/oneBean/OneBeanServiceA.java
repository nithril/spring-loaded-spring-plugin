package org.nigajuan.springloaded.service.oneBean;

import javax.annotation.PostConstruct;

import org.nigajuan.springloaded.StateHolder;
import org.springframework.stereotype.Service;

/**
 * Created by nigajuan on 08/02/14.
 */
@Service
public class OneBeanServiceA implements OneBean {

    public Integer version = 2;

    @PostConstruct
    public void init(){
        StateHolder.BLOCKING_QUEUE.offer(STEP1);
    }
}
