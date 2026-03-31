import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InterpolationLab3 extends JPanel {

    // Данные для варианта 9
    private final float[] X = {7, 9, 10, 12, 14};
    private final float[] Y = {8, 11, 7, 2, -5};
    private final int pointsCount = 5;

    // Параметры для построения графиков
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private final float step = 0.01f;

    // Цвета для графиков (RGB)
    private final Color COLOR_SOURCE = new Color(0, 0, 0);        // Черный
    private final Color COLOR_LAGRANGE = new Color(255, 0, 0);    // Красный
    private final Color COLOR_NEWTON = new Color(0, 0, 255);      // Синий
    private final Color COLOR_LSQ1 = new Color(0, 128, 0);        // Зеленый
    private final Color COLOR_LSQ2 = new Color(255, 165, 0);      // Оранжевый
    private final Color COLOR_LSQ3 = new Color(128, 0, 128);      // Фиолетовый
    private final Color COLOR_CUSTOM = new Color(165, 42, 42);     // Коричневый

    // Данные для графиков
    private List<Point2D> sourcePoints;
    private List<Point2D> lagrangePoints;
    private List<Point2D> newtonPoints;
    private List<Point2D> lsq1Points;
    private List<Point2D> lsq2Points;
    private List<Point2D> lsq3Points;
    private List<Point2D> customPolyPoints;

    // Коэффициенты МНК
    private float[] coeffs1; // степень 1 (линейная)
    private float[] coeffs2; // степень 2 (квадратичная)
    private float[] coeffs3; // степень 3 (кубическая)

    // Внутренний класс для точки с координатами float
    private static class Point2D {
        float x, y;
        Point2D(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public InterpolationLab3() {
        setPreferredSize(new Dimension(1000, 700));
        setBackground(Color.WHITE);

        // Вычисляем границы для отображения
        calculateBounds();

        // Инициализируем списки точек
        sourcePoints = new ArrayList<>();
        lagrangePoints = new ArrayList<>();
        newtonPoints = new ArrayList<>();
        lsq1Points = new ArrayList<>();
        lsq2Points = new ArrayList<>();
        lsq3Points = new ArrayList<>();
        customPolyPoints = new ArrayList<>();

        // Добавляем исходные точки
        for (int i = 0; i < pointsCount; i++) {
            sourcePoints.add(new Point2D(X[i], Y[i]));
        }

        // Вычисляем коэффициенты МНК
        coeffs1 = leastSquares(1);
        coeffs2 = leastSquares(2);
        coeffs3 = leastSquares(3);

        // Выводим коэффициенты в консоль
        System.out.println("Коэффициенты МНК (степень 1): a0 = " + coeffs1[0] + ", a1 = " + coeffs1[1]);
        System.out.println("Коэффициенты МНК (степень 2): a0 = " + coeffs2[0] + ", a1 = " + coeffs2[1] + ", a2 = " + coeffs2[2]);
        System.out.println("Коэффициенты МНК (степень 3): a0 = " + coeffs3[0] + ", a1 = " + coeffs3[1] + ", a2 = " + coeffs3[2] + ", a3 = " + coeffs3[3]);

        // Строим точки для всех графиков
        for (float x = minX; x <= maxX; x += step) {
            lagrangePoints.add(new Point2D(x, lagrangePolynomial(x)));
            newtonPoints.add(new Point2D(x, newtonPolynomial(x)));
            lsq1Points.add(new Point2D(x, polynomialValue(coeffs1, x)));
            lsq2Points.add(new Point2D(x, polynomialValue(coeffs2, x)));
            lsq3Points.add(new Point2D(x, polynomialValue(coeffs3, x)));
            customPolyPoints.add(new Point2D(x, customPolynomial(x)));
        }
    }

    /**
     * Вычисление границ для отображения графиков
     */
    private void calculateBounds() {
        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        minY = Float.MAX_VALUE;
        maxY = Float.MIN_VALUE;

        // По исходным точкам
        for (int i = 0; i < pointsCount; i++) {
            if (X[i] < minX) minX = X[i];
            if (X[i] > maxX) maxX = X[i];
            if (Y[i] < minY) minY = Y[i];
            if (Y[i] > maxY) maxY = Y[i];
        }

        // Добавляем отступы
        float xPadding = (maxX - minX) * 0.2f;
        float yPadding = (maxY - minY) * 0.2f;

        minX -= xPadding;
        maxX += xPadding;
        minY -= yPadding;
        maxY += yPadding;

        // Дополнительно расширяем для многочленов (могут выходить за пределы)
        minY -= 10;
        maxY += 10;
    }

    // ==================== МЕТОДЫ ИНТЕРПОЛЯЦИИ ====================

    /**
     * Интерполяционный многочлен Лагранжа
     */
    private float lagrangePolynomial(float x) {
        float result = 0;
        for (int i = 0; i < pointsCount; i++) {
            float term = Y[i];
            for (int j = 0; j < pointsCount; j++) {
                if (j != i) {
                    term *= (x - X[j]) / (X[i] - X[j]);
                }
            }
            result += term;
        }
        return result;
    }

    /**
     * Вычисление разделенных разностей для формы Ньютона
     */
    private float[][] computeDividedDifferences() {
        int n = pointsCount;
        float[][] diff = new float[n][n];

        // 0-го порядка - значения функции
        for (int i = 0; i < n; i++) {
            diff[i][0] = Y[i];
        }

        // Разделенные разности высших порядков
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                diff[i][j] = (diff[i + 1][j - 1] - diff[i][j - 1]) / (X[i + j] - X[i]);
            }
        }
        return diff;
    }

    /**
     * Интерполяционный многочлен Ньютона (с разделенными разностями)
     */
    private float newtonPolynomial(float x) {
        float[][] diff = computeDividedDifferences();
        float result = diff[0][0]; // a0

        for (int j = 1; j < pointsCount; j++) {
            float term = diff[0][j];
            for (int i = 0; i < j; i++) {
                term *= (x - X[i]);
            }
            result += term;
        }
        return result;
    }

    // ==================== МЕТОД НАИМЕНЬШИХ КВАДРАТОВ ====================

    /**
     * Решение системы методом Гаусса с выбором главного элемента
     */
    private float[] solveGauss(float[][] a, float[] b, int n) {
        float[][] aCopy = new float[n][n];
        float[] bCopy = new float[n];

        // Копируем матрицы, чтобы не изменять оригиналы
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, aCopy[i], 0, n);
            bCopy[i] = b[i];
        }

        // Прямой ход
        for (int i = 0; i < n; i++) {
            // Поиск главного элемента
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(aCopy[k][i]) > Math.abs(aCopy[maxRow][i])) {
                    maxRow = k;
                }
            }

            // Перестановка строк
            float[] tempRow = aCopy[i];
            aCopy[i] = aCopy[maxRow];
            aCopy[maxRow] = tempRow;

            float tempB = bCopy[i];
            bCopy[i] = bCopy[maxRow];
            bCopy[maxRow] = tempB;

            // Нормализация
            for (int k = i + 1; k < n; k++) {
                float factor = aCopy[k][i] / aCopy[i][i];
                bCopy[k] -= factor * bCopy[i];
                for (int j = i; j < n; j++) {
                    aCopy[k][j] -= factor * aCopy[i][j];
                }
            }
        }

        // Обратный ход
        float[] x = new float[n];
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
     * Аппроксимация методом наименьших квадратов полиномом степени degree
     */
    private float[] leastSquares(int degree) {
        int n = pointsCount;
        int m = degree + 1; // количество коэффициентов (от 0 до degree)

        // Матрица коэффициентов (суммы степеней x)
        float[][] A = new float[m][m];
        float[] B = new float[m];

        // Заполнение матрицы A и вектора B
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                float sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += (float) Math.pow(X[k], i + j);
                }
                A[i][j] = sum;
            }

            float sumB = 0;
            for (int k = 0; k < n; k++) {
                sumB += Y[k] * (float) Math.pow(X[k], i);
            }
            B[i] = sumB;
        }

        // Решение системы
        return solveGauss(A, B, m);
    }

    /**
     * Вычисление значения полинома по коэффициентам
     */
    private float polynomialValue(float[] coeffs, float x) {
        float result = 0;
        for (int i = 0; i < coeffs.length; i++) {
            result += coeffs[i] * (float) Math.pow(x, i);
        }
        return result;
    }

    // ==================== ПРОИЗВОЛЬНЫЙ МНОГОЧЛЕН ====================

    /**
     * Пример многочлена 4 степени: 1 + 2x - 3x^2 + 0.5x^3 - 0.1x^4
     */
    private float customPolynomial(float x) {
        return 1 + 2 * x - 3 * (float) Math.pow(x, 2) + 0.5f * (float) Math.pow(x, 3) - 0.1f * (float) Math.pow(x, 4);
    }

    // ==================== ОТОБРАЖЕНИЕ ГРАФИКОВ ====================

    /**
     * Преобразование координаты X в пиксель
     */
    private int mapX(float x, int width) {
        return (int) ((x - minX) / (maxX - minX) * width);
    }

    /**
     * Преобразование координаты Y в пиксель (инвертированная Y)
     */
    private int mapY(float y, int height) {
        return (int) (height - (y - minY) / (maxY - minY) * height);
    }

    /**
     * Отрисовка легенды
     */
    private void drawLegend(Graphics2D g2d, int x, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int rectSize = 15;

        String[] names = {
                "Исходные точки",
                "Лагранж",
                "Ньютон",
                "МНК (степень 1)",
                "МНК (степень 2)",
                "МНК (степень 3)",
                "Многочлен 4 степени"
        };

        Color[] colors = {
                COLOR_SOURCE,
                COLOR_LAGRANGE,
                COLOR_NEWTON,
                COLOR_LSQ1,
                COLOR_LSQ2,
                COLOR_LSQ3,
                COLOR_CUSTOM
        };

        Stroke[] strokes = {
                new BasicStroke(1),
                new BasicStroke(2),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 3}, 0),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4}, 0),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4, 2, 4}, 0),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0),
                new BasicStroke(2)
        };

        for (int i = 0; i < names.length; i++) {
            int currentY = y + i * (lineHeight + 5);

            // Рисуем прямоугольник с цветом и стилем линии
            Stroke oldStroke = g2d.getStroke();
            g2d.setColor(colors[i]);

            if (i == 0) {
                // Для точек рисуем круг
                g2d.fillOval(x, currentY - rectSize, rectSize, rectSize);
            } else {
                g2d.setStroke(strokes[i]);
                g2d.drawLine(x, currentY - rectSize / 2, x + rectSize, currentY - rectSize / 2);
            }

            // Восстанавливаем стиль для текста
            g2d.setStroke(oldStroke);
            g2d.setColor(Color.BLACK);
            g2d.drawString(names[i], x + rectSize + 5, currentY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Рисуем сетку
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= 10; i++) {
            int x = i * width / 10;
            int y = i * height / 10;
            g2d.drawLine(x, 0, x, height);
            g2d.drawLine(0, y, width, y);
        }

        // Рисуем оси
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        int originX = mapX(0, width);
        int originY = mapY(0, height);

        if (originX >= 0 && originX <= width) {
            g2d.drawLine(originX, 0, originX, height);
        }
        if (originY >= 0 && originY <= height) {
            g2d.drawLine(0, originY, width, originY);
        }

        // Функция для рисования линии по точкам
        java.awt.geom.GeneralPath path;

        // Рисуем графики (от менее важных к более важным)

        // МНК (степень 1) - зеленая
        g2d.setColor(COLOR_LSQ1);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4}, 0));
        path = new java.awt.geom.GeneralPath();
        boolean first = true;
        for (Point2D p : lsq1Points) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        g2d.draw(path);

        // МНК (степень 2) - оранжевая
        g2d.setColor(COLOR_LSQ2);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4, 2, 4}, 0));
        path = new java.awt.geom.GeneralPath();
        first = true;
        for (Point2D p : lsq2Points) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        g2d.draw(path);

        // МНК (степень 3) - фиолетовая (точки)
        g2d.setColor(COLOR_LSQ3);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
        path = new java.awt.geom.GeneralPath();
        first = true;
        for (Point2D p : lsq3Points) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        g2d.draw(path);

        // Многочлен 4 степени - коричневая
        g2d.setColor(COLOR_CUSTOM);
        g2d.setStroke(new BasicStroke(2));
        path = new java.awt.geom.GeneralPath();
        first = true;
        for (Point2D p : customPolyPoints) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        g2d.draw(path);

        // Ньютон - синяя (пунктир)
        g2d.setColor(COLOR_NEWTON);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 3}, 0));
        path = new java.awt.geom.GeneralPath();
        first = true;
        for (Point2D p : newtonPoints) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        g2d.draw(path);

        // Лагранж - красная (сплошная)
        g2d.setColor(COLOR_LAGRANGE);
        g2d.setStroke(new BasicStroke(2));
        path = new java.awt.geom.GeneralPath();
        first = true;
        for (Point2D p : lagrangePoints) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        g2d.draw(path);

        // Исходные точки - черные кружки
        g2d.setColor(COLOR_SOURCE);
        g2d.setStroke(new BasicStroke(1));
        for (Point2D p : sourcePoints) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            g2d.fillOval(x - 4, y - 4, 8, 8);
        }

        // Рисуем легенду
        drawLegend(g2d, 20, 30);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Численные методы - Лабораторная работа 3 (Вариант 9)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new InterpolationLab3());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}