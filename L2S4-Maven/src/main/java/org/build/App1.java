package org.build;

import Jama.Matrix;

public class App1 extends Main {

    private boolean straightDone = false;
    private Matrix matrixA, matrixB, matrixS;

    // Геттеры и сеттеры
    public float getMatrixA(int k, int n) { return (float) matrixA.get(k, n); }
    public void setMatrixA(double x, int k, int n) { this.matrixA.set(k, n, x); }
    public float getMatrixB(int k, int n) { return (float) matrixB.get(k, n); }
    public void setMatrixB(double x, int k, int n) { this.matrixB.set(k, n, x); }
    public float getMatrixS(int n) { return (float) matrixS.get(0, n); }
    public void setMatrixS(double x, int n) { this.matrixS.set(0, n, x); }

    public void StraightWayByFullSearch() {
        try {
            if (straightDone) { return; }
            if (matrixA.getColumnDimension() != matrixA.getRowDimension())
                throw new RuntimeException("Матрица не квадратная.");
            if (matrixA == null) { return; }

            int n = matrixA.getRowDimension();

            for (int k = 0; k < n - 1; k++) {

                // Поиск главного элемента по модулю
                int pivotRow = k, pivotColumn = k;
                float max = (float) Math.abs(matrixA.get(k, k));

                for (int i = k; i < n; i++) {
                    for (int j = k; j < n; j++) {
                        float val = (float) Math.abs(matrixA.get(i, j));
                        if (val > max) {
                            max = val;
                            pivotRow = i;
                            pivotColumn = j;
                        }
                    }
                }

                // Перестановки
                if (pivotRow != k) {
                    swapRows(pivotRow, k);
                    float tempB = (float) matrixB.get(pivotRow, 0);
                    matrixB.set(pivotRow, 0, matrixB.get(k, 0));
                    matrixB.set(k, 0, tempB);
                }

                if (pivotColumn != k) {
                    swapColumns(pivotColumn, k);
                }

                float diag = (float) matrixA.get(k, k);

                // Исключение с компенсационным суммированием
                for (int i = k + 1; i < n; i++) {
                    float factor = (float) (matrixA.get(i, k) / diag);

                    for (int j = k; j < n; j++) {
                        float product = (float) (factor * matrixA.get(k, j));
                        float newValue = (float) (matrixA.get(i, j) - product);
                        matrixA.set(i, j, newValue);
                    }

                    // Для правой части
                    float productB = (float) (factor * matrixB.get(k, 0));
                    float newB = (float) (matrixB.get(i, 0) - productB);
                    matrixB.set(i, 0, newB);
                }
            }

            // Не зануляем маленькие числа, просто запоминаем, что они есть
            straightDone = true;

        } catch (Exception ex) {
            throw new RuntimeException("Ошибка в прямом ходе метода Гаусса. " + ex.getMessage());
        }
    }

    public void StandardStraightWay() {
        try {
            if (straightDone) { return; }
            if (matrixA.getColumnDimension() != matrixA.getRowDimension())
                throw new RuntimeException("Матрица не квадратная.");
            if (matrixA == null) { return; }

            int n = matrixA.getRowDimension();

            for (int k = 0; k < n - 1; k++) {

                // 1. Поиск главного элемента (первый ненулевой в столбце k)
                int pivotRow = -1;
                for (int i = k; i < n; i++) {
                    if (Math.abs(matrixA.get(i, k)) > 1e-10) {
                        pivotRow = i;
                        break;
                    }
                }
                if (pivotRow == -1) {
                    continue; // нулевой столбец
                }

                // 2. Перестановка строк, если нужно
                if (pivotRow != k) {
                    swapRows(pivotRow, k);
                    // Также меняем соответствующие элементы в B
                    float tempB = (float) matrixB.get(pivotRow, 0);
                    matrixB.set(pivotRow, 0, matrixB.get(k, 0));
                    matrixB.set(k, 0, tempB);
                }

                // 3. Исключение переменной из нижних строк
                for (int i = k + 1; i < n; i++) {
                    float factor = (float) (matrixA.get(i, k) / matrixA.get(k, k));

                    // Обновляем строку i в матрице A
                    for (int j = k; j < n; j++) {
                        float newValue = (float) (matrixA.get(i, j) - factor * matrixA.get(k, j));
                        matrixA.set(i, j, newValue);
                    }

                    // Обновляем соответствующий элемент в B
                    float newB = (float) (matrixB.get(i, 0) - factor * matrixB.get(k, 0));
                    matrixB.set(i, 0, newB);
                }
            }

            straightDone = true;

        } catch (Exception ex) {
            throw new RuntimeException("Ошибка в прямом ходе метода Гаусса. " + ex.getMessage());
        }
    }

    public void GausMetod() {
        try {
            if (!straightDone) {
                throw new RuntimeException("Пропущен прямой ход метода Гаусса.");
            }
            if (matrixB.getColumnDimension() != 1) {
                throw new RuntimeException("В матрице-столбце B больше одного столбца");
            }

            int n = matrixA.getColumnDimension();
            matrixS = new Matrix(1, n);

            // Обратный ход метода Гаусса
            for (int i = n - 1; i >= 0; i--) {
                float sum = 0;
                for (int j = i + 1; j < n; j++) {
                    sum += matrixA.get(i, j) * getMatrixS(j);
                }

                if (Math.abs(matrixA.get(i, i)) < 1e-10) {
                    throw new RuntimeException("Нулевой элемент на диагонали. Система вырождена.");
                }

                float x = (float) ((matrixB.get(i, 0) - sum) / matrixA.get(i, i));
                setMatrixS(x, i);
            }

        } catch (Exception ex) {
            throw new RuntimeException("Ошибка в обратном ходе метода Гаусса: " + ex.getMessage());
        }
    }

    private void swapRows(int i1, int i2) {
        if (i1 < 0 || i1 >= matrixA.getRowDimension() || i2 < 0 || i2 >= matrixA.getRowDimension()) {
            throw new RuntimeException("Ошибка в аргументах swapRows()");
        }

        int cols = matrixA.getColumnDimension();

        // Меняем строки в матрице A
        for (int j = 0; j < cols; j++) {
            float temp = (float) matrixA.get(i1, j);
            matrixA.set(i1, j, matrixA.get(i2, j));
            matrixA.set(i2, j, temp);
        }
    }
    private void swapColumns(int i1, int i2) {
        if (i1 < 0 || i1 >= matrixA.getColumnDimension() || i2 < 0 || i2 >= matrixA.getColumnDimension()) {
            throw new RuntimeException("Ошибка в аргументах swapRows()");
        }

        int cols = matrixA.getRowDimension();

        // Меняем строки в матрице A
        for (int j = 0; j < cols; j++) {
            float temp = (float) matrixA.get(i1, j);
            matrixA.set(j, i1, matrixA.get(i2, j));
            matrixA.set(j, i2, temp);
        }
    }

    public void printSolution() {
        if (matrixS != null) {
            System.out.println("\n=== РЕШЕНИЕ СИСТЕМЫ ===");
            System.out.println("Вектор x:");
            for (int i = 0; i < matrixS.getColumnDimension(); i++) {
                System.out.printf("x[%d] = %10.6f\n", i, getMatrixS(i));
            }
        }
    }

    public void verifySolution() {
        //System.out.println("\n=== ПРОВЕРКА РЕШЕНИЯ ===");

        // Транспонируем matrixS, так как это строка, а нам нужен столбец
        Matrix x = matrixS.transpose();

        // Вычисляем A * x
        Matrix Ax = matrixA.times(x);

        //System.out.println("A * x =");
        Ax.print(10, 6);

        //System.out.println("B (должно быть равно A*x) =");
        matrixB.print(10, 6);

        // Вычисляем невязку
        double maxResidual = 0;
        for (int i = 0; i < Ax.getRowDimension(); i++) {
            double residual = Math.abs(Ax.get(i, 0) - matrixB.get(i, 0));
            maxResidual = Math.max(maxResidual, residual);
        }
        if (maxResidual < 1e-8) {
            System.out.println("РЕШЕНИЕ ВЕРНОЕ");
        } else {
            System.out.println("РЕШЕНИЕ МОЖЕТ БЫТЬ НЕВЕРНЫМ");
        }
    }

    public App1(String[] args) {
        try {
            // Матрица A: 3×3
            double[][] A = {
                    {15, 20, 30, 40},
                    {1, 1.333333, 1, 1},
                    {4, 3, 2, 1},
                    {-1,1,-1,1}
            };
            matrixA = new Matrix(A);

            // Матрица B: 3×1 (столбец)
            double[][] B = {
                    {105},
                    {4.333333},
                    {10},
                    {0}
            };
            matrixB = new Matrix(B);

            System.out.println("Исходная матрица A:");
            matrixA.print(8, 2);

            System.out.println("\nИсходная матрица B:");
            matrixB.print(8, 2);

            // Прямой ход метода Гаусса ( с выбором главного и без )

            //StraightWayByFullSearch();

            StandardStraightWay();
            System.out.println("\nМатрица A после прямого хода:");
            matrixA.print(8, 2);

            System.out.println("\nМатрица B после прямого хода:");
            matrixB.print(8, 2);

            // Обратный ход метода Гаусса
            GausMetod();

            // Вывод решения
            printSolution();

            // Проверка решения
            verifySolution();

        } catch (Exception ex) {
            System.err.println("Ошибка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}