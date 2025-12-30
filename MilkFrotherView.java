import java.awt.*;

public class MilkFrotherView {

    private final KitchenProcess process;
    private final Rectangle frotherArea;

    public MilkFrotherView(KitchenProcess process, Rectangle frotherArea) {
        this.process = process;
        this.frotherArea = frotherArea;
    }

    // =====================================================
    // UPDATE
    // =====================================================
    public void update() {
        // Logika update jest w KitchenProcess (czas) i KitchenItemsView (myszka).
        // Tutaj zajmujemy się tylko renderowaniem.
    }

    // =====================================================
    // RENDER
    // =====================================================
    public void render(Graphics g) {

        // 1. RYSOWANIE OBUDOWY
        g.setColor(new Color(150, 150, 180)); // Stalowy niebieski
        g.fillRect(frotherArea.x, frotherArea.y, frotherArea.width, frotherArea.height);

        g.setColor(Color.BLACK);
        g.drawRect(frotherArea.x, frotherArea.y, frotherArea.width, frotherArea.height);

        // Etykieta na dole urządzenia
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Spieniacz", frotherArea.x + 25, frotherArea.y + 180);

        // 2. RYSOWANIE PASKA POSTĘPU
        // Rysujemy go ZAWSZE, gdy dzbanek jest w środku, aby gracz widział efekt swojej pracy
        if (process.isJugInFrother()) {
            drawProgressBar(g);
        }
    }

    private void drawProgressBar(Graphics g) {
        // Wymiary paska
        int barW = 100;
        int barH = 15;
        int x = frotherArea.x + (frotherArea.width - barW) / 2; // Wyśrodkowanie
        int y = frotherArea.y + 50;

        // Tło paska (ciemne)
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barW, barH);

        // Pobranie danych
        float progress = process.getFrothProgress(); // 0.0 do >1.0
        KitchenProcess.MilkState state = process.getMilkState();

        // Obliczenie szerokości wypełnienia
        int filledWidth = (int) (progress * barW);
        if (filledWidth > barW) filledWidth = barW; // Nie wychodzimy poza ramkę

        // Dobór koloru i tekstu w zależności od stanu
        Color c = Color.WHITE;
        String statusText = "";

        switch (state) {
            case COLD -> {
                c = Color.CYAN;
                statusText = "Zimne";
            }
            case WARM -> {
                c = Color.YELLOW;
                statusText = "Letnie";
            }
            case PERFECT -> {
                c = Color.GREEN;
                statusText = "IDEALNE";
            }
            case BURNT -> {
                c = Color.RED;
                statusText = "SPALONE!";
            }
        }

        // Rysowanie wypełnienia
        g.setColor(c);
        g.fillRect(x, y, filledWidth, barH);

        // Ramka paska
        g.setColor(Color.BLACK);
        g.drawRect(x, y, barW, barH);

        // Znacznik "Celu" (Biała kreska oznaczająca moment idealny - ok. 90-100% paska)
        g.setColor(Color.WHITE);
        g.drawLine(x + 90, y - 3, x + 90, y + barH + 3);

        // Tekst statusu nad paskiem
        g.setFont(new Font("Arial", Font.BOLD, 11));

        // Jeśli proces jest aktywny (trzymamy przycisk), pokazujemy stan dynamicznie
        // Jeśli nie jest aktywny, ale dzbanek jest w środku, pokazujemy wynik końcowy
        if (process.isFrothingActive()) {
            g.drawString("Spienianie...", x, y - 5);
        } else {
            g.setColor(c == Color.CYAN ? Color.WHITE : c); // Dla czytelności
            g.drawString(statusText, x, y - 5);
        }
    }

    // =====================================================
    // GETTER
    // =====================================================
    public Rectangle getArea() {
        return frotherArea;
    }
}