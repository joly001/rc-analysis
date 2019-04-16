package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.analysis.warning.service.WarningService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.machinery.dao.MachineryDAO;
import com.zcsoft.rc.machinery.model.entity.Machinery;
import com.zcsoft.rc.mileage.dao.WorkSegmentDAO;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.user.dao.OrganizationDAO;
import com.zcsoft.rc.user.dao.UserDAO;
import com.zcsoft.rc.user.model.entity.Organization;
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.dao.WorkWarningDAO;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkWarningServiceImpl extends BaseServiceImpl<WorkWarning, java.lang.String> implements WorkWarningService, InitializingBean {

	private WorkWarningDAO workWarningDAO;
	private WorkSegmentDAO workSegmentDAO;
	private UserDAO userDAO;
	private MachineryDAO machineryDAO;
	private OrganizationDAO organizationDAO;

	private WarningService warningService;
	private NoticeService noticeService;

	@Resource
	public void setWorkWarningDAO(WorkWarningDAO workWarningDAO) {
		super.setBaseDAO(workWarningDAO);
		this.workWarningDAO = workWarningDAO;
	}
	@Resource
	public void setWorkSegmentDAO(WorkSegmentDAO workSegmentDAO) {
		this.workSegmentDAO = workSegmentDAO;
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
	public void setOrganizationDAO(OrganizationDAO organizationDAO) {
		this.organizationDAO = organizationDAO;
	}
	@Resource
	public void setWarningService(WarningService warningService) {
		this.warningService = warningService;
	}
	@Resource
	public void setNoticeService(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	public Organization getDep(String organizationId) {
		Organization organization = organizationDAO.queryById(organizationId);
		if(organization.getParentId() == null) {
			return organization;
		}

		return getDep(organization.getParentId());
	}

	@Override
	public void addCordonWarning(String id,String type,Double longitude, Double latitude) {

		if(warningService.getWarning(id) != null) {
			return;
		}

		WorkSegment workSegment = workSegmentDAO.queryByStartLongitudeEndLongitude(longitude);

		if(workSegment == null) {
			logger.error("workSegment is null, longitude:{}", longitude);
			return;
		}

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

		Organization organization = organizationDAO.queryById(user.getOrganizationId());

		Organization dep;
		if(organization.getParentId() == null) {
			dep = organization;
		} else {
			dep = getDep(organization.getParentId());
		}

		WorkWarning workWarning = new WorkWarning();

		workWarning.setWorkWarningId(id);

		workWarning.setMileageSegmentId(workSegment.getMileageSegmentId());
		workWarning.setMileageSegmentName(workSegment.getMileageSegmentName());

		workWarning.setWorkSegmentId(workSegment.getId());
		workWarning.setWorkSegmentName(workSegment.getWorkSegmentName());
		workWarning.setWorkSegmentStartLongitude(workSegment.getStartLongitude());
		workWarning.setWorkSegmentStartLatitude(workSegment.getStartLatitude());
		workWarning.setWorkSegmentEndLongitude(workSegment.getEndLongitude());
		workWarning.setWorkSegmentEndLatitude(workSegment.getEndLatitude());

		workWarning.setUserId(user.getId());
		workWarning.setBuilderUserType(type);
		workWarning.setDepId(dep.getId());
		workWarning.setDepName(dep.getOrgName());
		workWarning.setOrgId(organization.getId());
		workWarning.setOrgName(organization.getOrgName());
		workWarning.setNick(user.getNick());
		workWarning.setMobile(user.getMobile());
		workWarning.setStatus(WorkWarning.STATUS_CREATE);
		workWarning.setType(WorkWarning.TYPE_APPROACHING_THE_WARNING_LINE);

		workWarning.setLongitude(longitude);
		workWarning.setLatitude(latitude);

		workWarningDAO.insert(workWarning);

		noticeService.addCordonNotice(workWarning);

		warningService.addWarning(id, workWarning);
	}

	@Override
	public void finishCordonWarning(String id) {
		if(warningService.getWarning(id) == null) {
			return;
		}

		workWarningDAO.updateStatusByWorkWarningIdStatus(id, WorkWarning.STATUS_CREATE, WorkWarning.STATUS_FINISH);

		warningService.removeWarning(id);
	}

	@Override
	public List<WorkWarning> getCreateStatus() {
		WorkWarning queryWorkWarning = new WorkWarning();
		queryWorkWarning.setStatus(WorkWarning.STATUS_CREATE);

		List<WorkWarning> workWarningList = workWarningDAO.queryList(queryWorkWarning);

		return workWarningList;

	}

	@Override
	public void finishAll() {
		List<WorkWarning> workWarningList = getCreateStatus();

		if(workWarningList == null || workWarningList.isEmpty()) {
			return;
		}

		workWarningList.forEach(workWarning -> {
			finishCordonWarning(workWarning.getWorkWarningId());
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<WorkWarning> workWarningList = getCreateStatus();

		if(workWarningList == null || workWarningList.isEmpty()) {
			return;
		}

		workWarningList.forEach(workWarning -> {
			warningService.addWarning(workWarning.getWorkWarningId(), workWarning);
		});
	}
}
