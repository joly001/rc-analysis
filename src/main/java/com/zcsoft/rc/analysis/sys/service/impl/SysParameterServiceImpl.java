package com.zcsoft.rc.analysis.sys.service.impl;


import com.sharingif.cube.support.service.base.impl.BaseServiceImpl;
import com.zcsoft.rc.analysis.sys.service.SysParameterService;
import com.zcsoft.rc.sys.dao.SysParameterDAO;
import com.zcsoft.rc.sys.model.entity.SysParameter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SysParameterServiceImpl extends BaseServiceImpl<SysParameter, String> implements SysParameterService, InitializingBean {

	private Map<String, String> sysParameterMap = new ConcurrentHashMap<>(50);
	
	private SysParameterDAO sysParameterDAO;

	@Resource
	public void setSysParameterDAO(SysParameterDAO sysParameterDAO) {
		super.setBaseDAO(sysParameterDAO);
		this.sysParameterDAO = sysParameterDAO;
	}

	@Override
	public void updateSysParameter() {
		List<SysParameter> sysParameterList = sysParameterDAO.queryAll();

		if(sysParameterList == null || sysParameterList.isEmpty()) {
			return;
		}

		sysParameterList.forEach(sysParameter -> {
			sysParameterMap.put(sysParameter.getParameterName(), sysParameter.getParameterValue());
		});
	}

	@Override
	public double getCordon() {
		String cordon = sysParameterMap.get(SysParameter.KEY_CORDON);
		return Double.valueOf(cordon);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		updateSysParameter();
	}
}
