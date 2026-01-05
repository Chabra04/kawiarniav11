import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;
import java.util.List;
import java.util.ArrayList;
public class Game extends Canvas implements Runnable, MouseListener, MouseMotionListener {

    public static int WIDTH = 1280;
    public static int HEIGHT = 720;
    public static final String TITLE = "Kawiarnia Telekomunikacyjna, Bartłomiej Stachniuk 198205";

    public enum State {
        MENU,
        GAME,
        KITCHEN,
        OPTIONS,
        INSTRUCTIONS,
        LEVEL_SUMMARY
    }

    public static State gameState = State.MENU;

    private Thread thread;
    private boolean running = false;

    private final Menu menu;
    private Gameplay gameplay;

    public int currentLevel = 1;
    public int score = 0;
    public double money = 300.0;
    public int clientsServedFromSave = 0;
    public int loadedBeans = 60;
    public int loadedMilkCow = 600;
    public int loadedMilkLactose = 600;
    public int loadedMilkSoy = 600;

    public List<Client> loadedClients = new ArrayList<>();
    public Game() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));

        this.menu = new Menu(this);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public synchronized void start() {
        if (running) return;
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                tick();
                delta--;
            }
            if (running) render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frames = 0;
            }
        }
        stop();
    }

    private void tick() {
        if (gameState == State.GAME || gameState == State.KITCHEN) {
            if (gameplay != null) gameplay.update();
        } else if (gameState == State.MENU || gameState == State.OPTIONS ||
                gameState == State.INSTRUCTIONS || gameState == State.LEVEL_SUMMARY) {
            menu.update();
        }
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Rysowanie Gry
        if (gameState == State.GAME || gameState == State.KITCHEN ||
                gameState == State.OPTIONS || gameState == State.LEVEL_SUMMARY) {
            if (gameplay != null) gameplay.render(g);
            if (gameState == State.GAME || gameState == State.KITCHEN) {
                g.setColor(new Color(200, 50, 50));
                g.fillRect(WIDTH - 80, HEIGHT - 60, 60, 40);
                g.setColor(Color.WHITE);
                g.drawRect(WIDTH - 80, HEIGHT - 60, 60, 40);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("OPCJE", WIDTH - 70, HEIGHT - 35);
            }
        }

        if (gameState == State.MENU || gameState == State.OPTIONS ||
                gameState == State.INSTRUCTIONS || gameState == State.LEVEL_SUMMARY) {
            menu.render(g);
        }

        g.dispose();
        bs.show();
    }

    public void newGame() {
        this.currentLevel = 1;
        this.score = 0;
        this.money = 300.0;
        this.gameplay = new Gameplay(this, false);
        gameState = State.GAME;
    }

    public void continueGame() {
        SaveManager.load(this);
        this.gameplay = new Gameplay(this, true);
        gameState = State.GAME;
    }

    public Gameplay getGameplay() {
        return gameplay;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (gameState == State.GAME || gameState == State.KITCHEN) {

            // --- ZMIANA: Obsługa przycisku OPCJE w GRZE i KUCHNI ---
            if (mx > WIDTH - 80 && my > HEIGHT - 60) {
                gameState = State.OPTIONS;
                return;
            }

            if (gameplay != null) gameplay.mouseClicked(mx, my);

        } else {
            menu.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameState == State.KITCHEN && gameplay != null) {
            gameplay.mouseReleased(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameState == State.KITCHEN && gameplay != null) {
            gameplay.kitchenMouseMoved(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (gameState == State.KITCHEN && gameplay != null) {
            gameplay.kitchenMouseMoved(e.getX(), e.getY());
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        Game game = new Game();

        JFrame frame = new JFrame(TITLE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.add(game);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        if (frame.getWidth() > 0 && frame.getHeight() > 0) {
            Game.WIDTH = frame.getWidth();
            Game.HEIGHT = frame.getHeight();
        }

        game.start();
    }
}
