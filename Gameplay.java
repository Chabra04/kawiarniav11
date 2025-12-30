import java.awt.*;

public class Gameplay {

    private final Game game;
    private Level level;
    private final Kitchen kitchen;

    // Zapamiętujemy punkty na starcie poziomu (do restartu)
    private int startScore;

    // Zapamiętujemy pieniądze na starcie poziomu (do restartu)
    private double startMoney;

    // ===================== EKONOMIA =====================
    private double money = 200.0; // Startowa gotówka (PLN)

    // Statystyki bieżącego poziomu (do podsumowania w Menu)
    private double levelIncome = 0;
    private double levelExpenses = 0;

    // Koszty stałe
    private static final double RENT_COST = 100.0; // Czynsz płatny po każdym poziomie

    public Gameplay(Game game, boolean continued) {
        this.game = game;
        this.kitchen = new Kitchen(this);

        // Jeśli gra jest kontynuowana, dane (level, score, money) powinny być wczytane w Game.java
        // Zakładamy, że Game.java przekazało stan do pól publicznych lub wczytujemy tu.
        // Dla uproszczenia przy kontynuacji używamy tego co jest w game.score i game.money (jeśli dodasz to pole do Game).
        // Tutaj bazujemy na lokalnym polu 'money', które przy wczytywaniu powinno być zaktualizowane.

        this.startScore = game.score;
        this.startMoney = this.money;

        this.level = new Level(game.currentLevel, this);

        // Zawsze startujemy level, żeby wygenerować klientów
        level.start();
    }

    // =====================================================
    // ZARZĄDZANIE FINANSAMI
    // =====================================================

    public void earnMoney(double amount) {
        this.money += amount;
        this.levelIncome += amount;
    }

    public void spendMoney(double amount, String reason) {
        this.money -= amount;
        this.levelExpenses += amount;
        System.out.println("Wydatek: -" + amount + " PLN (" + reason + ")");

        // Jeśli spadniemy poniżej zera, gra się nie kończy, ale mamy dług.
    }

    public double getMoney() { return money; }
    public double getLevelIncome() { return levelIncome; }
    public double getLevelExpenses() { return levelExpenses; }

    // =====================================================
    // UPDATE
    // =====================================================
    public void update() {
        if (Game.gameState == Game.State.GAME) {
            level.update();
        } else if (Game.gameState == Game.State.KITCHEN) {
            kitchen.update();
        }
    }

    // =====================================================
    // RENDER
    // =====================================================
    public void render(Graphics g) {
        // Jeśli jesteśmy w kuchni, rysujemy kuchnię
        if (Game.gameState == Game.State.KITCHEN) {
            kitchen.render(g, Game.WIDTH, Game.HEIGHT);
            return;
        }

        // Rysowanie Sali (Level)
        g.setColor(new Color(230, 220, 200));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
        level.render(g);

        // ===== HUD (Interfejs w Sali) =====

        // 1. Pieniądze
        g.setColor(new Color(30, 100, 30));
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(String.format("Kasa: %.2f PLN", money), 40, 40);

        // 2. Koszt czynszu (info dla gracza)
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("(Czynsz po poziomie: -" + RENT_COST + " PLN)", 40, 65);
    }

    // =====================================================
    // INPUT
    // =====================================================
    public void mouseClicked(int mx, int my) {
        if (Game.gameState == Game.State.KITCHEN) {
            kitchen.mouseClicked(mx, my);
        } else {
            level.click(mx, my, this);
        }
    }

    public void mouseReleased(int mx, int my) {
        if (Game.gameState == Game.State.KITCHEN) {
            kitchen.mouseReleased(mx, my);
        }
    }

    public void kitchenMouseMoved(int mx, int my) {
        if (Game.gameState == Game.State.KITCHEN) {
            kitchen.mouseMoved(mx, my);
        }
    }

    // =====================================================
    // LOGIKA GRY / PRZEJŚCIA
    // =====================================================

    public void goToKitchen(Client client) {
        kitchen.startOrder(client.getOrderName(), client); // Używamy getOrderName lub getOrder w zależności od wersji Client.java
        Game.gameState = Game.State.KITCHEN;
    }

    // Klient obsłużony - aktualizacja punktów i pieniędzy
    public void clientServed(Client client, int points, double earnings) {
        level.clientServed(client);
        addScore(points);
        earnMoney(earnings);

        // Zapis stanu (dodaj money do SaveManager!)
        // SaveManager.save(game.currentLevel, game.score, money);
        SaveManager.save(game.currentLevel, game.score); // Wersja tymczasowa bez money w zapisie
    }

    // Koniec poziomu (wszyscy obsłużeni)
    public void levelCompleted() {
        // Naliczamy czynsz
        spendMoney(RENT_COST, "Czynsz za lokal");

        Game.gameState = Game.State.LEVEL_SUMMARY;

        // Zapisujemy odblokowany następny poziom
        // SaveManager.save(game.currentLevel + 1, game.score, money);
        SaveManager.save(game.currentLevel + 1, game.score);
    }

    // Przejście do następnego poziomu
    public void nextLevel() {
        game.currentLevel++;

        // Resetujemy statystyki dzienne, ale pieniądze zostają
        resetLevelStats();

        // Ustawiamy nowe punkty startowe dla ewentualnego restartu
        this.startScore = game.score;
        this.startMoney = this.money;

        level = new Level(game.currentLevel, this);
        level.start();

        Game.gameState = Game.State.GAME;
        SaveManager.save(game.currentLevel, game.score);
    }

    // Restart poziomu (nie chcemy farmić kasy, więc cofamy stan finansów)
    public void restartLevel() {
        // Przywracamy punkty
        game.score = startScore;

        // Przywracamy pieniądze (cofamy to co zarobiliśmy i wydaliśmy w tej nieudanej próbie)
        // Opcja 1: Proste przywrócenie stanu z początku
        this.money = this.startMoney;

        // Resetujemy liczniki statystyk
        resetLevelStats();

        level = new Level(game.currentLevel, this);
        level.start();

        Game.gameState = Game.State.GAME;
        SaveManager.save(game.currentLevel, game.score);
    }

    private void resetLevelStats() {
        this.levelIncome = 0;
        this.levelExpenses = 0;
    }

    // =====================================================
    // PUNKTY I GETTERY
    // =====================================================
    public void addScore(int points) {
        game.score += points;
    }

    public int getScore() {
        return game.score;
    }

    public int getLevelScore() {
        return game.score - startScore;
    }
}
