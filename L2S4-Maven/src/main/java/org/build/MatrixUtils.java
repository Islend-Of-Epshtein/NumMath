package org.build;

public final class MatrixUtils {

    private MatrixUtils() {
    }

    public static float matrixNorm(float[][] m, NormType normType) {
        switch (normType) {
            case ONE:
                return matrixNormOne(m);
            case TWO:
                return matrixNormFrobenius(m); // учебное приближение
            case INF:
            default:
                return matrixNormInf(m);
        }
    }

    public static float vectorNorm(float[] v, NormType normType) {
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

    public static float[] copy(float[] src) {
        float[] dst = new float[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static float[] subtract(float[] a, float[] b) {
        float[] r = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] - b[i];
        }
        return r;
    }

    public static boolean hasNonFinite(float[] v) {
        for (float x : v) {
            if (Float.isNaN(x) || Float.isInfinite(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean tooLarge(float[] v, float threshold) {
        for (float x : v) {
            if (Math.abs(x) > threshold) {
                return true;
            }
        }
        return false;
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