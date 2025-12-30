import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class KitchenView {

    private final KitchenProcess process;

    // Podwidoki (Logika przedmiotów i wydawania)
    private final KitchenItemsView itemsView;
    private final KitchenServeView serveView;

    // Grafiki (opcjonalnie)
    private BufferedImage bgImage;

    // =====================================================
    // OBSZARY URZĄDZEŃ (LAYOUT)
    // =====================================================
    private final Rectangle grinderRect   = new Rectangle(50, 180, 120, 200);
    private final Rectangle espressoRect  = new Rectangle(250, 150, 220, 240);
    private final Rectangle frotherRect   = new Rectangle(600, 180, 120, 200);
    private final Rectangle trashArea     = new Rectangle(800, 300, 240, 150);

    // Przyciski ekspresu (Definiujemy je tutaj, aby mieć dostęp w render i mousePressed)
    private Rectangle btnSingle;
    private Rectangle btnDouble;
    private Rectangle btnWater;

    // =====================================================
    // KONSTRUKTOR
    // =====================================================
    public KitchenView(KitchenProcess process, Gameplay gameplay) {
        this.process = process;

        // 1. Tworzymy widok przedmiotów (przekazujemy gameplay do finansów)
        this.itemsView = new KitchenItemsView(process, gameplay);

        // 2. Tworzymy widok wydawania
        this.serveView = new KitchenServeView(process);

        // 3. Łączymy widoki i konfigurujemy strefy
        itemsView.setServeView(serveView);
        itemsView.configure(grinderRect, espressoRect, frotherRect, trashArea);

        // 4. Inicjalizacja przycisków ekspresu (względem pozycji maszyny)
        int bx = espressoRect.x + 10;
        int by = espressoRect.y + 10;
        btnSingle = new Rectangle(bx, by, 60, 40);
        btnDouble = new Rectangle(bx + 70, by, 60, 40);
        btnWater  = new Rectangle(bx + 140, by, 60, 40);

        loadImages();
    }

    private void loadImages() {
        try {
            // Tu można wczytać tło: bgImage = ImageIO.read(new File("resources/kitchen_bg.png"));
        } catch (Exception ignored) {}
    }

    // =====================================================
    // METODY STERUJĄCE
    // =====================================================

    // Reset pozycji kubka przy nowym zamówieniu
    public void prepareForNewOrder() {
        itemsView.resetCupPosition();
    }

    public void update() {
        itemsView.update();     // Animacje przedmiotów (np. ubijanie)
        process.update();       // WAŻNE: Aktualizacja licznika czasu parzenia!
    }

    // =====================================================
    // OBSŁUGA WEJŚCIA (INPUT)
    // =====================================================

    public void mouseMoved(int x, int y) {
        itemsView.mouseMoved(x, y);
    }

    public boolean mousePressed(int x, int y) {
        // KOLEJNOŚĆ JEST KLUCZOWA DLA NAPRAWY BŁĘDU:

        // 1. Najpierw sprawdzamy przyciski na maszynach.
        // Jeśli klikniemy przycisk, NIE chcemy podnosić kubka/kolby.
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

        // 2. Dopiero jeśli nie kliknięto przycisku, sprawdzamy przedmioty.
        // To pozwala wyjąć kubek klikając na niego, ale nie gdy klikamy w guzik nad nim.
        if (itemsView.mousePressed(x, y)) {
            return true;
        }

        return false;
    }

    public void mouseReleased(int x, int y) {
        itemsView.mouseReleased(x, y);
    }

    // =====================================================
    // RENDER (GŁÓWNY)
    // =====================================================
    public void render(Graphics g, int w, int h) {
        // 1. Tło
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g.setColor(new Color(210, 200, 180)); // Ściana
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(100, 60, 30));   // Blat
            g.fillRect(0, 400, w, h - 400);
        }

        // 2. Maszyny (Rysujemy je POD przedmiotami)
        drawGrinder(g);
        drawEspressoMachine(g);
        drawFrother(g);

        // 3. Przedmioty (ItemsView rysuje kolby, kubki, kartony)
        itemsView.render(g);

        // 4. Okno Wydawania
        serveView.render(g, w);
    }

    // =====================================================
    // RYSOWANIE POSZCZEGÓLNYCH URZĄDZEŃ
    // =====================================================

    private void drawGrinder(Graphics g) {
        // Obudowa
        g.setColor(new Color(50, 50, 50));
        g.fillRect(grinderRect.x, grinderRect.y, grinderRect.width, grinderRect.height);
        g.setColor(Color.BLACK);
        g.drawRect(grinderRect.x, grinderRect.y, grinderRect.width, grinderRect.height);
        g.setColor(Color.WHITE);
        g.drawString("Młynek", grinderRect.x + 10, grinderRect.y + 190);

        // --- PASEK POSTĘPU MIELENIA ---
        if (process.getCurrentGrindTime() > 0) {
            int barX = grinderRect.x + 10;
            int barY = grinderRect.y + 50;
            int barW = 100;
            int barH = 15;

            // Tło paska
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);

            float progress = process.getGrindProgress();
            int currentW = (int) (progress * barW);
            if (currentW > barW) currentW = barW;

            // Kolor zależny od czasu (Under=Żółty, Perfect=Zielony, Over=Czerwony)
            // Używamy tych samych progów co w Process (3000ms +/- 500)
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
        }
    }

    private void drawFrother(Graphics g) {
        // Obudowa
        g.setColor(new Color(150, 150, 180));
        g.fillRect(frotherRect.x, frotherRect.y, frotherRect.width, frotherRect.height);
        g.setColor(Color.BLACK);
        g.drawRect(frotherRect.x, frotherRect.y, frotherRect.width, frotherRect.height);

        // Etykieta
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Spieniacz", frotherRect.x + 30, frotherRect.y + 190);

        // --- PASEK POSTĘPU SPIENIANIA ---
        if (process.isJugInFrother()) {
            int barX = frotherRect.x + 10;
            int barY = frotherRect.y + 50;
            int barW = 100;
            int barH = 15;

            // Tło paska
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);

            // Postęp
            float progress = process.getFrothProgress();
            int currentW = (int) (progress * barW);
            if (currentW > barW) currentW = barW;

            // Kolor zależny od stanu (Wizualna pomoc dla gracza)
            KitchenProcess.MilkState state = process.getMilkState();
            if (state == KitchenProcess.MilkState.BURNT) g.setColor(Color.RED);
            else if (state == KitchenProcess.MilkState.PERFECT) g.setColor(Color.GREEN);
            else g.setColor(Color.YELLOW); // Cold/Warm

            g.fillRect(barX, barY, currentW, barH);

            // Ramka
            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, barW, barH);

            // Znacznik idealnego momentu
            g.setColor(Color.WHITE);
            g.drawLine(barX + 90, barY - 2, barX + 90, barY + barH + 2);
        }
    }

    private void drawEspressoMachine(Graphics g) {
        Rectangle r = espressoRect;

        // Obudowa
        g.setColor(new Color(160, 40, 40));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, r.width, r.height);

        // Przyciski (Rysujemy helperem)
        drawButton(g, btnSingle, "Single", Color.DARK_GRAY);
        drawButton(g, btnDouble, "Double", Color.DARK_GRAY);
        drawButton(g, btnWater,  "Woda",   new Color(50, 80, 150));

        // Grupa (miejsce wpięcia kolby)
        g.setColor(new Color(200, 200, 200));
        g.fillRect(r.x + 60, r.y + 140, 100, 30);
        g.setColor(Color.BLACK);
        g.drawRect(r.x + 60, r.y + 140, 100, 30);

        // --- PASEK POSTĘPU PARZENIA ---
        if (process.getStep() == KitchenProcess.Step.BREWING) {
            int barX = r.x + 60;
            int barY = r.y + 115;
            int barW = 100;
            int barH = 10;

            // Tekst
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Parzenie...", r.x + 80, r.y + 110);

            // Tło paska
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);

            // Postęp
            g.setColor(new Color(200, 120, 0)); // Kawowy brąz/pomarańcz
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
        g.setFont(new Font("Arial", Font.PLAIN, 10));

        // Wyśrodkowanie tekstu
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(txt)) / 2;
        int ty = r.y + (r.height + fm.getAscent()) / 2 - 3;
        g.drawString(txt, tx, ty);
    }
}