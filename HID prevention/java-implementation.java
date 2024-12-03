import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;

public class KeyMonitor {
    private static final double TYPING_THRESHOLD = 0.019;
    private static final double DEBOUNCE_TIME = 0.5;
    private static final double LONG_PRESS_THRESHOLD = 1.0;
    private static final double IGNORED_KEY_GAP = 0.014;
    private static final String PASSWORD = "123";
    private static long lastKeyTime = 0;
    private static long lastTriggerTime = 0;
    private static Key lastKey = null;
    private static Map<Key, Long> keyPressDuration = new HashMap<>();
    private static boolean isPasswordPromptActive = false;
    private static int fastTypingCount = 0; // Track consecutive fast typing
    private static String enteredPassword = "";
    private static Logger logger = Logger.getLogger(KeyMonitor.class.getName());

    public static void main(String[] args) {
        setupLogger();
        SwingUtilities.invokeLater(KeyMonitor::startGui);
    }

    private static void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("keystroke_log.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logEvent(String message) {
        logger.info(message);
    }

    private static boolean isValidKey(Key key) {
        return key.getID() == KeyEvent.KEY_TYPED || Character.isLetterOrDigit(key.getKeyChar());
    }

    private static void showPasswordPrompt() {
        if (isPasswordPromptActive) return;
        isPasswordPromptActive = true;

        JFrame window = new JFrame("Password Prompt");
        window.setSize(400, 300);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setAlwaysOnTop(true);
        window.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter Password:");
        window.add(label);

        JPasswordField passwordField = new JPasswordField(20);
        window.add(passwordField);

        JButton submitButton = new JButton("Submit");
        window.add(submitButton);

        JLabel resultLabel = new JLabel();
        window.add(resultLabel);

        submitButton.addActionListener(e -> {
            enteredPassword = new String(passwordField.getPassword());
            if (enteredPassword.equals(PASSWORD)) {
                logEvent("Password entered correctly: " + enteredPassword);
                JOptionPane.showMessageDialog(window, "Password entered correctly! Access granted.");
                window.dispose();
            } else {
                logEvent("Incorrect password attempt: " + enteredPassword);
                resultLabel.setText("Incorrect password, please try again.");
            }
            isPasswordPromptActive = false;
        });

        window.setVisible(true);
    }

    private static void startGui() {
        JFrame frame = new JFrame("Key Press Monitor");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel messageLabel = new JLabel("Key monitoring is off", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(messageLabel);

        JButton toggleButton = new JButton("Turn On Monitoring");
        toggleButton.addActionListener(e -> toggleMonitoring(toggleButton, messageLabel));
        frame.add(toggleButton);

        frame.setVisible(true);
    }

    private static void toggleMonitoring(JButton button, JLabel label) {
        if (label.getText().equals("Key monitoring is off")) {
            label.setText("Key monitoring is on");
            button.setText("Turn Off Monitoring");
            startKeyListener();
        } else {
            label.setText("Key monitoring is off");
            button.setText("Turn On Monitoring");
            stopKeyListener();
        }
    }

    private static void startKeyListener() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(KeyMonitor::onKeyPress);
    }

    private static void stopKeyListener() {
        // Java does not provide an easy way to stop key listeners, so this would be a manual step
        // by removing the dispatcher or modifying the event listener to return early.
    }

    private static boolean onKeyPress(KeyEvent e) {
        if (isPasswordPromptActive) {
            return true;
        }

        long currentTime = System.currentTimeMillis();

        if (lastKeyTime != 0) {
            double timeDiff = (currentTime - lastKeyTime) / 1000.0;
            if (timeDiff < IGNORED_KEY_GAP) {
                return true;
            }
        }

        if (keyPressDuration.containsKey(e.getKey())) {
            keyPressDuration.put(e.getKey(), currentTime);
        } else {
            keyPressDuration.put(e.getKey(), currentTime);
        }

        if (!isValidKey(e.getKey())) {
            return true;
        }

        if (lastKeyTime != 0) {
            double timeDiff = (currentTime - lastKeyTime) / 1000.0;
            if (timeDiff < TYPING_THRESHOLD) {
                fastTypingCount++;
                if (fastTypingCount >= 2) {
                    if (lastTriggerTime == 0 || (currentTime - lastTriggerTime) > DEBOUNCE_TIME) {
                        logEvent("Fast typing detected! Triggering password entry...");
                        JOptionPane.showMessageDialog(null, "Fast typing detected! Triggering password entry...");
                        showPasswordPrompt();
                        openNotepadAndFocus();
                        lastTriggerTime = currentTime;
                    }
                }
            } else {
                fastTypingCount = 0;
                logEvent(String.format("Time between keys: %.4f seconds", timeDiff));
            }
        } else {
            logEvent("First key press detected");
        }

        lastKeyTime = currentTime;
        lastKey = e.getKey();
        return true;
    }

    private static void openNotepadAndFocus() {
        try {
            Runtime.getRuntime().exec("notepad.exe");
            Thread.sleep(1000); // Wait for Notepad to open
            bringNotepadToForeground();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void bringNotepadToForeground() {
        try {
            Robot robot = new Robot();
            // Focus the window (Simulated ALT+TAB or similar may be necessary for advanced window management)
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_ALT);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
