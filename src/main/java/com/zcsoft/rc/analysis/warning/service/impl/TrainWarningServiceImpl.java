package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.warning.service.TrainWarningService;
import com.zcsoft.rc.warning.dao.TrainWarningDAO;
import com.zcsoft.rc.warning.model.entity.TrainWarning;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrainWarningServiceImpl extends BaseServiceImpl<TrainWarning, String> implements TrainWarningService {

	private Map<String, String> trainApproachingMap = new ConcurrentHashMap<>(200);

	private TrainWarningDAO trainWarningDAO;

	@Resource
	public void setTrainWarningDAO(TrainWarningDAO trainWarningDAO) {
		super.setBaseDAO(trainWarningDAO);
		this.trainWarningDAO = trainWarningDAO;
	}

	@Override
	public void addTrainApproachingWarning(String id, String type, Double longitude, Double latitude) {

	}

	@Override
	public void finishTrainApproachingWarning(String id) {

	}

	@Override
	public void finishTrainApproachingAll() {

	}
}
