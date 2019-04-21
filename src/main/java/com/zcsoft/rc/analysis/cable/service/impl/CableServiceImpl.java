package com.zcsoft.rc.analysis.cable.service.impl;

import com.sharingif.cube.core.util.StringUtils;
import com.zcsoft.rc.analysis.cable.dao.CableDAO;
import com.zcsoft.rc.analysis.cable.service.CableService;
import com.zcsoft.rc.analysis.machinery.service.MachineryService;
import com.zcsoft.rc.analysis.sys.service.SysParameterService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.machinery.model.entity.Machinery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CableServiceImpl implements CableService {

    private CableDAO cableDAO;

    private SysParameterService sysParameterService;
    private MachineryService machineryService;
    private WorkWarningService workWarningService;

    @Resource
    public void setCableDAO(CableDAO cableDAO) {
        this.cableDAO = cableDAO;
    }
    @Resource
    public void setSysParameterService(SysParameterService sysParameterService) {
        this.sysParameterService = sysParameterService;
    }
    @Resource
    public void setMachineryService(MachineryService machineryService) {
        this.machineryService = machineryService;
    }
    @Resource
    public void setWorkWarningService(WorkWarningService workWarningService) {
        this.workWarningService = workWarningService;
    }

    @Override
    public void analysis(CurrentRcRsp currentRcRsp) {

        Machinery machinery = machineryService.getMachinery(currentRcRsp.getId());

        if(machinery == null) {
            return;
        }

        String typeRollingAlarmMachinery = sysParameterService.getTypeRollingAlarmMachinery();

        if(!typeRollingAlarmMachinery.equals(machinery.getMachineryType())) {
            return;
        }

        double workRadius = machinery.getWorkRadius()/100;

        int rollingLimitDistance = sysParameterService.getRollingLimitDistance();

        double maxDistance = rollingLimitDistance-workRadius;

        if(maxDistance<0) {
            maxDistance = 0;
        }


        String nearDataId = cableDAO.near("geometry",currentRcRsp.getLongitude(),currentRcRsp.getLatitude(),maxDistance,0);

        if(StringUtils.isTrimEmpty(nearDataId)) {
            workWarningService.finishCableWarning(currentRcRsp.getId());
        } else {
            workWarningService.addCableWarning(currentRcRsp.getId(), currentRcRsp.getType(), currentRcRsp.getLongitude(), currentRcRsp.getLatitude());
        }
    }

}
