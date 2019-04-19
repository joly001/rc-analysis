package com.zcsoft.rc.analysis.app.autoconfigure.components;

import com.zcsoft.rc.analysis.app.components.LocationComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

    @Bean("workThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor createThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        threadPoolTaskExecutor.setCorePoolSize(200);
        threadPoolTaskExecutor.setMaxPoolSize(200);
        threadPoolTaskExecutor.setThreadNamePrefix("workThreadPoolTaskExecutor-");

        return threadPoolTaskExecutor;
    }

    @Bean("locationComponent")
    public LocationComponent createLocationComponent() {
        return new LocationComponent();
    }

}
