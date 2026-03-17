package org.build;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Main2 {

    public static void main(String[] args) {
        PrintStream console = System.out;

        try (FileOutputStream fileOutput = new FileOutputStream("results.txt", false);
             PrintStream teePrint = new PrintStream(
                     new TeeOutputStream(console, fileOutput),
                     true,
                     "UTF-8")) {

            System.setOut(teePrint);

            System.out.println("Результаты вычислений");
            System.out.println("=====================");
            System.out.println();

            new App1(args);
            System.out.println();
            new App2(args);

            System.out.flush();
        } catch (IOException e) {
            console.println("Ошибка при записи в results.txt: " + e.getMessage());
            e.printStackTrace(console);
        } finally {
            System.setOut(console);
        }
    }
}