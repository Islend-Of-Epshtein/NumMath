package org.build;

import java.util.Arrays;

public class App1 {

    public App1(String[] args) {
        runDemo();
    }

    private void runDemo() {
        System.out.println("=== Пункт 1: метод Гаусса ===");

        float[][] a = {
                {15f, 20f, 30f, 40f},
                {1f, 1.333333f, 1f, 1f},
                {4f, 3f, 2f, 1f},
                {-1f, 1f, -1f, 1f}
        };
        float[] b = {105f, 4.333333f, 10f, 0f};

        printGaussianCase("Без выбора главного элемента", a, b, GaussianPivotMode.NONE, null);
        printGaussianCase("С выбором главного элемента по столбцу", a, b, GaussianPivotMode.PARTIAL_BY_COLUMN, null);
        printGaussianCase("С полным выбором главного элемента", a, b, GaussianPivotMode.FULL, null);
    }

    private static void printGaussianCase(String title,
                                          float[][] a,
                                          float[] b,
                                          GaussianPivotMode mode,
                                          float[] reference) {
        System.out.println("\n--- " + title + " ---");

        GaussianEliminationSolver.Result result =
                GaussianEliminationSolver.solve(a, b, mode);

        SolveReport report = MatrixUtils.buildReport(a, b, result.solution, reference, 0.1f);

        System.out.println("Режим: " + result.pivotMode);
        System.out.println("x = " + Arrays.toString(result.solution));
        System.out.println("max|r| = " + report.maxResidual);

        if (!Float.isNaN(report.maxDifferenceFromReference)) {
            System.out.println("max|x - x_ref| = " + report.maxDifferenceFromReference);
        }

        System.out.println("Комментарий: " + report.comment);
    }
}