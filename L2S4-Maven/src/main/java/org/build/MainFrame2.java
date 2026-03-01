package org.build;

import java.awt.*;

public class MainFrame2 extends MainFrame{
    public MainFrame2(App p) {
        super(new Main());
        Main.main(null);
        setVisible(false);
        super.getMenuBar().add(new Menu("Lab2"));
    }
}
