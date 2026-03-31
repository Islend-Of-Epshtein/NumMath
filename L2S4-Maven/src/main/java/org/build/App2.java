package org.build;

import Jama.Matrix;

import java.util.Arrays;

public class App2 {

    public App2() {
        runDemo();
    }

    public App2(String[] args) {
        runDemo();
    }

    private void runDemo() {
        System.out.println("=== Пункт 2: итерационные методы и прогонка ===");

        float[][] a = {
                {20f, 4f, 3f},
                {5f, 30f, 6f},
                {1f, 2f, 10f}
        };
        float[] b = {27f, 41f, 3f};

        float[] reference =
                GaussianEliminationSolver.solve(a, b, GaussianPivotMode.PARTIAL_BY_COLUMN).solution;

        CanonicalSystem canonical = CanonicalSystem.fromAxEqualsB(a, b);

        System.out.println("\nalpha = ");
        printMatrix(canonical.alpha);
        System.out.println("beta = " + Arrays.toString(canonical.beta));

        IterativeSolver.Options simpleOptions = new IterativeSolver.Options();
        simpleOptions.method = IterativeMethod.SIMPLE_ITERATION;
        simpleOptions.stopCriterion = StopCriterion.SIMPLE_28;
        simpleOptions.userEps = 1e-3f;
        simpleOptions.maxIterations = 100_000;
        simpleOptions.checkConvergenceBeforeStart = false;

        printIterativeCase("Метод простых итераций", a, b, canonical, simpleOptions, reference);

        IterativeSolver.Options seidelOptions = new IterativeSolver.Options();
        seidelOptions.method = IterativeMethod.SEIDEL;
        seidelOptions.stopCriterion = StopCriterion.SEIDEL_210;
        seidelOptions.userEps = 1e-3f;
        seidelOptions.maxIterations = 100_000;
        seidelOptions.checkConvergenceBeforeStart = false;

        printIterativeCase("Метод Зейделя", a, b, canonical, seidelOptions, reference);

        float minStableEps = IterativeSolver.findMinimalStableEpsilon(
                canonical,
                seidelOptions,
                1e-1f,
                1e-7f,
                5
        );
        System.out.println("\nМинимальный стабильный epsilon для текущего примера: " + minStableEps);

        printTridiagonalDemo();
    }

    private static void printIterativeCase(String title,
                                           float[][] a,
                                           float[] b,
                                           CanonicalSystem canonical,
                                           IterativeSolver.Options options,
                                           float[] reference) {
        System.out.println("\n--- " + title + " ---");

        float alphaNorm = MatrixUtils.matrixNorm(canonical.alpha, options.normType);
        System.out.println("||alpha|| = " + alphaNorm);
        System.out.println("Условие (2.5): " + (alphaNorm < 1f ? "выполнено" : "не выполнено"));

        IterativeSolver.Result result = IterativeSolver.solve(canonical, options);
        SolveReport report = MatrixUtils.buildReport(a, b, result.x, reference, 0.1f);

        System.out.println("Метод: " + result.method);
        System.out.println("Критерий остановки: " + result.usedStopCriterion);
        System.out.println("Итераций: " + result.iterations);
        System.out.println("Сошёлся: " + result.converged);
        System.out.println("Остановлен защитой: " + result.stoppedByGuard);
        System.out.println("x = " + Arrays.toString(result.x));
        System.out.println("||x(k)-x(k-1)|| = " + result.lastDeltaNorm);
        System.out.println("Оценка погрешности = " + result.estimatedError);
        System.out.println("max|r| = " + report.maxResidual);
        System.out.println("max|x - x_ref| = " + report.maxDifferenceFromReference);
        System.out.println("Комментарий: " + report.comment);

        if (result.converged && report.maxResidual > 0.1f) {
            System.out.println("Внимание: критерий остановки сработал, но невязка всё ещё велика.");
        }
    }

    private static void printTridiagonalDemo() {
        System.out.println("\n--- Метод прогонки ---");
        float[] lower = {1f, 1f, 1f};
        float[] diagonal = {4f, 4f, 4f, 4f};
        float[] upper = {1f, 1f, 1f};
        float[] rhs = {5f, 6f, 6f, 5f};

        float[] solution = TridiagonalSolver.solve(lower, diagonal, upper, rhs);

        float[][] fullMatrix = {
                {4f, 1f, 0f, 0f},
                {1f, 4f, 1f, 0f},
                {0f, 1f, 4f, 1f},
                {0f, 0f, 1f, 4f}
        };

        SolveReport report = MatrixUtils.buildReport(fullMatrix, rhs, solution, null, 0.1f);

        System.out.println("x = " + Arrays.toString(solution));
        System.out.println("max|r| = " + report.maxResidual);
        System.out.println("Комментарий: " + report.comment);
    }

    private static void printMatrix(float[][] matrix) {
        for (float[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}