package org.build;

public final class TridiagonalSolver {

    private static final float EPS = 1e-7f;

    private TridiagonalSolver() {
    }

    public static float[] solve(float[] lower, float[] diagonal, float[] upper, float[] rhs) {
        validate(lower, diagonal, upper, rhs);

        int n = diagonal.length;

        float[] alpha = new float[n];
        float[] beta = new float[n];

        float denominator = diagonal[0];
        if (Math.abs(denominator) < EPS) {
            throw new IllegalArgumentException("Нулевой знаменатель на первом шаге метода прогонки");
        }

        alpha[0] = (n == 1) ? 0f : -upper[0] / denominator;
        beta[0] = rhs[0] / denominator;

        for (int i = 1; i < n; i++) {
            denominator = diagonal[i] + lower[i - 1] * alpha[i - 1];
            if (Math.abs(denominator) < EPS) {
                throw new IllegalArgumentException("Нулевой знаменатель на шаге " + i + " метода прогонки");
            }

            if (i < n - 1) {
                alpha[i] = -upper[i] / denominator;
            }
            beta[i] = (rhs[i] - lower[i - 1] * beta[i - 1]) / denominator;
        }

        float[] x = new float[n];
        x[n - 1] = beta[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            x[i] = alpha[i] * x[i + 1] + beta[i];
        }

        return x;
    }

    private static void validate(float[] lower, float[] diagonal, float[] upper, float[] rhs) {
        MatrixUtils.validateVector(diagonal);
        MatrixUtils.validateVector(rhs);

        if (diagonal.length != rhs.length) {
            throw new IllegalArgumentException("Размеры diagonal и rhs должны совпадать");
        }

        int n = diagonal.length;

        if (n == 1) {
            if ((lower != null && lower.length != 0) || (upper != null && upper.length != 0)) {
                throw new IllegalArgumentException("Для n = 1 нижняя и верхняя диагонали должны быть пустыми");
            }
            return;
        }

        if (lower == null || upper == null || lower.length != n - 1 || upper.length != n - 1) {
            throw new IllegalArgumentException("Для трехдиагональной матрицы lower и upper должны иметь длину n - 1");
        }
    }
}