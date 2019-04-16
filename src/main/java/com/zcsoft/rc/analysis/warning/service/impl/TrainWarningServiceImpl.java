package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.components.json.IJsonService;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.analysis.warning.service.TrainWarningService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.collectors.api.warning.entity.WarningCollectReq;
import com.zcsoft.rc.collectors.api.warning.service.WarningApiService;
import com.zcsoft.rc.machinery.dao.MachineryDAO;
import com.zcsoft.rc.machinery.model.entity.Machinery;
import com.zcsoft.rc.user.dao.UserDAO;
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.dao.TrainWarningDAO;
import com.zcsoft.rc.warning.model.entity.TrainWarning;
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

	private Map<String, Map<String,String>> temporaryStationMap = new ConcurrentHashMap<>(200);
	private Map<String, String> trainApproachingMap = new ConcurrentHashMap<>(200);

	private TrainWarningDAO trainWarningDAO;
	private UserDAO userDAO;
	private MachineryDAO machineryDAO;

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
	public void setMachineryDAO(MachineryDAO machineryDAO) {
		this.machineryDAO = machineryDAO;
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

			String waringContent;
			if(TrainWarning.TYPE_TEMPORARY_STATION.equals(trainWarning.getType())) {
				waringContent = applicationContext.getMessage("waring.content"+trainWarning.getType(), new String[]{trainWarning.getWorkSegmentName()}, Locale.CHINESE);
			} else {
				waringContent = applicationContext.getMessage("waring.content"+trainWarning.getType(), new String[]{trainWarning.getRailwayLinesName()}, Locale.CHINESE);
			}

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

	protected void addTrainWarning(String id, String type, Double longitude, Double latitude, String trainWarningType, String direction, String railwayLinesId, String railwayLinesName, String workSegmentId, String workSegmentName) {
		User user;
		if(User.BUILDER_USER_TYPE_LOCOMOTIVE.equals(type)) {
			Machinery machinery = machineryDAO.queryById(id);
			user = userDAO.queryById(machinery.getUserId());

			if(machinery == null) {
				logger.error("machinery is null, machineryId:{}", id);
				return;
			}
		} else {
			user = userDAO.queryById(id);
		}

		if(user == null) {
			logger.error("user is null, userId:{}", id);
			return;
		}

		TrainWarning trainWarning = new TrainWarning();

		trainWarning.setWorkWarningId(id);

		trainWarning.setNick(user.getNick());
		trainWarning.setMobile(user.getMobile());
		trainWarning.setUserId(user.getId());

		trainWarning.setDirection(direction);
		trainWarning.setStatus(TrainWarning.STATUS_CREATE);
		trainWarning.setType(trainWarningType);
		trainWarning.setLongitude(longitude);
		trainWarning.setLatitude(latitude);
		trainWarning.setRailwayLinesId(railwayLinesId);
		trainWarning.setRailwayLinesName(railwayLinesName);
		trainWarning.setWorkSegmentId(workSegmentId);
		trainWarning.setWorkSegmentName(workSegmentName);

		trainWarningDAO.insert(trainWarning);

		noticeService.addTrainWarningNotice(trainWarning);

		addWarning(id, trainWarning);
	}

	protected void finishWarning(String id) {
		TrainWarning queryTrainWarning = new TrainWarning();
		queryTrainWarning.setWorkWarningId(id);
		queryTrainWarning.setStatus(TrainWarning.STATUS_CREATE);

		List<TrainWarning> trainWarningList = trainWarningDAO.queryList(queryTrainWarning);

		if(trainWarningList == null || trainWarningList.isEmpty()) {
			return;
		}

		trainWarningList.forEach(trainWarning -> {
			TrainWarning updateTrainWarning = new TrainWarning();
			updateTrainWarning.setId(trainWarning.getId());
			updateTrainWarning.setStatus(TrainWarning.STATUS_FINISH);

			trainWarningDAO.updateById(updateTrainWarning);
		});
	}

	@Override
	public void addTemporaryStationWarning(String id, Double longitude, Double latitude, String direction, String railwayLinesId, String railwayLinesName, List<CurrentRcRsp> currentRcRspList) {
		Map<String, String> currentRcRspMap = temporaryStationMap.get(id);

		currentRcRspList.forEach(currentRcRsp -> {

			if(currentRcRspMap !=null) {
				if(currentRcRspMap.get(currentRcRsp.getId()) != null) {
					return;
				}
			}

			addTrainWarning(currentRcRsp.getId(),currentRcRsp.getType(),longitude,latitude,direction, TrainWarning.TYPE_TEMPORARY_STATION,railwayLinesId,railwayLinesName,null,null);
		});

	}

	@Override
	public void finishTemporaryStationWarning(String id) {
		if(temporaryStationMap.get(id) == null) {
			return;
		}

		finishWarning(id);

		temporaryStationMap.remove(id);
	}

	@Override
	public void addTrainApproachingWarning(String id, String type, Double longitude, Double latitude, String direction, String workSegmentId, String workSegmentName) {
		if(trainApproachingMap.get(id) != null) {
			return;
		}

		addTrainWarning(id,type,longitude,latitude,direction, TrainWarning.TYPE_TRAIN_APPROACHING,null,null,workSegmentId,workSegmentName);
	}

	@Override
	public void finishTrainApproachingWarning(String id) {
		if(trainApproachingMap.get(id) == null) {
			return;
		}

		finishWarning(id);

		trainApproachingMap.remove(id);
	}
}
