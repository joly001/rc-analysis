package com.zcsoft.rc.analysis.cordon.service.impl;

import com.zcsoft.rc.analysis.cordon.dao.CordonDAO;
import com.zcsoft.rc.analysis.cordon.service.CordonService;
import com.zcsoft.rc.analysis.sys.service.SysParameterService;
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
    private SysParameterService sysParameterService;

    @Resource
    public void setCordonDAO(CordonDAO cordonDAO) {
        this.cordonDAO = cordonDAO;
    }
    @Resource
    public void setWorkWarningService(WorkWarningService workWarningService) {
        this.workWarningService = workWarningService;
    }
    @Resource
    public void setSysParameterService(SysParameterService sysParameterService) {
        this.sysParameterService = sysParameterService;
    }

    @Override
    public void analysis(CurrentRcRsp rcRsp) {
        String nearDataId = cordonDAO.near("geometry",rcRsp.getLongitude(),rcRsp.getLatitude(),sysParameterService.getCordon(),0);

        if(nearDataId == null) {
            workWarningService.finishCordonWarning(rcRsp.getId());
        } else {
            workWarningService.addCordonWarning(rcRsp.getId() ,rcRsp.getType(), rcRsp.getLongitude(), rcRsp.getLatitude());
        }
    }
}
