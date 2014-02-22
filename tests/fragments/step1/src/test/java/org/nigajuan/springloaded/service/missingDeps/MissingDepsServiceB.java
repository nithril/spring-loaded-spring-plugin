package org.nigajuan.springloaded.service.missingDeps;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by nigajuan on 08/02/14.
 */
@Service
@Scope
public class MissingDepsServiceB implements MissingDepsB {

    public Integer version = 1;

    @Autowired
    private MissingDepsServiceA missingDepsServiceA;

    @PostConstruct
    public void init(){
        missingDepsServiceA.sayHello();
    }


}
