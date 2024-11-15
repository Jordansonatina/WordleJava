import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Wordle extends JPanel implements ActionListener, KeyListener {
    Timer gameLoop;

    int WIDTH = 500;
    int HEIGHT = 700;

    private char[][] letters;
    private char nullCharacter = ' ';
    private int guesses;
    private int currentIndexOfWord;
    private String answer;
    private Set<String> dictionary;
    private int totalAnswers;
    private String[] answers;

    private int tileSize = 80;
    private int tileMargin = 10;
    private int fontSize = 50;

    private boolean end = false;

    private Font font;

    // Colors
    Color backgroundColor = Color.WHITE;
    Color borderIdleColor = new Color(170, 170, 170);
    Color borderSelectedColor = new Color(120, 124, 126);
    Color textIdleColor = Color.BLACK;
    Color textSelectedColor = Color.WHITE;
    Color wrongLetterColor = new Color(120, 124, 126);
    Color letterInWordColor = new Color(201, 180, 88);
    Color letterInCorrectPositionColor = new Color(106, 170, 100);

    public Wordle() {
        setPreferredSize(new Dimension(600, 600));
        setFocusable(true);
        addKeyListener(this);

        initializeLettersArray();
        loadAllowedGuesses();
        loadAnswers();
        selectAnswer();
        loadFont();

        guesses = 0;
        currentIndexOfWord = 0;

        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }
    private void initializeLettersArray() {
        letters = new char[6][5];
        for (char[] letter : letters) {
            Arrays.fill(letter, nullCharacter);
        }
    }
    private void clearLettersArray() {
        for (char[] letter : letters) {
            Arrays.fill(letter, nullCharacter);
        }
    }
    private void loadFont() {
        try (InputStream fontStream = getClass().getResourceAsStream("font.otf")) {
            if (fontStream == null) {
                throw new IOException("Font file not found in resources");
            }
            font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont((float)fontSize);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            font = new Font("Arial", Font.BOLD, fontSize); // Fallback font
        }
    }
    private void loadAllowedGuesses() {
        dictionary = new HashSet<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("allowed.txt");

        if (inputStream == null) {
            System.out.println("File not found in resources!");
            return;
        }

        // Use BufferedReader to read the file content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.toUpperCase());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadAnswers() {
        totalAnswers = 2315;
        answers = new String[totalAnswers];

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("answers.txt");

        if (inputStream == null) {
            System.out.println("File not found in resources!");
            return;
        }

        // Use BufferedReader to read the file content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int i = 0;
            String line;
            while ((line = reader.readLine()) != null) {

                answers[i] = line.toUpperCase();
                i++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean charCompletelySolved(char letter, String word) {
        int charOccurences = (int)answer.chars().filter(c -> c == letter).count();
        int charCounter = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter && answer.charAt(i) == word.charAt(i)) {
                charCounter++;
            }
        }
        return charCounter == charOccurences;
    }
    private void selectAnswer() {
        int randomIndex = (int) (Math.random() * totalAnswers);
        answer = answers[randomIndex];
    }
    private Color determineColorOfTile(int row, int col) {
        // if the row hasn't been guessed, don't show color hints
        if (row >= guesses) {
            return backgroundColor;
        }
        char letter = letters[row][col];
        String word = getWord(row);

        // check to see if letter has been completely solved in word by guesses

        // if letter is in the answer, AND in correct index use green color
        if (answer.charAt(col) == word.charAt(col)) {
            return letterInCorrectPositionColor;
        }
        // if letter is in the answer, then use gold color
        if (answer.contains(String.valueOf(letter)) && !charCompletelySolved(letter, word)) {
            return letterInWordColor;
        } else {
            return wrongLetterColor;
        }
    }
    private void printLetters() {
        for (char[] letter : letters) {
            for (char c : letter) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }
    private void addLetter(char c) {
        int maxCharacters = letters[0].length;

        if (end) {
            return;
        }

        if (!Character.isLetter(c)) {
            System.out.println("Character entered is not a letter.");
            return;
        } else if (currentIndexOfWord > maxCharacters - 1) {
            System.out.println("Row is filled. Delete letters.");
            return;
        }
        c = Character.toUpperCase(c);
        letters[guesses][currentIndexOfWord] = c;
        currentIndexOfWord++;
    }
    private void removeLetter() {
        if (end) {
            return;
        }
        // If user hasn't entered letters, don't try to remove them.
        if (currentIndexOfWord == 0) {
            System.out.println("No letters to delete.");
            return;
        }
        letters[guesses][currentIndexOfWord-1] = nullCharacter;
        currentIndexOfWord--;
    }
    private String getWord(int row) {
        StringBuilder word = new StringBuilder();
        for (char c : letters[row]) {
            word.append(c);
        }
        return word.toString();
    }
    private void attemptGuess() {
        if (end) {
            return;
        }
        String wordGuess = getWord(guesses);

        if (currentIndexOfWord < 5) {
            System.out.println("Guess needs to be a complete word.");
            return;
        }

        if (!dictionary.contains(wordGuess)) {
            System.out.println("Invalid word.");
            return;
        }

        if (isCorrectWord(wordGuess)) {
            winGame();
        } else {
            newGuess();
        }
    }
    private boolean isCorrectWord(String word) {
        return word.equals(answer);
    }
    private void newGuess() {
        System.out.println(guesses);
        if (guesses >= 5) {
            loseGame();
            return;
        }
        System.out.println("Incorrect Word, try again.");
        guesses++;
        currentIndexOfWord = 0;
    }
    private void loseGame() {
        guesses++;
        currentIndexOfWord = 0;
        System.out.println("You lost!");
        end = true;
    }
    private void winGame() {
        guesses++;
        currentIndexOfWord = 0;
        end = true;
        System.out.println("You won!");
    }
    private void resetGame() {
        end = false;
        clearLettersArray();
        guesses = 0;
        currentIndexOfWord = 0;
        selectAnswer();
    }
    private void render(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        // render grid
        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < letters[0].length; j++) {
                int widthGrid = (tileSize + tileMargin) * letters[0].length;
                int lengthGrid = (tileSize + tileMargin) * letters.length;
                int offsetToCenterX = (WIDTH - widthGrid) / 2 - tileMargin/4;
                int offsetToCenterY = (HEIGHT - lengthGrid) / 2;
                int x = offsetToCenterX + j * (tileSize + tileMargin);
                int y = offsetToCenterX + i * (tileSize + tileMargin);
                g.setColor(determineColorOfTile(i, j));
                g.fillRoundRect(x, y, tileSize, tileSize, 10, 10);

                // if there is a letter in a tile, then make the border darker
                if (letters[i][j] == nullCharacter) {
                    g.setColor(borderIdleColor);
                } else {
                    if (i < guesses) {
                        g.setColor(determineColorOfTile(i,j));
                    } else {
                        g.setColor(borderSelectedColor);
                    }
                }
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(x, y, tileSize, tileSize, 10, 10);

                // if a row has already been guessed, make the font white
                if (i < guesses) {
                    g.setColor(textSelectedColor);
                } else {
                    g.setColor(textIdleColor);
                }
                g.setFont(font);
                FontMetrics metrics = g.getFontMetrics(font);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int textCenteredX = x + (tileSize - metrics.stringWidth(String.valueOf(letters[i][j]))) / 2;
                int textCenteredY = y + ((tileSize - metrics.getHeight()) / 2) + metrics.getAscent();

                g.drawString(String.valueOf(letters[i][j]), textCenteredX, textCenteredY+tileMargin);
                if (end) {
                    g.setColor(textIdleColor);
                    g.drawString(answer, WIDTH / 2 - metrics.stringWidth(answer) / 2 - tileMargin, HEIGHT-offsetToCenterX*3);
                }


            }
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        render(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (end && e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            removeLetter();
            return;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            attemptGuess();
            return;
        }
        addLetter(e.getKeyChar());
    }
    @Override
    public void keyReleased(KeyEvent e) {

    }
}
