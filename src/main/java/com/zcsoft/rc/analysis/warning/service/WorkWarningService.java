package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.warning.model.entity.WorkWarning;


public interface WorkWarningService extends IBaseService<WorkWarning, String> {

    /**
     * 添加接近警告线警告
     * @param id
     * @param type
     * @param longitude
     * @param latitude
     */
    void addCordonWarning(String id, String type,  Double longitude, Double latitude);

    /**
     * 关闭警告
     * @param id
     */
    void finishCordonWarning(String id);
	
}
