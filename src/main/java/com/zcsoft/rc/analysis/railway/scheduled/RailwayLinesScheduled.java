package com.zcsoft.rc.analysis.railway.scheduled;

import com.zcsoft.rc.analysis.railway.service.RailwayLinesService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class RailwayLinesScheduled {

    private RailwayLinesService railwayLinesService;

    @Resource
    public void setRailwayLinesService(RailwayLinesService railwayLinesService) {
        this.railwayLinesService = railwayLinesService;
    }

    @Scheduled(fixedRate = 1000*1)
    public synchronized void setWarningRailwayLinesListCache() {
        railwayLinesService.setWarningRailwayLinesListCache();
    }

    @Scheduled(fixedRate = 1000*5)
    public synchronized void decideDirection() {
        railwayLinesService.decideDirection();
    }

}
