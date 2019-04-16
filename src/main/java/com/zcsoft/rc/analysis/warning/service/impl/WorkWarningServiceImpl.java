package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.components.json.IJsonService;
import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.notice.service.NoticeService;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.collectors.api.warning.entity.WarningCollectReq;
import com.zcsoft.rc.collectors.api.warning.entity.WarningDeleteReq;
import com.zcsoft.rc.collectors.api.warning.service.WarningApiService;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
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
public class WorkWarningServiceImpl extends BaseServiceImpl<WorkWarning, java.lang.String> implements WorkWarningService, ApplicationContextAware, InitializingBean {

	private Map<String, String> warningMap = new ConcurrentHashMap<>(200);

	private WorkWarningDAO workWarningDAO;
	private WorkSegmentDAO workSegmentDAO;
	private UserDAO userDAO;
	private MachineryDAO machineryDAO;
	private OrganizationDAO organizationDAO;

	private NoticeService noticeService;
	private WarningApiService warningApiService;
	private IJsonService jsonService;
	private ApplicationContext applicationContext;

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
	public void setNoticeService(NoticeService noticeService) {
		this.noticeService = noticeService;
	}
	@Resource
	public void setWarningApiService(WarningApiService warningApiService) {
		this.warningApiService = warningApiService;
	}
	@Resource
	public void setJsonService(IJsonService jsonService) {
		this.jsonService = jsonService;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
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

		if(getWarning(id) != null) {
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

		addWarning(id, workWarning);
	}

	@Override
	public void finishCordonWarning(String id) {
		if(getWarning(id) == null) {
			return;
		}

		workWarningDAO.updateStatusByWorkWarningIdStatus(id, WorkWarning.STATUS_CREATE, WorkWarning.STATUS_FINISH);

		removeWarning(id);
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

	protected void addWarning(String id, WorkWarning workWarning) {
		warningMap.put(id,id);

		try {

			String builderUserType = applicationContext.getMessage(workWarning.getBuilderUserType(), null, Locale.CHINESE);
			String waringContent = applicationContext.getMessage("waring.content.cordon", new String[]{builderUserType, workWarning.getNick()}, Locale.CHINESE);

			Map<String, Object> waring = new HashMap<>();
			waring.put("workSegmentStartLongitude", workWarning.getWorkSegmentStartLongitude());
			waring.put("workSegmentStartLatitude", workWarning.getWorkSegmentStartLatitude());
			waring.put("workSegmentEndLongitude", workWarning.getWorkSegmentEndLongitude());
			waring.put("workSegmentEndLatitude", workWarning.getWorkSegmentEndLatitude());

			waring.put("userId", workWarning.getUserId());
			waring.put("nick", workWarning.getNick());
			waring.put("mobile", workWarning.getMobile());
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

	protected void removeWarning(String id) {
		warningMap.remove(id);

		try {
			WarningDeleteReq req = new WarningDeleteReq();
			req.setId(id);

			warningApiService.delete(req);
		} catch (Exception e) {
			logger.error("remove warning error", e);
		}
	}

	protected String getWarning(String id) {
		return warningMap.get(id);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<WorkWarning> workWarningList = getCreateStatus();

		if(workWarningList == null || workWarningList.isEmpty()) {
			return;
		}

		workWarningList.forEach(workWarning -> {
			addWarning(workWarning.getWorkWarningId(), workWarning);
		});
	}
}
