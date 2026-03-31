// ==================== SplineModel.java ====================
import java.util.ArrayList;
import java.util.List;

public class SplineModel {

    // Данные для варианта 9 из таблицы 3.2
    private double[] x;
    private double[] y;
    private int n;

    // Коэффициенты кубического сплайна
    private double[] a;  // a_i = f(x_i-1)
    private double[] b;  // b_i
    private double[] c;  // c_i
    private double[] d;  // d_i
    private double[] h;  // h_i = x_i - x_{i-1}

    // Результаты для графиков
    private List<Point2D> splinePoints;
    private List<Point2D> firstDerivativePoints;
    private List<Point2D> secondDerivativePoints;

    public static class Point2D {
        public double x, y;
        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public SplineModel() {
        // Вариант 9 из таблицы 3.2
        this.x = new double[]{7, 9, 12, 14};
        this.y = new double[]{8, 11, 2, -5};
        this.n = x.length;

        calculateSpline();
        buildPoints();
    }

    /**
     * Вычисление кубического сплайна по формулам (3.3), (3.7), (3.9)-(3.11)
     */
    private void calculateSpline() {
        int m = n - 1; // количество интервалов

        // Шаг 1: вычисляем h_i
        h = new double[m];
        for (int i = 0; i < m; i++) {
            h[i] = x[i + 1] - x[i];
        }

        // Шаг 2: коэффициенты a_i (формула 3.3)
        a = new double[m];
        for (int i = 0; i < m; i++) {
            a[i] = y[i];
        }

        // Шаг 3: построение системы для c_i (формула 3.11)
        // Матрица системы трехдиагональная
        int size = m + 1; // c_0 ... c_m, но c_0 = 0 (условие 3.7)
        // Фактически решаем для c_1 ... c_{m-1}, c_m определяется из (3.12)

        double[] diag = new double[m - 1];     // главная диагональ (для c_1..c_{m-1})
        double[] upper = new double[m - 2];    // верхняя диагональ
        double[] lower = new double[m - 2];    // нижняя диагональ
        double[] rhs = new double[m - 1];      // правая часть

        for (int i = 1; i < m; i++) {
            // Коэффициенты уравнения (3.11)
            double coeffCprev = h[i - 1];
            double coeffC = 2 * (h[i - 1] + h[i]);
            double coeffCnext = h[i];

            // Правая часть
            double right = 3 * ((y[i + 1] - y[i]) / h[i] - (y[i] - y[i - 1]) / h[i - 1]);

            if (i == 1) {
                // Первое уравнение: c_0 = 0, поэтому член с c_0 исчезает
                diag[i - 1] = coeffC;
                rhs[i - 1] = right - coeffCprev * 0; // c_0 = 0
                if (m > 2) {
                    upper[i - 1] = coeffCnext;
                }
            } else if (i == m - 1) {
                // Последнее уравнение: c_m = 0 (формула 3.12)
                lower[i - 2] = coeffCprev;
                diag[i - 1] = coeffC;
                rhs[i - 1] = right - coeffCnext * 0; // c_m = 0
            } else {
                lower[i - 2] = coeffCprev;
                diag[i - 1] = coeffC;
                upper[i - 1] = coeffCnext;
                rhs[i - 1] = right;
            }
        }

        // Решение трехдиагональной системы методом прогонки
        double[] cInternal = solveTridiagonal(lower, diag, upper, rhs, m - 1);

        // Формируем полный массив c (размер m+1)
        c = new double[m + 1];
        c[0] = 0; // условие (3.7)
        for (int i = 1; i < m; i++) {
            c[i] = cInternal[i - 1];
        }
        c[m] = 0; // условие (3.12)

        // Шаг 4: вычисляем d_i (формула 3.9)
        d = new double[m];
        for (int i = 0; i < m; i++) {
            d[i] = (c[i + 1] - c[i]) / (3 * h[i]);
        }

        // Шаг 5: вычисляем b_i (формула 3.10)
        b = new double[m];
        for (int i = 0; i < m; i++) {
            b[i] = (y[i + 1] - y[i]) / h[i] - (c[i + 1] + 2 * c[i]) * h[i] / 3;
        }

        // Вывод коэффициентов в консоль
        printCoefficients();
    }

    /**
     * Решение трехдиагональной системы методом прогонки
     */
    private double[] solveTridiagonal(double[] lower, double[] diag, double[] upper, double[] rhs, int size) {
        double[] p = new double[size];
        double[] q = new double[size];

        // Прямая прогонка
        p[0] = upper[0] / diag[0];
        q[0] = rhs[0] / diag[0];

        for (int i = 1; i < size - 1; i++) {
            double denominator = diag[i] - lower[i - 1] * p[i - 1];
            p[i] = upper[i] / denominator;
            q[i] = (rhs[i] - lower[i - 1] * q[i - 1]) / denominator;
        }

        // Последний шаг
        double lastDenom = diag[size - 1] - lower[size - 2] * p[size - 2];
        q[size - 1] = (rhs[size - 1] - lower[size - 2] * q[size - 2]) / lastDenom;

        // Обратная прогонка
        double[] solution = new double[size];
        solution[size - 1] = q[size - 1];
        for (int i = size - 2; i >= 0; i--) {
            solution[i] = q[i] - p[i] * solution[i + 1];
        }

        return solution;
    }

    /**
     * Вычисление значения сплайна на интервале i в точке t (0 <= t <= 1)
     */
    private double splineValue(int i, double t) {
        // x = x[i] + t * h[i]
        // S(x) = a[i] + b[i]*(x-x[i]) + c[i]*(x-x[i])^2 + d[i]*(x-x[i])^3
        double dx = t * h[i];
        return a[i] + b[i] * dx + c[i] * dx * dx + d[i] * dx * dx * dx;
    }

    /**
     * Первая производная сплайна
     */
    private double firstDerivative(int i, double t) {
        double dx = t * h[i];
        return b[i] + 2 * c[i] * dx + 3 * d[i] * dx * dx;
    }

    /**
     * Вторая производная сплайна
     */
    private double secondDerivative(int i, double t) {
        double dx = t * h[i];
        return 2 * c[i] + 6 * d[i] * dx;
    }

    /**
     * Построение точек для графиков
     */
    private void buildPoints() {
        int pointsPerInterval = 100;
        double step = 1.0 / pointsPerInterval;

        splinePoints = new ArrayList<>();
        firstDerivativePoints = new ArrayList<>();
        secondDerivativePoints = new ArrayList<>();

        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k <= pointsPerInterval; k++) {
                double t = k * step;
                double xVal = x[i] + t * h[i];
                double yVal = splineValue(i, t);
                double y1Val = firstDerivative(i, t);
                double y2Val = secondDerivative(i, t);

                splinePoints.add(new Point2D(xVal, yVal));
                firstDerivativePoints.add(new Point2D(xVal, y1Val));
                secondDerivativePoints.add(new Point2D(xVal, y2Val));
            }
        }
    }

    /**
     * Вывод коэффициентов в консоль
     */
    private void printCoefficients() {
        System.out.println("\n=== КУБИЧЕСКИЙ СПЛАЙН (Вариант 9) ===");
        System.out.println("Исходные точки:");
        for (int i = 0; i < n; i++) {
            System.out.printf("  (%.1f, %.1f)%n", x[i], y[i]);
        }
        System.out.println("\nКоэффициенты сплайна:");
        System.out.println("Интервал |    a[i]    |    b[i]    |    c[i]    |    d[i]    |    h[i]    ");
        System.out.println("---------|------------|------------|------------|------------|------------");
        for (int i = 0; i < n - 1; i++) {
            System.out.printf("[%.1f; %.1f] | %10.4f | %10.4f | %10.4f | %10.4f | %10.4f%n",
                    x[i], x[i + 1], a[i], b[i], c[i], d[i], h[i]);
        }

        // Проверка непрерывности производных
        System.out.println("\nПроверка непрерывности во внутренних узлах:");
        for (int i = 1; i < n - 1; i++) {
            // Значение сплайна слева и справа должно совпадать (по построению)
            // Проверяем первую производную
            double leftDeriv = b[i - 1] + 2 * c[i - 1] * h[i - 1] + 3 * d[i - 1] * h[i - 1] * h[i - 1];
            double rightDeriv = b[i];
            System.out.printf("x = %.1f: S'(x-) = %10.6f, S'(x+) = %10.6f, разница = %.2e%n",
                    x[i], leftDeriv, rightDeriv, Math.abs(leftDeriv - rightDeriv));

            // Проверяем вторую производную
            double leftSecond = 2 * c[i - 1] + 6 * d[i - 1] * h[i - 1];
            double rightSecond = 2 * c[i];
            System.out.printf("x = %.1f: S''(x-) = %10.6f, S''(x+) = %10.6f, разница = %.2e%n",
                    x[i], leftSecond, rightSecond, Math.abs(leftSecond - rightSecond));
        }
    }

    // Геттеры
    public List<Point2D> getSplinePoints() { return splinePoints; }
    public List<Point2D> getFirstDerivativePoints() { return firstDerivativePoints; }
    public List<Point2D> getSecondDerivativePoints() { return secondDerivativePoints; }
    public double[] getX() { return x; }
    public double[] getY() { return y; }
}