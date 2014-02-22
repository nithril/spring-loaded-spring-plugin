package org.nigajuan.springloaded;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springsource.loaded.Plugins;
import org.springsource.loaded.ReloadEventProcessorPlugin;

/**
 * Automatically reloads Spring Beans when Spring Loaded triggers a hot reload event.
 *
 * <p>
 * To have Spring Loaded working, run your Application class with these VM options:
 * "-javaagent:spring_loaded/springloaded-1.1.5-dev.jar -noverify"
 * </p>
 */
public class ReloadPlugin implements ReloadEventProcessorPlugin, Callable<Object> {

    private static final Logger log = LoggerFactory.getLogger(ReloadPlugin.class);

    private static ConfigurableApplicationContext applicationContext;

    private Debouncer debouncer = new Debouncer(this, 2000);

    private Object lock = new Object();

    private Set<ToReloadBean> toReloadBeans = new HashSet<>();

    @Override
    public boolean shouldRerunStaticInitializer(String typename, Class<?> aClass, String encodedTimestamp) {
        return false;
    }

    public void reloadEvent(String typename, Class<?> clazz, String encodedTimestamp) {
        toReloadBeans.add(new ToReloadBean(typename , clazz , encodedTimestamp));
        debouncer.call("debounce");
    }

    @Override
    public synchronized Object call() throws Exception {
        Set<ToReloadBean> toReloadBeansCopy = new HashSet<>(toReloadBeans);
        toReloadBeans = new HashSet<>();

        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();

        List<ToReloadBean> newSpringBeans = new ArrayList<>();
        List<ToReloadBean> existingSpringBeans = new ArrayList<>();

        //1) Split between new/existing beans
        for (ToReloadBean toReloadBean : toReloadBeansCopy) {
            Annotation annotation = getSpringClassAnnotation(toReloadBean.getClazz());
            if (annotation != null){
                String beanName = constructBeanName(annotation , toReloadBean.getClazz());

                RootBeanDefinition beanDefinition = null;
                try {
                    beanFactory.getBeanDefinition(beanName);
                } catch (NoSuchBeanDefinitionException e) {
                    //not registered
                    log.error(e.getMessage() , e);
                }

                if (beanDefinition == null) {
                    newSpringBeans.add(toReloadBean);
                }else{
                    existingSpringBeans.add(toReloadBean);
                }
            }
        }

        //2) Declare new beans prior to instanciation for cross bean references
        for (ToReloadBean toReloadBean : newSpringBeans) {
            Annotation annotation = getSpringClassAnnotation(toReloadBean.getClazz());
            String beanName = constructBeanName(annotation , toReloadBean.getClazz());
            String scope = getScope(toReloadBean.getClazz());
            RootBeanDefinition bd = new RootBeanDefinition(toReloadBean.getClazz(), AbstractBeanDefinition.AUTOWIRE_BY_TYPE, true);
            bd.setScope(scope);
            beanFactory.registerBeanDefinition(beanName, bd);
        }

        //3) Instanciate new beans
        for (ToReloadBean toReloadBean : newSpringBeans) {
            Annotation annotation = getSpringClassAnnotation(toReloadBean.getClazz());
            String beanName = constructBeanName(annotation , toReloadBean.getClazz());
            try {
                beanFactory.getBean(beanName);
            } catch (BeansException e) {
                //buggy bean, try later
                log.error(e.getMessage(), e);
                toReloadBeans.add(toReloadBean);
            }
        }

        //4) Resolve deps for existing beans
        for (ToReloadBean toReloadBean : existingSpringBeans) {
            Object beanInstance = applicationContext.getBean(toReloadBean.getClazz());

            log.debug("Existing bean, autowiring fields"); // We only support autowiring on fields
            if (AopUtils.isCglibProxy(beanInstance)) {
                log.trace("This is a CGLIB proxy, getting the real object");
                beanInstance = ((Advised) beanInstance).getTargetSource().getTarget();
            } else if (AopUtils.isJdkDynamicProxy(beanInstance)) {
                log.trace("This is a JDK proxy, getting the real object");
                beanInstance = ((Advised) beanInstance).getTargetSource().getTarget();
            }
            Field[] fields = beanInstance.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                    log.debug("@Inject annotation found on field {}", field.getName());
                    Object beanToInject = applicationContext.getBean(field.getType());
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, beanInstance, beanToInject);
                }
            }
        }

        return null;
    }

    private Annotation getSpringClassAnnotation(Class clazz){
        Annotation classAnnotation = AnnotationUtils.findAnnotation(clazz, Component.class);

        if (classAnnotation == null) {
            classAnnotation = AnnotationUtils.findAnnotation(clazz, Controller.class);
        }
        if (classAnnotation == null) {
            classAnnotation = AnnotationUtils.findAnnotation(clazz, Service.class);
        }
        if (classAnnotation == null) {
            classAnnotation = AnnotationUtils.findAnnotation(clazz, Repository.class);
        }

        return classAnnotation;
    }

    private String getScope(Class clazz){
        String scope = ConfigurableBeanFactory.SCOPE_SINGLETON;
        Annotation scopeAnnotation = AnnotationUtils.findAnnotation(clazz, Scope.class);
        if (scopeAnnotation != null){
            scope = (String) AnnotationUtils.getValue(scopeAnnotation);
        }
        return scope;
    }

    private String constructBeanName(Annotation annotation , Class clazz){
        String beanName = (String)AnnotationUtils.getValue(annotation);
        if (beanName == null || beanName.isEmpty()){
            beanName = StringUtils.uncapitalize(clazz.getSimpleName());
        }
        return beanName;
    }

    public static void register(ConfigurableApplicationContext ctx) {
        log.trace("Registering JHipster hot reloading plugin - your Spring Beans should be automatically reloaded!");
        ReloadPlugin.applicationContext = ctx;
        Plugins.registerGlobalPlugin(new ReloadPlugin());
    }




}