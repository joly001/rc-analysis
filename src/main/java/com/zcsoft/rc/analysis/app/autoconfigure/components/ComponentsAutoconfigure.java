package com.zcsoft.rc.analysis.app.autoconfigure.components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ComponentsAutoconfigure {

    @Bean("taskScheduler")
    public TaskScheduler createTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(100);
        threadPoolTaskScheduler.setThreadNamePrefix("taskScheduler-");
        return threadPoolTaskScheduler;
    }

}
