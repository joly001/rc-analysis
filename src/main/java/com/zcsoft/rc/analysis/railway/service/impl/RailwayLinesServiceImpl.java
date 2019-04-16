package com.zcsoft.rc.analysis.railway.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.app.components.LocationComponent;
import com.zcsoft.rc.analysis.railway.service.RailwayLinesService;
import com.zcsoft.rc.analysis.sys.service.SysParameterService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.railway.dao.RailwayLinesDAO;
import com.zcsoft.rc.railway.model.entity.RailwayLines;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class RailwayLinesServiceImpl extends BaseServiceImpl<RailwayLines, String> implements RailwayLinesService {
	
	private RailwayLinesDAO railwayLinesDAO;
	private SysParameterService sysParameterService;
	private LocationComponent locationComponent;

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

	protected void temporaryStation(CurrentRcRsp rcRsp) {

	}

	protected void trainApproaching(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap) {
		rcMap.forEach((id, currentRcRsp) -> {
			if(rcRsp.getId().equals(id)) {
				return;
			}

			double trainApproachingDistance = sysParameterService.getTrainApproachingDistance();

			double locationDistance = locationComponent.getDistance(rcRsp.getLongitude(), rcRsp.getLatitude(), currentRcRsp.getLongitude(), currentRcRsp.getLatitude());

			if(locationDistance<=trainApproachingDistance) {

			} else {

			}

		});
	}

	@Override
	public void analysis(CurrentRcRsp rcRsp, Map<String,CurrentRcRsp> rcMap) {

	}


}
