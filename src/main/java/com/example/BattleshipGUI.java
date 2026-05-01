package com.example;

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class BattleshipGUI extends JFrame {

    // Cell state constants (for visual preview only — no logic)
    static final int WATER    = 0;
    static final int SHIP     = 1;
    static final int HIT      = 2;
    static final int MISS     = 3;

    // Colors
    static final Color COLOR_WATER      = new Color(30, 100, 180);
    static final Color COLOR_WATER_DARK = new Color(20, 70, 130);
    static final Color COLOR_SHIP       = new Color(120, 130, 140);
    static final Color COLOR_HIT        = new Color(210, 50, 50);
    static final Color COLOR_MISS       = new Color(200, 210, 220);
    static final Color COLOR_BG         = new Color(15, 25, 45);
    static final Color COLOR_HEADER     = new Color(170, 195, 225);
    static final Color COLOR_LABEL_BG   = new Color(15, 25, 45);

    static final String[] COL_LABELS = {"A","B","C","D","E","F","G","H","I","J"};

    static final String CARD_SPLASH = "splash";
    static final String CARD_GAME   = "game";

    private CardLayout cardLayout;
    private JPanel     cardPanel;
    private Battleship game;
    private JPanel     boardsContainer;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;
    private boolean isPlayerOne = true; // Default to P1, server will update

    public BattleshipGUI() {
        setTitle("Battleship");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        game = new Battleship(10);
        connectToServer();

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        cardPanel.add(buildSplash(), CARD_SPLASH);
        cardPanel.add(buildGame(),   CARD_GAME);

        add(cardPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void connectToServer() {
        try {
            System.out.println("Attempting to connect to server...");
            socket = new Socket("127.0.0.1", 9876);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());

            // Start reader thread
            Thread readerThread = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        Object obj = ois.readObject();
                        if (obj instanceof String) {
                            String jsonState = (String) obj;
                            updateStateFromJson(jsonState);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server: " + e.getMessage());
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + e.getMessage());
        }
    }

    private void updateStateFromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            
            // Update maps
            game.mapOne = mapper.convertValue(root.get("mapOne"), int[][].class);
            game.mapTwo = mapper.convertValue(root.get("mapTwo"), int[][].class);
            
            // Update player identity
            if (root.has("isPlayerOne")) {
                this.isPlayerOne = root.get("isPlayerOne").asBoolean();
            }
            
            SwingUtilities.invokeLater(() -> refreshBoards());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel buildSplash() {
        JPanel splash = new JPanel(new GridBagLayout());
        splash.setBackground(COLOR_BG);
        splash.setPreferredSize(new Dimension(860, 560));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 12, 0);

        JLabel title = new JLabel("BATTLESHIP", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 52));
        title.setForeground(new Color(220, 230, 255));
        gbc.gridy = 0;
        splash.add(title, gbc);

        JLabel subtitle = new JLabel("Naval Combat Strategy", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 16));
        subtitle.setForeground(new Color(110, 145, 190));
        gbc.gridy = 1;
        splash.add(subtitle, gbc);

        JButton startBtn = new JButton("START GAME");
        startBtn.setFont(new Font("Monospaced", Font.BOLD, 18));
        startBtn.setForeground(new Color(220, 230, 255));
        startBtn.setBackground(new Color(30, 70, 130));
        startBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 130, 200), 2),
            new EmptyBorder(12, 40, 12, 40)
        ));
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> cardLayout.show(cardPanel, CARD_GAME));
        gbc.gridy = 2;
        gbc.insets = new Insets(30, 80, 12, 80);
        splash.add(startBtn, gbc);

        return splash;
    }

    private JPanel buildGame() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("BATTLESHIP", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        title.setForeground(new Color(220, 230, 255));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        root.add(title, BorderLayout.NORTH);

        boardsContainer = new JPanel(new GridLayout(1, 2, 40, 0));
        boardsContainer.setBackground(COLOR_BG);

        refreshBoards();

        root.add(boardsContainer, BorderLayout.CENTER);
        root.add(buildLegend(), BorderLayout.SOUTH);

        return root;
    }

    private void refreshBoards() {
        boardsContainer.removeAll();
        
        if (isPlayerOne) {
            boardsContainer.add(buildBoard("YOUR FLEET", game.mapOne, true, false));
            boardsContainer.add(buildBoard("ENEMY WATERS", game.mapTwo, false, true));
        } else {
            // For Player 2, mapTwo is THEIR fleet, and mapOne is the ENEMY waters
            boardsContainer.add(buildBoard("YOUR FLEET", game.mapTwo, true, false));
            boardsContainer.add(buildBoard("ENEMY WATERS", game.mapOne, false, true));
        }

        boardsContainer.revalidate();
        boardsContainer.repaint();
    }

    private JPanel buildBoard(String label, int[][] grid, boolean showShips, boolean interactive) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(COLOR_BG);

        // Board title
        JLabel boardTitle = new JLabel(label, SwingConstants.CENTER);
        boardTitle.setFont(new Font("Monospaced", Font.BOLD, 14));
        boardTitle.setForeground(COLOR_HEADER);
        wrapper.add(boardTitle, BorderLayout.NORTH);

        // Grid + row/col headers in a panel
        JPanel gridPanel = new JPanel(new GridLayout(11, 11, 2, 2));
        gridPanel.setBackground(COLOR_BG);

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                if (row == 0 && col == 0) {
                    gridPanel.add(headerCell(""));
                } else if (row == 0) {
                    gridPanel.add(headerCell(COL_LABELS[col - 1]));
                } else if (col == 0) {
                    gridPanel.add(headerCell(String.valueOf(row)));
                } else {
                    int r = row - 1;
                    int c = col - 1;
                    int state = grid[r][c];
                    gridPanel.add(makeCell(state, showShips, interactive, r, c));
                }
            }
        }

        wrapper.add(gridPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JLabel headerCell(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 11));
        lbl.setForeground(COLOR_HEADER);
        lbl.setBackground(COLOR_LABEL_BG);
        lbl.setOpaque(true);
        lbl.setPreferredSize(new Dimension(36, 36));
        return lbl;
    }

    private JPanel makeCell(int state, boolean showShips, boolean interactive, int r, int c) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setPreferredSize(new Dimension(36, 36));
        cell.setBorder(BorderFactory.createLineBorder(new Color(10, 30, 60), 1));

        Color bg;
        String symbol = "";
        Color fgColor = Color.WHITE;

        switch (state) {
            case SHIP:
                bg = showShips ? COLOR_SHIP : COLOR_WATER;
                break;
            case HIT:
                bg = COLOR_HIT;
                symbol = "✕";
                break;
            case MISS:
                bg = COLOR_MISS;
                symbol = "•";
                fgColor = new Color(80, 90, 100);
                break;
            default: // WATER
                bg = COLOR_WATER;
                break;
        }

        cell.setBackground(bg);

        if (!symbol.isEmpty()) {
            JLabel sym = new JLabel(symbol, SwingConstants.CENTER);
            sym.setFont(new Font("Dialog", Font.BOLD, 16));
            sym.setForeground(fgColor);
            cell.add(sym, BorderLayout.CENTER);
        }

        if (interactive && state != HIT && state != MISS) {
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    try {
                        oos.writeObject("SHOOT " + r + " " + c);
                        oos.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        return cell;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        legend.setBackground(COLOR_BG);
        legend.setBorder(new EmptyBorder(18, 0, 0, 0));

        legend.add(legendItem(COLOR_WATER,  "Water"));
        legend.add(legendItem(COLOR_SHIP,   "Ship"));
        legend.add(legendItem(COLOR_HIT,    "Hit"));
        legend.add(legendItem(COLOR_MISS,   "Miss"));

        return legend;
    }

    private JPanel legendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setBackground(COLOR_BG);

        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(16, 16));
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(new Color(80, 100, 130), 1));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lbl.setForeground(COLOR_HEADER);

        item.add(swatch);
        item.add(lbl);
        return item;
    }

    // Helpers to place sample ships on the preview grid
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BattleshipGUI().setVisible(true));
    }
}
