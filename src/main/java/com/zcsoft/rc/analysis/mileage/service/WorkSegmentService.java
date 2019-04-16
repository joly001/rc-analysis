package com.zcsoft.rc.analysis.mileage.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;

import java.util.List;


public interface WorkSegmentService extends IBaseService<WorkSegment, String> {

    /**
     * 设置正在作业的作业面缓存
     */
    void setWorkingWorkSegmentListCache();

    /**
     * 获取正在作业的作业面缓存
     * @return
     */
    List<WorkSegment> getWorkSegmentListCache();

}
