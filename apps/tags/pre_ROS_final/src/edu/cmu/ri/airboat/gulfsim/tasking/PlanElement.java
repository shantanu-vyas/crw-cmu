package edu.cmu.ri.airboat.gulfsim.tasking;

import gov.nasa.worldwind.geom.Position;
import java.text.DecimalFormat;

public abstract class PlanElement {

    private final String name;
    
    private double priority = 1.0;

    public PlanElement(String name) {
        this.name = name;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }
    
    DecimalFormat format = new DecimalFormat("#.##");
    public String posConcisely(Position p) {
        return "(" + format.format(p.latitude.degrees) + ", " + format.format(p.longitude.degrees) + ")";
    }
}
