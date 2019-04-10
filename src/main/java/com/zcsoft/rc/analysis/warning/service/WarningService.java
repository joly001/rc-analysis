package com.zcsoft.rc.analysis.warning.service;

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

}
