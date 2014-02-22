package org.nigajuan.springloaded.configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.nigajuan.springloaded.FilesystemWatcher;
import org.nigajuan.springloaded.ReloadPlugin;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by nigajuan on 13/02/14.
 */
@Configuration
@ComponentScan("org.nigajuan")
@EnableScheduling
public class SpringConfiguration implements ApplicationContextAware{

    private ApplicationContext applicationContext;

    private FilesystemWatcher watcher;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext =  applicationContext;
    }

    @PostConstruct
    public void init() throws URISyntaxException, IOException {
        ReloadPlugin.register((ConfigurableApplicationContext) applicationContext);
        watcher = new FilesystemWatcher(Paths.get("target/test-classes"));
        watcher.init();


    }

    @Scheduled(fixedDelay = 2000)
    public void scan() throws IOException {
        watcher.scan();
    }
}
