import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Gameplay {

    private final Game game;
    private Level level;
    private final Kitchen kitchen;

    private int startScore;

    private double startMoney;

    // ===================== EKONOMIA =====================
    private double money = 300.0;

    private double levelIncome = 0;
    private double levelExpenses = 0;

    private BufferedImage bgImage;
    private static final double RENT_COST = 100.0;
    private int clientsServedCount = 0;

    public Gameplay(Game game, boolean continued) {
        this.game = game;
        this.kitchen = new Kitchen(this);

        this.startScore = game.score;
        this.startMoney = game.money;

        // Przepisujemy aktualną kasę z obiektu Game
        this.money = game.money;

        try {
            bgImage = ImageIO.read(new File("resources/tloGry.jpg"));
        } catch (IOException e) {
            System.out.println("Nie znaleziono tła gry: resources/tloGry.jpg");
            bgImage = null;
        }

        this.level = new Level(game.currentLevel, this);

        // LOGIKA WCZYTYWANIA STANU (Kontynuacja gry)
        if (continued) {
            // 1. Ustawiamy licznik obsłużonych
            this.clientsServedCount = game.clientsServedFromSave;
            level.setAlreadyServed(this.clientsServedCount);

            // 2. Wczytujemy stan surowców w kuchni
            kitchen.getProcess().loadState(
                    game.loadedBeans,
                    game.loadedMilkCow,
                    game.loadedMilkLactose,
                    game.loadedMilkSoy
            );

            System.out.println("Wczytano Gameplay. Klienci z zapisu: " + (game.loadedClients != null ? game.loadedClients.size() : 0));
        level.start(game.loadedClients);

        } else {
            this.clientsServedCount = 0;

            level.start(null);
        }
    }

    // ZARZĄDZANIE FINANSAMI
    public void earnMoney(double amount) {
        this.money += amount;
        this.levelIncome += amount;
    }

    public void spendMoney(double amount, String reason) {
        this.money -= amount;
        this.levelExpenses += amount;
        System.out.println("Wydatek: -" + amount + " PLN (" + reason + ")");
    }

    public double getMoney() { return money; }
    public double getLevelIncome() { return levelIncome; }
    public double getLevelExpenses() { return levelExpenses; }

    // UPDATE
    public void update() {
        // Level (klienci) aktualizuje się zawsze (żeby czas leciał)
        if (Game.gameState == Game.State.GAME || Game.gameState == Game.State.KITCHEN) {
            level.update();
        }

        // Kuchnia aktualizuje się tylko gdy w niej jesteśmy
        if (Game.gameState == Game.State.KITCHEN) {
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

        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, Game.WIDTH, Game.HEIGHT, null);
        } else {
            g.setColor(new Color(230, 220, 200));
            g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
        }

        level.render(g);

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
        kitchen.startOrder(client.getOrderName(), client);
        Game.gameState = Game.State.KITCHEN;
    }

    // Klient obsłużony - aktualizacja punktów, pieniędzy i ZAPIS
    public void clientServed(Client client, int points, double earnings) {
        level.clientServed(client);
        addScore(points);
        earnMoney(earnings);

        clientsServedCount++;

        // Pobieramy stan kuchni
        KitchenProcess kp = kitchen.getProcess();

        // ZAPISUJEMY STAN GRY WRAZ Z KLIENTAMI!
        // level.getActiveClients() zwraca listę obecnych klientów z ich paskami cierpliwości
        SaveManager.save(
                game.currentLevel,
                game.score,
                money,
                clientsServedCount,
                kp.getCoffeeBeansLevel(),
                kp.getMilkLevel(KitchenProcess.MilkType.COW),
                kp.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE),
                kp.getMilkLevel(KitchenProcess.MilkType.SOY),
                level.getActiveClients() // <-- Przekazujemy listę klientów
        );
    }

    // Koniec poziomu
    public void levelCompleted() {
        spendMoney(RENT_COST, "Czynsz za lokal");

        Game.gameState = Game.State.LEVEL_SUMMARY;

        KitchenProcess kp = kitchen.getProcess();

        // Zapisujemy start nowego poziomu.
        // Lista klientów jest null, bo poziom się skończył.
        SaveManager.save(
                game.currentLevel + 1,
                game.score,
                money,
                0,
                kp.getCoffeeBeansLevel(),
                kp.getMilkLevel(KitchenProcess.MilkType.COW),
                kp.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE),
                kp.getMilkLevel(KitchenProcess.MilkType.SOY),
                null
        );
    }

    // Przejście do następnego poziomu
    public void nextLevel() {
        game.currentLevel++;

        resetLevelStats();
        clientsServedCount = 0;

        this.startScore = game.score;
        this.startMoney = this.money;

        level = new Level(game.currentLevel, this);
        // Nowy poziom -> null (generuj nowych klientów)
        level.start(null);

        Game.gameState = Game.State.GAME;

        KitchenProcess kp = kitchen.getProcess();
        SaveManager.save(
                game.currentLevel, game.score, money, 0,
                kp.getCoffeeBeansLevel(),
                kp.getMilkLevel(KitchenProcess.MilkType.COW),
                kp.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE),
                kp.getMilkLevel(KitchenProcess.MilkType.SOY),
                null
        );
    }

    // Restart poziomu
    public void restartLevel() {
        game.score = startScore;
        this.money = this.startMoney;
        this.clientsServedCount = 0;

        resetLevelStats();

        level = new Level(game.currentLevel, this);
        // Restart -> null (generuj nowych klientów)
        level.start(null);

        Game.gameState = Game.State.GAME;

        KitchenProcess kp = kitchen.getProcess();
        SaveManager.save(
                game.currentLevel, game.score, money, 0,
                kp.getCoffeeBeansLevel(),
                kp.getMilkLevel(KitchenProcess.MilkType.COW),
                kp.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE),
                kp.getMilkLevel(KitchenProcess.MilkType.SOY),
                null
        );
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
