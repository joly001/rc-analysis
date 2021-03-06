package com.zcsoft.rc.analysis.sys.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.sys.model.entity.SysParameter;


public interface SysParameterService extends IBaseService<SysParameter, String> {

    /**
     * 更新系统参数
     */
    void updateSysParameterCache();

    /**
     * 安全红线
     * @return
     */
    double getCordon();

    /**
     * 火车接近作业面人员报警距离,单位米
     */
    double getTrainApproachingDistance();

    /**
     * 临站报警提前站数
     */
    int getNumberAlarmadvanceStations();

    /**
     * 碾压限定距离
     * @return
     */
    int getRollingLimitDistance();

    /**
     * 碾压报警机械类型
     * @return
     */
    String getTypeRollingAlarmMachinery();

    /**
     * 判定线缆附近动土报警距离
     * @return
     */
    int getCableLimitDistance();

    /**
     * 判定线缆附近动土报警时间
     * @return
     */
    int getCableLimitTime();
	
}
