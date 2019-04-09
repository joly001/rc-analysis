package com.zcsoft.rc.analysis.warning.controller;


import com.sharingif.cube.core.handler.bind.annotation.RequestMapping;
import com.zcsoft.rc.analysis.warning.service.WorkWarningService;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;


@Controller
@RequestMapping(value="workWarning")
public class WorkWarningController {
	
	private WorkWarningService workWarningService;

	@Resource
	public void setWorkWarningService(WorkWarningService workWarningService) {
		this.workWarningService = workWarningService;
	}
	
}
