import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Menu extends MouseAdapter {

    private final Game game;
    private BufferedImage background;
    private Game.State previousState = Game.State.MENU;

    private final Rectangle continueBtn = new Rectangle(100, 180, 320, 60);
    private final Rectangle newGameBtn  = new Rectangle(100, 260, 320, 60);
    private final Rectangle instrBtn    = new Rectangle(100, 340, 320, 60);
    private final Rectangle exitBtn     = new Rectangle(100, 420, 320, 60);

    private final Rectangle resumeBtn      = new Rectangle(490, 200, 320, 60);
    private final Rectangle instrOptionBtn = new Rectangle(490, 280, 320, 60);
    private final Rectangle restartBtn     = new Rectangle(490, 360, 320, 60);
    private final Rectangle menuBtn        = new Rectangle(490, 440, 320, 60);

    private final Rectangle backBtn = new Rectangle(50, 50, 120, 50);

    private final Rectangle summaryNextBtn    = new Rectangle(0, 0, 320, 60);
    private final Rectangle summaryRestartBtn = new Rectangle(0, 0, 320, 60);
    private final Rectangle summaryMenuBtn    = new Rectangle(0, 0, 320, 60);

    public Menu(Game game) {
        this.game = game;
        try {
            background = ImageIO.read(new File("resources/tlo.jpg"));
        } catch (Exception e) {
            background = null;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (Game.gameState == Game.State.MENU) {
            if (continueBtn.contains(mx, my) && SaveManager.exists()) {
                game.continueGame();
            } else if (newGameBtn.contains(mx, my)) {
                game.newGame();
            } else if (instrBtn.contains(mx, my)) {
                previousState = Game.State.MENU;
                Game.gameState = Game.State.INSTRUCTIONS;
            } else if (exitBtn.contains(mx, my)) {
                System.exit(0);
            }
        }
        else if (Game.gameState == Game.State.OPTIONS) {
            if (resumeBtn.contains(mx, my)) {
                Game.gameState = Game.State.GAME;
            } else if (instrOptionBtn.contains(mx, my)) {
                previousState = Game.State.OPTIONS;
                Game.gameState = Game.State.INSTRUCTIONS;
            } else if (restartBtn.contains(mx, my)) {
                if (game.getGameplay() != null) game.getGameplay().restartLevel();
            } else if (menuBtn.contains(mx, my)) {
                Game.gameState = Game.State.MENU;
            }
        }
        else if (Game.gameState == Game.State.INSTRUCTIONS) {
            if (backBtn.contains(mx, my)) {
                Game.gameState = previousState;
            }
        }
        else if (Game.gameState == Game.State.LEVEL_SUMMARY) {
            if (game.getGameplay() == null) return;
            if (summaryNextBtn.contains(mx, my)) {
                game.getGameplay().nextLevel();
            } else if (summaryRestartBtn.contains(mx, my)) {
                game.getGameplay().restartLevel();
            } else if (summaryMenuBtn.contains(mx, my)) {
                Game.gameState = Game.State.MENU;
            }
        }
    }

    public void update() {}

    public void render(Graphics g) {
        int w = game.getWidth();
        int h = game.getHeight();

        if (Game.gameState == Game.State.MENU ||
                (Game.gameState == Game.State.INSTRUCTIONS && previousState == Game.State.MENU)) {
            if (background != null) g.drawImage(background, 0, 0, w, h, null);
            else { g.setColor(new Color(60, 60, 60)); g.fillRect(0, 0, w, h); }
        }
        else if (Game.gameState == Game.State.OPTIONS ||
                Game.gameState == Game.State.LEVEL_SUMMARY ||
                (Game.gameState == Game.State.INSTRUCTIONS && previousState == Game.State.OPTIONS)) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, w, h);
        }

        switch (Game.gameState) {
            case MENU -> renderMainMenu(g);
            case OPTIONS -> renderOptions(g, w);
            case INSTRUCTIONS -> renderInstructions(g, w, h);
            case LEVEL_SUMMARY -> renderLevelSummary(g, w, h);
            default -> {}
        }
    }

    private void renderMainMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Kawiarnia Telekomunikacyjna", 100, 110);
        drawButton(g, continueBtn, "Kontynuuj", SaveManager.exists());
        drawButton(g, newGameBtn, "Nowa gra", true);
        drawButton(g, instrBtn, "Instrukcja", true);
        drawButton(g, exitBtn, "Wyjście", true);
    }

    private void renderOptions(Graphics g, int w) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Pauza", w / 2 - 60, 150);
        drawButton(g, resumeBtn, "Wróć do gry", true);
        drawButton(g, instrOptionBtn, "Instrukcja", true);
        drawButton(g, restartBtn, "Restart poziomu", true);
        drawButton(g, menuBtn, "Menu główne", true);
    }

    // --- ZMIANA: PRZEPISY NA KAWY ---
    private void renderInstructions(Graphics g, int w, int h) {
        drawButton(g, backBtn, "Wróć", true);

        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("KSIĘGA PRZEPISÓW", 220, 80);

        int x = 100;
        int y = 140;
        int gap = 30;

        // NAGŁÓWEK
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Wymagania dla każdego napoju:", x, y);
        y += gap + 10;

        g.setFont(new Font("Arial", Font.BOLD, 22));

        // 1. ESPRESSO
        g.setColor(Color.YELLOW);
        g.drawString("ESPRESSO:", x, y);
        g.setColor(Color.WHITE);
        g.drawString("1x Przycisk Single (1 shot)", x + 200, y);
        y += gap;

        // 2. DOUBLE
        g.setColor(Color.ORANGE);
        g.drawString("DOUBLE:", x, y);
        g.setColor(Color.WHITE);
        g.drawString("1x Przycisk Double (2 shoty)", x + 200, y);
        y += gap;

        // 3. AMERICANO
        g.setColor(Color.CYAN);
        g.drawString("AMERICANO:", x, y);
        g.setColor(Color.WHITE);
        g.drawString("1x Przycisk Woda + 1x Przycisk Single", x + 200, y);
        y += gap;

        // 4. LATTE
        g.setColor(new Color(255, 200, 200));
        g.drawString("LATTE:", x, y);
        g.setColor(Color.WHITE);
        g.drawString("1x Przycisk Single + Spienione Mleko", x + 200, y);
        y += gap + 10;

        g.setFont(new Font("Arial", Font.ITALIC, 16));
        g.setColor(Color.GRAY);
        g.drawString("Uwaga: Do Latte musisz wybrać odpowiednie mleko (Krowie/Sojowe/Bez laktozy)!", x, y);
        y += gap + 20;

        // PORADY
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("JAK PRZYGOTOWAĆ?", x, y);
        y += gap;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("1. MIELENIE: Przytrzymaj myszkę na młynku, aż pasek będzie zielony.", x, y);
        y += gap;
        g.drawString("2. SPIENIANIE: Wlej mleko, włóż dzbanek, przytrzymaj do zielonego paska.", x, y);
    }

    private void renderLevelSummary(Graphics g, int w, int h) {
        Graphics2D g2d = (Graphics2D) g;
        int boxW = 600;
        int boxH = 500;
        int boxX = (w - boxW) / 2;
        int boxY = (h - boxH) / 2;

        g.setColor(new Color(245, 240, 230));
        g.fillRect(boxX, boxY, boxW, boxH);

        g.setColor(new Color(60, 40, 20));
        g2d.setStroke(new BasicStroke(3));
        g.drawRect(boxX, boxY, boxW, boxH);

        g.setColor(new Color(90, 60, 40));
        g2d.setStroke(new BasicStroke(1));
        g.drawRect(boxX + 5, boxY + 5, boxW - 10, boxH - 10);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        String title = "Raport Dzienny";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, boxX + (boxW - fm.stringWidth(title)) / 2, boxY + 60);

        Gameplay gp = game.getGameplay();
        if (gp == null) return;

        int startX = boxX + 60;
        int valX = boxX + boxW - 60;
        int y = boxY + 120;
        int gap = 35;

        g.setFont(new Font("Arial", Font.PLAIN, 20));

        g.setColor(new Color(0, 100, 0));
        g.drawString("Sprzedaż + Napiwki:", startX, y);
        String incStr = String.format("+ %.2f PLN", gp.getLevelIncome());
        g.drawString(incStr, valX - g.getFontMetrics().stringWidth(incStr), y);
        y += gap;

        g.setColor(new Color(180, 0, 0));
        g.drawString("Towar, Czynsz i Kary:", startX, y);
        String expStr = String.format("- %.2f PLN", gp.getLevelExpenses());
        g.drawString(expStr, valX - g.getFontMetrics().stringWidth(expStr), y);
        y += gap;

        g.setColor(Color.GRAY);
        g.drawLine(startX, y - 10, valX, y - 10);
        y += 10;

        double profit = gp.getLevelIncome() - gp.getLevelExpenses();
        g.setFont(new Font("Arial", Font.BOLD, 22));

        if (profit >= 0) {
            g.setColor(new Color(0, 100, 0));
            g.drawString("Zysk Netto:", startX, y);
            String profStr = String.format("+ %.2f PLN", profit);
            g.drawString(profStr, valX - g.getFontMetrics().stringWidth(profStr), y);
        } else {
            g.setColor(Color.RED);
            g.drawString("Strata:", startX, y);
            String profStr = String.format("%.2f PLN", profit);
            g.drawString(profStr, valX - g.getFontMetrics().stringWidth(profStr), y);
        }
        y += gap * 1.5;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("Stan Konta:", startX, y);
        String balStr = String.format("%.2f PLN", gp.getMoney());
        g.setColor(gp.getMoney() >= 0 ? new Color(0, 80, 0) : Color.RED);
        g.drawString(balStr, valX - g.getFontMetrics().stringWidth(balStr), y);

        int btnW = 300;
        int btnH = 50;
        int btnX = boxX + (boxW - btnW) / 2;
        int btnStartY = boxY + 280;

        summaryNextBtn.setBounds(btnX, btnStartY, btnW, btnH);
        summaryRestartBtn.setBounds(btnX, btnStartY + 60, btnW, btnH);
        summaryMenuBtn.setBounds(btnX, btnStartY + 120, btnW, btnH);

        drawButton(g, summaryNextBtn, "Następny dzień", true);
        drawButton(g, summaryRestartBtn, "Powtórz dzień", true);
        drawButton(g, summaryMenuBtn, "Menu główne", true);
    }

    private void drawButton(Graphics g, Rectangle r, String text, boolean enabled) {
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(r.x + 3, r.y + 3, r.width, r.height);
        g.setColor(enabled ? new Color(100, 70, 50) : new Color(60, 60, 60));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(new Color(60, 40, 20));
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setColor(enabled ? Color.WHITE : Color.GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + (r.height + fm.getAscent()) / 2 - 5;
        g.drawString(text, tx, ty);
    }
}
