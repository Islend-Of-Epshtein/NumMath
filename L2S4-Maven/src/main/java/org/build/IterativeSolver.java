package org.build;

public final class IterativeSolver {

    private IterativeSolver() {
    }

    public static class Options {
        public IterativeMethod method = IterativeMethod.SIMPLE_ITERATION;
        public StopCriterion stopCriterion = StopCriterion.SIMPLE_29;
        public NormType normType = NormType.INF;
        public float userEps = 1e-3f;
        public int maxIterations = 10000;
        public boolean checkConvergenceBeforeStart = false;
        public boolean enableOptionalChecks = true;
        public boolean enableFormula25Check = true;
        public boolean enableOverflowGuards = true;
        public float explosionThreshold = 1e30f;
    }

    public static class Result {
        public float[] x;
        public int iterations;
        public boolean converged;
        public boolean stoppedByGuard;
        public float lastDeltaNorm;
        public float estimatedError;
        public float alphaNorm;
        public float alpha2Norm;
        public String message;
        public IterativeMethod method;
        public StopCriterion usedStopCriterion;
    }

    public static Result solve(CanonicalSystem cs, Options opt) {
        validateOptions(opt);

        float[][] alpha = cs.alpha;
        float[] beta = cs.beta;

        Result result = new Result();
        result.method = opt.method;
        result.usedStopCriterion = opt.stopCriterion;
        result.alphaNorm = MatrixUtils.matrixNorm(alpha, opt.normType);
        result.alpha2Norm = MatrixUtils.matrixNorm(strictUpper(alpha), opt.normType);
        result.estimatedError = Float.NaN;

        if (opt.checkConvergenceBeforeStart && opt.enableFormula25Check && result.alphaNorm >= 1f) {
            result.x = MatrixUtils.copy(beta);
            result.message = "Условие (2.5) не выполнено до старта: ||alpha|| >= 1";
            return result;
        }

        float[] prev = MatrixUtils.copy(beta);
        float[] cur = new float[beta.length];

        for (int k = 1; k <= opt.maxIterations; k++) {
            if (opt.method == IterativeMethod.SEIDEL) {
                stepSeidel(alpha, beta, prev, cur);
            } else {
                stepSimple(alpha, beta, prev, cur);
            }

            if (opt.enableOverflowGuards) {
                if (MatrixUtils.hasNonFinite(cur)) {
                    result.x = MatrixUtils.copy(cur);
                    result.iterations = k;
                    result.stoppedByGuard = true;
                    result.message = "Остановлено: NaN или Infinity";
                    return result;
                }
                if (MatrixUtils.tooLarge(cur, opt.explosionThreshold)) {
                    result.x = MatrixUtils.copy(cur);
                    result.iterations = k;
                    result.stoppedByGuard = true;
                    result.message = "Остановлено: превышен порог переполнения";
                    return result;
                }
            }

            float[] delta = MatrixUtils.subtract(cur, prev);
            float deltaNorm = MatrixUtils.vectorNorm(delta, opt.normType);
            result.lastDeltaNorm = deltaNorm;
            result.estimatedError = estimateError(opt, result.alphaNorm, result.alpha2Norm, deltaNorm);

            if (shouldStop(opt, result.alphaNorm, result.alpha2Norm, deltaNorm, result.estimatedError)) {
                result.x = MatrixUtils.copy(cur);
                result.iterations = k;
                result.converged = true;
                result.message = "Итерационный процесс завершён";
                return result;
            }

            float[] tmp = prev;
            prev = cur;
            cur = tmp;
        }

        result.x = MatrixUtils.copy(prev);
        result.iterations = opt.maxIterations;
        result.message = "Достигнут лимит по числу итераций";
        return result;
    }

    public static float findMinimalStableEpsilon(CanonicalSystem cs,
                                                 Options base,
                                                 float startEps,
                                                 float minEps,
                                                 int stepsPerDecade) {
        if (startEps <= 0f || minEps <= 0f || stepsPerDecade <= 0 || minEps > startEps) {
            throw new IllegalArgumentException("Некорректные параметры поиска epsilon");
        }

        float lastGood = -1f;
        float eps = startEps;
        float factor = (float) Math.pow(10.0, 1.0 / stepsPerDecade);

        while (eps >= minEps) {
            Options copy = copyOptions(base);
            copy.userEps = eps;

            Result result = solve(cs, copy);
            if (result.converged && !result.stoppedByGuard) {
                lastGood = eps;
                eps /= factor;
            } else {
                break;
            }
        }

        return lastGood;
    }

    private static boolean shouldStop(Options opt,
                                      float alphaNorm,
                                      float alpha2Norm,
                                      float deltaNorm,
                                      float estimatedError) {
        switch (opt.stopCriterion) {
            case SIMPLE_29:
                return deltaNorm <= opt.userEps;
            case SIMPLE_28:
                return alphaNorm < 1f && !Float.isNaN(estimatedError) && estimatedError <= opt.userEps;
            case SEIDEL_210:
                return opt.method == IterativeMethod.SEIDEL
                        && alphaNorm < 1f
                        && !Float.isNaN(estimatedError)
                        && estimatedError <= opt.userEps;
            default:
                return false;
        }
    }

    private static float estimateError(Options opt, float alphaNorm, float alpha2Norm, float deltaNorm) {
        if (opt.stopCriterion == StopCriterion.SIMPLE_28) {
            if (alphaNorm >= 1f) {
                return Float.NaN;
            }
            return (alphaNorm / (1f - alphaNorm)) * deltaNorm;
        }

        if (opt.stopCriterion == StopCriterion.SEIDEL_210) {
            if (alphaNorm >= 1f) {
                return Float.NaN;
            }
            return (alpha2Norm / (1f - alphaNorm)) * deltaNorm;
        }

        return deltaNorm;
    }

    private static void stepSimple(float[][] alpha, float[] beta, float[] prev, float[] cur) {
        for (int i = 0; i < beta.length; i++) {
            float sum = beta[i];
            for (int j = 0; j < beta.length; j++) {
                sum += alpha[i][j] * prev[j];
            }
            cur[i] = sum;
        }
    }

    private static void stepSeidel(float[][] alpha, float[] beta, float[] prev, float[] cur) {
        for (int i = 0; i < beta.length; i++) {
            float sum = beta[i];

            for (int j = 0; j < i; j++) {
                sum += alpha[i][j] * cur[j];
            }
            for (int j = i; j < beta.length; j++) {
                sum += alpha[i][j] * prev[j];
            }

            cur[i] = sum;
        }
    }

    private static float[][] strictUpper(float[][] matrix) {
        float[][] result = new float[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix.length; j++) {
                result[i][j] = matrix[i][j];
            }
        }
        return result;
    }

    private static Options copyOptions(Options src) {
        Options dst = new Options();
        dst.method = src.method;
        dst.stopCriterion = src.stopCriterion;
        dst.normType = src.normType;
        dst.userEps = src.userEps;
        dst.maxIterations = src.maxIterations;
        dst.checkConvergenceBeforeStart = src.checkConvergenceBeforeStart;
        dst.enableOptionalChecks = src.enableOptionalChecks;
        dst.enableFormula25Check = src.enableFormula25Check;
        dst.enableOverflowGuards = src.enableOverflowGuards;
        dst.explosionThreshold = src.explosionThreshold;
        return dst;
    }

    private static void validateOptions(Options opt) {
        if (opt == null) {
            throw new IllegalArgumentException("Options must not be null");
        }
        if (opt.userEps <= 0f) {
            throw new IllegalArgumentException("userEps must be > 0");
        }
        if (opt.maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be > 0");
        }
        if (opt.stopCriterion == StopCriterion.SEIDEL_210 && opt.method != IterativeMethod.SEIDEL) {
            throw new IllegalArgumentException("Критерий (2.10) можно использовать только для метода Зейделя");
        }
    }
}