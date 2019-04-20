package com.zcsoft.rc.analysis.machinery.scheduled;

import com.zcsoft.rc.analysis.machinery.service.MachineryService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class MachineryScheduled {

    private MachineryService machineryService;

    @Resource
    public void setMachineryService(MachineryService machineryService) {
        this.machineryService = machineryService;
    }

    @Scheduled(fixedRate = 1000*60*5)
    public synchronized void setMachineryCache() {
        machineryService.setMachineryCache();
    }

}
