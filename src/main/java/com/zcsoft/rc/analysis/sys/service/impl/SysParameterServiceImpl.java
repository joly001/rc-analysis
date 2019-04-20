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
	public void updateSysParameterCache() {
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
	public double getTrainApproachingDistance() {
		String trainApproachingDistance = sysParameterMap.get(SysParameter.KEY_TRAIN_APPROACHING_DISTANCE);
		double distance =  Double.valueOf(trainApproachingDistance)*1000;

		return distance;
	}

	@Override
	public int getNumberAlarmadvanceStations() {
		String numberAlarmadvanceStations = sysParameterMap.get(SysParameter.KEY_NUMBER_ALARM_ADVANCE_STATIONS);

		return Integer.valueOf(numberAlarmadvanceStations);
	}

	@Override
	public int getRollingLimitDistance() {
		String rollingLimitDistance = sysParameterMap.get(SysParameter.KEY_ROLLING_LIMIT_DISTANCE);

		return Integer.valueOf(rollingLimitDistance);
	}

	@Override
	public String getTypeRollingAlarmMachinery() {
		return sysParameterMap.get(SysParameter.KEY_TYPE_ROLLING_ALARM_MACHINERY);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		updateSysParameterCache();
	}
}
