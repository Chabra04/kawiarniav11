import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Menu extends MouseAdapter {
    private int mouseX;
    private int mouseY;

    private Rectangle startButton;
    private Rectangle optionsButton;
    private Rectangle exitButton;

    public Menu(){
        int w=300;
        int h=60;

        startButton = new Rectangle(100,200,w,h);
        optionsButton=new Rectangle(100,300,w,h);
        exitButton=new Rectangle(100,400,w,h);
    }
    public void update(){

    }
    public void render(Graphics g){

        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial",Font.BOLD,60));
        g.drawString("Kawiarnia Telekomuynikayjna",100,120);

        drawButton(g,startButton,"Start");
        drawButton(g, optionsButton,"opcje");
        drawButton(g,exitButton,"Wyjscie");
    }

    private void drawButton(Graphics g, Rectangle r, String text){
        g.setColor(new Color(80,50,30));
        g.fillRect(r.x,r.y,r.width,r.height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial",Font.BOLD,35));
        g.drawString(text,r.x+80,r.y+40);

    }
    @Override

    public void mousePressed(MouseEvent e){
        int mx = e.getX();
        int my = e.getY();

        if (startButton.contains(mx, my)){
            Game.gameState = Game.State.GAME;
        }

        if (exitButton.contains(mx,my)){
            System.exit(0);
        }
    }
}
