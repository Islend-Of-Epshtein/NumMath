// ==================== Model.java ====================
import java.util.ArrayList;
import java.util.List;

public class Model {
    private List<Point2D> points;
    private double[] customPolyCoeffs;

    // Коэффициенты МНК
    private double[] coeffs1; // степень 1
    private double[] coeffs2; // степень 2
    private double[] coeffs3; // степень 3

    // Данные для графиков
    private List<Point2D> sourcePoints;
    private List<Point2D> lagrangePoints;
    private List<Point2D> newtonPoints;
    private List<Point2D> lsq1Points;
    private List<Point2D> lsq2Points;
    private List<Point2D> lsq3Points;
    private List<Point2D> customPolyPoints;

    // Параметры для построения
    private double minX, maxX, minY, maxY;
    private static final double STEP = 0.01;

    // Внутренний класс для точки
    public static class Point2D {
        public double x, y;
        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public Model() {
        points = new ArrayList<>();
        // Данные для варианта 9 по умолчанию
        points.add(new Point2D(7, 8));
        points.add(new Point2D(9, 11));
        points.add(new Point2D(10, 7));
        points.add(new Point2D(12, 2));
        points.add(new Point2D(14, -5));

        // Многочлен 4 степени по умолчанию: 1 + 2x - 3x^2 + 0.5x^3 - 0.1x^4
        customPolyCoeffs = new double[]{1, 2, -3, 0.5, -0.1};

        recalculateAll();
    }

    /**
     * Обновление всех вычислений после изменения данных
     */
    public void recalculateAll() {
        // Копируем исходные точки
        sourcePoints = new ArrayList<>();
        for (Point2D p : points) {
            sourcePoints.add(new Point2D(p.x, p.y));
        }

        // Вычисляем границы
        calculateBounds();

        // Вычисляем коэффициенты МНК
        coeffs1 = leastSquares(1);
        coeffs2 = leastSquares(2);
        coeffs3 = leastSquares(3);

        // Строим точки для всех графиков
        lagrangePoints = new ArrayList<>();
        newtonPoints = new ArrayList<>();
        lsq1Points = new ArrayList<>();
        lsq2Points = new ArrayList<>();
        lsq3Points = new ArrayList<>();
        customPolyPoints = new ArrayList<>();

        for (double x = minX; x <= maxX; x += STEP) {
            lagrangePoints.add(new Point2D(x, lagrangePolynomial(x)));
            newtonPoints.add(new Point2D(x, newtonPolynomial(x)));
            lsq1Points.add(new Point2D(x, polynomialValue(coeffs1, x)));
            lsq2Points.add(new Point2D(x, polynomialValue(coeffs2, x)));
            lsq3Points.add(new Point2D(x, polynomialValue(coeffs3, x)));
            customPolyPoints.add(new Point2D(x, customPolynomial(x)));
        }

        // Выводим коэффициенты в консоль
        System.out.println("\n--------------------------------");
        System.out.println("Исходные точки: " + pointsToString());
        System.out.println("Коэффициенты МНК (степень 1): " + arrayToString(coeffs1));
        System.out.println("Коэффициенты МНК (степень 2): " + arrayToString(coeffs2));
        System.out.println("Коэффициенты МНК (степень 3): " + arrayToString(coeffs3));
        System.out.println("Многочлен 4 степени: " + polyToString(customPolyCoeffs));
        System.out.println("--------------------------------\n");
    }

    private String pointsToString() {
        StringBuilder sb = new StringBuilder();
        for (Point2D p : points) {
            sb.append(String.format("(%.1f,%.1f) ", p.x, p.y));
        }
        return sb.toString();
    }

    private String arrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("a%d=%.4f ", i, arr[i]));
        }
        return sb.toString();
    }

    private String polyToString(double[] coeffs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coeffs.length; i++) {
            if (i == 0) sb.append(String.format("%.2f", coeffs[i]));
            else if (coeffs[i] >= 0) sb.append(String.format(" + %.2f*x^%d", coeffs[i], i));
            else sb.append(String.format(" - %.2f*x^%d", -coeffs[i], i));
        }
        return sb.toString();
    }

    /**
     * Вычисление границ для отображения
     */
    private void calculateBounds() {
        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        // Добавляем отступы
        double xPadding = (maxX - minX) * 0.3;
        double yPadding = (maxY - minY) * 0.5;

        minX -= xPadding;
        maxX += xPadding;
        minY -= yPadding;
        maxY += yPadding;

        // Дополнительно расширяем для многочленов
        minY -= 10;
        maxY += 10;
    }

    // ==================== МЕТОДЫ ИНТЕРПОЛЯЦИИ ====================

    /**
     * Интерполяционный многочлен Лагранжа
     */
    private double lagrangePolynomial(double x) {
        int n = points.size();
        double result = 0;
        for (int i = 0; i < n; i++) {
            double term = points.get(i).y;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    term *= (x - points.get(j).x) / (points.get(i).x - points.get(j).x);
                }
            }
            result += term;
        }
        return result;
    }

    /**
     * Вычисление разделенных разностей
     */
    private double[][] computeDividedDifferences() {
        int n = points.size();
        double[][] diff = new double[n][n];

        for (int i = 0; i < n; i++) {
            diff[i][0] = points.get(i).y;
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                diff[i][j] = (diff[i + 1][j - 1] - diff[i][j - 1]) / (points.get(i + j).x - points.get(i).x);
            }
        }
        return diff;
    }

    /**
     * Интерполяционный многочлен Ньютона
     */
    private double newtonPolynomial(double x) {
        double[][] diff = computeDividedDifferences();
        double result = diff[0][0];

        for (int j = 1; j < points.size(); j++) {
            double term = diff[0][j];
            for (int i = 0; i < j; i++) {
                term *= (x - points.get(i).x);
            }
            result += term;
        }
        return result;
    }

    // ==================== МЕТОД НАИМЕНЬШИХ КВАДРАТОВ ====================

    /**
     * Решение системы методом Гаусса
     */
    private double[] solveGauss(double[][] a, double[] b, int n) {
        double[][] aCopy = new double[n][n];
        double[] bCopy = new double[n];

        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, aCopy[i], 0, n);
            bCopy[i] = b[i];
        }

        // Прямой ход
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(aCopy[k][i]) > Math.abs(aCopy[maxRow][i])) {
                    maxRow = k;
                }
            }

            double[] tempRow = aCopy[i];
            aCopy[i] = aCopy[maxRow];
            aCopy[maxRow] = tempRow;

            double tempB = bCopy[i];
            bCopy[i] = bCopy[maxRow];
            bCopy[maxRow] = tempB;

            for (int k = i + 1; k < n; k++) {
                double factor = aCopy[k][i] / aCopy[i][i];
                bCopy[k] -= factor * bCopy[i];
                for (int j = i; j < n; j++) {
                    aCopy[k][j] -= factor * aCopy[i][j];
                }
            }
        }

        // Обратный ход
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = bCopy[i];
            for (int j = i + 1; j < n; j++) {
                x[i] -= aCopy[i][j] * x[j];
            }
            x[i] /= aCopy[i][i];
        }
        return x;
    }

    /**
     * Аппроксимация методом наименьших квадратов
     */
    private double[] leastSquares(int degree) {
        int n = points.size();
        int m = degree + 1;

        double[][] A = new double[m][m];
        double[] B = new double[m];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += Math.pow(points.get(k).x, i + j);
                }
                A[i][j] = sum;
            }

            double sumB = 0;
            for (int k = 0; k < n; k++) {
                sumB += points.get(k).y * Math.pow(points.get(k).x, i);
            }
            B[i] = sumB;
        }

        return solveGauss(A, B, m);
    }

    /**
     * Вычисление значения полинома по коэффициентам
     */
    private double polynomialValue(double[] coeffs, double x) {
        double result = 0;
        for (int i = 0; i < coeffs.length; i++) {
            result += coeffs[i] * Math.pow(x, i);
        }
        return result;
    }

    /**
     * Пользовательский многочлен 4 степени
     */
    private double customPolynomial(double x) {
        double result = 0;
        for (int i = 0; i < customPolyCoeffs.length; i++) {
            result += customPolyCoeffs[i] * Math.pow(x, i);
        }
        return result;
    }

    // ==================== ГЕТТЕРЫ ====================

    public List<Point2D> getSourcePoints() { return sourcePoints; }
    public List<Point2D> getLagrangePoints() { return lagrangePoints; }
    public List<Point2D> getNewtonPoints() { return newtonPoints; }
    public List<Point2D> getLsq1Points() { return lsq1Points; }
    public List<Point2D> getLsq2Points() { return lsq2Points; }
    public List<Point2D> getLsq3Points() { return lsq3Points; }
    public List<Point2D> getCustomPolyPoints() { return customPolyPoints; }

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }

    /**
     * Установка новых точек
     */
    public boolean setPoints(String input) {
        try {
            List<Point2D> newPoints = new ArrayList<>();
            String clean = input.replaceAll("\\s+", "");

            // Парсинг формата: (x1,y1),(x2,y2),...
            String[] parts = clean.split("\\(|\\)");
            for (String part : parts) {
                if (part.isEmpty() || part.equals(",")) continue;
                String[] coords = part.split(",");
                if (coords.length >= 2) {
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    newPoints.add(new Point2D(x, y));
                }
            }

            if (newPoints.size() < 2) {
                System.err.println("Нужно минимум 2 точки");
                return false;
            }

            points = newPoints;
            recalculateAll();
            return true;

        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга: " + e.getMessage());
            return false;
        }
    }

    /**
     * Установка коэффициентов многочлена 4 степени
     */
    public boolean setCustomPolyCoeffs(String input) {
        try {
            String[] parts = input.trim().split("\\s+");
            if (parts.length != 5) {
                System.err.println("Нужно 5 коэффициентов (a0 a1 a2 a3 a4)");
                return false;
            }

            double[] newCoeffs = new double[5];
            for (int i = 0; i < 5; i++) {
                newCoeffs[i] = Double.parseDouble(parts[i]);
            }

            customPolyCoeffs = newCoeffs;
            recalculateAll();
            return true;

        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга коэффициентов: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получение строки с текущими точками
     */
    public String getPointsString() {
        StringBuilder sb = new StringBuilder();
        for (Point2D p : points) {
            sb.append(String.format("(%.1f,%.1f)", p.x, p.y));
        }
        return sb.toString();
    }

    /**
     * Получение строки с коэффициентами многочлена
     */
    public String getCustomPolyString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < customPolyCoeffs.length; i++) {
            sb.append(String.format("%.2f ", customPolyCoeffs[i]));
        }
        return sb.toString().trim();
    }
}