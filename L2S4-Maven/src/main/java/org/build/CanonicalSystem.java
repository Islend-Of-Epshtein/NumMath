package org.build;

public class CanonicalSystem {

    public final float[][] alpha;
    public final float[] beta;

    public CanonicalSystem(float[][] alpha, float[] beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Приведение Ax=b к виду x = alpha*x + beta (2.3)
     */
    public static CanonicalSystem fromAxEqualsB(float[][] a, float[] b) {
        validate(a, b);

        int n = a.length;
        float[][] alpha = new float[n][n];
        float[] beta = new float[n];

        for (int i = 0; i < n; i++) {
            float aii = a[i][i];
            if (aii == 0f) {
                throw new IllegalArgumentException(
                        "Diagonal element a[" + i + "][" + i + "] = 0"
                );
            }

            beta[i] = b[i] / aii;

            for (int j = 0; j < n; j++) {
                if (i == j) {
                    alpha[i][j] = 0f;
                } else {
                    alpha[i][j] = -a[i][j] / aii;
                }
            }
        }

        return new CanonicalSystem(alpha, beta);
    }

    private static void validate(float[][] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("A or b is null");
        }
        if (a.length == 0) {
            throw new IllegalArgumentException("Matrix A is empty");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException("Rows(A) must equal length(b)");
        }

        int n = a.length;
        for (int i = 0; i < n; i++) {
            if (a[i] == null || a[i].length != n) {
                throw new IllegalArgumentException("Matrix A must be square");
            }
        }
    }
}