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
        System.out.println("=== Лабораторная работа 2: Итерационные методы решения СЛАУ ===");

        float[][] A = {
                {20f, 4f, 3f},
                {5f, 30f, 6f},
                {1f, 2f, 10f}
        };
        float[] b = {27f, 41f, 3f};

        // Точность по заданию:
        float targetEps = 1e-3f;

        // Настройки решателя
        IterativeSolver.Options options = new IterativeSolver.Options();
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

            System.out.println("\n-- x = alpha*x + beta  --");
            printMatrix("alpha", canonical.alpha);
            printVector("beta", canonical.beta);

            // Опциональная проверка (2.5)
            if (options.enableOptionalChecks && options.enableFormula25Check) {
                float alphaNorm = MatrixUtils.matrixNorm(canonical.alpha, options.normType);
                System.out.printf("||alpha||_%s = %.6f%n", options.normType, alphaNorm);
                System.out.println("Условие (2.5) ||alpha|| < 1 : " + (alphaNorm < 1f ? "ДА" : "НЕТ"));
            }

            IterativeSolver.Result result = IterativeSolver.solve(canonical, options);

            System.out.println("\n-- Результат --");
            System.out.println("Критерий остановки    : " + result.usedStopCriterion);
            System.out.println("Итераций              : " + result.iterations);
            System.out.println("Сошёлся               : " + (result.converged ? "ДА" : "НЕТ"));
            System.out.println("Остановлен защитой    : " + (result.stoppedByGuard ? "ДА" : "НЕТ"));
            printVector("x*", result.x);
            System.out.printf("Последняя норма разницы : %.9f%n", result.lastDeltaNorm);
            if (!Float.isNaN(result.estimatedError)) {
                System.out.printf("Оценка погрешности     : %.9f%n", result.estimatedError);
            }
            System.out.println("Сообщение             : " + result.message);

            // Эксперимент: подбор минимальной eps, при которой вычисления завершаются корректно
            System.out.println("\n-- Эксперимент с точностью (минимальный стабильный epsilon) --");
            float found = IterativeSolver.findMinimalStableEpsilon(canonical, options,
                    1e-1f, 1e-12f, 10);

            if (found > 0f) {
                System.out.printf("Минимальный стабильный epsilon ~ %.12f%n", found);
            } else {
                System.out.println("Не удалось найти стабильный epsilon в заданном диапазоне.");
            }

        } catch (IllegalArgumentException ex) {
            System.out.println("Ошибка ввода: " + ex.getMessage());
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