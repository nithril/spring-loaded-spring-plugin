package org.nigajuan.springloaded.service.deps;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nigajuan on 08/02/14.
 */
@Service
public class DepsServiceB {

    public Integer version = 1;

    @Autowired
    private DepsServiceA depsServiceA;

    @PostConstruct
    public void init(){
        depsServiceA.sayHello();
    }


}
