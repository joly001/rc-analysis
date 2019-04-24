package com.zcsoft.rc.analysis.cable.dao;

import com.zcsoft.rc.analysis.app.dao.ICubeMongoDBDAO;

public interface CablePolygonDAO extends ICubeMongoDBDAO {

    /**
     * 查询点是否在某个多边行内
     * @param fieldName
     * @param longitude
     * @param latitude
     * @return
     */
    String intersects(String fieldName, double longitude, double latitude);

}
