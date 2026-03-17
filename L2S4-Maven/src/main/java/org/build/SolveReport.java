package org.build;

public final class SolveReport {
    public final float[] solution;
    public final float[] residual;
    public final float maxResidual;
    public final float maxDifferenceFromReference;
    public final String comment;

    public SolveReport(float[] solution,
                       float[] residual,
                       float maxResidual,
                       float maxDifferenceFromReference,
                       String comment) {
        this.solution = solution;
        this.residual = residual;
        this.maxResidual = maxResidual;
        this.maxDifferenceFromReference = maxDifferenceFromReference;
        this.comment = comment;
    }
}