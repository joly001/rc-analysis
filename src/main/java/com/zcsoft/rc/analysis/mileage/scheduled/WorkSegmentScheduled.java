package com.zcsoft.rc.analysis.mileage.scheduled;


import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class WorkSegmentScheduled {

    private WorkSegmentService workSegmentService;

    @Resource
    public void setWorkSegmentService(WorkSegmentService workSegmentService) {
        this.workSegmentService = workSegmentService;
    }

    @Scheduled(fixedRate = 1000*60*5)
    public synchronized void setWorkingWorkSegmentListCache() {
        workSegmentService.setWorkingWorkSegmentListCache();
    }
}
