package com.zcsoft.rc.analysis.warning.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import com.zcsoft.rc.machinery.dao.MachineryDAO;
import com.zcsoft.rc.machinery.model.entity.Machinery;
import com.zcsoft.rc.user.dao.OrganizationDAO;
import com.zcsoft.rc.user.dao.UserDAO;
import com.zcsoft.rc.user.model.entity.Organization;
import com.zcsoft.rc.user.model.entity.User;
import com.zcsoft.rc.warning.dao.WorkWarningDAO;
import com.zcsoft.rc.warning.model.entity.WorkWarning;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WorkWarningServiceImpl extends BaseServiceImpl<WorkWarning, java.lang.String> implements WorkWarningService {
	
	private WorkWarningDAO workWarningDAO;
	private UserDAO userDAO;
	private MachineryDAO machineryDAO;
	private OrganizationDAO organizationDAO;

	@Resource
	public void setWorkWarningDAO(WorkWarningDAO workWarningDAO) {
		super.setBaseDAO(workWarningDAO);
		this.workWarningDAO = workWarningDAO;
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

	public Organization getDep(String organizationId) {
		Organization organization = organizationDAO.queryById(organizationId);
		if(organization.getParentId() == null) {
			return organization;
		}

		return getDep(organization.getParentId());
	}

	@Override
	public void addCordonWarning(Double longitude, Double latitude, String type, String id) {

		User user;
		if(User.BUILDER_USER_TYPE_LOCOMOTIVE.equals(type)) {
			Machinery machinery = machineryDAO.queryById(id);
			user = userDAO.queryById(machinery.getUserId());
		} else {
			user = userDAO.queryById(id);
		}

		Organization organization = organizationDAO.queryById(user.getOrganizationId());

		Organization dep;
		if(organization.getParentId() == null) {
			dep = organization;
		} else {
			dep = getDep(organization.getParentId());
		}

		WorkWarning workWarning = new WorkWarning();
		workWarning.setUserId(user.getId());
		workWarning.setBuilderUserType(user.getBuilderUserType());
		workWarning.setDepId(dep.getId());
		workWarning.setDepName(dep.getOrgName());
		workWarning.setOrgId(organization.getId());
		workWarning.setOrgName(organization.getOrgName());
		workWarning.setNick(user.getNick());
		workWarning.setMobile(user.getMobile());
		workWarning.setStatus(WorkWarning.STATUS_CREATE);
		workWarning.setType(WorkWarning.TYPE_APPROACHING_THE_WARNING_LINE);

		workWarningDAO.insert(workWarning);
	}
}
