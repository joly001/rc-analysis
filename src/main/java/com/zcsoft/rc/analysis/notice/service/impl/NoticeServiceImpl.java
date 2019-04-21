package com.zcsoft.rc.analysis.notice.service.impl;

import com.sharingif.cube.core.util.StringUtils;
import com.tencent.xinge.XingeApp;
import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.notice.dao.NoticeDAO;
import com.zcsoft.rc.notice.model.entity.Notice;
import com.zcsoft.rc.user.dao.UserDAO;
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.model.entity.TrainWarning;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Service
public class NoticeServiceImpl implements NoticeService, ApplicationContextAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private long xingeAppId;
    private String xingeSecretKey;

    private NoticeDAO noticeDAO;
    private UserDAO userDAO;

    private ApplicationContext applicationContext;

    @Value("${xinge.app.id}")
    public void setXingeAppId(long xingeAppId) {
        this.xingeAppId = xingeAppId;
    }
    @Value("${xinge.secret.key}")
    public void setXingeSecretKey(String xingeSecretKey) {
        this.xingeSecretKey = xingeSecretKey;
    }
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
        queryNotice.setStatus(Notice.STATUS_UNTREATED);

        return noticeDAO.queryList(queryNotice);
    }

    @Override
    public void send(Notice notice) {

        if(User.OPERATING_SYSTEM_ANDROID.equals(notice.getOperatingSystem())) {
            XingeApp.pushTokenAndroid(xingeAppId, xingeSecretKey, null, notice.getContent(), notice.getMessagingToken());
        }

        if (User.OPERATINGSYSTEM_IOS.equals(notice.getOperatingSystem())) {
            XingeApp.pushTokenIos(xingeAppId, xingeSecretKey, notice.getContent(), notice.getMessagingToken(), XingeApp.IOSENV_PROD);
        }

        updateStatusToSuccess(notice.getId());
    }

    @Override
    public void addWorkWarningNotice(WorkWarning workWarning) {
        User user = userDAO.queryById(workWarning.getUserId());

        if(StringUtils.isTrimEmpty(user.getMessagingToken()) || StringUtils.isTrimEmpty(user.getOperatingSystem())) {
            logger.error("user messagingToken or operatingSystem is null, user:{}", user);
            return;
        }

        String type = null;
        if(WorkWarning.TYPE_APPROACHING_THE_WARNING_LINE.equals(workWarning.getType())) {
            type = Notice.TYPE_CORDON;
        }
        if(WorkWarning.TYPE_ROLLING_CABLE.equals(workWarning.getType())) {
            type = Notice.TYPE_ROLLING_CABLE;
        }

        String content = applicationContext.getMessage("notice.type."+type, null, Locale.CHINESE);

        Notice notice = new Notice();
        notice.setType(type);
        notice.setOperatingSystem(user.getOperatingSystem());
        notice.setContent(content);
        notice.setDataId(workWarning.getId());
        notice.setMessagingToken(user.getMessagingToken());
        notice.setStatus(Notice.STATUS_UNTREATED);
        notice.setOperatingSystem(user.getOperatingSystem());

        noticeDAO.insert(notice);
    }

    @Override
    public void addTrainWarningNotice(TrainWarning trainWarning) {
        User user = userDAO.queryById(trainWarning.getUserId());

        if(StringUtils.isTrimEmpty(user.getMessagingToken()) || StringUtils.isTrimEmpty(user.getOperatingSystem())) {
            logger.error("user messagingToken or operatingSystem is null, user:{}", user);
            return;
        }

        String type;
        if(TrainWarning.TYPE_TEMPORARY_STATION.equals(trainWarning.getType())) {
            type =  Notice.TYPE_TEMPORARY_STATION;
        } else {
            type = Notice.TYPE_TRAIN_APPROACHING;
        }

        String content = applicationContext.getMessage("notice.type."+type, new String[]{trainWarning.getWorkSegmentName()}, Locale.CHINESE);

        Notice notice = new Notice();
        notice.setType(type);
        notice.setOperatingSystem(user.getOperatingSystem());
        notice.setContent(content);
        notice.setDataId(trainWarning.getId());
        notice.setMessagingToken(user.getMessagingToken());
        notice.setStatus(Notice.STATUS_UNTREATED);
        notice.setOperatingSystem(user.getOperatingSystem());

        noticeDAO.insert(notice);

    }
}
