package com.zcsoft.rc.analysis.cable.dao;

public interface SafetyZoneDAO {

    /**
     * 查询点是否在某个多边行内
     * @param fieldName
     * @param longitude
     * @param latitude
     * @return
     */
    String intersects(String fieldName, double longitude, double latitude);

}
