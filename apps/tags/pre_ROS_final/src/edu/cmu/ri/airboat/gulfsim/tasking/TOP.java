/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim.tasking;

import edu.cmu.ri.airboat.gulfsim.tasking.PlanOperator;

/**
 *
 * @author pscerri
 */
public class TOP {
    private final String name;

    private final PlanOperator op;
        
    public TOP(String name, PlanOperator op) {
        this.name = name;
        this.op = op;        
    }

    public PlanOperator getRootOp() {
        return op;
    }
        
    public String toString() {
        return "A TOP";
    }
}
