package com.zcsoft.rc.analysis.cordon.service;

import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;

public interface CordonService {

    /**
     * 警告线预警分析
     * @param rcRsp
     */
    void analysis(CurrentRcRsp rcRsp);

}
