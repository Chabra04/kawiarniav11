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

    private int clientsAlreadyServed = 0;

    public Level(int levelNumber, Gameplay gameplay) {
        this.levelNumber = levelNumber;
        this.gameplay = gameplay;
    }

    public void setAlreadyServed(int count) {
        this.clientsAlreadyServed = count;
    }

    // GETTER DO ZAPISU (Nowość)
    // Pozwala SaveManagerowi pobrać aktualnych klientów z sali
    public List<Client> getActiveClients() {
        return activeClients;
    }

    // START POZIOMU
    // Przyjmuje listę wczytanych klientów (może być null, jeśli to nowa gra)
    public void start(List<Client> loadedClients) {
        activeClients.clear();
        waitingClients.clear();

        // 1. Jeśli wczytaliśmy grę i mamy zapisanych klientów -> wrzucamy ich na salę
        if (loadedClients != null && !loadedClients.isEmpty()) {
            System.out.println("Wczytano " + loadedClients.size() + " klientów z zapisu.");
            activeClients.addAll(loadedClients);
        }

        // 2. Obliczamy ilu klientów jeszcze brakuje do końca poziomu
        int totalClients = 2 + levelNumber;
        int currentCount = clientsAlreadyServed + activeClients.size();
        int remainingToSpawn = totalClients - currentCount;

        if (remainingToSpawn < 0) remainingToSpawn = 0;

        System.out.println("Start poziomu " + levelNumber + ". Do stworzenia w kolejce: " + remainingToSpawn);

        // 3. Generujemy resztę klientów do kolejki (waitingClients)
        for (int i = 0; i < remainingToSpawn; i++) {
            waitingClients.add(createRandomClient());
        }

        // 4. Jeśli na sali jest luz (np. wczytaliśmy tylko 1 klienta), dobieramy z kolejki
        while (activeClients.size() < MAX_VISIBLE_CLIENTS && !waitingClients.isEmpty()) {
            activeClients.add(waitingClients.poll());
        }
        layoutClients();
    }

    // TWORZENIE LOSOWEGO KLIENTA
    public Client createRandomClient() {
        String drink = randomDrinkName();
        KitchenProcess.MilkType milk = null;

        // Jeśli napój mleczny, losujemy rodzaj mleka
        if (drink.equals("Latte") || drink.equals("Cappuccino")) {
            int r = random.nextInt(100);
            if (r < 60) milk = KitchenProcess.MilkType.COW;        // 60% Krowie
            else if (r < 85) milk = KitchenProcess.MilkType.LACTOSE_FREE; // 25% Bez laktozy
            else milk = KitchenProcess.MilkType.SOY;              // 15% Sojowe
        }

        // Tworzymy nowego klienta (konstruktor domyślny = pełna cierpliwość)
        return new Client(randomName(), drink, milk, 0, 0);
    }

    private String randomDrinkName() {
        if (levelNumber == 1) return random.nextBoolean() ? "Espresso" : "Latte";

        String[] d = { "Espresso", "Double Espresso", "Latte", "Cappuccino", "Americano" };
        return d[random.nextInt(d.length)];
    }

    // UPDATE
    public void update() {
        // 1. Aktualizacja cierpliwości aktywnych klientów
        for (Client c : activeClients) {
            c.update();
        }

        // 2. Usuwanie klientów, którym skończyła się cierpliwość
        activeClients.removeIf(c -> {
            if (c.isOutOfPatience()) {
                gameplay.addScore(-PATIENCE_PENALTY);
                return true;
            }
            return false;
        });

        // 3. Uzupełnianie sali klientami z kolejki (jeśli są miejsca)
        while (activeClients.size() < MAX_VISIBLE_CLIENTS && !waitingClients.isEmpty()) {
            activeClients.add(waitingClients.poll());
            layoutClients(); // Przelicz pozycje
        }

        // 4. Sprawdzenie końca poziomu
        if (activeClients.isEmpty() && waitingClients.isEmpty()) {
            gameplay.levelCompleted();
        }
    }

    // OBSŁUGA ZDARZEŃ

    // Wywoływane przez Gameplay, gdy klient został obsłużony w kuchni
    public void clientServed(Client client) {
        activeClients.remove(client);

        // Od razu wpuszczamy następnego
        if (!waitingClients.isEmpty()) {
            activeClients.add(waitingClients.poll());
            layoutClients();
        }
    }

    // Kliknięcie w sali (przejście do kuchni)
    public void click(int mx, int my, Gameplay gp) {
        for (Client c : activeClients) {
            // Jeśli kliknięto klienta i nie jest on jeszcze obsłużony
            if (c.contains(mx, my) && !c.isProcessed()) {
                gp.goToKitchen(c);
                return;
            }
        }
    }

    // RENDER
    public void render(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Poziom " + levelNumber, 1000, 60);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Punkty: " + gameplay.getScore(), 40, 100);
        g.drawString("W kolejce: " + waitingClients.size(), 40, 135);

        for (Client c : activeClients) {
            c.render(g);
        }
    }

    // POMOCNICZE
    // Ustawia klientów jeden pod drugim
    private void layoutClients() {
        int startY = 180;
        int gap = 170; // Odstęp pionowy

        for (int i = 0; i < activeClients.size(); i++) {
            activeClients.get(i).setPosition(480, startY + i * gap);
        }
    }

    private String randomName() {
        String[] names = { "Anna", "Piotr", "Julia", "Marek", "Ola", "Bartek", "Kasia", "Tomek", "Jan", "Zosia" };
        return names[random.nextInt(names.length)];
    }
}
