package org.build;

import java.util.Arrays;

public class App2 extends Main{

    public App2() {
        runDemo();
    }

    public App2(String[] args) {
        runDemo();
    }

    private void runDemo() {
        System.out.println("=== Lab2: Iterative methods for SLAU ===");

        /*
         * ВАЖНО:
         * Ниже стоит ПРИМЕР системы.
         * Подставь сюда свою СЛАУ 2 из таблицы 2.1 (исходную или уже преобразованную).
         *
         * Формат:
         * A * x = b
         */
        float[][] A = {
                {10f, 1f, 1f},
                {2f, 10f, 1f},
                {2f, 2f, 10f}
        };
        float[] b = {12f, 13f, 14f};

        // Точность по заданию:
        float targetEps = 1e-3f;

        // Настройки solver'а
        IterativeSolver.Options options = new IterativeSolver.Options();
        options.method = IterativeMethod.SIMPLE_ITERATION; // или SEIDEL
        options.normType = NormType.INF;                   // удобно и стабильно
        options.maxIterations = 100_000;
        options.userEps = targetEps;

        // По заданию: в базовом варианте НЕ контролируем сходимость до запуска
        options.checkConvergenceBeforeStart = false;

        // Базовый критерий остановки по (2.9)
        options.stopCriterion = StopCriterion.SIMPLE_29;

        // Доп. опции (по заданию — опционально)
        options.enableOptionalChecks = true;          // включить доп. проверки
        options.enableFormula25Check = true;          // проверка (2.5): ||alpha|| < 1
        options.enableOverflowGuards = true;

        // Для эксперимента можно переключать метод и критерий:
        // options.method = IterativeMethod.SEIDEL;
        // options.stopCriterion = StopCriterion.SEIDEL_210;

        try {
            CanonicalSystem canonical = CanonicalSystem.fromAxEqualsB(A, b);

            System.out.println("\n-- Canonical form x = alpha*x + beta (2.3) --");
            printMatrix("alpha", canonical.alpha);
            printVector("beta ", canonical.beta);

            // Опциональная проверка (2.5)
            if (options.enableOptionalChecks && options.enableFormula25Check) {
                float alphaNorm = MatrixUtils.matrixNorm(canonical.alpha, options.normType);
                System.out.printf("||alpha||_%s = %.6f%n", options.normType, alphaNorm);
                System.out.println("Condition (2.5) ||alpha|| < 1 : " + (alphaNorm < 1f ? "YES" : "NO"));
            }

            IterativeSolver.Result result = IterativeSolver.solve(canonical, options);

            System.out.println("\n-- Result --");
            System.out.println("Method            : " + result.method);
            System.out.println("Stop criterion    : " + result.usedStopCriterion);
            System.out.println("Iterations        : " + result.iterations);
            System.out.println("Converged         : " + result.converged);
            System.out.println("Stopped by guard  : " + result.stoppedByGuard);
            printVector("x*", result.x);
            System.out.printf("Last delta norm   : %.9f%n", result.lastDeltaNorm);
            if (!Float.isNaN(result.estimatedError)) {
                System.out.printf("Estimated error   : %.9f%n", result.estimatedError);
            }
            System.out.println("Message           : " + result.message);

            // Эксперимент: подбор минимальной eps, при которой вычисления завершаются корректно
            System.out.println("\n-- Epsilon experiment (minimal stable user epsilon) --");
            float found = IterativeSolver.findMinimalStableEpsilon(canonical, options,
                    1e-1f, 1e-12f, 10);

            if (found > 0f) {
                System.out.printf("Approx minimal stable epsilon ~ %.12f%n", found);
            } else {
                System.out.println("Could not find stable epsilon in tested range.");
            }

        } catch (IllegalArgumentException ex) {
            System.out.println("Input error: " + ex.getMessage());
        }
    }

    private static void printMatrix(String name, float[][] m) {
        System.out.println(name + " = ");
        for (float[] row : m) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static void printVector(String name, float[] v) {
        System.out.println(name + " = " + Arrays.toString(v));
    }
}