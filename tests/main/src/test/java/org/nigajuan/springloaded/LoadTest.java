package org.nigajuan.springloaded;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.nigajuan.springloaded.configuration.SpringConfiguration;
import org.nigajuan.springloaded.service.deps.Deps;
import org.nigajuan.springloaded.service.missingDeps.MissingDepsA;
import org.nigajuan.springloaded.service.missingDeps.MissingDepsB;
import org.nigajuan.springloaded.service.oneBean.OneBean;
import org.nigajuan.springloaded.service.MainService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by nigajuan on 08/02/14.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SpringConfiguration.class)
public class LoadTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MainService reloadService;

    private URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    private Method addURL;

    @BeforeClass
    public void init() {
        addURL = ReflectionUtils.findMethod(URLClassLoader.class, "addURL", URL.class);
        addURL.setAccessible(true);
    }

    @Test(groups = "testStep0")
    public void testStep0() throws InterruptedException, IOException {
        FileUtils.copyDirectory(
                new File("../fragments/step0/target/test-classes/org/nigajuan/springloaded/service/"),
                new File("target/test-classes/org/nigajuan/springloaded/service/"));
    }


    @Test(groups = "testStep0", dependsOnMethods = "testStep0", expectedExceptions = NoSuchBeanDefinitionException.class)
    public void testStep0_OneBean() throws InterruptedException, IOException {
        Class<?> aClass = null;

        while (aClass == null) {
            try {
                Thread.sleep(250);
                aClass = urlClassLoader.getClass().forName("org.nigajuan.springloaded.service.oneBean.OneBeanServiceA");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Assert.assertNull(this.applicationContext.getBean(OneBean.class));
    }


    @Test(groups = "testStep0", dependsOnMethods = "testStep0")
    public void testStep0_Deps() throws InterruptedException, IOException {
        String state = StateHolder.BLOCKING_QUEUE.poll(3000000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(state, Deps.STEP1);
    }


    @Test(groups = "testStep1", dependsOnGroups = "testStep0")
    public void testStep1() throws InterruptedException, IOException {
        FileUtils.copyDirectory(
                new File("../fragments/step1/target/test-classes/org/nigajuan/springloaded/service/"),
                new File("target/test-classes/org/nigajuan/springloaded/service/"));

        //Thread.sleep(1000000000);
    }


    @Test(groups = "testStep1", dependsOnMethods = "testStep1")
    public void testStep1_Deps() throws InterruptedException, IOException {
        String state = StateHolder.BLOCKING_QUEUE.poll(3000000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(state, OneBean.STEP1);
        Assert.assertNotNull(this.applicationContext.getBean(OneBean.class));
    }

    @Test(groups = "testStep1", dependsOnMethods = "testStep1", expectedExceptions = NoSuchBeanDefinitionException.class)
    public void testStep1_MissingDeps() throws InterruptedException, IOException {
        Class<?> aClass = null;

        while (aClass == null) {
            try {
                Thread.sleep(250);
                aClass = urlClassLoader.getClass().forName("org.nigajuan.springloaded.service.missingDeps.MissingDepsServiceB");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Assert.assertNull(this.applicationContext.getBean(MissingDepsA.class));
    }



    @Test(groups = "testStep2", dependsOnGroups = "testStep1")
    public void testStep2() throws InterruptedException, IOException {
        FileUtils.copyDirectory(
                new File("../fragments/step2/target/test-classes/org/nigajuan/springloaded/service/"),
                new File("target/test-classes/org/nigajuan/springloaded/service/"));

        //Thread.sleep(1000000000);
    }


    @Test(groups = "testStep2", dependsOnMethods = "testStep2")
    public void testStep2_MissingDeps() throws InterruptedException, IOException {
        String state = StateHolder.BLOCKING_QUEUE.poll(3000000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(state, MissingDepsA.STEP1);
        Assert.assertNotNull(this.applicationContext.getBean(MissingDepsA.class));
        Assert.assertNotNull(this.applicationContext.getBean(MissingDepsB.class));
    }

/*
    @Test(dependsOnMethods = "testInitialStep")
    public void testAddNewService() throws InterruptedException, IOException {
        FileUtils.copyFile(
                new File("../fragments/step1/target/test-classes/org/nigajuan/springloaded/service/NewService.class"),
                new File("target/test-classes/org/nigajuan/springloaded/service/NewService.class"));

        String state = StateHolder.BLOCKING_QUEUE.poll(3000000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(state, StateHolder.STEP1);
    }

    @Test(dependsOnMethods = "testAddNewService")
    public void testInject() throws InterruptedException, IOException {
        FileUtils.copyFile(
                new File("../fragments/step2/target/test-classes/org/nigajuan/springloaded/service/NewService.class"),
                new File("target/test-classes/org/nigajuan/springloaded/service/NewService.class"));

        while (StateHolder.BLOCKING_QUEUE.size() == 0) {
            Thread.sleep(200);
            try {
                this.applicationContext.getBean(OneBean.class).sayHelloToMainService();
            } catch (Exception e) {
            }
        }

        String state = StateHolder.BLOCKING_QUEUE.poll(3000000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(state, StateHolder.STEP2);
    }

*/
    private void addToCl(URL url) throws InvocationTargetException, IllegalAccessException {
        addURL.invoke(urlClassLoader, url);
    }


}
