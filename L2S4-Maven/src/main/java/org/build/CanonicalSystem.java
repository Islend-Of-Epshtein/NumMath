package org.build;

public final class CanonicalSystem {

    public final float[][] alpha;
    public final float[] beta;

    public CanonicalSystem(float[][] alpha, float[] beta) {
        MatrixUtils.validateMatrix(alpha);
        MatrixUtils.validateVector(beta);
        if (alpha.length != beta.length || alpha.length != alpha[0].length) {
            throw new IllegalArgumentException("Canonical system dimensions are inconsistent");
        }
        this.alpha = MatrixUtils.copy(alpha);
        this.beta = MatrixUtils.copy(beta);
    }

    public static CanonicalSystem fromAxEqualsB(float[][] a, float[] b) {
        MatrixUtils.validateLinearSystem(a, b);

        int n = a.length;
        float[][] alpha = new float[n][n];
        float[] beta = new float[n];

        for (int i = 0; i < n; i++) {
            float aii = a[i][i];
            if (Math.abs(aii) < 1e-7f) {
                throw new IllegalArgumentException(
                        "Diagonal element a[" + i + "][" + i + "] is zero or too small"
                );
            }

            beta[i] = b[i] / aii;

            for (int j = 0; j < n; j++) {
                alpha[i][j] = (i == j) ? 0f : -a[i][j] / aii;
            }
        }

        return new CanonicalSystem(alpha, beta);
    }
}