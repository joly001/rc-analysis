package com.zcsoft.rc.analysis.railway.service.impl;


import com.sharingif.cube.core.util.StringUtils;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.app.components.LocationComponent;
import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
import com.zcsoft.rc.analysis.railway.model.entity.TrainDirection;
import com.zcsoft.rc.analysis.railway.model.entity.WorkSegmentRailwayLines;
import com.zcsoft.rc.analysis.railway.service.RailwayLinesService;
import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;
import com.zcsoft.rc.analysis.rc.service.CoordinatesService;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RailwayLinesServiceImpl extends BaseServiceImpl<RailwayLines, String> implements RailwayLinesService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, WorkSegmentRailwayLines> warningRailwayLinesListCache = new ConcurrentHashMap<>(20);
	private Map<String, TrainDirection> trainDirectionMap = new ConcurrentHashMap<>(20);

	private RailwayLinesDAO railwayLinesDAO;
	private SysParameterService sysParameterService;
	private LocationComponent locationComponent;
	private TrainWarningService trainWarningService;
	private WorkSegmentService workSegmentService;
	private CoordinatesService coordinatesService;

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
	@Resource
	public void setCoordinatesService(CoordinatesService coordinatesService) {
		this.coordinatesService = coordinatesService;
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

	protected WorkSegmentRailwayLines getWarningRailwayLines(CurrentRcRsp currentRcRsp) {
		for(Map.Entry<String, WorkSegmentRailwayLines> entry : warningRailwayLinesListCache.entrySet()) {
			WorkSegmentRailwayLines workSegmentRailwayLines = entry.getValue();


			RailwayLines railwayLines = workSegmentRailwayLines.getRailwayLines();
			Coordinates startCoordinates = new Coordinates(railwayLines.getStartLongitude(), railwayLines.getStartLatitude());
			Coordinates endCoordinates = new Coordinates(railwayLines.getEndLongitude(), railwayLines.getEndLatitude());

			if(coordinatesService.isIn(
					currentRcRsp.getLongitude()
					,currentRcRsp.getLongitude()
					,startCoordinates
					,endCoordinates
			)) {
				return workSegmentRailwayLines;
			}
		}

		return null;
	}

	protected void temporaryStation(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap, String direction) {
		WorkSegmentRailwayLines workSegmentRailwayLines = getWarningRailwayLines(rcRsp);

		if(workSegmentRailwayLines == null) {
			logger.error("work segment railway lines is null, rcRsp:{}", rcRsp);
			trainWarningService.finishTemporaryStationWarning(rcRsp.getId());
			return;
		}

		WorkSegment workSegment = workSegmentRailwayLines.getWorkSegment();
		Coordinates startCoordinates = new Coordinates(workSegment.getStartLongitude(), workSegment.getStartLatitude());
		Coordinates endCoordinates = new Coordinates(workSegment.getEndLongitude(), workSegment.getEndLatitude());
		RailwayLines railwayLines = workSegmentRailwayLines.getRailwayLines();

		rcMap.forEach((id, currentRcRsp) -> {
			if(coordinatesService.isIn(
					currentRcRsp.getLongitude()
					,currentRcRsp.getLongitude()
					,startCoordinates
					,endCoordinates
			)) {
				trainWarningService.addTemporaryStationWarning(rcRsp.getId(), rcRsp.getLongitude(), rcRsp.getLatitude(), direction, railwayLines, currentRcRsp);
			}
		});


	}

	protected void trainApproaching(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap, String direction) {

		WorkSegment workSegment = null;
		for(Map.Entry<String, CurrentRcRsp> entry : rcMap.entrySet()) {
			CurrentRcRsp currentRcRsp = entry.getValue();

			double trainApproachingDistance = sysParameterService.getTrainApproachingDistance();

			double locationDistance = locationComponent.getDistance(rcRsp.getLongitude(), rcRsp.getLatitude(), currentRcRsp.getLongitude(), currentRcRsp.getLatitude());

			if(locationDistance<=trainApproachingDistance) {
				if(workSegment == null) {
					workSegment = workSegmentService.getInWorkSegment(currentRcRsp.getLongitude(), currentRcRsp.getLatitude());
				}
				if(workSegment != null) {
					trainWarningService.addTrainApproachingWarning(rcRsp.getId(), rcRsp.getLongitude(), rcRsp.getLatitude(), direction, workSegment,currentRcRsp);
				}
			} else {
				trainWarningService.finishTrainApproachingWarning(rcRsp.getId(), currentRcRsp.getId());
			}
		}
	}

	protected TrainDirection initDirection(String id, Coordinates coordinates) {
		RailwayLines railwayLines = railwayLinesDAO.queryByStartLongitudeEndLongitude(coordinates.getLongitude());

		Coordinates startCoordinates = new Coordinates(railwayLines.getStartLongitude(), railwayLines.getStartLatitude());


		Coordinates endCoordinates = new Coordinates(railwayLines.getEndLongitude(), railwayLines.getEndLatitude());


		TrainDirection trainDirection = new TrainDirection(id, startCoordinates, endCoordinates);

		trainDirection.addCoordinates(coordinates);

		return trainDirection;
	}

	protected TrainDirection putTrainDirectionMap(String id, Double longitude, Double latitude) {
		TrainDirection trainDirection = trainDirectionMap.get(id);

		if(trainDirection == null) {
			trainDirectionMap.put(id, initDirection(id, new Coordinates(longitude, latitude)));

			return trainDirection;
		}

		trainDirection.addCoordinates(new Coordinates(longitude, latitude));

		return trainDirection;
	}

	@Override
	public void analysis(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap) {
		TrainDirection trainDirection = putTrainDirectionMap(rcRsp.getId(), rcRsp.getLongitude(), rcRsp.getLatitude());

		if(trainDirection.getDirection() == null) {
			return;
		}

		temporaryStation(rcRsp, rcMap, trainDirection.getDirection());

		trainApproaching(rcRsp, rcMap, trainDirection.getDirection());
	}

	@Override
	public void decideDirection() {
		trainDirectionMap.forEach((id, trainDirection) -> {
			trainDirectionMap.put(id, initDirection(id, trainDirection.getLastCoordinates()));
		});
	}


}
