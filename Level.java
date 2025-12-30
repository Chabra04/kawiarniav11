import java.awt.*;
import java.util.*;
import java.util.List;

public class Level {

    private static final int MAX_VISIBLE_CLIENTS = 4;
    private static final int PATIENCE_PENALTY = 5;

    private final int levelNumber;
    private final Gameplay gameplay;

    private final List<Client> activeClients = new ArrayList<>();
    private final Queue<Client> waitingClients = new LinkedList<>();

    private final Random random = new Random();

    public Level(int levelNumber, Gameplay gameplay) {
        this.levelNumber = levelNumber;
        this.gameplay = gameplay;
    }

// W klasie Level, metoda randomDrink jest zastępowana logiką w start()

    // Metoda pomocnicza do tworzenia klienta
    private Client createRandomClient() {
        String drink = randomDrinkName();
        KitchenProcess.MilkType milk = null;

        // Jeśli to kawa mleczna, losujemy mleko
        if (drink.equals("Latte") || drink.equals("Cappuccino")) {
            int r = random.nextInt(100);
            if (r < 60) milk = KitchenProcess.MilkType.COW;        // 60% Krowie
            else if (r < 85) milk = KitchenProcess.MilkType.LACTOSE_FREE; // 25% Bez laktozy
            else milk = KitchenProcess.MilkType.SOY;              // 15% Sojowe
        }

        return new Client(randomName(), drink, milk, 0, 0);
    }

    private String randomDrinkName() {
        if (levelNumber == 1) return random.nextBoolean() ? "Espresso" : "Latte";

        String[] d = { "Espresso", "Double Espresso", "Latte", "Cappuccino", "Americano" };
        return d[random.nextInt(d.length)];
    }

    // W metodzie start() używamy teraz createRandomClient()
    public void start() {
        activeClients.clear();
        waitingClients.clear();
        int totalClients = 2 + levelNumber;

        for (int i = 0; i < totalClients; i++) {
            Client c = createRandomClient(); // <--- Zmiana
            if (activeClients.size() < MAX_VISIBLE_CLIENTS) activeClients.add(c);
            else waitingClients.add(c);
        }
        layoutClients();
    }

    // =====================================================
    // UPDATE
    // =====================================================
    public void update() {

        // aktualizacja cierpliwości
        for (Client c : activeClients) {
            c.update();
        }

        // klienci, którym skończyła się cierpliwość
        activeClients.removeIf(c -> {
            if (c.isOutOfPatience()) {
                gameplay.addScore(-PATIENCE_PENALTY);
                return true;
            }
            return false;
        });

        // uzupełnianie sali
        while (activeClients.size() < MAX_VISIBLE_CLIENTS
                && !waitingClients.isEmpty()) {

            activeClients.add(waitingClients.poll());
            layoutClients();
        }

        // === KONIEC POZIOMU ===
        if (activeClients.isEmpty() && waitingClients.isEmpty()) {
            // ZMIANA: Nie przechodzimy od razu dalej, tylko pokazujemy podsumowanie
            gameplay.levelCompleted();
        }
    }

    // =====================================================
    // KLIENT OBSŁUŻONY (z kuchni)
    // =====================================================
    public void clientServed(Client client) {
        // Punkty są dodawane w Gameplay/Kitchen na podstawie jakości,
        // tutaj tylko usuwamy klienta z sali.
        activeClients.remove(client);

        // od razu uzupełnij miejsce
        if (!waitingClients.isEmpty()) {
            activeClients.add(waitingClients.poll());
            layoutClients();
        }
    }

    // =====================================================
    // KLIKNIĘCIE KLIENTA
    // =====================================================
    public void click(int mx, int my, Gameplay gp) {
        for (Client c : activeClients) {
            if (c.contains(mx, my) && !c.isProcessed()) {
                gp.goToKitchen(c);
                return;
            }
        }
    }

    // =====================================================
    // RENDER
    // =====================================================
    public void render(Graphics g) {

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Poziom " + levelNumber, 40, 60);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Punkty: " + gameplay.getScore(), 40, 100);
        g.drawString("W kolejce: " + waitingClients.size(), 40, 135);

        for (Client c : activeClients) {
            c.render(g);
        }
    }

    // =====================================================
    // LAYOUT KLIENTÓW
    // =====================================================
    private void layoutClients() {
        int startY = 180;
        int gap = 170;

        for (int i = 0; i < activeClients.size(); i++) {
            activeClients.get(i).setPosition(
                    480,
                    startY + i * gap
            );
        }
    }

    // =====================================================
    // LOSOWANIA
    // =====================================================
    private String randomName() {
        String[] names = {
                "Anna", "Piotr", "Julia", "Marek",
                "Ola", "Bartek", "Kasia", "Tomek",
                "Jan", "Zosia", "Michał", "Ewa"
        };
        return names[random.nextInt(names.length)];
    }

    private String randomDrink() {
        if (levelNumber == 1) {
            return random.nextBoolean() ? "Espresso" : "Latte";
        }

        // Od poziomu 2 dochodzi więcej opcji
        String[] d = {
                "Espresso",
                "Double Espresso",
                "Latte",
                "Cappuccino",
                "Americano" // <--- ZMIANA: Dodano Americano
        };
        return d[random.nextInt(d.length)];
    }
}
