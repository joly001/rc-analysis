package com.zcsoft.rc.analysis.railway.service.impl;


import com.sharingif.cube.core.util.StringUtils;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.app.components.LocationComponent;
import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
import com.zcsoft.rc.analysis.railway.model.entity.TrainDirection;
import com.zcsoft.rc.analysis.railway.model.entity.WorkSegmentRailwayLine;
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
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.model.entity.TrainWarning;
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

			if(railwayLines == null) {
				return;
			}

			RailwayLines previousStation = getPreviousStation(railwayLines, numberAlarmadvanceStations, 1);
			RailwayLines nextStation = getNextStation(railwayLines, numberAlarmadvanceStations, 1);

			WorkSegmentRailwayLines workSegmentRailwayLines = new WorkSegmentRailwayLines(workSegment, previousStation, nextStation);
			warningRailwayLinesListCache.put(previousStation.getId(), workSegmentRailwayLines);
		});

	}

	protected WorkSegmentRailwayLine getWarningRailwayLines(CurrentRcRsp trainCurrentRcRsp, String direction) {
		for(Map.Entry<String, WorkSegmentRailwayLines> entry : warningRailwayLinesListCache.entrySet()) {
			WorkSegmentRailwayLines workSegmentRailwayLines = entry.getValue();

			RailwayLines railwayLines;
			if(TrainWarning.DIRECTION_UP.equals(direction)) {
				railwayLines = workSegmentRailwayLines.getNextRailwayLines();
			} else {
				railwayLines = workSegmentRailwayLines.getPreviousRailwayLines();
			}
			Coordinates startCoordinates = new Coordinates(railwayLines.getStartLongitude(), railwayLines.getStartLatitude());
			Coordinates endCoordinates = new Coordinates(railwayLines.getEndLongitude(), railwayLines.getEndLatitude());

			if(coordinatesService.isIn(
					trainCurrentRcRsp.getLongitude()
					,startCoordinates
					,endCoordinates
			)) {
				WorkSegmentRailwayLine workSegmentRailwayLine = new WorkSegmentRailwayLine();
				workSegmentRailwayLine.setWorkSegment(workSegmentRailwayLines.getWorkSegment());
				workSegmentRailwayLine.setRailwayLines(railwayLines);


				return workSegmentRailwayLine;
			}
		}

		return null;
	}

	protected void temporaryStation(CurrentRcRsp trainCurrentRcRsp, CurrentRcRsp currentRcRsp, String direction) {
		WorkSegmentRailwayLine workSegmentRailwayLine = getWarningRailwayLines(trainCurrentRcRsp, direction);

		if(workSegmentRailwayLine == null) {
			logger.info("work segment railway lines is null, rcRsp:{}", trainCurrentRcRsp);
			trainWarningService.finishTemporaryStationWarning(trainCurrentRcRsp.getId());
			return;
		}

		WorkSegment workSegment = workSegmentRailwayLine.getWorkSegment();
		Coordinates startCoordinates = new Coordinates(workSegment.getStartLongitude(), workSegment.getStartLatitude());
		Coordinates endCoordinates = new Coordinates(workSegment.getEndLongitude(), workSegment.getEndLatitude());
		RailwayLines railwayLines = workSegmentRailwayLine.getRailwayLines();

		if(coordinatesService.isIn(
				currentRcRsp.getLongitude()
				,startCoordinates
				,endCoordinates
		)) {
			trainWarningService.addTemporaryStationWarning(currentRcRsp.getId(), trainCurrentRcRsp.getLongitude(), trainCurrentRcRsp.getLatitude(), direction, railwayLines, currentRcRsp);
		}


	}

	protected WorkSegment getInWorkSegment(CurrentRcRsp trainCurrentRcRsp, CurrentRcRsp currentRcRsp, String direction) {
		for(Map.Entry<String, WorkSegmentRailwayLines> entry : warningRailwayLinesListCache.entrySet()) {
			WorkSegmentRailwayLines workSegmentRailwayLines = entry.getValue();
			WorkSegment workSegment = workSegmentRailwayLines.getWorkSegment();

			if(coordinatesService.isIn(
					currentRcRsp.getLongitude()
					,new Coordinates(workSegment.getStartLongitude(), workSegment.getStartLatitude())
					,new Coordinates(workSegment.getEndLongitude(), workSegment.getEndLatitude())
			)) {

				// 判断列车行驶方向，如果是离开作业人员就不报警
				RailwayLines railwayLines;
				if(TrainWarning.DIRECTION_UP.equals(direction)) {
					railwayLines = workSegmentRailwayLines.getNextRailwayLines();
				} else {
					railwayLines = workSegmentRailwayLines.getPreviousRailwayLines();
				}

				if(((railwayLines.getStartLongitude() - currentRcRsp.getLongitude())>0) == ((trainCurrentRcRsp.getLongitude() - currentRcRsp.getLongitude())>0)) {
					return workSegment;
				}
			}
		}

		return null;
	}

	protected void trainApproaching(CurrentRcRsp trainCurrentRcRsp, CurrentRcRsp currentRcRsp, String direction) {
		double trainApproachingDistance = sysParameterService.getTrainApproachingDistance();
		double locationDistance = locationComponent.getDistance(trainCurrentRcRsp.getLongitude(), trainCurrentRcRsp.getLatitude(), currentRcRsp.getLongitude(), currentRcRsp.getLatitude());

		if(locationDistance <= trainApproachingDistance) {
			WorkSegment workSegment = getInWorkSegment(trainCurrentRcRsp, currentRcRsp, direction);
			if(workSegment == null) {
				logger.error("currentRcRsp not in workSegment,currentRcRsp:{}",currentRcRsp);
				return;
			} else {
				trainWarningService.addTrainApproachingWarning(currentRcRsp.getId(), trainCurrentRcRsp.getLongitude(), trainCurrentRcRsp.getLatitude(), direction, workSegment,currentRcRsp);
			}
		} else {
			trainWarningService.finishTrainApproachingWarning(currentRcRsp.getId(), currentRcRsp.getId());
		}
	}

	protected TrainDirection initDirection(String id, Coordinates coordinates) {
		RailwayLines railwayLines = railwayLinesDAO.queryByStartLongitudeEndLongitude(coordinates.getLongitude());

		if(railwayLines == null) {
			logger.error("init direction railwayLines is null,coordinates:{}", coordinates);
			return null;
		}

		Coordinates startCoordinates = new Coordinates(railwayLines.getStartLongitude(), railwayLines.getStartLatitude());


		Coordinates endCoordinates = new Coordinates(railwayLines.getEndLongitude(), railwayLines.getEndLatitude());


		TrainDirection trainDirection = new TrainDirection(id, startCoordinates, endCoordinates);

		trainDirection.addCoordinates(coordinates);

		return trainDirection;
	}

	protected TrainDirection putTrainDirectionMap(String id, Double longitude, Double latitude) {
		TrainDirection trainDirection = trainDirectionMap.get(id);

		if(trainDirection == null) {
			trainDirection = initDirection(id, new Coordinates(longitude, latitude));

			if(trainDirection == null) {
				return null;
			}

			trainDirectionMap.put(id, trainDirection);

			return trainDirection;
		}

		trainDirection.addCoordinates(new Coordinates(longitude, latitude));

		return trainDirection;
	}

	@Override
	public void analysis(CurrentRcRsp currentRcRsp) {
		if(User.BUILDER_USER_TYPE_TRAIN.equals(currentRcRsp.getType())) {
			TrainDirection trainDirection = putTrainDirectionMap(currentRcRsp.getId(), currentRcRsp.getLongitude(), currentRcRsp.getLatitude());

			if(trainDirection == null || trainDirection.getDirection() == null) {
				logger.info("train direction is null, trainDirection:{}", trainDirection);
				return;
			}

			return;
		}

		trainDirectionMap.forEach((id, trainDirection) -> {
			CurrentRcRsp trainCurrentRcRsp = new CurrentRcRsp();
			trainCurrentRcRsp.setId(id);
			trainCurrentRcRsp.setType(User.BUILDER_USER_TYPE_TRAIN);
			trainCurrentRcRsp.setLongitude(trainDirection.getLastCoordinates().getLongitude());
			trainCurrentRcRsp.setLatitude(trainDirection.getLastCoordinates().getLatitude());

			temporaryStation(trainCurrentRcRsp, currentRcRsp, trainDirection.getDirection());

			trainApproaching(trainCurrentRcRsp, currentRcRsp, trainDirection.getDirection());
		});
	}

	@Override
	public void decideDirection() {
		trainDirectionMap.forEach((id, trainDirection) -> {
			trainDirectionMap.put(id, initDirection(id, trainDirection.getLastCoordinates()));
		});
	}


}
