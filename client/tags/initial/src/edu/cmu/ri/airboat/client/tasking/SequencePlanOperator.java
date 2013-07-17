package edu.cmu.ri.airboat.client.tasking;

public class SequencePlanOperator extends PlanOperator {

    TOP outer;

    public SequencePlanOperator(String name, TOP outer) {
        super(name);
        this.outer = outer;
    }
}
