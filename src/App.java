import javax.swing.*;

public class App {
    public static void main(String[] args) {
        int WIDTH = 500;
        int HEIGHT = 700;
        JFrame frame = new JFrame("Wordle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Wordle wordle = new Wordle();
        frame.add(wordle);
        frame.pack();
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}