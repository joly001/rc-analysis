package com.zcsoft.rc.analysis.warning.service;

import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.model.entity.WorkWarning;

public interface WarningService {

    /**
     * 警告是否打开
     * @return
     */
    boolean isOpen();

    /**
     * 设置警告打开状态
     */
    void setWarningOpenStatus();

    /**
     * 添加警告
     * @param id
     * @param workWarning
     */
    void addWarning(String id, WorkWarning workWarning);

    /**
     * 删除警告
     * @param id
     */
    void removeWarning(String id);

    /**
     * 获取警告
     * @param id
     * @return
     */
    String getWarning(String id);

}
