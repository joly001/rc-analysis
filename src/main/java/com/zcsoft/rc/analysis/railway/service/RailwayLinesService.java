package com.zcsoft.rc.analysis.railway.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.railway.model.entity.RailwayLines;

import java.util.Map;


public interface RailwayLinesService extends IBaseService<RailwayLines, String> {

    /**
     * 设置警告站缓存
     */
    void setWarningRailwayLinesListCache();

    /**
     * 线路预警分析
     * @param rcRsp
     */
    void analysis(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap);

    /**
     * 判断列车方向
     */
    void decideDirection();
	
}
