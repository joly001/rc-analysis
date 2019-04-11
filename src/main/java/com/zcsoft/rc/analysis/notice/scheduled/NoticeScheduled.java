package com.zcsoft.rc.analysis.notice.scheduled;

import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.notice.model.entity.Notice;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@EnableScheduling
public class NoticeScheduled {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ThreadPoolTaskExecutor workThreadPoolTaskExecutor;
    private NoticeService noticeService;
    private WorkWarningService workWarningService;

    @Resource
    public void setWorkThreadPoolTaskExecutor(ThreadPoolTaskExecutor workThreadPoolTaskExecutor) {
        this.workThreadPoolTaskExecutor = workThreadPoolTaskExecutor;
    }
    @Resource
    public void setNoticeService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }
    @Resource
    public void setWorkWarningService(WorkWarningService workWarningService) {
        this.workWarningService = workWarningService;
    }

    @Scheduled(fixedRate = 1000*1)
    public synchronized void notice() {
        List<Notice> noticeList = noticeService.getUntreatedNotice();

        if(noticeList == null || noticeList.isEmpty()) {
            return;
        }

        for(Notice notice : noticeList) {
            try {
                noticeService.updateStatusToProcessing(notice.getId());

                workThreadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        noticeService.send(notice);
                    }
                });
            }catch (Exception e) {
                logger.error("send notice error",e);
                noticeService.updateStatusToUntreated(notice.getId());
            }
        }
    }

    @Scheduled(fixedRate = 1000*60*3)
    public synchronized void addCordonNotice() {
        List<WorkWarning> workWarningList = workWarningService.getCreateStatus();

        if(workWarningList == null || workWarningList.isEmpty()) {
            return;
        }

        for(WorkWarning workWarning : workWarningList) {
            workThreadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    noticeService.addCordonNotice(workWarning);
                }
            });
        }

    }

}
