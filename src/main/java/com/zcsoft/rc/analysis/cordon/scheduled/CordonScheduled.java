package com.zcsoft.rc.analysis.cordon.scheduled;

import com.zcsoft.rc.analysis.cordon.service.CordonService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class CordonScheduled {

    private CordonService cordonService;

    @Resource
    public void setCordonService(CordonService cordonService) {
        this.cordonService = cordonService;
    }

    @Scheduled(fixedRate = 1000*1)
    public synchronized void analysis() {
        cordonService.analysis();
    }

}
