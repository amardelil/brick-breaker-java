import java.awt.*;

public class Ball {
    private int x, y, width, height;
    private int dx = 3, dy = 3; // Movement speed
    
    public Ball(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, width, height);
    }
    
    public void move() {
        x += dx;
        y += dy;
    }
    
    public void reverseX() { dx = -dx; }
    public void reverseY() { dy = -dy; }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}