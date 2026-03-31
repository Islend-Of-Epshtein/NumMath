// ==================== DerivativeView.java ====================
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

public class View extends JPanel {

    private Model model;

    // Компоненты GUI
    private JTable resultsTable;
    private ResultsTableModel tableModel;
    private JPanel graphPanel;
    private JComboBox<String> pointSelector;
    private JLabel optimalStepLabel;
    private JLabel minErrorLabel;

    // Цвета для графика
    private static final Color COLOR_EXACT = Color.RED;
    private static final Color COLOR_APPROX = Color.BLUE;

    public View(Model model) {
        this.model = model;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initControlPanel();
        initTable();
        initGraphPanel();
    }

    private void initControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        // Выбор точки для анализа
        controlPanel.add(new JLabel("Точка x:"));
        pointSelector = new JComboBox<>();
        for (double x : model.getPoints()) {
            pointSelector.addItem(String.format("%.2f", x));
        }
        pointSelector.addActionListener(e -> updateGraph());
        controlPanel.add(pointSelector);

        // Информация об оптимальном шаге
        controlPanel.add(new JLabel("Оптимальный шаг:"));
        optimalStepLabel = new JLabel("-");
        optimalStepLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        optimalStepLabel.setForeground(new Color(0, 150, 0));
        controlPanel.add(optimalStepLabel);

        controlPanel.add(new JLabel("Минимальная погрешность:"));
        minErrorLabel = new JLabel("-");
        minErrorLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        minErrorLabel.setForeground(Color.RED);
        controlPanel.add(minErrorLabel);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new ResultsTableModel(model);
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        resultsTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        resultsTable.setRowHeight(22);

        // Форматирование ячеек
        resultsTable.setDefaultRenderer(Double.class, new ScientificRenderer());

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Результаты вычислений"));
        scrollPane.setPreferredSize(new Dimension(800, 300));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void initGraphPanel() {
        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph((Graphics2D) g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(800, 350));
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setBorder(BorderFactory.createTitledBorder("Зависимость погрешности от шага (логарифмический масштаб)"));

        add(graphPanel, BorderLayout.SOUTH);
    }

    private void drawGraph(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = graphPanel.getWidth();
        int height = graphPanel.getHeight();
        int paddingLeft = 70;
        int paddingRight = 30;
        int paddingTop = 40;
        int paddingBottom = 60;

        int graphWidth = width - paddingLeft - paddingRight;
        int graphHeight = height - paddingTop - paddingBottom;

        // Получаем данные для выбранной точки
        int selectedIdx = pointSelector.getSelectedIndex();
        if (selectedIdx < 0) return;

        double x = model.getPoints()[selectedIdx];
        Model.Result result = model.getResultForX(x);
        if (result == null) return;

        List<Model.StepResult> steps = result.stepResults;

        // Находим диапазоны для логарифмического масштаба
        double minH = Double.MAX_VALUE;
        double maxH = -Double.MAX_VALUE;
        double minError = Double.MAX_VALUE;
        double maxError = -Double.MAX_VALUE;

        for (Model.StepResult sr : steps) {
            double logH = Math.log10(sr.h);
            double logError = Math.log10(sr.absoluteError + 1e-20);

            minH = Math.min(minH, logH);
            maxH = Math.max(maxH, logH);
            minError = Math.min(minError, logError);
            maxError = Math.max(maxError, logError);
        }

        // Добавляем отступы
        minError -= 0.5;
        maxError += 0.5;

        // Рисуем сетку
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1));

        // Горизонтальные линии
        for (int i = (int) Math.floor(minError); i <= (int) Math.ceil(maxError); i++) {
            int y = paddingTop + (int) ((maxError - i) / (maxError - minError) * graphHeight);
            g2d.drawLine(paddingLeft, y, width - paddingRight, y);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
            g2d.drawString(String.format("10^%d", i), paddingLeft - 45, y + 4);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Вертикальные линии
        for (int i = (int) Math.floor(minH); i <= (int) Math.ceil(maxH); i++) {
            int xPos = paddingLeft + (int) ((i - minH) / (maxH - minH) * graphWidth);
            g2d.drawLine(xPos, paddingTop, xPos, height - paddingBottom);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.format("10^%d", i), xPos - 15, height - paddingBottom + 20);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Рисуем оси
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom);
        g2d.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom);

        // Подписи осей
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2d.drawString("log₁₀(h)", width / 2 - 20, height - paddingBottom + 40);

        // Поворачиваем текст для оси Y
        Graphics2D g2dRot = (Graphics2D) g2d.create();
        g2dRot.translate(20, height / 2);
        g2dRot.rotate(-Math.PI / 2);
        g2dRot.drawString("log₁₀(Погрешность)", -50, 0);
        g2dRot.dispose();

        // Рисуем график погрешности
        g2d.setColor(COLOR_APPROX);
        g2d.setStroke(new BasicStroke(2));

        java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
        boolean first = true;

        for (Model.StepResult sr : steps) {
            double logH = Math.log10(sr.h);
            double logError = Math.log10(sr.absoluteError + 1e-20);

            int xPos = paddingLeft + (int) ((logH - minH) / (maxH - minH) * graphWidth);
            int yPos = paddingTop + (int) ((maxError - logError) / (maxError - minError) * graphHeight);

            if (first) {
                path.moveTo(xPos, yPos);
                first = false;
            } else {
                path.lineTo(xPos, yPos);
            }
        }
        g2d.draw(path);

        // Отмечаем оптимальный шаг
        Model.StepResult optimal = model.findOptimalStep(x);
        if (optimal != null) {
            double logHOpt = Math.log10(optimal.h);
            double logErrorOpt = Math.log10(optimal.absoluteError + 1e-20);

            int xOpt = paddingLeft + (int) ((logHOpt - minH) / (maxH - minH) * graphWidth);
            int yOpt = paddingTop + (int) ((maxError - logErrorOpt) / (maxError - minError) * graphHeight);

            g2d.setColor(Color.RED);
            g2d.fillOval(xOpt - 4, yOpt - 4, 8, 8);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
            g2d.drawString(String.format("Оптимум: h=%.1e", optimal.h), xOpt + 5, yOpt - 5);
        }

        // Обновляем метки
        if (optimal != null) {
            optimalStepLabel.setText(String.format("%.1e", optimal.h));
            minErrorLabel.setText(String.format("%.2e", optimal.absoluteError));
        }
    }

    private void updateGraph() {
        graphPanel.repaint();
    }

    // Модель таблицы
    class ResultsTableModel extends AbstractTableModel {
        private Model model;
        private String[] columns = {"x", "h", "f'(x) приближ.", "Точное f'(x)", "Абс. погрешность", "Отн. погрешность"};

        ResultsTableModel(Model model) {
            this.model = model;
        }

        @Override
        public int getRowCount() {
            int count = 0;
            for (Model.Result r : model.getResults()) {
                count += r.stepResults.size();
            }
            return count;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            // Находим нужную строку
            int currentRow = 0;
            for (Model.Result r : model.getResults()) {
                for (Model.StepResult sr : r.stepResults) {
                    if (currentRow == row) {
                        switch (col) {
                            case 0: return r.x;
                            case 1: return sr.h;
                            case 2: return sr.approxDerivative;
                            case 3: return r.exactDerivative;
                            case 4: return sr.absoluteError;
                            case 5: return sr.relativeError;
                        }
                    }
                    currentRow++;
                }
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return Double.class;
        }
    }

    // Форматирование чисел в научном виде
    class ScientificRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Double) {
                double d = (Double) value;
                if (Math.abs(d) < 1e-3 || Math.abs(d) > 1e4) {
                    value = String.format("%.4e", d);
                } else {
                    value = String.format("%.6f", d);
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Model model = new Model();
            View view = new View(model);

            JFrame frame = new JFrame("Лабораторная работа 4");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}