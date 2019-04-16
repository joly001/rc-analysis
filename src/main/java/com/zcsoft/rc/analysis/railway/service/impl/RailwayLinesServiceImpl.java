package com.zcsoft.rc.analysis.railway.service.impl;


import com.sharingif.cube.core.util.StringUtils;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.app.components.LocationComponent;
import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
import com.zcsoft.rc.analysis.railway.model.entity.WorkSegmentRailwayLines;
import com.zcsoft.rc.analysis.railway.service.RailwayLinesService;
import com.zcsoft.rc.analysis.sys.service.SysParameterService;
import com.zcsoft.rc.analysis.warning.service.TrainWarningService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.railway.dao.RailwayLinesDAO;
import com.zcsoft.rc.railway.model.entity.RailwayLines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RailwayLinesServiceImpl extends BaseServiceImpl<RailwayLines, String> implements RailwayLinesService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, WorkSegmentRailwayLines> warningRailwayLinesListCache = new ConcurrentHashMap<>(20);

	private RailwayLinesDAO railwayLinesDAO;
	private SysParameterService sysParameterService;
	private LocationComponent locationComponent;
	private TrainWarningService trainWarningService;
	private WorkSegmentService workSegmentService;

	@Resource
	public void setRailwayLinesDAO(RailwayLinesDAO railwayLinesDAO) {
		super.setBaseDAO(railwayLinesDAO);
		this.railwayLinesDAO = railwayLinesDAO;
	}
	@Resource
	public void setSysParameterService(SysParameterService sysParameterService) {
		this.sysParameterService = sysParameterService;
	}
	@Resource
	public void setLocationComponent(LocationComponent locationComponent) {
		this.locationComponent = locationComponent;
	}
	@Resource
	public void setTrainWarningService(TrainWarningService trainWarningService) {
		this.trainWarningService = trainWarningService;
	}
	@Resource
	public void setWorkSegmentService(WorkSegmentService workSegmentService) {
		this.workSegmentService = workSegmentService;
	}

	protected RailwayLines getPreviousStation(RailwayLines railwayLines, int numberAlarmadvanceStations, int current) {
		if(StringUtils.isTrimEmpty(railwayLines.getPreviousStationId())) {
			return railwayLines;
		}

		RailwayLines previousStation = railwayLinesDAO.queryById(railwayLines.getPreviousStationId());

		if(previousStation == null || StringUtils.isTrimEmpty(previousStation.getId())) {
			return railwayLines;
		}

		if(current == numberAlarmadvanceStations) {
			return previousStation;
		}

		return getPreviousStation(previousStation, numberAlarmadvanceStations, ++current);
	}

	protected RailwayLines getNextStation(RailwayLines railwayLines, int numberAlarmadvanceStations, int current) {
		RailwayLines queryRailwayLines = new RailwayLines();
		queryRailwayLines.setPreviousStationId(railwayLines.getId());

		List<RailwayLines> railwayLinesList = railwayLinesDAO.queryList(queryRailwayLines);

		if(railwayLinesList == null || railwayLinesList.isEmpty()) {
			return railwayLines;
		}

		RailwayLines nextStation = railwayLinesList.get(0);

		if(current == numberAlarmadvanceStations) {
			return nextStation;
		}

		return getNextStation(nextStation, numberAlarmadvanceStations, ++current);

	}

	@Override
	public void setWarningRailwayLinesListCache() {
		warningRailwayLinesListCache.clear();

		List<WorkSegment> workSegmentList = workSegmentService.getWorkSegmentListCache();
		if(workSegmentList == null || workSegmentList.isEmpty()) {
			return;
		}

		int numberAlarmadvanceStations = sysParameterService.getNumberAlarmadvanceStations();

		workSegmentList.forEach(workSegment -> {
			RailwayLines railwayLines = railwayLinesDAO.queryByStartLongitudeEndLongitude(workSegment.getStartLongitude());

			RailwayLines previousStation = getPreviousStation(railwayLines, numberAlarmadvanceStations, 1);
			RailwayLines nextStation = getNextStation(railwayLines, numberAlarmadvanceStations, 1);

			WorkSegmentRailwayLines previousWorkSegmentRailwayLines = new WorkSegmentRailwayLines(workSegment, previousStation);
			warningRailwayLinesListCache.put(previousStation.getId(), previousWorkSegmentRailwayLines);
			WorkSegmentRailwayLines nextWorkSegmentRailwayLines = new WorkSegmentRailwayLines(workSegment, nextStation);
			warningRailwayLinesListCache.put(nextStation.getId(), nextWorkSegmentRailwayLines);
		});

	}

	protected WorkSegmentRailwayLines getWarningRailwayLines(CurrentRcRsp rcRsp) {
		for(Map.Entry<String, WorkSegmentRailwayLines> entry : warningRailwayLinesListCache.entrySet()) {
			WorkSegmentRailwayLines workSegmentRailwayLines = entry.getValue();


			RailwayLines railwayLines = workSegmentRailwayLines.getRailwayLines();

			if(rcRsp.getLongitude() > railwayLines.getEndLongitude() && rcRsp.getLongitude() < railwayLines.getStartLongitude()) {
				return workSegmentRailwayLines;
			}

			if(rcRsp.getLongitude() > railwayLines.getStartLongitude() && rcRsp.getLongitude() < railwayLines.getEndLongitude()) {
				return workSegmentRailwayLines;
			}
		}

		return null;
	}

	protected void temporaryStation(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap, String direction) {
		WorkSegmentRailwayLines workSegmentRailwayLines = getWarningRailwayLines(rcRsp);


		if(workSegmentRailwayLines == null) {

		}
	}

	private WorkSegment getApproachingWorkSegment(CurrentRcRsp rcRsp) {
		List<WorkSegment> workSegmentList = workSegmentService.getWorkSegmentListCache();

		for(WorkSegment workSegment : workSegmentList) {
			if(rcRsp.getLongitude() > workSegment.getEndLongitude() && rcRsp.getLongitude() < workSegment.getStartLongitude()) {
				return workSegment;
			}

			if(rcRsp.getLongitude() > workSegment.getStartLongitude() && rcRsp.getLongitude() < workSegment.getEndLongitude()) {
				return workSegment;
			}
		}

		return null;
	}

	protected void trainApproaching(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap, String direction) {

		WorkSegment workSegment = getApproachingWorkSegment(rcRsp);

		if(workSegment == null) {
			logger.error("train approaching work segment is null, rcRsp:{}", rcRsp);

			return;
		}

		rcMap.forEach((id, currentRcRsp) -> {
			if(rcRsp.getId().equals(id)) {
				return;
			}

			double trainApproachingDistance = sysParameterService.getTrainApproachingDistance();

			double locationDistance = locationComponent.getDistance(rcRsp.getLongitude(), rcRsp.getLatitude(), currentRcRsp.getLongitude(), currentRcRsp.getLatitude());

			if(locationDistance<=trainApproachingDistance) {
				trainWarningService.addTrainApproachingWarning(currentRcRsp.getId(), rcRsp.getLongitude(), rcRsp.getLatitude(), direction, workSegment.getId(), workSegment.getWorkSegmentName());
			} else {
				trainWarningService.finishTrainApproachingWarning(currentRcRsp.getId());
			}

		});
	}

	@Override
	public void analysis(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap) {

	}


}
