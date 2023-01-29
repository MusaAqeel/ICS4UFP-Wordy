import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import javax.swing.*;

class WordleGame {
    private final JFrame frame;
    private final JPanel mainPanel;
    private final JLabel title;
    private final JPanel wordGuessesPanel;
    private final JPanel[] wordGuessRowPanels;

    /**
     * Custom font
     */
    private Font sansSerif18;

    /**
     * The current row and column in
     * the play grid
     */
    private int cursorRow;
    private int cursorCol;

    /**
     * Colors
     */
    private static final Color characterDefaultColor = new Color(230, 230, 230);
    private static final Color characterNotPresentColor = new Color(58, 58, 60);
    private static final Color characterPresentAndWrongPosition = new Color(177, 160, 76);
    private static final Color characterPresentAndCorrectPosition = new Color(97, 140, 85);

    /*
     * If a game is active
     */
    private boolean gameActive;

    /*
     * The random word needing to be guessed
     */
    private String guessWord = "";

    public WordleGame() {

        /* Register the custom Roboto font */
        sansSerif18 = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
        

        /* Choose a random first word */
        pickWord();

        /* Set game to active */
        gameActive = true;

        /* Initial cursor position */
        cursorRow = 0;
        cursorCol = -1;


        /* Create JFrame */
        frame = new JFrame("Wordy - Musa & Abufazl");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                /* Reset game on Command/Control + R */
                if (event.getKeyCode() == KeyEvent.VK_R && (event.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    /* Choose new random word, reset cursor,
                     * and set game to active
                     */
                    pickWord();
        
                    cursorCol = -1;
                    cursorRow = 0;
        
                    gameActive = true;
        
                    /* Clear the board */
                    for (int y = 0; y < 5; y++) {
                        for (int x = 0; x < 5; x++) {
                            setGuessCharacter(y, x, characterDefaultColor, ' ');
                        }
                    }
                    return;
                }

                /* Ignore other inputs if there is no game active */
                if (!gameActive) {
                    return;
                }

                /* Check if pressed a character */
                if (Character.isLetter(event.getKeyChar())) {
                    /* Ignore if at end of row */
                    if (cursorCol >= 4) {
                        return;
                    }

                    /* Increment column and insert character */
                    cursorCol++;
                    setGuessCharacter(cursorRow, cursorCol, characterDefaultColor, Character.toUpperCase(event.getKeyChar()));
                    return;
                }

                /* Check if BACKSPACE - remove character */
                if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    /* Ignore if at start of row */
                    if (cursorCol < 0) {
                        return;
                    }

                    /* Set character to ' ' and decrement column */
                    setGuessCharacter(cursorRow, cursorCol, characterDefaultColor, ' ');
                    cursorCol--;
                    return;
                }

                /* Check if ENTER - confirmed guess */
                if (event.getKeyChar() == KeyEvent.VK_ENTER) {
                    /* Ignore if not enough letters */
                    if (cursorCol < 4) {
                        return;
                    }

                    /* Construct word and check it exists in the file */
                    String word = "";
                    for (int i = 0; i < 5; i++) {
                        word = word + getGuessCharacter(cursorRow, i);
                    }
                    if (!wordInWordsFile(word)) {
                        JOptionPane.showMessageDialog(frame, "That word does not exist", "Non-existant word", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    /* Check if guess was correct */
                    boolean success = checkGuess();

                    /* If not success, but not on last row, increment row and reset column */
                    if (!success && cursorRow < 4) {
                        cursorCol = -1;
                        cursorRow++;
                        return;
                    }

                    /* If not success and at on last row, stop game and reveal answer */
                    if (!success && !(cursorRow < 4)) {
                        JOptionPane.showMessageDialog(frame, "The correct was: "+guessWord+".", "You failed", JOptionPane.ERROR_MESSAGE);
                        gameActive = false;
                        return;
                    }

                    /* They guessed the word! */
                    JOptionPane.showMessageDialog(frame, "Well done! You guessed the word.", "You win", JOptionPane.INFORMATION_MESSAGE);
                    gameActive = false;
                }
            }

        });



        /* Main panel */
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        frame.add(mainPanel);


        /* Title label */
        title = new JLabel("Wordy");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mainPanel.add(title);        

        /* Holds the rows */
        wordGuessesPanel = new JPanel();
        wordGuessesPanel.setLayout(new BoxLayout(wordGuessesPanel, BoxLayout.Y_AXIS));
        mainPanel.add(wordGuessesPanel);

        // This holds the word guess rows
        wordGuessRowPanels = new JPanel[5];

        for (int i = 0; i < 5; i++) {
            JPanel wordRow = new JPanel();
            wordRow.setLayout(new BoxLayout(wordRow, BoxLayout.X_AXIS));
            wordGuessRowPanels[i] = wordRow;
          
            for (int j = 0; j < 5; j++) {
              JPanel charContainer = new JPanel();
              charContainer.setLayout(new BorderLayout());
              charContainer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
              charContainer.setMinimumSize(new Dimension(50, 50));
              charContainer.setPreferredSize(new Dimension(50, 50));
              charContainer.setMaximumSize(new Dimension(50, 50));
          
              JLabel charLabel = new JLabel("", SwingConstants.CENTER);
              charLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
              charLabel.setOpaque(true);
              charLabel.setFont(sansSerif18);
              charLabel.setBackground(characterDefaultColor);
              charContainer.add(charLabel);
          
              wordRow.add(charContainer);
            }
            wordGuessesPanel.add(wordRow);
          }
          frame.pack();
          
    }

    /**
     * Checks if {@code word} is present in the words file
     *
     * @param word The word
     * @return If the word is present
     */

    private final boolean wordInWordsFile(String word) {
        boolean inWordsFile = false;

        try {
            inWordsFile = Files.lines(new File("5LetterWords.txt").toPath())
                    .anyMatch(s -> s.equalsIgnoreCase(word));
        } catch (IOException e) {
            /* handle the exception */
        }

        return inWordsFile;
    }

/**
 * Chooses a random word and sets the {@code guessWord} attribute
 * to it
 */
private final void pickWord() {
    // Create a File object for the source file "5LetterWords.txt"
    File source = new File("5LetterWords.txt");

    try {
        // Read all the lines from the source file and store them in a List
        List<String> possibleWords = Files.readAllLines(source.toPath());

        // Generate a random index within the bounds of the possibleWords list
        int randomIndex = new Random().nextInt(possibleWords.size());

        // Set the `guessWord` attribute to the randomly selected word, converted to uppercase
        this.guessWord = possibleWords.get(randomIndex).toUpperCase();
    } catch (IOException ex) {
        // If an IOException occurs, set the `guessWord` attribute to the default value "ICS4U"
        this.guessWord = "ICS4U";
    }
}

    /**
     * Checks if the users inputted word matches the word
     * to be guessed
     *
     * @return If the words match!
     */
    private final boolean checkGuess() {

        // Stores the count of each character checked
        Hashtable<Character, Integer> characterCounts = new Hashtable<>();

        // Pre-populate the hashtable
        for (int i = 0; i < 5; i++) {
            char c = getGuessCharacter(cursorRow, i);

            if (characterCounts.get(c) == null) {
                characterCounts.put(c, 0);
            }
        }

        // Holds what character indexes have been checked
        Boolean[] checkedIndexes = new Boolean[] {false, false, false, false, false};

        // Holds whether all the characters are in the
        // correct locations
        boolean isAllCorrect = true;

        // First pass, going through and comparing each
        // character to see if they are the same
        for (int i = 0; i < 5; i++) {
            char c = getGuessCharacter(cursorRow, i);

            if (guessWord.charAt(i) == c) {
                // Correct
                checkedIndexes[i] = true;
                setGuessCharacter(cursorRow, i, characterPresentAndCorrectPosition, c);
                characterCounts.put(c, characterCounts.get(c)+1);
                continue;
            }

            isAllCorrect = false;
        }

        // Check if the word is already correct after the first pass
        if (isAllCorrect) {
            return true;
        }

        // Send pass, going through all the missed indexes and seeing
        // if they exist in the word
        for (int i = 0; i < 5; i++) {
            if (checkedIndexes[i]) {
                // Already checked
                continue;
            }

            char c = getGuessCharacter(cursorRow, i);

            long characterAppearances = guessWord.chars().filter(ch -> ch == c).count();

            // Check if we have already seen all appearances
            if (characterCounts.get(c) >= characterAppearances) {
                setGuessCharacter(cursorRow, i, characterNotPresentColor, c);
                characterCounts.put(c, characterCounts.get(c)+1);
                continue;
            }

            // Not seen all appearances, so it is present, just not in
            // the correct position
            setGuessCharacter(cursorRow, i, characterPresentAndWrongPosition, c);
            characterCounts.put(c, characterCounts.get(c)+1);
        }

        return false;
    }

    /**
     * Gets the character at the specific row and col
     *
     * @param row Row
     * @param col Colum
     * @return The character
     */
    private final char getGuessCharacter(int row, int col) {
        JPanel characterPanel = (JPanel) wordGuessRowPanels[row].getComponent(col);
        JLabel characterLabel = (JLabel) characterPanel.getComponent(0);
        

        return characterLabel.getText().toCharArray()[0];
    }

    /**
     * Set the character and color of a grid cell thing
     *
     * @param row       Row
     * @param col       Colum
     * @param color     Color
     * @param character Character
     */
    private final void setGuessCharacter(int row, int col, Color color, char character) {
        JPanel characterPanel = (JPanel) wordGuessRowPanels[row].getComponent(col);
        JLabel characterLabel = (JLabel) characterPanel.getComponent(0);

        characterLabel.setText(Character.toString(character));
        characterLabel.setBackground(color);
    }

    /**
     * Starts the game
     */
    public final void start() {
        frame.setVisible(true);
    }



}

public class Wordle {
    public static void main(String[] args) {
        WordleGame game = new WordleGame();
        game.start();
    }
}