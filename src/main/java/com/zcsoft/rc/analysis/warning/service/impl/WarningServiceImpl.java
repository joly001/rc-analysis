package com.zcsoft.rc.analysis.warning.service.impl;

import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
import com.zcsoft.rc.analysis.warning.service.WarningService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.mileage.model.entity.WorkSegmentDataTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class WarningServiceImpl implements WarningService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isOpen = false;

    private WorkWarningService workWarningService;
    private WorkSegmentService workSegmentService;

    @Resource
    public void setWorkWarningService(WorkWarningService workWarningService) {
        this.workWarningService = workWarningService;
    }
    @Resource
    public void setWorkSegmentService(WorkSegmentService workSegmentService) {
        this.workSegmentService = workSegmentService;
    }

    @Override
    public boolean isOpen() {
        logger.info("warnig status isOpen:{}",isOpen);

        return isOpen;
    }

    protected void close() {
        logger.info("warnig status is close");

        isOpen = false;

        workWarningService.finishAll();
    }

    protected void open() {
        isOpen = true;
    }

    @Override
    public void setWarningOpenStatus() {

        List<WorkSegment> workSegmentList = workSegmentService.getWorkSegmentListCache();

        if(workSegmentList == null || workSegmentList.isEmpty()) {
            close();
            return;
        }

        Date currentDateTime = new Date();
        for(WorkSegment workSegment : workSegmentList) {
            WorkSegmentDataTime queryWorkSegmentDataTime = new WorkSegmentDataTime();
            queryWorkSegmentDataTime.setWorkSegmentId(workSegment.getId());

            List<WorkSegmentDataTime> workSegmentDataTimeList = workSegment.getWorkSegmentDataTimeList();

            if(workSegmentDataTimeList ==  null || workSegmentDataTimeList.isEmpty()) {
                continue;
            }

            for(WorkSegmentDataTime workSegmentDataTime : workSegmentDataTimeList) {
                if(currentDateTime.after(workSegmentDataTime.getStartworkTime())
                        && currentDateTime.before(workSegmentDataTime.getEndWorkTime())
                ) {
                    open();
                    return;
                }
            }
        }

        close();
    }

}
