package com.zcsoft.rc.analysis.warning.scheduled;

import com.zcsoft.rc.analysis.warning.service.WarningService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class WarningScheduled {

    private WarningService warningService;

    @Resource
    public void setWarningService(WarningService warningService) {
        this.warningService = warningService;
    }

    @Scheduled(fixedRate = 1000*60*1)
    public synchronized void setWarningOpenStatus() {
        warningService.setWarningOpenStatus();
    }
}
