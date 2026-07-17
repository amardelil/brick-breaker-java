import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BrickBreaker extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

    // ===== GAME WINDOW SETTINGS =====
    private static final int WIDTH = 700;
    private static final int HEIGHT = 600;
    private static final String TITLE = "🧱 Brick Breaker";

    // ===== PADDLE =====
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 15;
    private int paddleX = (WIDTH - PADDLE_WIDTH) / 2;
    private final int paddleY = HEIGHT - 50;

    // ===== BALL =====
    private static final int BALL_SIZE = 14;
    private int ballX = WIDTH / 2;
    private int ballY = HEIGHT / 2;
    private int ballDirX = -3;
    private int ballDirY = -4;
    private static final int BALL_SPEED = 4;

    // ===== BRICKS =====
    private static final int ROWS = 6;
    private static final int COLS = 10;
    private static final int BRICK_W = 60;
    private static final int BRICK_H = 22;
    private static final int BRICK_PAD = 4;
    private boolean[][] bricks;
    private static final int BRICK_TOP_OFFSET = 60;

    // ===== GAME STATE =====
    private int score = 0;
    private int lives = 3;
    private boolean running = true;
    private boolean gameOver = false;
    private boolean won = false;
    private Timer timer;

    // ===== CONSTRUCTOR =====
    public BrickBreaker() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(18, 25, 35)); // dark navy
        setFocusable(true);
        addKeyListener(this);
        addMouseMotionListener(this);

        initBricks();
        timer = new Timer(10, this);
        timer.start();
    }

    // ===== INIT BRICKS =====
    private void initBricks() {
        bricks = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                bricks[r][c] = true;
            }
        }
    }

    // ===== RESET BALL & PADDLE =====
    private void resetBallAndPaddle() {
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;
        paddleX = (WIDTH - PADDLE_WIDTH) / 2;
        ballDirX = -3;
        ballDirY = -4;
    }

    // ===== COLLISION & GAME LOGIC =====
    private void updateGame() {
        if (!running || gameOver || won) return;

        // --- Ball movement ---
        ballX += ballDirX;
        ballY += ballDirY;

        // --- Wall collisions ---
        if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) ballDirX = -ballDirX;
        if (ballY <= 0) ballDirY = -ballDirY;

        // --- Lose life (ball falls below paddle) ---
        if (ballY > HEIGHT) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                running = false;
            } else {
                resetBallAndPaddle();
            }
            return;
        }

        // --- Paddle collision ---
        Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        if (ballRect.intersects(paddleRect)) {
            ballDirY = -Math.abs(ballDirY); // always go upward
            // Change angle based on where ball hits the paddle
            int hitPos = ballX + BALL_SIZE/2 - (paddleX + PADDLE_WIDTH/2);
            ballDirX = hitPos / 10; // -5 to +5 roughly
            if (ballDirX == 0) ballDirX = (Math.random() > 0.5) ? 2 : -2;
        }

        // --- Brick collisions ---
        outer:
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (bricks[r][c]) {
                    int brickX = c * (BRICK_W + BRICK_PAD) + BRICK_PAD/2;
                    int brickY = r * (BRICK_H + BRICK_PAD) + BRICK_TOP_OFFSET;
                    Rectangle brickRect = new Rectangle(brickX, brickY, BRICK_W, BRICK_H);

                    if (ballRect.intersects(brickRect)) {
                        bricks[r][c] = false;
                        score += 10;

                        // Reverse ball direction based on which side it hit
                        if (ballX + BALL_SIZE - BALL_SPEED <= brickX || ballX + BALL_SPEED >= brickX + BRICK_W) {
                            ballDirX = -ballDirX;
                        } else {
                            ballDirY = -ballDirY;
                        }
                        break outer;
                    }
                }
            }
        }

        // --- Check win ---
        boolean allCleared = true;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (bricks[r][c]) {
                    allCleared = false;
                    break;
                }
            }
        }
        if (allCleared) {
            won = true;
            running = false;
        }
    }

    // ===== RENDERING =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Draw bricks ---
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (bricks[r][c]) {
                    int x = c * (BRICK_W + BRICK_PAD) + BRICK_PAD/2;
                    int y = r * (BRICK_H + BRICK_PAD) + BRICK_TOP_OFFSET;
                    // Color gradient based on row
                    float hue = 0.0f + (float) r / ROWS * 0.7f;
                    g2d.setColor(Color.getHSBColor(hue, 0.8f, 0.7f));
                    g2d.fillRoundRect(x, y, BRICK_W, BRICK_H, 8, 8);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(x, y, BRICK_W, BRICK_H, 8, 8);
                }
            }
        }

        // --- Draw paddle ---
        g2d.setColor(new Color(230, 180, 80)); // gold
        g2d.fillRoundRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT, 12, 12);

        // --- Draw ball ---
        g2d.setColor(new Color(255, 120, 80)); // warm orange
        g2d.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        g2d.setColor(Color.WHITE);
        g2d.drawOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // --- UI: Score & Lives ---
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.drawString("Score: " + score, 20, 35);
        g2d.drawString("Lives: " + "❤️".repeat(Math.max(0, lives)), WIDTH - 140, 35);

        // --- Game Over / Win overlay ---
        if (gameOver) {
            drawOverlay(g2d, "💀 GAME OVER", "Press R to restart", new Color(200, 50, 50));
        } else if (won) {
            drawOverlay(g2d, "🎉 YOU WIN!", "Press R to play again", new Color(50, 200, 100));
        }
    }

    private void drawOverlay(Graphics2D g2d, String title, String sub, Color bgColor) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(bgColor);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, tx, HEIGHT/2 - 20);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        fm = g2d.getFontMetrics();
        tx = (WIDTH - fm.stringWidth(sub)) / 2;
        g2d.drawString(sub, tx, HEIGHT/2 + 40);
    }

    // ===== RESTART =====
    private void restartGame() {
        initBricks();
        score = 0;
        lives = 3;
        running = true;
        gameOver = false;
        won = false;
        resetBallAndPaddle();
        timer.start();
        repaint();
    }

    // ===== TIMER LOOP =====
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    // ===== KEYBOARD CONTROLS =====
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_R && (gameOver || won)) {
            restartGame();
        }
        if (key == KeyEvent.VK_LEFT) {
            paddleX = Math.max(0, paddleX - 20);
        }
        if (key == KeyEvent.VK_RIGHT) {
            paddleX = Math.min(WIDTH - PADDLE_WIDTH, paddleX + 20);
        }
        if (key == KeyEvent.VK_SPACE && !running && !gameOver && !won) {
            running = true;
            timer.start();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ===== MOUSE CONTROL =====
    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX() - PADDLE_WIDTH / 2;
        paddleX = Math.max(0, Math.min(WIDTH - PADDLE_WIDTH, mouseX));
    }
    @Override public void mouseDragged(MouseEvent e) {}

    // ===== MAIN LAUNCHER =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            BrickBreaker game = new BrickBreaker();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
