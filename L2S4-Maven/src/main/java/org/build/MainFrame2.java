package org.build;

import java.awt.Menu;
import java.awt.MenuBar;

public class MainFrame2 extends MainFrame {

    public MainFrame2(App p) {
        super(p);

        MenuBar menuBar = getMenuBar();
        if (menuBar == null) {
            menuBar = new MenuBar();
            setMenuBar(menuBar);
        }

        menuBar.add(new Menu("Lab2"));
        // Здесь позже можно добавить MenuItem и запуск new App2(...)
        // Не скрываем окно без причины:
        // setVisible(false);
    }
}