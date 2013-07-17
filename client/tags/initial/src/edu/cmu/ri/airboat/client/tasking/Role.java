package edu.cmu.ri.airboat.client.tasking;

import gov.nasa.worldwind.geom.Position;

public class Role extends PlanElement {
    
    private final Position location;

    public Role(String name, Position location) {
        super(name);        
        this.location = location;
    }

    public Position getLocation() {
        return location;
    }
        
}
