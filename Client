import java.awt.*;

public class Client {
        private String name;
        private String order;

        public Client(String name, String order){
        this.name=name;
        this.order=order; }

       public void update(){}

        public void render(Graphics g,int x, int y){
        g.setColor(new Color(200,170,150));
        g.fillRect(x,y,120,150);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD,18));
        g.drawString(name,x+10,y+25);

        g.setFont(new Font("Arial",Font.PLAIN,16));
        g.drawString("zamówienie: ",x+10,y+55);
        g.drawString(order,x+10,y+80);
        }

            public String getName(){return name;}
            public String getOrder(){return order;}
}
