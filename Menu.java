import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Menu extends MouseAdapter {

    private BufferedImage background;
    private Game game;

    private Rectangle continueButton;
    private Rectangle newGameButton;
    private Rectangle optionsButton;
    private Rectangle exitButton;

    public Menu(Game game) {
        this.game = game;

        try {
            background = ImageIO.read(new File("resources/tło.jpg"));
        } catch (Exception e) {
            System.out.println("Nie udało się wczytać tła!");
            background = null;
        }

        int w = 300;
        int h = 60;

        continueButton = new Rectangle(100, 180, w, h);
        newGameButton  = new Rectangle(100, 260, w, h);
        optionsButton  = new Rectangle(100, 340, w, h);
        exitButton     = new Rectangle(100, 420, w, h);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (continueButton.contains(mx, my) && SaveManager.exists()) {
            game.continueGame();
        }

        if (newGameButton.contains(mx, my)) {
            game.newGame();
        }

        if (exitButton.contains(mx, my)) {
            System.exit(0);
        }
    }

    public void update() {}

    public void render(Graphics g) {
        int width = game.getWidth();
        int height = game.getHeight();

        if (background != null)
            g.drawImage(background, 0, 0, width, height, null);
        else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, width, height);
        }

        drawButton(g, continueButton, "Kontynuuj", SaveManager.exists());
        drawButton(g, newGameButton, "Nowa gra", true);
        drawButton(g, optionsButton, "Opcje", true);
        drawButton(g, exitButton, "Wyjście", true);
    }

    private void drawButton(Graphics g, Rectangle r, String text, boolean enabled) {
        g.setColor(enabled ? new Color(80, 50, 30) : new Color(40, 40, 40));
        g.fillRect(r.x, r.y, r.width, r.height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 35));
        g.drawString(text, r.x + 50, r.y + 40);
    }
}
