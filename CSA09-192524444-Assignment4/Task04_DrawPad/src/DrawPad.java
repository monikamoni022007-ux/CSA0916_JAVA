import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class DrawPad extends Frame {
    private final ArrayList<Point> points = new ArrayList<>();
    private final Label clickCountLabel = new Label("Click Count: 0");
    private final Button clearButton = new Button("Clear");

    public DrawPad() {
        super("DrawPad");
        setSize(500, 400);
        setLocation(100, 100);
        setLayout(null);
        setBackground(Color.WHITE);
        setFocusable(true);

        clickCountLabel.setBounds(20, 20, 150, 25);
        clearButton.setBounds(200, 20, 80, 25);
        add(clickCountLabel);
        add(clearButton);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                points.add(e.getPoint());
                clickCountLabel.setText("Click Count: " + points.size());
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                char keyChar = Character.toLowerCase(e.getKeyChar());

                if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE
                        || keyCode == KeyEvent.VK_ESCAPE || keyChar == 'c') {
                    clearDrawing();
                    return;
                }

                if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_UP
                        || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_LEFT
                        || keyCode == KeyEvent.VK_RIGHT || keyChar == 'd' || keyChar == 'p') {
                    int x = Math.max(20, Math.min(getWidth() - 20, getWidth() / 2 + (keyCode == KeyEvent.VK_LEFT ? -30
                            : (keyCode == KeyEvent.VK_RIGHT ? 30 : 0))));
                    int y = Math.max(40, Math.min(getHeight() - 20, getHeight() / 2 + (keyCode == KeyEvent.VK_UP ? -30
                            : (keyCode == KeyEvent.VK_DOWN ? 30 : 0))));
                    points.add(new Point(x, y));
                    clickCountLabel.setText("Click Count: " + points.size());
                    repaint();
                }
            }
        });

        clearButton.addActionListener(e -> clearDrawing());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                requestFocus();
            }
        });
    }

    private void clearDrawing() {
        points.clear();
        clickCountLabel.setText("Click Count: 0");
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Point point : points) {
            g.setColor(Color.BLACK);
            g.fillOval(point.x - 3, point.y - 3, 6, 6);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            DrawPad pad = new DrawPad();
            pad.setVisible(true);
        });
    }
}
