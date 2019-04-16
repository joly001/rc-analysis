package com.zcsoft.rc.analysis.railway.model.entity;

import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.railway.model.entity.RailwayLines;

public class WorkSegmentRailwayLines {

    private WorkSegment workSegment;
    private RailwayLines railwayLines;

    public WorkSegmentRailwayLines(WorkSegment workSegment, RailwayLines railwayLines) {
        this.workSegment = workSegment;
        this.railwayLines = railwayLines;
    }

    public WorkSegment getWorkSegment() {
        return workSegment;
    }

    public RailwayLines getRailwayLines() {
        return railwayLines;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkSegmentRailwayLines{");
        sb.append("workSegment=").append(workSegment);
        sb.append(", railwayLines=").append(railwayLines);
        sb.append('}');
        return sb.toString();
    }
}
