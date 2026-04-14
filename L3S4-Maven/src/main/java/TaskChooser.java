
import javax.swing.*;
import java.awt.*;

public class TaskChooser extends JFrame {

    public TaskChooser() {
        setTitle("Select Task");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setResizable(false);

        // Главная панель с тёмным фоном
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(45, 45, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // Кнопка Task №1
        JButton task1Button = createTaskButton("Task №1");
        task1Button.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                Task1Model model = new Task1Model();
                Task1View view = new Task1View(model);
                JFrame frame = new JFrame("Численные методы - Задание 1");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(view);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(task1Button, gbc);

        // Кнопка Task №2
        JButton task2Button = createTaskButton("Task №2");
        task2Button.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                Task2Model model = new Task2Model();
                Task2View view = new Task2View(model);
                JFrame frame = new JFrame("Численные методы - Задание 2");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(view);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(task2Button, gbc);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createTaskButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 50));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 130, 200));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Эффект при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 200));
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TaskChooser::new);
    }
}