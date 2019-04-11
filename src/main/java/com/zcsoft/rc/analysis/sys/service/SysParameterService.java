package com.zcsoft.rc.analysis.sys.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.sys.model.entity.SysParameter;


public interface SysParameterService extends IBaseService<SysParameter, String> {

    /**
     * 更新系统参数
     */
    void updateSysParameter();

    /**
     * 安全红线
     * @return
     */
    double getCordon();
	
}
