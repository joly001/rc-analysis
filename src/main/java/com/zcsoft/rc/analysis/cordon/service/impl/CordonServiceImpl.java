package com.zcsoft.rc.analysis.cordon.service.impl;

import com.zcsoft.rc.analysis.cordon.dao.CordonDAO;
import com.zcsoft.rc.analysis.cordon.service.CordonService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CordonServiceImpl implements CordonService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private CordonDAO cordonDAO;

    private WorkWarningService workWarningService;

    @Resource
    public void setCordonDAO(CordonDAO cordonDAO) {
        this.cordonDAO = cordonDAO;
    }
    @Resource
    public void setWorkWarningService(WorkWarningService workWarningService) {
        this.workWarningService = workWarningService;
    }

    @Override
    public void analysis(CurrentRcRsp rcRsp) {
        String id = cordonDAO.near("geometry",rcRsp.getLongitude(),rcRsp.getLatitude(),1.2,0);

        if(id != null) {
            workWarningService.addCordonWarning(rcRsp.getId() ,rcRsp.getType(), rcRsp.getLongitude(), rcRsp.getLatitude());
        }
    }
}
