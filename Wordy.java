import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.*;


class WordyGame {
    private final JFrame frame;
    private final JPanel mainPanel;
    private final JLabel title;
    private final JPanel wordPan;
    private final JPanel[] wordGuessPan;

    /**
     * Font
     */
    private Font sansSerif18;

    /**
     * The current row and column in
     * the play grid
     */
    private int cursorRow;
    private int cursorCol;

    /**
     * White Color
     */
    private static final Color whiteColor = new Color(230, 230, 230);

    /**
     * Black, used when character is not in the word
     */
    private static final Color characterNotInWord = new Color(58, 58, 60);

    /**
     * Yellow, used when character is in the word but the position is wrong
     */
    private static final Color characterInWordWrongPos = new Color(177, 160, 76);

    /**
     * Green, used when character is in the word and the correct position
     */
    private static final Color characterInWordCorrectPos = new Color(97, 140, 85);

    /**
     * If a game is active
     */
    private boolean Active;

    /**
     * The Random Word!
     */
    private String guessWord = "";

    public WordyGame() {

        /* Game Font */
        sansSerif18 = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
        

        /* Call to function that picks a random  word from the list. */
        pickWord();

         /* Backdoor to the correct word */
         System.out.println("WORD: " + guessWord);

        /* Game is set to Active */
        Active = true;

        /* Initial Cursor */
        cursorRow = 0;
        cursorCol = -1;



        /* JFrame */
        frame = new JFrame("Wordy - Musa & Abufazl");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                char key = event.getKeyChar();
                switch (key) {
                    case '9':
                        pickWord();
                        cursorCol = -1;
                        cursorRow = 0;
                        Active = true;
                        
                        clearBoard();
                        break;
                    case '1':
                        JOptionPane.showMessageDialog(frame, "How To Play: \r\n• Guess the correct word! \r\n• Each guess must be a valid 5-letter word. \r\n• The color of the tiles will change to show how close your guess was to the word. \r\n• Press '9' to Restart \r\n\r\n Colors: \r\n• Green - Correct letter and position  \r\n• Yellow - Present in word but wrong position  \r\n• Black - Letter not in word \r\n\r\n Credits: \r\n• Musa Aqeel & Abulfazl Hassani", "Help", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        /* Ignore other inputs if game is inactive*/
                        if (!Active) {
                            break;
                        }
                        /* Check if user pressed a character */
                        if (Character.isLetter(key)) {
                            /* Ignore if user is @ end of row */
                            if (cursorCol >= 4) {
                                break;
                            }
                            /* Increment col, insert character */
                            cursorCol++;
                            setGuessCharacter(cursorRow, cursorCol, whiteColor, Character.toUpperCase(key));
                            break;
                        }
                        /* If user backspaces, remove last character */
                        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                             /* Ignore if @ start of row */
                            if (cursorCol < 0) {
                                break;
                            }
                             /* Set character to ' ' and decrement column */
                            setGuessCharacter(cursorRow, cursorCol, whiteColor, ' ');
                            cursorCol--;
                            break;
                        }
                         /* If user clicks enter, guess is confirmed */
                        if (key == KeyEvent.VK_ENTER) {
                            /* Ignore if there isnt sufficent letters */
                            if (cursorCol < 4) {
                                break;
                            }
                             /* Construct word and check it is in the words file */
                            String word = "";
                            for (int i = 0; i < 5; i++) {
                                word = word + getGuessCharacter(cursorRow, i);
                            }
                            if (!wordInFile(word)) {
                                JOptionPane.showMessageDialog(frame, "Not a word!", "That is not a word.", JOptionPane.ERROR_MESSAGE);
                                break;
                            }
                            /* Check if guess was correct */
                            boolean win = checkGuess();
                            /* Not gusssed and not on last row.  */
                            if (!win && cursorRow < 4) {
                                cursorCol = -1;
                                cursorRow++;
                                break;
                            }
                            /* Not gussed and last Row, end the game  */
                            if (!win && !(cursorRow < 4)) {
                                JOptionPane.showMessageDialog(frame, "The correct was: "+guessWord+".", "You failed :(", JOptionPane.ERROR_MESSAGE);
                                Active = false;
                                break;
                            }
                            /* Correct Guess*/
                            JOptionPane.showMessageDialog(frame, "Nice! You guessed the word!", "You win", JOptionPane.INFORMATION_MESSAGE);
                            Active = false;
                            break;
                        }
                }
            }

            /**
             * Resets Game
             */
            private void clearBoard() {
                /* Choose new random word, reset cursor,
                     * and set game to active
                     */
                    pickWord();
                    /* Backdoor to the correct word */
                     System.out.println("WORD: " + guessWord);
                    cursorCol = -1;
                    cursorRow = 0;
        
                    Active = true;
        
                    /* Clear the board */
                    for (int y = 0; y < 5; y++) {
                        for (int x = 0; x < 5; x++) {
                            setGuessCharacter(y, x, whiteColor, ' ');
                        }
                    }
                    return;
                
            }
        });
        



        /* Main panel */
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        frame.add(mainPanel);

        /* Title */
        title = new JLabel("Wordy");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mainPanel.add(title);  

        /* Holds the rows */ 
        wordPan = new JPanel();
        wordPan.setLayout(new BoxLayout(wordPan, BoxLayout.Y_AXIS));
        mainPanel.add(wordPan);

        /* Holds the word guess rows */
        wordGuessPan = new JPanel[5];

        for (int i = 0; i < 5; i++) {
        JPanel wordRow = new JPanel();
        wordRow.setLayout(new BoxLayout(wordRow, BoxLayout.X_AXIS));
        wordGuessPan[i] = wordRow;

        for (int j = 0; j < 5; j++) {
            JPanel charBox = new JPanel();
            charBox.setLayout(new BorderLayout());
            charBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            charBox.setMinimumSize(new Dimension(50, 50));
            charBox.setPreferredSize(new Dimension(50, 50));
            charBox.setMaximumSize(new Dimension(50, 50));

            JLabel charLabel = new JLabel("", SwingConstants.CENTER);
            charLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
            charLabel.setOpaque(true);
            charLabel.setFont(sansSerif18);
            charLabel.setBackground(whiteColor);
            charBox.add(charLabel);

            wordRow.add(charBox);
        }
        wordPan.add(wordRow);
        }
        frame.pack();

          
    }

    /**
     * Checks if guess is present in the words file
     * @return If the word is present
     */

     private final boolean wordInFile(String word) {
        boolean inWordsFile = false;
        try (Stream<String> lines = Files.lines(new File("5LetterWords.txt").toPath())) {
            inWordsFile = lines.anyMatch(s -> s.equalsIgnoreCase(word));
        } catch (IOException e) {
            /* handle the exception */
        }
        return inWordsFile;
    }
    
    /**
     * Chooses a random word and sets guessWord
     */
    private final void pickWord() {
        /* Create a File object for the source file "5LetterWords.txt" */
        File source = new File("5LetterWords.txt");


        try {
            /* Read all the lines from the source file and store them in a List */
            List<String> possibleWords = Files.readAllLines(source.toPath());

            /* Generate a random index within the bounds of the possibleWords list */
            int randomIndex = new Random().nextInt(possibleWords.size());

            /* Set the `guessWord` attribute to the randomly selected word, converted to uppercase */
            this.guessWord = possibleWords.get(randomIndex).toUpperCase();
        } catch (IOException ex) {
            /* If an IOException occurs, set the `guessWord` attribute to the default value "Wordy" */
            this.guessWord = "Wordy";
        }
    }
    

    /**
     * Checks if the current guess is correct
     */
    private boolean checkGuess() {
        Hashtable<Character, Integer> charCounts = new Hashtable<>();
        Boolean[] checkedIndexes = new Boolean[5];
        Arrays.fill(checkedIndexes, false);
    
        for (int i = 0; i < 5; i++) {
            char c = getGuessCharacter(cursorRow, i);
            charCounts.putIfAbsent(c, 0);
        }
    
        boolean isCorrect = true;
        for (int i = 0; i < 5; i++) {
            char c = getGuessCharacter(cursorRow, i);
            if (guessWord.charAt(i) == c) {
                checkedIndexes[i] = true;
                setGuessCharacter(cursorRow, i, characterInWordCorrectPos, c);
                charCounts.put(c, charCounts.get(c)+1);
            } else {
                isCorrect = false;
            }
        }
    
        if (isCorrect) return true;
    
        for (int i = 0; i < 5; i++) {
            if (checkedIndexes[i]) continue;
    
            char c = getGuessCharacter(cursorRow, i);
            long charAppearance = guessWord.chars().filter(ch -> ch == c).count();
            if (charCounts.get(c) >= charAppearance) {
                setGuessCharacter(cursorRow, i, characterNotInWord, c);
                charCounts.put(c, charCounts.get(c)+1);
            } else {
                setGuessCharacter(cursorRow, i, characterInWordWrongPos, c);
                charCounts.put(c, charCounts.get(c)+1);
            }
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
        JPanel characterPanel = (JPanel) wordGuessPan[row].getComponent(col);
        JLabel characterLabel = (JLabel) characterPanel.getComponent(0);
        

        return characterLabel.getText().toCharArray()[0];
    }

    /**
     * Set Color
     */
    private final void setGuessCharacter(int row, int col, Color color, char character) {
        JPanel characterPanel = (JPanel) wordGuessPan[row].getComponent(col);
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

public class Wordy {
    public static void main(String[] args) {
        WordyGame game = new WordyGame();
        game.start();
    }
}