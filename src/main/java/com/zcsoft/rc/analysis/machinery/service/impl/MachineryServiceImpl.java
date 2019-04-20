package com.zcsoft.rc.analysis.machinery.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.machinery.service.MachineryService;
import com.zcsoft.rc.machinery.dao.MachineryDAO;
import com.zcsoft.rc.machinery.model.entity.Machinery;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MachineryServiceImpl extends BaseServiceImpl<Machinery, String> implements MachineryService, InitializingBean {

	private Map<String, Machinery> machineryCache = new ConcurrentHashMap<>(50);

	private MachineryDAO machineryDAO;

	@Resource
	public void setMachineryDAO(MachineryDAO machineryDAO) {
		super.setBaseDAO(machineryDAO);
		this.machineryDAO = machineryDAO;
	}

	@Override
	public void setMachineryCache() {
		List<Machinery> machineryList = machineryDAO.queryAll();

		if(machineryList == null || machineryList.isEmpty()) {
			return;
		}

		machineryList.forEach(machinery -> {
			machineryCache.put(machinery.getId(), machinery);
		});
	}

	@Override
	public Machinery getMachinery(String id) {
		return machineryCache.get(id);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setMachineryCache();
	}
}
