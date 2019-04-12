package com.zcsoft.rc.analysis.warning.service.impl;

import com.sharingif.cube.components.json.IJsonService;
import com.zcsoft.rc.analysis.warning.service.WarningService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.collectors.api.warning.entity.WarningCollectReq;
import com.zcsoft.rc.collectors.api.warning.entity.WarningDeleteReq;
import com.zcsoft.rc.collectors.api.warning.service.WarningApiService;
import com.zcsoft.rc.mileage.dao.WorkSegmentDAO;
import com.zcsoft.rc.mileage.dao.WorkSegmentDataTimeDAO;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.mileage.model.entity.WorkSegmentDataTime;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WarningServiceImpl implements WarningService, ApplicationContextAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isOpen = false;
    private Map<String, String> warningMap = new ConcurrentHashMap<>(200);

    private WorkSegmentDAO workSegmentDAO;
    private WorkSegmentDataTimeDAO workSegmentDataTimeDAO;
    private WorkWarningService workWarningService;
    private WarningApiService warningApiService;
    private IJsonService jsonService;
    private ApplicationContext applicationContext;


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
    @Resource
    public void setWarningApiService(WarningApiService warningApiService) {
        this.warningApiService = warningApiService;
    }
    @Resource
    public void setJsonService(IJsonService jsonService) {
        this.jsonService = jsonService;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    protected void close() {
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

    @Override
    public void addWarning(String id, WorkWarning workWarning) {
        warningMap.put(id,id);

        try {

            String builderUserType = applicationContext.getMessage(workWarning.getBuilderUserType(), null, Locale.CHINESE);
            String waringContent = applicationContext.getMessage("waring.content", new String[]{builderUserType, workWarning.getNick()}, Locale.CHINESE);

            Map<String, Object> waring = new HashMap<>();
            waring.put("workSegmentStartLongitude", workWarning.getWorkSegmentStartLongitude());
            waring.put("workSegmentStartLatitude", workWarning.getWorkSegmentStartLatitude());
            waring.put("workSegmentEndLongitude", workWarning.getWorkSegmentEndLongitude());
            waring.put("workSegmentEndLatitude", workWarning.getWorkSegmentEndLatitude());

            waring.put("userId", workWarning.getUserId());
            waring.put("nick", workWarning.getNick());
            waring.put("mobile", workWarning.getMobile());
            waring.put("waringContent", waringContent);

            String waringJson = jsonService.objectoJson(waring);

            WarningCollectReq req = new WarningCollectReq();
            req.setId(id);
            req.setWarning(waringJson);


            warningApiService.collect(req);
        }catch (Exception e) {
            logger.error("collect waring error", e);
        }
    }

    @Override
    public void removeWarning(String id) {
        warningMap.remove(id);

        try {
            WarningDeleteReq req = new WarningDeleteReq();
            req.setId(id);

            warningApiService.delete(req);
        } catch (Exception e) {
            logger.error("remove warning error", e);
        }
    }

    @Override
    public String getWarning(String id) {
        return warningMap.get(id);
    }


}
