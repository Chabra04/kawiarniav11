import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class KitchenView {

    private final KitchenProcess process;

    // Podwidoki
    private final KitchenItemsView itemsView;
    private final KitchenServeView serveView;

    private BufferedImage bgImage;

    // OBSZARY URZĄDZEŃ (LAYOUT)
    private final Rectangle grinderRect   = new Rectangle(60, 180, 150, 240);
    private final Rectangle espressoRect  = new Rectangle(300, 150, 280, 280);
    private final Rectangle frotherRect   = new Rectangle(660, 180, 150, 240);
    private final Rectangle trashArea     = new Rectangle(900, 700, 330, 150);
    // Przyciski ekspresu
    private Rectangle btnSingle;
    private Rectangle btnDouble;
    private Rectangle btnWater;

    // KONSTRUKTOR
    public KitchenView(KitchenProcess process, Gameplay gameplay) {
        this.process = process;
        this.itemsView = new KitchenItemsView(process, gameplay);
        this.serveView = new KitchenServeView(process);

        itemsView.setServeView(serveView);
        itemsView.configure(grinderRect, espressoRect, frotherRect, trashArea);

        int bx = espressoRect.x + 20;
        int by = espressoRect.y + 20;
        int btnW = 70;
        int btnH = 50;
        btnSingle = new Rectangle(bx, by, btnW, btnH);
        btnDouble = new Rectangle(bx + 90, by, btnW, btnH);
        btnWater  = new Rectangle(bx + 180, by, btnW, btnH);
        loadImages();
    }

    private void loadImages() {
        try {
        } catch (Exception ignored) {}
    }

    // METODY STERUJĄCE
    public void prepareForNewOrder() {
        itemsView.resetCupPosition();
    }

    public void update() {
        itemsView.update();
        process.update();
    }

    // OBSŁUGA WEJŚCIA (INPUT)
    public void mouseMoved(int x, int y) {
        itemsView.mouseMoved(x, y);
    }

    public boolean mousePressed(int x, int y) {

        // 1. Najpierw sprawdzamy przyciski na maszynach
        if (btnSingle.contains(x, y)) {
            process.brew(1);
            return true;
        }
        if (btnDouble.contains(x, y)) {
            process.brew(2);
            return true;
        }
        if (btnWater.contains(x, y)) {
            process.brew(0);
            return true;
        }

        if (itemsView.mousePressed(x, y)) {
            return true;
        }

        return false;
    }

    public void mouseReleased(int x, int y) {
        itemsView.mouseReleased(x, y);
    }

    // RENDER (GŁÓWNY)
    public void render(Graphics g, int w, int h) {
        // 1. Tło
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g.setColor(new Color(210, 200, 180));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(100, 60, 30));
            g.fillRect(0, 420, w, h - 420);
        }

        // 2. Maszyny
        drawGrinder(g);
        drawEspressoMachine(g);
        drawFrother(g);

        itemsView.render(g);
        serveView.render(g, w);
    }

    // RYSOWANIE POSZCZEGÓLNYCH URZĄDZEŃ
    private void drawGrinder(Graphics g) {
        g.setColor(new Color(50, 50, 50));
        g.fillRect(grinderRect.x, grinderRect.y, grinderRect.width, grinderRect.height);
        g.setColor(Color.BLACK);
        g.drawRect(grinderRect.x, grinderRect.y, grinderRect.width, grinderRect.height);
        g.setColor(Color.WHITE);
        g.drawString("Młynek", grinderRect.x + 40, grinderRect.y + 220);

        // --- PASEK POSTĘPU MIELENIA ---
        if (process.getCurrentGrindTime() > 0) {
            int barX = grinderRect.x + 10;
            int barY = grinderRect.y + 60;
            int barW = 130;
            int barH = 15;

            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);

            float progress = process.getGrindProgress();
            int currentW = (int) (progress * barW);
            if (currentW > barW) currentW = barW;

            long time = process.getCurrentGrindTime();
            if (time < 2500) g.setColor(Color.YELLOW);
            else if (time <= 3500) g.setColor(Color.GREEN);
            else g.setColor(Color.RED);

            g.fillRect(barX, barY, currentW, barH);
            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, barW, barH);

            // Marker ideału
            g.setColor(Color.WHITE);
            g.drawLine(barX + 90, barY - 2, barX + 90, barY + barH + 2);
            int beans = process.getCoffeeBeansLevel();

            g.setColor(Color.BLACK);
            g.fillRect(grinderRect.x + 10, grinderRect.y + 10, 130, 25);
            g.setColor(Color.WHITE);
            g.drawRect(grinderRect.x + 10, grinderRect.y + 10, 130, 25);

            if (beans < 10) g.setColor(Color.RED);
            else g.setColor(Color.GREEN);

            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Ziarna: " + beans + "/60", grinderRect.x + 15, grinderRect.y + 28);
        }
        int beans = process.getCoffeeBeansLevel();

        g.setColor(Color.BLACK);
        g.fillRect(grinderRect.x + 10, grinderRect.y + 10, 130, 25);
        g.setColor(Color.WHITE);
        g.drawRect(grinderRect.x + 10, grinderRect.y + 10, 130, 25);

        if (beans < 10) g.setColor(Color.RED);
        else g.setColor(Color.GREEN);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Ziarna: " + beans + "/60g", grinderRect.x + 15, grinderRect.y + 28);
    }

    private void drawFrother(Graphics g) {
        g.setColor(new Color(150, 150, 180));
        g.fillRect(frotherRect.x, frotherRect.y, frotherRect.width, frotherRect.height);
        g.setColor(Color.BLACK);
        g.drawRect(frotherRect.x, frotherRect.y, frotherRect.width, frotherRect.height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Spieniacz", frotherRect.x + 40, frotherRect.y + 220);

        // --- PASEK POSTĘPU SPIENIANIA ---
        if (process.isJugInFrother()) {
            int barX = frotherRect.x + 10;
            int barY = frotherRect.y + 60;
            int barW = 130;
            int barH = 15;

            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);

            float progress = process.getFrothProgress();
            int currentW = (int) (progress * barW);
            if (currentW > barW) currentW = barW;

            // Kolor zależny od stanu
            KitchenProcess.MilkState state = process.getMilkState();
            if (state == KitchenProcess.MilkState.BURNT) g.setColor(Color.RED);
            else if (state == KitchenProcess.MilkState.PERFECT) g.setColor(Color.GREEN);
            else g.setColor(Color.YELLOW); // Cold/Warm

            g.fillRect(barX, barY, currentW, barH);

            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, barW, barH);

            // Znacznik idealnego momentu
            g.setColor(Color.WHITE);
            g.drawLine(barX + 90, barY - 2, barX + 90, barY + barH + 2);
        }
    }

    private void drawEspressoMachine(Graphics g) {
        Rectangle r = espressoRect;

        g.setColor(new Color(160, 40, 40));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, r.width, r.height);

        drawButton(g, btnSingle, "Single", Color.DARK_GRAY);
        drawButton(g, btnDouble, "Double", Color.DARK_GRAY);
        drawButton(g, btnWater,  "Woda",   new Color(50, 80, 150));

        g.setColor(new Color(200, 200, 200));
        g.fillRect(r.x + 90, r.y + 160, 100, 30);
        g.setColor(Color.BLACK);
        g.drawRect(r.x + 90, r.y + 160, 100, 30);

        // --- PASEK POSTĘPU PARZENIA ---
        if (process.getStep() == KitchenProcess.Step.BREWING) {
            int barX = r.x + 90;
            int barY = r.y + 130;
            int barW = 100;
            int barH = 10;

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Parzenie...", r.x + 80, r.y + 110);

            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);

            // Postęp
            g.setColor(new Color(200, 120, 0));
            int currentW = (int) (process.getBrewProgress() * barW);
            g.fillRect(barX, barY, currentW, barH);

            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barW, barH);
        }
    }

    // Metoda pomocnicza do rysowania guzików
    private void drawButton(Graphics g, Rectangle r, String txt, Color c) {
        g.setColor(c);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(txt)) / 2;
        int ty = r.y + (r.height + fm.getAscent()) / 2 - 3;
        g.drawString(txt, tx, ty);
    }
    public void showFeedback(String text, Color color) {
        serveView.triggerFeedback(text, color);
    }

    public boolean isFeedbackActive() {
        return serveView.isFeedbackActive();
    }
}
