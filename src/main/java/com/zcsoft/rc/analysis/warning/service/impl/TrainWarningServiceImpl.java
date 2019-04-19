package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.components.json.IJsonService;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.analysis.warning.model.entity.TemporaryStation;
import com.zcsoft.rc.analysis.warning.service.TrainWarningService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.collectors.api.warning.entity.WarningCollectReq;
import com.zcsoft.rc.collectors.api.warning.entity.WarningDeleteReq;
import com.zcsoft.rc.collectors.api.warning.service.WarningApiService;
import com.zcsoft.rc.machinery.dao.MachineryDAO;
import com.zcsoft.rc.machinery.model.entity.Machinery;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.railway.model.entity.RailwayLines;
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

	private Map<String, TemporaryStation> temporaryStationMap = new ConcurrentHashMap<>(200);
	private Map<String, TemporaryStation> trainApproachingMap = new ConcurrentHashMap<>(200);

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

			if(TrainWarning.TYPE_TEMPORARY_STATION.equals(trainWarning.getType())) {
				warningApiService.collectTemporaryStation(req);
			} {
				warningApiService.collectTrainApproaching(req);
			}
		}catch (Exception e) {
			logger.error("collect waring error", e);
		}
	}

	protected TrainWarning addTrainWarning(String id, Double longitude, Double latitude, String trainWarningType, String direction,RailwayLines railwayLines, WorkSegment workSegment) {

		Machinery machinery = machineryDAO.queryById(id);
		if(machinery == null) {
			logger.error("machinery is null, machineryId:{}", id);
			return null;
		}

		User user = userDAO.queryById(machinery.getUserId());
		if(user == null) {
			logger.error("user is null, userId:{}", id);
			return null;
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
		trainWarning.setRailwayLinesId(railwayLines.getId());
		trainWarning.setRailwayLinesName(railwayLines.getRailwayLinesName());
		trainWarning.setWorkSegmentId(workSegment.getId());
		trainWarning.setWorkSegmentName(workSegment.getWorkSegmentName());

		trainWarningDAO.insert(trainWarning);

		return trainWarning;
	}

	protected void finishWarning(String id, String type) {
		TrainWarning queryTrainWarning = new TrainWarning();
		queryTrainWarning.setWorkWarningId(id);
		queryTrainWarning.setType(type);
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
	public void addTemporaryStationWarning(String id, Double longitude, Double latitude, String direction, RailwayLines railwayLines, CurrentRcRsp currentRcRsp) {
		TemporaryStation temporaryStation = temporaryStationMap.get(id);

		if(temporaryStation == null) {
			TrainWarning trainWarning = addTrainWarning(id,longitude,latitude,direction, TrainWarning.TYPE_TEMPORARY_STATION,railwayLines, null);

			if(trainWarning == null) {
				return;
			}

			temporaryStation = new TemporaryStation(trainWarning, new HashMap<>());

			temporaryStationMap.put(id, temporaryStation);
		}

		if(temporaryStation.get(currentRcRsp.getId()) == null) {
			noticeService.addTrainWarningNotice(temporaryStation.getTrainWarning());

			addWarning(currentRcRsp.getId(), temporaryStation.getTrainWarning());

			temporaryStation.put(id);
		}

	}

	@Override
	public void finishTemporaryStationWarning(String id) {
		TemporaryStation temporaryStation = temporaryStationMap.get(id);
		if(temporaryStation == null) {
			return;
		}

		finishWarning(id, TrainWarning.TYPE_TEMPORARY_STATION);

		temporaryStation.getCurrentRcRspMap().forEach((key, value) -> {
			WarningDeleteReq req = new WarningDeleteReq();
			req.setId(key);
			warningApiService.deleteTemporaryStation(req);
		});

		temporaryStationMap.remove(id);
	}

	@Override
	public void addTrainApproachingWarning(String id, Double longitude, Double latitude, String direction, WorkSegment workSegment, CurrentRcRsp currentRcRsp) {
		TemporaryStation temporaryStation = trainApproachingMap.get(id);

		if(temporaryStation == null) {
			TrainWarning trainWarning = addTrainWarning(id,longitude,latitude,direction, TrainWarning.TYPE_TRAIN_APPROACHING,null, workSegment);

			if(trainWarning == null) {
				return;
			}

			temporaryStation = new TemporaryStation(trainWarning, new HashMap<>());

			trainApproachingMap.put(id, temporaryStation);
		}

		if(temporaryStation.get(currentRcRsp.getId()) == null) {
			noticeService.addTrainWarningNotice(temporaryStation.getTrainWarning());

			addWarning(currentRcRsp.getId(), temporaryStation.getTrainWarning());

			temporaryStation.put(id);
		}

	}

	@Override
	public void finishTrainApproachingWarning(String id, String currentRcRspId) {
		TemporaryStation temporaryStation = trainApproachingMap.get(id);
		if(temporaryStation == null) {
			return;
		}
		if(trainApproachingMap.get(id) == null) {
			return;
		}

		WarningDeleteReq req = new WarningDeleteReq();
		req.setId(currentRcRspId);
		warningApiService.deleteTemporaryStation(req);

		temporaryStation.remove(currentRcRspId);

		if(temporaryStation.getCurrentRcRspMap().isEmpty()) {
			finishWarning(id, TrainWarning.TYPE_TRAIN_APPROACHING);
		}
	}
}
