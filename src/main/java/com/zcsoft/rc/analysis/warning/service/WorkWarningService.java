package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.warning.model.entity.WorkWarning;


public interface WorkWarningService extends IBaseService<WorkWarning, String> {

    /**
     * 添加接近警告线警告
     * @param longitude
     * @param latitude
     * @param type
     * @param id
     */
    void addCordonWarning(Double longitude, Double latitude, String type, String id);
	
}
