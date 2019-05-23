package com.zcsoft.rc.analysis.railway.model.entity;

import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.railway.model.entity.RailwayLines;

public class WorkSegmentRailwayLines {

    private WorkSegment workSegment;
    private RailwayLines previousRailwayLines;
    private RailwayLines nextRailwayLines;

    public WorkSegmentRailwayLines(WorkSegment workSegment, RailwayLines previousRailwayLines, RailwayLines nextRailwayLines) {
        this.workSegment = workSegment;
        this.previousRailwayLines = previousRailwayLines;
        this.nextRailwayLines = nextRailwayLines;
    }

    public WorkSegment getWorkSegment() {
        return workSegment;
    }

    public RailwayLines getPreviousRailwayLines() {
        return previousRailwayLines;
    }

    public RailwayLines getNextRailwayLines() {
        return nextRailwayLines;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkSegmentRailwayLines{");
        sb.append("workSegment=").append(workSegment);
        sb.append(", previousRailwayLines=").append(previousRailwayLines);
        sb.append(", nextRailwayLines=").append(nextRailwayLines);
        sb.append('}');
        return sb.toString();
    }
}
