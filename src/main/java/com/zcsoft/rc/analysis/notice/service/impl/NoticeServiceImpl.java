package com.zcsoft.rc.analysis.notice.service.impl;

import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.notice.dao.NoticeDAO;
import com.zcsoft.rc.notice.model.entity.Notice;
import com.zcsoft.rc.user.dao.UserDAO;
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Service
public class NoticeServiceImpl implements NoticeService, ApplicationContextAware {

    private NoticeDAO noticeDAO;
    private UserDAO userDAO;

    private ApplicationContext applicationContext;


    @Resource
    public void setNoticeDAO(NoticeDAO noticeDAO) {
        this.noticeDAO = noticeDAO;
    }
    @Resource
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void updateStatusToUntreated(String id) {
        Notice updateNotice = new Notice();
        updateNotice.setId(id);
        updateNotice.setStatus(Notice.STATUS_UNTREATED);

        noticeDAO.updateById(updateNotice);
    }

    @Override
    public void updateStatusToProcessing(String id) {
        Notice updateNotice = new Notice();
        updateNotice.setId(id);
        updateNotice.setStatus(Notice.STATUS_PROCESSING);

        noticeDAO.updateById(updateNotice);
    }

    @Override
    public void updateStatusToSuccess(String id) {
        Notice updateNotice = new Notice();
        updateNotice.setId(id);
        updateNotice.setStatus(Notice.STATUS_SUCCESS);

        noticeDAO.updateById(updateNotice);
    }

    @Override
    public List<Notice> getUntreatedNotice() {
        Notice queryNotice = new Notice();
        queryNotice.setStatus(Notice.STATUS_PROCESSING);

        return noticeDAO.queryList(queryNotice);
    }

    @Override
    public void send(Notice notice) {

        updateStatusToSuccess(notice.getId());
    }

    @Override
    public void addCordonNotice(WorkWarning workWarning) {
        User user = userDAO.queryById(workWarning.getUserId());

        String content = applicationContext.getMessage("notice.type."+Notice.TYPE_CORDON, null, Locale.CHINESE);

        Notice notice = new Notice();
        notice.setType(Notice.TYPE_CORDON);
        notice.setContent(content);
        notice.setDataId(workWarning.getWorkWarningId());
        notice.setMessagingToken(user.getMessagingToken());
        notice.setStatus(Notice.STATUS_UNTREATED);

        noticeDAO.insert(notice);
    }
}
