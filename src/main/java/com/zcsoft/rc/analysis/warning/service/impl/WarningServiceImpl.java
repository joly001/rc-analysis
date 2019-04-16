package com.zcsoft.rc.analysis.warning.service.impl;

import com.zcsoft.rc.analysis.warning.service.WarningService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.mileage.dao.WorkSegmentDAO;
import com.zcsoft.rc.mileage.dao.WorkSegmentDataTimeDAO;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.mileage.model.entity.WorkSegmentDataTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.*;
import java.util.Date;
import java.util.List;

@Service
public class WarningServiceImpl implements WarningService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isOpen = false;

    private WorkSegmentDAO workSegmentDAO;
    private WorkSegmentDataTimeDAO workSegmentDataTimeDAO;
    private WorkWarningService workWarningService;

    @Resource
    public void setWorkSegmentDAO(WorkSegmentDAO workSegmentDAO) {
        this.workSegmentDAO = workSegmentDAO;
    }
    @Resource
    public void setWorkSegmentDataTimeDAO(WorkSegmentDataTimeDAO workSegmentDataTimeDAO) {
        this.workSegmentDataTimeDAO = workSegmentDataTimeDAO;
    }
    @Resource
    public void setWorkWarningService(WorkWarningService workWarningService) {
        this.workWarningService = workWarningService;
    }

    @Override
    public boolean isOpen() {
        logger.info("warnig status is close");

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

        LocalDate nowDate = LocalDate.now();
        LocalDateTime beginLocalDateTime = LocalDateTime.of(nowDate, LocalTime.MIN);
        LocalDateTime endLocalDateTime = LocalDateTime.of(nowDate,LocalTime.MAX);

        ZoneId zone = ZoneId.systemDefault();
        Instant beginInstant = beginLocalDateTime.atZone(zone).toInstant();
        Instant endInstant = endLocalDateTime.atZone(zone).toInstant();

        Date beginDateTime = Date.from(beginInstant);
        Date endDateTime = Date.from(endInstant);


        List<WorkSegment> workSegmentList = workSegmentDAO.queryListByWorkDate(beginDateTime, endDateTime);

        if(workSegmentList == null || workSegmentList.isEmpty()) {
            close();
            return;
        }

        Date currentDateTime = new Date();
        for(WorkSegment workSegment : workSegmentList) {
            WorkSegmentDataTime queryWorkSegmentDataTime = new WorkSegmentDataTime();
            queryWorkSegmentDataTime.setWorkSegmentId(workSegment.getId());

            List<WorkSegmentDataTime> workSegmentDataTimeList = workSegmentDataTimeDAO.queryList(queryWorkSegmentDataTime);

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
