import java.io.*;

public class SaveManager {

    private static final String SAVE_FILE = "save.dat";

    public static void save(int level, int score) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            pw.println(level);
            pw.println(score);
        } catch (Exception e) {
            System.out.println("Błąd zapisywania gry!");
        }
    }

    public static boolean exists() {
        File f = new File(SAVE_FILE);
        return f.exists();
    }

    public static int[] load() {
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE))) {
            int level = Integer.parseInt(br.readLine());
            int score = Integer.parseInt(br.readLine());
            return new int[]{level, score};
        } catch (Exception e) {
            System.out.println("Błąd wczytywania save!");
            return null;
        }
    }

    public static void delete() {
        File f = new File(SAVE_FILE);
        if (f.exists()) f.delete();
    }
}
