package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String GAME_SETTINGS = "config.yml";
    private static int MIN;
    private static int MAX;
    private static int ATTEMPTS;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        setGameSettings();
        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    private void setGameSettings() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(GAME_SETTINGS)))){
            String line;
            while ((line = br.readLine()) != null) {
                final String args[] =  line.split(": ");
                switch (args[0]) {
                    case "minmum":
                        MIN = Integer.parseInt(args[1]);
                        break;
                    case "maximum":
                        MAX = Integer.parseInt(args[1]);
                        break;
                    case "attempts":
                        ATTEMPTS = Integer.parseInt(args[1]);
                        break;
                    default:
                        throw new IllegalArgumentException(GAME_SETTINGS + " is not well configured");
                }
            }
        } catch (Exception e) {
            views.forEach(v -> v.displayError(e.getMessage()));
        }
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(), new PrintStreamView(".log"), new PrintStreamView(System.out));
    }

}
