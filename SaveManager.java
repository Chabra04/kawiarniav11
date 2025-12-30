import java.io.*;

public class SaveManager {

    private static final String FILE_NAME = "save_data.txt";

    // =====================================================
    // ZAPISYWANIE
    // =====================================================
    // Teraz przyjmuje 3 argumenty: Level, Punkty i Pieniądze
    public static void save(int level, int score, double money) {
        // Używamy try-with-resources (automatycznie zamyka plik)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            // Linia 1: Poziom
            writer.write(String.valueOf(level));
            writer.newLine();

            // Linia 2: Wynik całkowity
            writer.write(String.valueOf(score));
            writer.newLine();

            // Linia 3: Pieniądze (Format np. 200.50)
            writer.write(String.valueOf(money));

            System.out.println("Gra zapisana! (Lvl: " + level + ", Kasa: " + money + ")");

        } catch (IOException e) {
            System.err.println("Błąd zapisu gry!");
            e.printStackTrace();
        }
    }

    // Wersja kompatybilna (gdybyś gdzieś zapomniał dodać money w wywołaniu)
    // Domyślnie zapisuje 0.0 lub obecny stan - lepiej jednak używać metody powyżej.
    public static void save(int level, int score) {
        save(level, score, 0.0);
    }

    // =====================================================
    // WCZYTYWANIE
    // =====================================================
    public static void load(Game game) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("Brak pliku zapisu.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String levelStr = reader.readLine();
            String scoreStr = reader.readLine();
            String moneyStr = reader.readLine();

            if (levelStr != null) {
                game.currentLevel = Integer.parseInt(levelStr);
            }

            if (scoreStr != null) {
                game.score = Integer.parseInt(scoreStr);
            }

            if (moneyStr != null) {
                game.money = Double.parseDouble(moneyStr);
            }

            System.out.println("Wczytano grę: Poziom " + game.currentLevel + ", Kasa: " + game.money);

        } catch (IOException | NumberFormatException e) {
            System.err.println("Błąd wczytywania zapisu! Plik może być uszkodzony.");
            e.printStackTrace();
            // W razie błędu resetujemy do domyślnych
            game.currentLevel = 1;
            game.money = 200.0;
        }
    }

    // =====================================================
    // SPRAWDZANIE CZY ISTNIEJE ZAPIS
    // =====================================================
    public static boolean exists() {
        File f = new File(FILE_NAME);
        return f.exists() && !f.isDirectory();
    }
}
