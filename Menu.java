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

    private int instructionPage = 0;
    private final int MAX_PAGES = 2;

    // ================= PRZYCISKI GŁÓWNE =================
    private final Rectangle continueBtn = new Rectangle(100, 180, 320, 60);
    private final Rectangle newGameBtn  = new Rectangle(100, 260, 320, 60);
    private final Rectangle instrBtn    = new Rectangle(100, 340, 320, 60);
    private final Rectangle exitBtn     = new Rectangle(100, 420, 320, 60);

    // ================= PRZYCISKI OPCJI (PAUZA) =================
    private final Rectangle resumeBtn      = new Rectangle(490, 200, 320, 60);
    private final Rectangle instrOptionBtn = new Rectangle(490, 280, 320, 60);
    private final Rectangle restartBtn     = new Rectangle(490, 360, 320, 60);
    private final Rectangle menuBtn        = new Rectangle(490, 440, 320, 60);

    // ================= PRZYCISKI NAWIGACJI W INSTRUKCJI =================
    private final Rectangle backBtn     = new Rectangle(50, 50, 120, 50);
    private final Rectangle nextPageBtn = new Rectangle(1100, 600, 150, 50);
    private final Rectangle prevPageBtn = new Rectangle(50, 600, 150, 50);

    // ================= PRZYCISKI PODSUMOWANIA =================
    private final Rectangle summaryNextBtn    = new Rectangle(0, 0, 320, 60);
    private final Rectangle summaryRestartBtn = new Rectangle(0, 0, 320, 60);
    private final Rectangle summaryMenuBtn    = new Rectangle(0, 0, 320, 60);

    // ================= KOLORY DLA JASNEGO TŁA =================
    private final Color TEXT_DARK = new Color(20, 20, 20);
    private final Color TEXT_HEADER = new Color(160, 60, 0);
    private final Color TEXT_SUBHEADER = new Color(0, 80, 120);
    private final Color TEXT_HIGHLIGHT = new Color(100, 50, 0);
    private final Color TEXT_GREEN_DARK = new Color(0, 100, 0);

    public Menu(Game game) {
        this.game = game;
        try {
            background = ImageIO.read(new File("resources/tło.jpg"));
        } catch (Exception e) {
            System.out.println("Nie znaleziono tła menu: resources/tło.jpg");
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
                instructionPage = 0;
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
                instructionPage = 0;
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
            else if (nextPageBtn.contains(mx, my) && instructionPage < MAX_PAGES) {
                instructionPage++;
            }
            else if (prevPageBtn.contains(mx, my) && instructionPage > 0) {
                instructionPage--;
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

        // Tło
        if (Game.gameState == Game.State.MENU ||
                (Game.gameState == Game.State.INSTRUCTIONS && previousState == Game.State.MENU)) {
            if (background != null) g.drawImage(background, 0, 0, w, h, null);
            else {
                g.setColor(new Color(220, 220, 220));
                g.fillRect(0, 0, w, h);
            }
        }
        else if (Game.gameState == Game.State.OPTIONS ||
                Game.gameState == Game.State.LEVEL_SUMMARY ||
                (Game.gameState == Game.State.INSTRUCTIONS && previousState == Game.State.OPTIONS)) {
            g.setColor(new Color(255, 255, 255, 220));
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
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "Kawiarnia Telekomunikacyjna," +
                " Bartłomiej Stachniuk s198205 Tele 2A";

        g.setColor(Color.WHITE);
        g.drawString(title, 102, 112);

        g.setColor(TEXT_DARK);
        g.drawString(title, 100, 110);

        drawButton(g, continueBtn, "Kontynuuj", SaveManager.exists());
        drawButton(g, newGameBtn, "Nowa gra", true);
        drawButton(g, instrBtn, "Instrukcja", true);
        drawButton(g, exitBtn, "Wyjście", true);
    }

    private void renderOptions(Graphics g, int w) {
        g.setColor(TEXT_DARK);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Pauza", w / 2 - 60, 150);
        drawButton(g, resumeBtn, "Wróć do gry", true);
        drawButton(g, instrOptionBtn, "Instrukcja", true);
        drawButton(g, restartBtn, "Restart poziomu", true);
        drawButton(g, menuBtn, "Menu główne", true);
    }

    private void renderInstructions(Graphics g, int w, int h) {
        drawButton(g, backBtn, "Wróć", true);

        g.setColor(TEXT_HEADER);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "SAMOUCZEK (" + (instructionPage + 1) + "/" + (MAX_PAGES + 1) + ")";
        g.drawString(title, 220, 85);

        int startX = 100;
        int startY = 140;

        switch (instructionPage) {
            case 0 -> renderPageBasics(g, startX, startY);
            case 1 -> renderPageRecipes(g, startX, startY);
            case 2 -> renderPageEco(g, startX, startY);
        }

        if (instructionPage > 0) drawButton(g, prevPageBtn, "<< Poprzednia", true);
        if (instructionPage < MAX_PAGES) drawButton(g, nextPageBtn, "Następna >>", true);
    }

    private void renderPageBasics(Graphics g, int x, int y) {
        int gap = 35;
        g.setFont(new Font("Arial", Font.BOLD, 26)); // Pogrubione
        g.setColor(TEXT_SUBHEADER);
        g.drawString("1. OBSŁUGA KLIENTA I SPRZĘTU", x, y);
        y += gap + 10;

        g.setFont(new Font("Arial", Font.BOLD, 20)); // Pogrubione
        g.setColor(TEXT_DARK); // Czarny
        g.drawString("- Kliknij na klienta w sali, aby przejść do kuchni.", x, y); y += gap;

        g.setColor(TEXT_HIGHLIGHT);
        g.drawString("MŁYNEK (Po lewej):", x, y); y += gap;
        g.setColor(TEXT_DARK);
        g.drawString("  1. Przeciągnij pustą kolbę do młynka.", x, y); y += gap;
        g.drawString("  2. Przytrzymaj LPM na młynku. Puść, gdy pasek jest ZIELONY.", x, y); y += gap;
        g.drawString("  3. Przeciągnij kolbę z kawą na podstawkę.", x, y); y += gap;

        g.setColor(TEXT_HIGHLIGHT);
        g.drawString("UBIJAK & EKSPRES:", x, y); y += gap;
        g.setColor(TEXT_DARK);
        g.drawString("  1. Przeciągnij ubijak na kolbę (poczekaj aż ubije).", x, y); y += gap;
        g.drawString("  2. Włóż ubitą kolbę i filiżankę do ekspresu.", x, y); y += gap;
        g.drawString("  3. Kliknij przycisk na maszynie (Single, Double lub Woda).", x, y); y += gap;

        g.setColor(TEXT_HIGHLIGHT);
        g.drawString("MLEKO (Po prawej):", x, y); y += gap;
        g.setColor(TEXT_DARK);
        g.drawString("  1. Nalej mleko do dzbanka (przeciągnij karton).", x, y); y += gap;
        g.drawString("  2. Wstaw dzbanek do spieniacza i przytrzymaj LPM (Celuj w ZIELONE).", x, y); y += gap;
        g.drawString("  3. Przeciągnij spienione mleko na filiżankę z kawą.", x, y); y += gap;
    }

    private void renderPageRecipes(Graphics g, int x, int y) {
        int gap = 40;
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.setColor(TEXT_GREEN_DARK); // Ciemna zieleń
        g.drawString("2. KSIĘGA PRZEPISÓW", x, y);
        y += gap + 10;

        g.setFont(new Font("Arial", Font.BOLD, 22));

        // ESPRESSO
        g.setColor(new Color(139, 69, 19));
        g.drawString("ESPRESSO:", x, y);
        g.setColor(TEXT_DARK);
        g.drawString("1x Przycisk SINGLE (1 shot)", x + 250, y);
        y += gap;

        // DOUBLE
        g.setColor(new Color(100, 40, 0));
        g.drawString("DOUBLE ESPRESSO:", x, y);
        g.setColor(TEXT_DARK);
        g.drawString("1x Przycisk DOUBLE (2 shoty)", x + 250, y);
        y += gap;

        // AMERICANO
        g.setColor(new Color(0, 0, 139));
        g.drawString("AMERICANO:", x, y);
        g.setColor(TEXT_DARK);
        g.drawString("1x Przycisk WODA + 1x Przycisk SINGLE", x + 250, y);
        y += gap;

        // LATTE
        g.setColor(new Color(199, 21, 133));
        g.drawString("LATTE:", x, y);
        g.setColor(TEXT_DARK);
        g.drawString("1x SINGLE + Mleko (Zielony pasek)", x + 250, y);
        y += gap;

        // CAPPUCCINO
        g.setColor(new Color(0, 150, 200));
        g.drawString("CAPPUCCINO:", x, y);
        g.setColor(TEXT_DARK);
        g.drawString("1x SINGLE + Pianka (Niebieski pasek - trzymaj dłużej!)", x + 250, y);
        y += gap;

        g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
        g.setColor(Color.RED);
        g.drawString("UWAGA: Klienci mogą zamawiać mleko krowie, sojowe lub bez laktozy!", x, y);
        y += 30;
        g.drawString("Użycie złego mleka = BRAK ZAPŁATY!", x, y);
    }

    private void renderPageEco(Graphics g, int x, int y) {
        int gap = 30;
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.setColor(new Color(75, 0, 130));
        g.drawString("3. EKONOMIA I SEGREGACJA ŚMIECI", x, y);
        y += gap + 10;

        // CENNIK
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(TEXT_DARK);
        g.drawString("Koszty towarów:", x, y); y += gap;
        g.setColor(TEXT_GREEN_DARK);
        g.drawString("- Nowy worek kawy: 15 PLN", x + 20, y); y += gap;
        g.drawString("- Karton mleka: 5 PLN", x + 20, y); y += gap;
        g.setColor(new Color(178, 34, 34));
        g.drawString("- Czynsz dzienny: 100 PLN", x + 20, y); y += gap + 20;

        // SEGREGACJA
        g.setColor(TEXT_DARK);
        g.drawString("GDZIE WYRZUCAĆ?", x, y); y += gap + 10;

        // Kosz BIO
        g.setColor(new Color(139, 69, 19)); // Brąz
        g.fillRect(x, y, 30, 30);
        g.setColor(Color.BLACK); // Obwódka
        g.drawRect(x, y, 30, 30);
        g.setColor(TEXT_DARK);
        g.drawString("Kosz BRĄZOWY (Bio): Tylko fusy z kolby!", x + 40, y + 22);
        y += gap + 10;

        // Kosz PLASTIK
        g.setColor(new Color(220, 200, 0)); // Ciemniejszy Żółty
        g.fillRect(x, y, 30, 30);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 30, 30);
        g.setColor(TEXT_DARK);
        g.drawString("Kosz ŻÓŁTY (Plastik): Puste kartony po mleku.", x + 40, y + 22);
        y += gap + 10;

        // Kosz ZMIESZANE/ZLEW
        g.setColor(Color.GRAY);
        g.fillRect(x, y, 30, 30);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 30, 30);
        g.setColor(TEXT_DARK);
        g.drawString("Kosz SZARY (Zlew): Wylewanie zepsutej kawy / mleka.", x + 40, y + 22);
        y += gap + 20;

        g.setColor(new Color(178, 34, 34));
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("KARA ZA ZŁĄ SEGREGACJĘ: -2.00 PLN!", x, y);
    }

    private void renderLevelSummary(Graphics g, int w, int h) {
        Graphics2D g2d = (Graphics2D) g;
        int boxW = 600;
        int boxH = 500;
        int boxX = (w - boxW) / 2;
        int boxY = (h - boxH) / 2;

        g.setColor(new Color(245, 240, 230)); // Jasny beż (papier)
        g.fillRect(boxX, boxY, boxW, boxH);

        g.setColor(new Color(60, 40, 20));
        g2d.setStroke(new BasicStroke(3));
        g.drawRect(boxX, boxY, boxW, boxH);

        g.setColor(new Color(90, 60, 40));
        g2d.setStroke(new BasicStroke(1));
        g.drawRect(boxX + 5, boxY + 5, boxW - 10, boxH - 10);

        g.setColor(TEXT_DARK);
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

        g.setFont(new Font("Arial", Font.BOLD, 20));

        g.setColor(TEXT_GREEN_DARK);
        g.drawString("Sprzedaż + Napiwki:", startX, y);
        String incStr = String.format("+ %.2f PLN", gp.getLevelIncome());
        g.drawString(incStr, valX - g.getFontMetrics().stringWidth(incStr), y);
        y += gap;

        g.setColor(new Color(178, 34, 34));
        g.drawString("Towar, Czynsz i Kary:", startX, y);
        String expStr = String.format("- %.2f PLN", gp.getLevelExpenses());
        g.drawString(expStr, valX - g.getFontMetrics().stringWidth(expStr), y);
        y += gap;

        g.setColor(Color.DARK_GRAY);
        g.drawLine(startX, y - 10, valX, y - 10);
        y += 10;

        double profit = gp.getLevelIncome() - gp.getLevelExpenses();
        g.setFont(new Font("Arial", Font.BOLD, 22));

        if (profit >= 0) {
            g.setColor(TEXT_GREEN_DARK);
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

        g.setColor(TEXT_DARK);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("Stan Konta:", startX, y);
        String balStr = String.format("%.2f PLN", gp.getMoney());
        g.setColor(gp.getMoney() >= 0 ? TEXT_GREEN_DARK : Color.RED);
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

        g.setColor(enabled ? new Color(80, 50, 30) : new Color(100, 100, 100));
        g.fillRect(r.x, r.y, r.width, r.height);

        g.setColor(new Color(40, 20, 10));
        g.drawRect(r.x, r.y, r.width, r.height);

        g.setColor(enabled ? Color.WHITE : Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + (r.height + fm.getAscent()) / 2 - 5;
        g.drawString(text, tx, ty);
    }
}
