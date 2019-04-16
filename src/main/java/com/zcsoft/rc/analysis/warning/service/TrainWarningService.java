package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.warning.model.entity.TrainWarning;


public interface TrainWarningService extends IBaseService<TrainWarning, String> {

    /**
     * 添加列车接近警告
     * @param id
     * @param longitude
     * @param latitude
     * @param direction
     * @param workSegmentId
     * @param workSegmentName
     */
    void addTrainApproachingWarning(String id, Double longitude, Double latitude, String direction, String workSegmentId, String workSegmentName);

    /**
     * 关闭列车接近警告
     * @param id
     */
    void finishTrainApproachingWarning(String id);

	
}
