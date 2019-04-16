package com.zcsoft.rc.analysis.notice.service;

import com.zcsoft.rc.notice.model.entity.Notice;
import com.zcsoft.rc.warning.model.entity.TrainWarning;
import com.zcsoft.rc.warning.model.entity.WorkWarning;

import java.util.List;

public interface NoticeService {

    /**
     * 修改状态为未处理
     * @param id
     */
    void updateStatusToUntreated(String id);

    /**
     * 修改状态为处理中
     * @param id
     */
    void updateStatusToProcessing(String id);

    /**
     * 修改状态为成功
     * @param id
     */
    void updateStatusToSuccess(String id);

    /**
     * 获取未处理通知
     * @return
     */
    List<Notice> getUntreatedNotice();

    /**
     * 发送通知
     * @param notice
     */
    void send(Notice notice);

    /**
     * 添加接近警告线通知
     * @param workWarning
     */
    void addCordonNotice(WorkWarning workWarning);

    /**
     * 添加列车接近警告通知
     * @param trainWarning
     */
    void addTrainApproachingNotice(TrainWarning trainWarning);

}
