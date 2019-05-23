package com.zcsoft.rc.analysis.railway.model.entity;

import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.railway.model.entity.RailwayLines;

public class WorkSegmentRailwayLine {

    private WorkSegment workSegment;
    private RailwayLines railwayLines;

    public WorkSegment getWorkSegment() {
        return workSegment;
    }

    public void setWorkSegment(WorkSegment workSegment) {
        this.workSegment = workSegment;
    }

    public RailwayLines getRailwayLines() {
        return railwayLines;
    }

    public void setRailwayLines(RailwayLines railwayLines) {
        this.railwayLines = railwayLines;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkSegmentRailwayLine{");
        sb.append("workSegment=").append(workSegment);
        sb.append(", railwayLines=").append(railwayLines);
        sb.append('}');
        return sb.toString();
    }
}
