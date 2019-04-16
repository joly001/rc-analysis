package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.components.json.IJsonService;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.analysis.warning.service.TrainWarningService;
import com.zcsoft.rc.collectors.api.warning.entity.WarningCollectReq;
import com.zcsoft.rc.collectors.api.warning.service.WarningApiService;
import com.zcsoft.rc.user.dao.UserDAO;
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.dao.TrainWarningDAO;
import com.zcsoft.rc.warning.model.entity.TrainWarning;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrainWarningServiceImpl extends BaseServiceImpl<TrainWarning, String> implements TrainWarningService, ApplicationContextAware  {

	private Map<String, String> trainApproachingMap = new ConcurrentHashMap<>(200);

	private TrainWarningDAO trainWarningDAO;
	private UserDAO userDAO;

	private NoticeService noticeService;
	private IJsonService jsonService;
	private ApplicationContext applicationContext;
	private WarningApiService warningApiService;

	@Resource
	public void setTrainWarningDAO(TrainWarningDAO trainWarningDAO) {
		super.setBaseDAO(trainWarningDAO);
		this.trainWarningDAO = trainWarningDAO;
	}
	@Resource
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	@Resource
	public void setNoticeService(NoticeService noticeService) {
		this.noticeService = noticeService;
	}
	@Resource
	public void setJsonService(IJsonService jsonService) {
		this.jsonService = jsonService;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	@Resource
	public void setWarningApiService(WarningApiService warningApiService) {
		this.warningApiService = warningApiService;
	}

	protected void addWarning(String id, TrainWarning trainWarning) {
		trainApproachingMap.put(id,id);

		try {

			String waringContent = applicationContext.getMessage("waring.content", new String[]{trainWarning.getWorkSegmentName()}, Locale.CHINESE);

			Map<String, Object> waring = new HashMap<>();

			waring.put("userId", trainWarning.getUserId());
			waring.put("nick", trainWarning.getNick());
			waring.put("mobile", trainWarning.getMobile());
			waring.put("waringContent", waringContent);

			String waringJson = jsonService.objectoJson(waring);

			WarningCollectReq req = new WarningCollectReq();
			req.setId(id);
			req.setWarning(waringJson);


			warningApiService.collect(req);
		}catch (Exception e) {
			logger.error("collect waring error", e);
		}
	}

	@Override
	public void addTrainApproachingWarning(String id, Double longitude, Double latitude, String direction, String workSegmentId, String workSegmentName) {
		if(trainApproachingMap.get(id) != null) {
			return;
		}

		User user = userDAO.queryById(id);

		if(user == null) {
			logger.error("user is null, userId:{}", id);

			return;
		}

		TrainWarning trainWarning = new TrainWarning();

		trainWarning.setNick(user.getNick());
		trainWarning.setMobile(user.getMobile());
		trainWarning.setUserId(user.getId());

		trainWarning.setDirection(direction);
		trainWarning.setStatus(TrainWarning.STATUS_CREATE);
		trainWarning.setType(TrainWarning.TYPE_TRAIN_APPROACHING);
		trainWarning.setLongitude(longitude);
		trainWarning.setLatitude(latitude);
		trainWarning.setWorkSegmentId(workSegmentId);
		trainWarning.setWorkSegmentName(workSegmentName);

		trainWarningDAO.insert(trainWarning);

		noticeService.addTrainApproachingNotice(trainWarning);
	}

	@Override
	public void finishTrainApproachingWarning(String id) {
		if(trainApproachingMap.get(id) == null) {
			return;
		}

		TrainWarning queryTrainWarning = new TrainWarning();
		queryTrainWarning.setUserId(id);
		queryTrainWarning.setStatus(TrainWarning.STATUS_CREATE);

		List<TrainWarning> trainWarningList = trainWarningDAO.queryList(queryTrainWarning);

		if(trainWarningList == null || trainWarningList.isEmpty()) {
			trainApproachingMap.remove(id);

			return;
		}

		trainWarningList.forEach(trainWarning -> {
			TrainWarning updateTrainWarning = new TrainWarning();
			updateTrainWarning.setId(trainWarning.getId());
			updateTrainWarning.setStatus(TrainWarning.STATUS_FINISH);

			trainWarningDAO.updateById(updateTrainWarning);
		});

		trainApproachingMap.remove(id);
	}
}
