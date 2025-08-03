import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Spacewar extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int STAR_COUNT = 100;

    private Timer gameTimer;
    private Ship ship1, ship2;
    private ArrayList<Bullet> bullets;
    private ArrayList<Point> stars;
    private boolean[] keys;
    private Random random;
    private Font font;

    
    private int ship1Score = 0;
    private int ship2Score = 0;
    private boolean gameRunning = true;

    public Spacewar() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        font = new Font("Arial", Font.BOLD, 16);

        initGame();

        gameTimer = new Timer(16, this); 
        gameTimer.start();
    }

    private void initGame() {
        
        ship1 = new Ship(200, HEIGHT / 2, Color.CYAN, 0);
        ship2 = new Ship(WIDTH - 200, HEIGHT / 2, Color.YELLOW, Math.PI);

        bullets = new ArrayList<>();
        keys = new boolean[256];

        
        stars = new ArrayList<>();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        g2d.setColor(Color.WHITE);
        for (Point star : stars) {
            g2d.fillOval(star.x, star.y, 1, 1);
        }

        
        ship1.draw(g2d);
        ship2.draw(g2d);

        
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }

        
        g2d.setFont(font);
        g2d.setColor(Color.CYAN);
        g2d.drawString("Oyuncu 1: " + ship1Score, 10, 30);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Oyuncu 2: " + ship2Score, WIDTH - 150, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Oyuncu 1: A/D - dön, W - ileri, S - ateş", 10, HEIGHT - 40);
        g2d.drawString("Oyuncu 2: ←/→ - dön, ↑ - ileri, ↓ - ateş", 10, HEIGHT - 20);

        if (!gameRunning) {
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.setColor(Color.RED);
            String winner = ship1Score > ship2Score ? "Oyuncu 1 Kazandı!" : "Oyuncu 2 Kazandı!";
            if (ship1Score == ship2Score) { 
                winner = "Berabere!";
            }
            FontMetrics fm = g2d.getFontMetrics();
            int x = (WIDTH - fm.stringWidth(winner)) / 2;
            g2d.drawString(winner, x, HEIGHT / 2);
            g2d.drawString("R tuşuna basarak yeniden başlayın", x - 50, HEIGHT / 2 + 30);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (keys[KeyEvent.VK_R] && !gameRunning) {
            restartGame();
            return; 
        }

        if (!gameRunning) return; 

        updateGame();
        repaint();
    }

    private void updateGame() {
        
        updateShipControls();

        
        ship1.update();
        ship2.update();

        
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update();

            
            if (bullet.x < 0 || bullet.x > WIDTH || bullet.y < 0 || bullet.y > HEIGHT) {
                bulletIter.remove();
                continue;
            }

            
            Ship target = (bullet.owner == 1) ? ship2 : ship1;
            if (bullet.hits(target)) {
                bulletIter.remove();
                target.lastHit = System.currentTimeMillis(); 
                if (bullet.owner == 1) {
                    ship1Score++;
                } else {
                    ship2Score++;
                }
                respawnShip(target);

                
                if (ship1Score >= 5 || ship2Score >= 5) {
                    gameRunning = false;
                }
            }
        }
    }

    private void updateShipControls() {
        
        if (keys[KeyEvent.VK_A]) ship1.turnLeft();
        if (keys[KeyEvent.VK_D]) ship1.turnRight();
        if (keys[KeyEvent.VK_W]) ship1.thrust();
        if (keys[KeyEvent.VK_S]) ship1.shoot();

        
        if (keys[KeyEvent.VK_LEFT]) ship2.turnLeft();
        if (keys[KeyEvent.VK_RIGHT]) ship2.turnRight();
        if (keys[KeyEvent.VK_UP]) ship2.thrust();
        if (keys[KeyEvent.VK_DOWN]) ship2.shoot();
    }

    private void respawnShip(Ship ship) {
        if (ship == ship1) {
            ship1 = new Ship(200, HEIGHT / 2, Color.CYAN, 0);
        } else {
            ship2 = new Ship(WIDTH - 200, HEIGHT / 2, Color.YELLOW, Math.PI);
        }
    }

    private void restartGame() {
        ship1Score = 0;
        ship2Score = 0;
        gameRunning = true;
        bullets.clear();
        respawnShip(ship1); 
        respawnShip(ship2); 
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Gemi sınıfı
    class Ship {
        double x, y, angle, vx, vy;
        Color color;
        int owner;
        long lastShot = 0;
        long lastHit = 0;
        boolean thrusting = false;

        public Ship(double x, double y, Color color, double angle) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.angle = angle;
            this.owner = (color == Color.CYAN) ? 1 : 2;
            this.vx = 0; 
            this.vy = 0; 
        }

        public void turnLeft() {
            angle -= 0.1;
        }

        public void turnRight() {
            angle += 0.1;
        }

        public void thrust() {
            thrusting = true;
            double thrust = 0.3;
            vx += Math.cos(angle) * thrust;
            vy += Math.sin(angle) * thrust;

            
            double maxSpeed = 8;
            double speed = Math.sqrt(vx * vx + vy * vy);
            if (speed > maxSpeed) {
                vx = (vx / speed) * maxSpeed;
                vy = (vy / speed) * maxSpeed;
            }
        }

        public void shoot() {
            long now = System.currentTimeMillis();
            if (now - lastShot > 200) { 
                double bulletSpeed = 10;
                
                double bx = x + Math.cos(angle) * 15;
                double by = y + Math.sin(angle) * 15;
                double bvx = vx + Math.cos(angle) * bulletSpeed;
                double bvy = vy + Math.sin(angle) * bulletSpeed;

                bullets.add(new Bullet(bx, by, bvx, bvy, owner));
                lastShot = now;
            }
        }

        public void update() {
            
            x += vx;
            y += vy;

            
            vx *= 0.99;
            vy *= 0.99;

            
            if (x < 0) x = WIDTH;
            if (x > WIDTH) x = 0;
            if (y < 0) y = HEIGHT;
            if (y > HEIGHT) y = 0;

            thrusting = false;
        }

        public void draw(Graphics2D g) {
            
            int[] mainBodyX = {0, -6, -10, -8, -12, -8, -10, -6};
            int[] mainBodyY = {16, 8, 6, 2, 0, -2, -6, -8};

            Polygon mainBody = new Polygon();
            for (int i = 0; i < mainBodyX.length; i++) {
                double px = mainBodyX[i] * Math.cos(angle) - mainBodyY[i] * Math.sin(angle);
                double py = mainBodyX[i] * Math.sin(angle) + mainBodyY[i] * Math.cos(angle);
                mainBody.addPoint((int) (x + px), (int) (y + py));
            }

            
            Color darkColor = color.darker().darker();
            Color lightColor = color.brighter();

            GradientPaint gradient = new GradientPaint(
                    (int) x - 10, (int) y - 10, lightColor,
                    (int) x + 10, (int) y + 10, darkColor
            );
            g.setPaint(gradient);
            g.fillPolygon(mainBody);

            
            g.setColor(color.brighter().brighter());
            g.setStroke(new BasicStroke(1.5f));
            g.drawPolygon(mainBody);

            
            drawWing(g, -4, 3, -8, 8);    
            drawWing(g, -4, -3, -8, -8);  

            
            g.setColor(Color.WHITE);
            int cockpitX = (int) (x + Math.cos(angle) * 8);
            int cockpitY = (int) (y + Math.sin(angle) * 8);
            g.fillOval(cockpitX - 2, cockpitY - 2, 4, 4);

            
            g.setColor(Color.GRAY);
            int gunX = (int) (x + Math.cos(angle) * 12);
            int gunY = (int) (y + Math.sin(angle) * 12);
            g.setStroke(new BasicStroke(2f));
            g.drawLine((int) x, (int) y, gunX, gunY);

            
            g.setColor(darkColor);
            int[] engineX = {-8, -12, -14, -12};
            int[] engineY = {4, 2, 0, -2};

            Polygon engine = new Polygon();
            for (int i = 0; i < engineX.length; i++) {
                double px = engineX[i] * Math.cos(angle) - engineY[i] * Math.sin(angle);
                double py = engineX[i] * Math.sin(angle) + engineY[i] * Math.cos(angle);
                engine.addPoint((int) (x + px), (int) (y + py));
            }
            g.fillPolygon(engine);

            
            if (thrusting) {
                drawThrusterFlame(g);
            }

            
            if (System.currentTimeMillis() - lastHit < 1000) {
                g.setColor(new Color(0, 255, 255, 50));
                g.fillOval((int) x - 20, (int) y - 20, 40, 40);
            }
        }

        private void drawWing(Graphics2D g, int baseX, int baseY, int tipX, int tipY) {
            Color wingColor = color.darker();
            g.setColor(wingColor);

            int[] wingX = {baseX, tipX, tipX - 2, baseX - 2};
            int[] wingY = {baseY, tipY, tipY - 1, baseY - 1};

            Polygon wing = new Polygon();
            for (int i = 0; i < wingX.length; i++) {
                double px = wingX[i] * Math.cos(angle) - wingY[i] * Math.sin(angle);
                double py = wingX[i] * Math.sin(angle) + wingY[i] * Math.cos(angle);
                wing.addPoint((int) (x + px), (int) (y + py));
            }
            g.fillPolygon(wing);

            g.setColor(color);
            g.setStroke(new BasicStroke(1f));
            g.drawPolygon(wing);
        }

        private void drawThrusterFlame(Graphics2D g) {
            
            g.setColor(new Color(255, 100, 0, 180));
            int flameX = (int) (x - Math.cos(angle) * 18);
            int flameY = (int) (y - Math.sin(angle) * 18);

            int[] flameXPoints = new int[6];
            int[] flameYPoints = new int[6];

            
            int[] localFlameX = {0, -8, -12, -10, -12, -8};
            int[] localFlameY = {0, -3, -1, 0, 1, 3};

            for (int i = 0; i < localFlameX.length; i++) {
                double px = localFlameX[i] * Math.cos(angle + Math.PI) - localFlameY[i] * Math.sin(angle + Math.PI);
                double py = localFlameX[i] * Math.sin(angle + Math.PI) + localFlameY[i] * Math.cos(angle + Math.PI);
                flameXPoints[i] = (int) (flameX + px);
                flameYPoints[i] = (int) (flameY + py);
            }

            g.fillPolygon(flameXPoints, flameYPoints, 6);

            
            g.setColor(new Color(255, 200, 50, 200));
            for (int i = 0; i < localFlameX.length; i++) {
                localFlameX[i] *= 0.6;
                localFlameY[i] *= 0.6;
                double px = localFlameX[i] * Math.cos(angle + Math.PI) - localFlameY[i] * Math.sin(angle + Math.PI);
                double py = localFlameX[i] * Math.sin(angle + Math.PI) + localFlameY[i] * Math.cos(angle + Math.PI);
                flameXPoints[i] = (int) (flameX + px);
                flameYPoints[i] = (int) (flameY + py);
            }
            g.fillPolygon(flameXPoints, flameYPoints, 6);
        }
    }

    // Mermi sınıfı
    class Bullet {
        double x, y, vx, vy;
        int owner;

        public Bullet(double x, double y, double vx, double vy, int owner) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.owner = owner;
        }

        public void update() {
            x += vx;
            y += vy;
        }

        public void draw(Graphics2D g) {
            g.setColor(Color.YELLOW);
            g.fillOval((int) x - 3, (int) y - 3, 6, 6);

            
            g.setColor(new Color(255, 255, 0, 100));
            g.fillOval((int) x - 5, (int) y - 5, 10, 10);
        }

        public boolean hits(Ship ship) {
            double distance = Math.sqrt(Math.pow(x - ship.x, 2) + Math.pow(y - ship.y, 2));
            return distance < 15; 
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Spacewar!");
            Spacewar game = new Spacewar();

            frame.add(game);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}