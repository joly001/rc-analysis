package com.zcsoft.rc.analysis.cable.service;

import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;

public interface CableService {

    /**
     * 警告线预警分析
     * @param currentRcRsp
     */
    void analysis(CurrentRcRsp currentRcRsp);

}
