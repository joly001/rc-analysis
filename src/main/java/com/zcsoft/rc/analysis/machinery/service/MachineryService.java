package com.zcsoft.rc.analysis.machinery.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.machinery.model.entity.Machinery;


public interface MachineryService extends IBaseService<Machinery, String> {

    /**
     * 设置机械缓存
     */
	void setMachineryCache();

    /**
     * 根据id查询机械
     * @param id
     */
    Machinery getMachinery(String id);

}
