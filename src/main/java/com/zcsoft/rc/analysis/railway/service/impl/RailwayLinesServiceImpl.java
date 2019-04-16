package com.zcsoft.rc.analysis.railway.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.app.components.LocationComponent;
import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
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
import java.util.List;
import java.util.Map;

@Service
public class RailwayLinesServiceImpl extends BaseServiceImpl<RailwayLines, String> implements RailwayLinesService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

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

	protected void temporaryStation(CurrentRcRsp rcRsp, String direction) {
		RailwayLines railwayLines = railwayLinesDAO.queryByStartLongitudeEndLongitude(rcRsp.getLongitude());


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
