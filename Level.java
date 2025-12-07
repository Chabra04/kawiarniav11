import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Level {

    private int levelNumber;
    private int timeLimit;
    private int timeLeft;
    private boolean started;
    private boolean finished;

    private List<Client>clients;
    private Random  random= new Random();

    private String[] names={
            "Anna", "Piotr", "Julia", "Marek", "Krzysztof",
            "Natalia", "Kinga", "Oskar", "Bartek", "Zuzanna"
    };

    private String[] drinks ={
            "Latte", "Espresso", "Cappuccino", "Americano", "Mocha"
    };

    public Level(int number, int timeLimit){
        this.levelNumber = number;
        this.timeLimit = timeLimit;
        this.timeLeft=timeLimit;

        this.started = false;
        this.finished = false;

        clients = new ArrayList<>();
    }

    //start i koniec poziomu
    public void start(){
        started=true;
        finished = false;
        timeLeft= timeLimit;

        generateClients(3);
    }
    public void finish(){
        finished=true;
        started=false;
    }

    private void generateClients(int count){
        clients.clear();

        for (int i=0;i<count; i++)  {
            String randomName=names[random.nextInt(names.length)];
            String randomDrink = drinks[random.nextInt(drinks.length)];

            clients.add(new Client(randomName, randomDrink));
        }
    }

    //Update :D
    public void update(){
        if(!started ||finished) return;

        if(timeLeft>0){
            timeLeft--;
        } else {finish();}

        for(Client c : clients){
            c.update();
        }
    }

    //render

    public void render(Graphics g){
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial",Font.BOLD,50));
        g.drawString("poziom "+ levelNumber,200,150);

        g.setFont(new Font("Arial",Font.PLAIN,30));
        g.drawString("pozostaly czas: "+ timeLeft,200,200);

        int x=600;
        int y= 150;
        for (Client c:clients){
            c.render(g,x,y);
            y+=180;
        }

        if(finished){
            g.setColor((Color.RED));
            g.setFont(new Font("Arial",Font.BOLD,40));
            g.drawString("koniec poziomu",200,320);

        }
    }
}
