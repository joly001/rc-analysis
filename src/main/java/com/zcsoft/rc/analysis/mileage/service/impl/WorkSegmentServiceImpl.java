package com.zcsoft.rc.analysis.mileage.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.mileage.service.WorkSegmentService;
import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;
import com.zcsoft.rc.analysis.rc.service.CoordinatesService;
import com.zcsoft.rc.mileage.dao.WorkSegmentDAO;
import com.zcsoft.rc.mileage.dao.WorkSegmentDataTimeDAO;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.mileage.model.entity.WorkSegmentDataTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class WorkSegmentServiceImpl extends BaseServiceImpl<WorkSegment, String> implements WorkSegmentService, InitializingBean {

	private List<WorkSegment> workSegmentListCache;

	private WorkSegmentDAO workSegmentDAO;
	private WorkSegmentDataTimeDAO workSegmentDataTimeDAO;
	private CoordinatesService coordinatesService;

	@Resource
	public void setWorkSegmentDAO(WorkSegmentDAO workSegmentDAO) {
		super.setBaseDAO(workSegmentDAO);
		this.workSegmentDAO = workSegmentDAO;
	}
	@Resource
	public void setWorkSegmentDataTimeDAO(WorkSegmentDataTimeDAO workSegmentDataTimeDAO) {
		this.workSegmentDataTimeDAO = workSegmentDataTimeDAO;
	}
	@Resource
	public void setCoordinatesService(CoordinatesService coordinatesService) {
		this.coordinatesService = coordinatesService;
	}

	synchronized protected void updateWorkSegmentListCache(List<WorkSegment> workSegmentList) {
		workSegmentListCache = workSegmentList;
	}

	@Override
	public void setWorkingWorkSegmentListCache() {
		LocalDate nowDate = LocalDate.now();
		LocalDateTime beginLocalDateTime = LocalDateTime.of(nowDate, LocalTime.MIN);
		LocalDateTime endLocalDateTime = LocalDateTime.of(nowDate,LocalTime.MAX);

		ZoneId zone = ZoneId.systemDefault();
		Instant beginInstant = beginLocalDateTime.atZone(zone).toInstant();
		Instant endInstant = endLocalDateTime.atZone(zone).toInstant();

		Date beginDateTime = Date.from(beginInstant);
		Date endDateTime = Date.from(endInstant);


		List<WorkSegment> workSegmentList = workSegmentDAO.queryListByWorkDate(beginDateTime, endDateTime);

		workSegmentList.forEach(workSegment -> {
			WorkSegmentDataTime queryWorkSegmentDataTime = new WorkSegmentDataTime();
			queryWorkSegmentDataTime.setWorkSegmentId(workSegment.getId());
			List<WorkSegmentDataTime> workSegmentDataTimeList = workSegmentDataTimeDAO.queryList(queryWorkSegmentDataTime);

			if(workSegmentDataTimeList != null && !workSegmentDataTimeList.isEmpty()) {
				workSegment.setWorkSegmentDataTimeList(workSegmentDataTimeList);
			}
		});

		updateWorkSegmentListCache(workSegmentList);

	}

	@Override
	synchronized public List<WorkSegment> getWorkSegmentListCache() {
		return workSegmentListCache;
	}

	@Override
	public WorkSegment getInWorkSegment(Double longitude, Double latitude) {
		if(workSegmentListCache == null) {
			return null;
		}

		for(WorkSegment workSegment : workSegmentListCache) {
			if(coordinatesService.isIn(
					longitude
					,new Coordinates(workSegment.getStartLongitude(), workSegment.getStartLatitude())
					,new Coordinates(workSegment.getEndLongitude(), workSegment.getEndLatitude())
			)) {
				return workSegment;
			}
		}

		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setWorkingWorkSegmentListCache();
	}
}
