package com.zcsoft.rc.analysis.cordon.service.impl;

import com.zcsoft.rc.analysis.cordon.dao.CordonDAO;
import com.zcsoft.rc.analysis.cordon.service.CordonService;
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
    public void analysis() {
        String id = cordonDAO.near("geometry",86.1937714521014,41.74397046715647,1.2,0);


    }
}
