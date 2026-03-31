// ==================== View.java ====================
import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

public class View extends JPanel {

    private Model model;

    // Панель управления
    private JPanel controlPanel;
    private JTextField pointsField;
    private JTextField polyCoeffsField;
    private JButton updateButton;
    private JButton resetButton;
    private JLabel statusLabel;

    // Компоненты для отображения текущих данных
    private JLabel currentPointsLabel;
    private JLabel currentPolyLabel;

    // Цвета для графиков
    private static final Color COLOR_SOURCE = Color.BLACK;
    private static final Color COLOR_LAGRANGE = new Color(255, 0, 0);
    private static final Color COLOR_NEWTON = new Color(0, 0, 255);
    private static final Color COLOR_LSQ1 = new Color(0, 128, 0);
    private static final Color COLOR_LSQ2 = new Color(255, 165, 0);
    private static final Color COLOR_LSQ3 = new Color(128, 0, 128);
    private static final Color COLOR_CUSTOM = new Color(165, 42, 42);

    public View(Model model) {
        this.model = model;
        setPreferredSize(new Dimension(1100, 900));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        initControlPanel();
    }

    private void initControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Панель управления"));
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Текущие точки
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Текущие точки:"), gbc);

        currentPointsLabel = new JLabel(model.getPointsString());
        currentPointsLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridx = 1;
        controlPanel.add(currentPointsLabel, gbc);

        // Текущий многочлен
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Многочлен 4 степени:"), gbc);

        currentPolyLabel = new JLabel(model.getCustomPolyString());
        currentPolyLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridx = 1;
        controlPanel.add(currentPolyLabel, gbc);

        // Поле ввода точек
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(new JLabel("точки:"), gbc);

        pointsField = new JTextField(40);
        pointsField.setToolTipText("Формат: (x1,y1), (x2,y2), (x3,y3), ...");
        pointsField.setText("(7,8), (9,11),(10,7), (12,2), (14,-5)");
        gbc.gridx = 1;
        controlPanel.add(pointsField, gbc);

        // Поле ввода коэффициентов
        gbc.gridx = 0;
        gbc.gridy = 3;
        controlPanel.add(new JLabel("Коэффициенты a0 a1 a2 a3 a4:"), gbc);

        polyCoeffsField = new JTextField(30);
        polyCoeffsField.setToolTipText("Пример: 1 2 -3 0.5 -0.1");
        polyCoeffsField.setText("1 2 -3 0.5 -0.1");
        gbc.gridx = 1;
        controlPanel.add(polyCoeffsField, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        updateButton = new JButton("Обновить");
        updateButton.addActionListener(e -> updateFromFields());
        buttonPanel.add(updateButton);

        resetButton = new JButton("Сброс");
        resetButton.addActionListener(e -> resetToDefault());
        buttonPanel.add(resetButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        controlPanel.add(buttonPanel, gbc);

        // Статус
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
        gbc.gridy = 5;
        controlPanel.add(statusLabel, gbc);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void updateFromFields() {
        // Обновляем точки
        String pointsText = pointsField.getText();
        boolean pointsOk = model.setPoints(pointsText);

        // Обновляем коэффициенты многочлена
        String coeffsText = polyCoeffsField.getText();
        boolean coeffsOk = model.setCustomPolyCoeffs(coeffsText);

        if (pointsOk && coeffsOk) {
            statusLabel.setText("Графики обновлены!");
            statusLabel.setForeground(new Color(0, 150, 0));
        } else if (!pointsOk) {
            statusLabel.setText("Ошибка парсинга точек! Формат: (x1,y1), (x2,y2), ...");
            statusLabel.setForeground(Color.RED);
        } else if (!coeffsOk) {
            statusLabel.setText("Ошибка парсинга коэффициентов! Нужно 5 чисел");
            statusLabel.setForeground(Color.RED);
        }

        // Обновляем отображение текущих значений
        currentPointsLabel.setText(model.getPointsString());
        currentPolyLabel.setText(model.getCustomPolyString());

        // Перерисовываем график
        repaint();
    }

    private void resetToDefault() {
        pointsField.setText("(7,8), (9,11),(10,7), (12,2), (14,-5)");
        polyCoeffsField.setText("1 2 -3 0.5 -0.1");
        updateFromFields();
    }

    // ==================== ОТРИСОВКА ====================

    private int mapX(double x, int width) {
        return (int) ((x - model.getMinX()) / (model.getMaxX() - model.getMinX()) * width);
    }

    private int mapY(double y, int height) {
        return (int) (height - (y - model.getMinY()) / (model.getMaxY() - model.getMinY()) * height);
    }

    private void drawLegend(Graphics2D g2d, int x, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int rectSize = 15;

        String[] names = {
                "Исходные точки",
                "Лагранж (сплошная, толстая)",
                "Ньютон (пунктир, синий)",
                "МНК степень 1 (зеленый)",
                "МНК степень 2 (оранжевый)",
                "МНК степень 3 (фиолетовый)",
                "Многочлен 4 степени (коричневый, жирный)"
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
                new BasicStroke(3),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 3}, 0),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4}, 0),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4, 2, 4}, 0),
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0),
                new BasicStroke(4)
        };

        g2d.setFont(new Font("Dialog", Font.PLAIN, 11));

        for (int i = 0; i < names.length; i++) {
            int currentY = y + i * (lineHeight + 3);

            Stroke oldStroke = g2d.getStroke();
            g2d.setColor(colors[i]);

            if (i == 0) {
                g2d.fillOval(x, currentY - rectSize + 2, rectSize, rectSize);
            } else {
                g2d.setStroke(strokes[i]);
                g2d.drawLine(x, currentY - rectSize / 2 + 2, x + rectSize, currentY - rectSize / 2 + 2);
            }

            g2d.setStroke(oldStroke);
            g2d.setColor(Color.BLACK);
            g2d.drawString(names[i], x + rectSize + 5, currentY);
        }
    }

    private void drawPoints(Graphics2D g2d, List<Model.Point2D> points, Color color, int width, int height) {
        g2d.setColor(color);
        for (Model.Point2D p : points) {
            int x = mapX(p.x, width);
            int y = mapY(p.y, height);
            if (x >= 0 && x <= width && y >= 0 && y <= height) {
                g2d.fillOval(x - 4, y - 4, 8, 8);
            }
        }
    }

    private void drawCurve(Graphics2D g2d, List<Model.Point2D> points, Color color, Stroke stroke, int width, int height) {
        if (points == null || points.isEmpty()) return;

        g2d.setColor(color);
        g2d.setStroke(stroke);

        GeneralPath path = new GeneralPath();
        boolean first = true;

        for (Model.Point2D p : points) {
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight() - controlPanel.getHeight();

        // Смещаем начало координат для графика (учитывая панель управления)
        int graphY = controlPanel.getHeight();
        g2d.translate(0, graphY);

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

        // Подписи осей
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2d.drawString("X", width - 15, originY - 5);
        g2d.drawString("Y", originX + 5, 15);

        // Рисуем графики (от менее важных к более важным)

        // МНК степень 1
        drawCurve(g2d, model.getLsq1Points(), COLOR_LSQ1,
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4}, 0),
                width, height);

        // МНК степень 2
        drawCurve(g2d, model.getLsq2Points(), COLOR_LSQ2,
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4, 2, 4}, 0),
                width, height);

        // МНК степень 3
        drawCurve(g2d, model.getLsq3Points(), COLOR_LSQ3,
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0),
                width, height);

        // Многочлен 4 степени (жирный)
        drawCurve(g2d, model.getCustomPolyPoints(), COLOR_CUSTOM, new BasicStroke(3), width, height);

        // Ньютон (пунктирный)
        drawCurve(g2d, model.getNewtonPoints(), COLOR_NEWTON,
                new BasicStroke(3),
                width, height);

        // Лагранж (сплошной)
        drawCurve(g2d, model.getLagrangePoints(), COLOR_LAGRANGE, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15, 10}, 0), width, height);

        // Исходные точки
        drawPoints(g2d, model.getSourcePoints(), COLOR_SOURCE, width, height);

        // Рисуем легенду
        drawLegend(g2d, width - 220, 10);

        g2d.translate(0, -graphY);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Model model = new Model();
            View view = new View(model);

            JFrame frame = new JFrame("Лабораторная работа 3");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}