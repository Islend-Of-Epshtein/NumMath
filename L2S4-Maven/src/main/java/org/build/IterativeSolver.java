package org.build;

public final class IterativeSolver {

    private IterativeSolver() {
    }

    public static class Options {
        public StopCriterion stopCriterion = StopCriterion.SIMPLE_29;
        public NormType normType = NormType.INF;

        public float userEps = 1e-3f;
        public int maxIterations = 10000;

        public boolean checkConvergenceBeforeStart = true;

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
        public String message;
        public StopCriterion usedStopCriterion;
    }

    public static Result solve(CanonicalSystem cs, Options opt) {
        validateOptions(opt);

        float[][] alpha = cs.alpha;
        float[] beta = cs.beta;
        int n = beta.length;

        Result result = new Result();
        result.usedStopCriterion = opt.stopCriterion;
        result.estimatedError = Float.NaN;

        float alphaNorm = MatrixUtils.matrixNorm(alpha, opt.normType);

        if (opt.checkConvergenceBeforeStart && opt.enableFormula25Check && alphaNorm >= 1f) {
            result.x = MatrixUtils.copy(beta);
            result.iterations = 0;
            result.converged = false;
            result.stoppedByGuard = false;
            result.message = "Condition (2.5) is not satisfied before start";
            return result;
        }

        float[] prev = MatrixUtils.copy(beta); // x^(0) = beta
        float[] cur = new float[n];

        float alpha2Norm = 0f;
        if (opt.stopCriterion == StopCriterion.SEIDEL_210) {
            alpha2Norm = MatrixUtils.matrixNorm(upperStrict(alpha), opt.normType);
        }

        for (int k = 1; k <= opt.maxIterations; k++) {
            stepSimple(alpha, beta, prev, cur);

            if (opt.enableOverflowGuards) {
                if (MatrixUtils.hasNonFinite(cur)) {
                    result.x = MatrixUtils.copy(cur);
                    result.iterations = k;
                    result.converged = false;
                    result.stoppedByGuard = true;
                    result.message = "NaN/Infinity detected";
                    return result;
                }
                if (MatrixUtils.tooLarge(cur, opt.explosionThreshold)) {
                    result.x = MatrixUtils.copy(cur);
                    result.iterations = k;
                    result.converged = false;
                    result.stoppedByGuard = true;
                    result.message = "Explosion threshold exceeded";
                    return result;
                }
            }

            float[] delta = MatrixUtils.subtract(cur, prev);
            float deltaNorm = MatrixUtils.vectorNorm(delta, opt.normType);
            result.lastDeltaNorm = deltaNorm;

            boolean stop = false;
            float estimatedError = Float.NaN;

            if (opt.stopCriterion == StopCriterion.SIMPLE_29) {
                // (2.9)
                stop = deltaNorm <= opt.userEps;
            } else if (opt.stopCriterion == StopCriterion.SIMPLE_28) {
                // (2.8)
                if (alphaNorm < 1f) {
                    estimatedError = (alphaNorm / (1f - alphaNorm)) * deltaNorm;
                    stop = estimatedError <= opt.userEps;
                }
            } else if (opt.stopCriterion == StopCriterion.SEIDEL_210) {
                // (2.10)
                if (alphaNorm < 1f) {
                    estimatedError = (alpha2Norm / (1f - alphaNorm)) * deltaNorm;
                    stop = estimatedError <= opt.userEps;
                }
            }

            result.estimatedError = estimatedError;

            if (stop) {
                result.x = MatrixUtils.copy(cur);
                result.iterations = k;
                result.converged = true;
                result.stoppedByGuard = false;
                result.message = "Converged";
                return result;
            }

            // swap prev <-> cur
            float[] tmp = prev;
            prev = cur;
            cur = tmp;
        }

        result.x = MatrixUtils.copy(prev);
        result.iterations = opt.maxIterations;
        result.converged = false;
        result.stoppedByGuard = false;
        result.message = "Max iterations reached";
        return result;
    }

    public static float findMinimalStableEpsilon(CanonicalSystem cs,
                                                 Options base,
                                                 float startEps,
                                                 float minEps,
                                                 int stepsPerDecade) {
        if (startEps <= 0f || minEps <= 0f || stepsPerDecade <= 0 || minEps > startEps) {
            throw new IllegalArgumentException("Invalid epsilon search parameters");
        }

        float lastGood = -1f;
        float eps = startEps;

        while (eps >= minEps) {
            Options copy = copyOptions(base);
            copy.userEps = eps;

            Result r = solve(cs, copy);

            if (r.converged && !r.stoppedByGuard) {
                lastGood = eps;
            } else {
                break;
            }

            float factor = (float) Math.pow(10.0, 1.0 / stepsPerDecade);
            eps /= factor;
        }

        return lastGood;
    }

    private static void stepSimple(float[][] alpha, float[] beta, float[] prev, float[] cur) {
        int n = beta.length;
        for (int i = 0; i < n; i++) {
            float sum = beta[i];
            for (int j = 0; j < n; j++) {
                sum += alpha[i][j] * prev[j];
            }
            cur[i] = sum;
        }
    }

    private static void stepSeidel(float[][] alpha, float[] beta, float[] prev, float[] cur) {
        int n = beta.length;
        for (int i = 0; i < n; i++) {
            float sum = beta[i];

            for (int j = 0; j < i; j++) {
                sum += alpha[i][j] * cur[j];
            }
            for (int j = i; j < n; j++) {
                sum += alpha[i][j] * prev[j];
            }

            cur[i] = sum;
        }
    }

    private static float[][] upperStrict(float[][] a) {
        int n = a.length;
        float[][] r = new float[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                r[i][j] = a[i][j];
            }
        }
        return r;
    }

    private static Options copyOptions(Options src) {
        Options dst = new Options();
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
        if (opt.userEps <= 0f) {
            throw new IllegalArgumentException("userEps must be > 0");
        }
        if (opt.maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be > 0");
        }
    }
}