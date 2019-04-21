package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.warning.model.entity.WorkWarning;

import java.util.List;


public interface WorkWarningService extends IBaseService<WorkWarning, String> {

    /**
     * 获取创建状态记录
     * @return
     */
    List<WorkWarning> getCreateStatus();

    /**
     * 添加接近警告线警告
     * @param id
     * @param type
     * @param longitude
     * @param latitude
     */
    void addCordonWarning(String id, String type, Double longitude, Double latitude);

    /**
     * 关闭接近警告线警告
     * @param id
     */
    void finishCordonWarning(String id);

    /**
     * 添加线缆警告
     * @param id
     * @param type
     * @param longitude
     * @param latitude
     */
    void addCableWarning(String id, String type, Double longitude, Double latitude);

    /**
     * 关闭线缆警告
     * @param id
     */
    void finishCableWarning(String id);

    /**
     * 关闭所有警告数据
     */
    void finishAll();
	
}
