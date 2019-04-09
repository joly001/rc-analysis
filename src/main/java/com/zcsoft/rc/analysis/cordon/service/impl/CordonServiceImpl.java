package com.zcsoft.rc.analysis.cordon.service.impl;

import com.zcsoft.rc.analysis.cordon.dao.CordonDAO;
import com.zcsoft.rc.analysis.cordon.service.CordonService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CordonServiceImpl implements CordonService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private CordonDAO cordonDAO;

    @Resource
    public void setCordonDAO(CordonDAO cordonDAO) {
        this.cordonDAO = cordonDAO;
    }

    @Override
    public void analysis(CurrentRcRsp rcRsp) {
        String id = cordonDAO.near("geometry",rcRsp.getLongitude(),rcRsp.getLatitude(),1.2,0);

        logger.info("id:{}", id);
    }
}
