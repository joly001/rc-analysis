package com.zcsoft.rc.analysis.sys.scheduled;

import com.zcsoft.rc.analysis.sys.service.SysParameterService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class SysParameterScheduled {

    private SysParameterService sysParameterService;

    @Resource
    public void setSysParameterService(SysParameterService sysParameterService) {
        this.sysParameterService = sysParameterService;
    }

    @Scheduled(fixedRate = 1000*60*1)
    public synchronized void updateSysParameter() {
        sysParameterService.updateSysParameterCache();
    }

}
