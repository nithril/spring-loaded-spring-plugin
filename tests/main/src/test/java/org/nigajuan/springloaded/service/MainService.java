package org.nigajuan.springloaded.service;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;

import org.nigajuan.springloaded.ReloadPlugin;
import org.nigajuan.springloaded.StateHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springsource.loaded.TypeRegistry;

/**
 * Created by nigajuan on 08/02/14.
 */
@Service
public class MainService {

    @PostConstruct
    public void init(){

    }

    public void hello(){
        StateHolder.BLOCKING_QUEUE.offer(StateHolder.STEP2);
    }

}
