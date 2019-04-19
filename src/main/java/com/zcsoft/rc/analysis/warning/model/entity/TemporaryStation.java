package com.zcsoft.rc.analysis.warning.model.entity;

import com.zcsoft.rc.warning.model.entity.TrainWarning;

import java.util.Map;

public class TemporaryStation {

    private TrainWarning trainWarning;
    private Map<String, String> currentRcRspMap;

    public TemporaryStation(TrainWarning trainWarning, Map<String, String> currentRcRspMap) {
        this.trainWarning = trainWarning;
        this.currentRcRspMap = currentRcRspMap;
    }

    public TrainWarning getTrainWarning() {
        return trainWarning;
    }

    public Map<String, String> getCurrentRcRspMap() {
        return currentRcRspMap;
    }

    public String get(String id) {
        return currentRcRspMap.get(id);
    }

    public void put(String id) {
        currentRcRspMap.put(id, id);
    }

    public void remove(String id) {
        currentRcRspMap.remove(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TemporaryStation{");
        sb.append("trainWarning=").append(trainWarning);
        sb.append(", currentRcRspMap=").append(currentRcRspMap);
        sb.append('}');
        return sb.toString();
    }
}
