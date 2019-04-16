package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.warning.model.entity.TrainWarning;


public interface TrainWarningService extends IBaseService<TrainWarning, String> {

    /**
     * 添加列车接近警告
     * @param id
     * @param type
     * @param longitude
     * @param latitude
     */
    void addTrainApproachingWarning(String id, String type,  Double longitude, Double latitude);

    /**
     * 关闭列车接近警告
     * @param id
     */
    void finishTrainApproachingWarning(String id);

    /**
     * 关闭所有列车接近警告
     */
    void finishTrainApproachingAll();

	
}
