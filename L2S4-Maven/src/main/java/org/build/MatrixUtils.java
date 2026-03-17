package org.build;

public final class MatrixUtils {

    private MatrixUtils() {
    }

    public static float matrixNorm(float[][] m, NormType normType) {
        validateMatrix(m);
        switch (normType) {
            case ONE:
                return matrixNormOne(m);
            case TWO:
                return matrixNormFrobenius(m);
            case INF:
            default:
                return matrixNormInf(m);
        }
    }

    public static float vectorNorm(float[] v, NormType normType) {
        validateVector(v);
        switch (normType) {
            case ONE:
                return vectorNormOne(v);
            case TWO:
                return vectorNormTwo(v);
            case INF:
            default:
                return vectorNormInf(v);
        }
    }

    public static float[][] copy(float[][] src) {
        validateMatrix(src);
        float[][] dst = new float[src.length][src[0].length];
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
        }
        return dst;
    }

    public static float[] copy(float[] src) {
        validateVector(src);
        float[] dst = new float[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static float[] subtract(float[] a, float[] b) {
        validateSameVectorSize(a, b);
        float[] r = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] - b[i];
        }
        return r;
    }

    public static float[] multiply(float[][] a, float[] x) {
        validateMatrix(a);
        validateVector(x);
        if (a[0].length != x.length) {
            throw new IllegalArgumentException("Columns(A) must equal length(x)");
        }

        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            float sum = 0f;
            for (int j = 0; j < x.length; j++) {
                sum += a[i][j] * x[j];
            }
            result[i] = sum;
        }
        return result;
    }

    public static float[] residual(float[][] a, float[] x, float[] b) {
        validateLinearSystem(a, b);
        validateVector(x);
        if (a[0].length != x.length) {
            throw new IllegalArgumentException("Columns(A) must equal length(x)");
        }
        return subtract(multiply(a, x), b);
    }

    public static float maxAbs(float[] v) {
        validateVector(v);
        float max = 0f;
        for (float value : v) {
            float abs = Math.abs(value);
            if (abs > max) {
                max = abs;
            }
        }
        return max;
    }

    public static boolean hasNonFinite(float[] v) {
        validateVector(v);
        for (float x : v) {
            if (Float.isNaN(x) || Float.isInfinite(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean tooLarge(float[] v, float threshold) {
        validateVector(v);
        for (float x : v) {
            if (Math.abs(x) > threshold) {
                return true;
            }
        }
        return false;
    }

    public static SolveReport buildReport(float[][] a,
                                          float[] b,
                                          float[] solution,
                                          float[] reference,
                                          float acceptableDifference) {
        float[] residual = residual(a, solution, b);
        float maxResidual = maxAbs(residual);
        float maxDifference = Float.NaN;

        if (reference != null) {
            maxDifference = maxAbs(subtract(solution, reference));
        }

        StringBuilder comment = new StringBuilder();

        if (maxResidual <= 1e-3f) {
            comment.append("Невязка мала, решение численно хорошее.");
        } else if (maxResidual <= 0.1f) {
            comment.append("Невязка приемлемая для грубой практической проверки, но хуже целевой точности 1e-3.");
        } else {
            comment.append("Невязка велика, решению доверять нельзя без дополнительной проверки.");
        }

        if (!Float.isNaN(maxDifference)) {
            if (maxDifference <= acceptableDifference) {
                comment.append(" Отклонение от опорного решения укладывается в практический порог ±")
                        .append(acceptableDifference).append('.');
            } else {
                comment.append(" Отклонение от опорного решения превышает практический порог ±")
                        .append(acceptableDifference).append('.');
            }
        }

        return new SolveReport(copy(solution), residual, maxResidual, maxDifference, comment.toString());
    }

    public static void validateLinearSystem(float[][] a, float[] b) {
        validateMatrix(a);
        validateVector(b);

        if (a.length != b.length) {
            throw new IllegalArgumentException("Rows(A) must equal length(b)");
        }
        if (a.length != a[0].length) {
            throw new IllegalArgumentException("Matrix A must be square");
        }
    }

    public static void validateMatrix(float[][] a) {
        if (a == null || a.length == 0 || a[0] == null || a[0].length == 0) {
            throw new IllegalArgumentException("Matrix is null or empty");
        }

        int cols = a[0].length;
        for (float[] row : a) {
            if (row == null || row.length != cols) {
                throw new IllegalArgumentException("Matrix rows must have equal length");
            }
        }
    }

    public static void validateVector(float[] v) {
        if (v == null || v.length == 0) {
            throw new IllegalArgumentException("Vector is null or empty");
        }
    }

    private static void validateSameVectorSize(float[] a, float[] b) {
        validateVector(a);
        validateVector(b);
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have equal length");
        }
    }

    private static float matrixNormOne(float[][] m) {
        int rows = m.length;
        int cols = m[0].length;
        float max = 0f;

        for (int j = 0; j < cols; j++) {
            float sum = 0f;
            for (int i = 0; i < rows; i++) {
                sum += Math.abs(m[i][j]);
            }
            if (sum > max) {
                max = sum;
            }
        }
        return max;
    }

    private static float matrixNormInf(float[][] m) {
        float max = 0f;
        for (float[] row : m) {
            float sum = 0f;
            for (float x : row) {
                sum += Math.abs(x);
            }
            if (sum > max) {
                max = sum;
            }
        }
        return max;
    }

    private static float matrixNormFrobenius(float[][] m) {
        double sum = 0.0;
        for (float[] row : m) {
            for (float x : row) {
                sum += (double) x * x;
            }
        }
        return (float) Math.sqrt(sum);
    }

    private static float vectorNormOne(float[] v) {
        float sum = 0f;
        for (float x : v) {
            sum += Math.abs(x);
        }
        return sum;
    }

    private static float vectorNormTwo(float[] v) {
        double sum = 0.0;
        for (float x : v) {
            sum += (double) x * x;
        }
        return (float) Math.sqrt(sum);
    }

    private static float vectorNormInf(float[] v) {
        float max = 0f;
        for (float x : v) {
            float ax = Math.abs(x);
            if (ax > max) {
                max = ax;
            }
        }
        return max;
    }
}