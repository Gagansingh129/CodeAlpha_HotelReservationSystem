package org.example.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class ThemeManager {
    // === COLORS ===
    public static final Color BG_DARK = new Color(0x0D, 0x11, 0x17);
    public static final Color BG_CARD = new Color(0x16, 0x1b, 0x22);
    public static final Color BG_SIDEBAR = new Color(0x0D, 0x11, 0x17);
    public static final Color BG_INPUT = new Color(0x21, 0x26, 0x2d);
    public static final Color BORDER = new Color(0x30, 0x36, 0x3d);

    public static final Color ACCENT_GOLD = new Color(0xE2, 0xB0, 0x4A);
    public static final Color ACCENT_BLUE = new Color(0x58, 0xA6, 0xFF);
    public static final Color ACCENT_GREEN = new Color(0x3F, 0xB9, 0x50);
    public static final Color ACCENT_RED = new Color(0xF8, 0x53, 0x49);
    public static final Color ACCENT_PURPLE = new Color(0xBC, 0x8C, 0xFF);
    public static final Color ACCENT_ORANGE = new Color(0xD2, 0x9A, 0x22);

    public static final Color TEXT_PRIMARY = new Color(0xE6, 0xED, 0xF3);
    public static final Color TEXT_SECONDARY = new Color(0x8B, 0x94, 0x9E);
    public static final Color TEXT_MUTED = new Color(0x6E, 0x76, 0x81);

    // === FONTS ===
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_STAT_NUMBER = new Font("Segoe UI", Font.BOLD, 36);

    // === DIMENSIONS ===
    public static final int SIDEBAR_WIDTH = 260;
    public static final int CARD_RADIUS = 16;
    public static final int BUTTON_RADIUS = 10;
    public static final int PADDING = 24;

    // ==================== INNER CLASSES ====================

    /**
     * A custom-painted rounded button with anti-aliased rendering and hover effects.
     */
    public static class RoundedButton extends JButton {
        private Color baseColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color textColor;
        private final int radius;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private boolean hasBorderOnly = false;
        private Color borderColor;

        public RoundedButton(String text, Color bg, Color fg, int radius) {
            super(text);
            this.baseColor = bg;
            this.textColor = fg;
            this.radius = radius;
            this.hoverColor = brighten(bg, 30);
            this.pressedColor = darken(bg, 20);

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setFont(FONT_BUTTON);
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 40));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setBorderOnly(boolean borderOnly, Color borderColor) {
            this.hasBorderOnly = borderOnly;
            this.borderColor = borderColor;
        }

        public void setBaseColor(Color c) {
            this.baseColor = c;
            this.hoverColor = brighten(c, 30);
            this.pressedColor = darken(c, 20);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();

            if (hasBorderOnly) {
                if (isHovered) {
                    g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));
                }
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));
            } else {
                Color fillColor;
                if (!isEnabled()) {
                    fillColor = new Color(0x3a, 0x3a, 0x3a);
                } else if (isPressed) {
                    fillColor = pressedColor;
                } else if (isHovered) {
                    fillColor = hoverColor;
                } else {
                    fillColor = baseColor;
                }
                g2.setColor(fillColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));
            }

            // Draw text
            g2.setFont(getFont());
            if (!isEnabled()) {
                g2.setColor(TEXT_MUTED);
            } else if (hasBorderOnly && !isHovered) {
                g2.setColor(borderColor);
            } else {
                g2.setColor(textColor);
            }
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int textX = (w - fm.stringWidth(text)) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, textX, textY);

            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            return new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius).contains(x, y);
        }
    }

    /**
     * A text field with placeholder text support.
     */
    public static class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setColor(TEXT_MUTED);
                g2.setFont(getFont());
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left + 2, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
                g2.dispose();
            }
        }
    }

    /**
     * A panel that draws a rounded rectangle background with optional border.
     */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bgColor;
        private Color borderColor;

        public RoundedPanel(int radius, Color bg, Color border) {
            this.radius = radius;
            this.bgColor = bg;
            this.borderColor = border;
            setOpaque(false);
        }

        public void setBgColor(Color c) {
            this.bgColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * A gradient panel that paints a linear gradient from start to end color.
     */
    public static class GradientPanel extends JPanel {
        private final Color startColor;
        private final Color endColor;

        public GradientPanel(Color start, Color end) {
            this.startColor = start;
            this.endColor = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a primary gold-themed button with dark text.
     */
    public static JButton createPrimaryButton(String text) {
        RoundedButton btn = new RoundedButton(text, ACCENT_GOLD, BG_DARK, BUTTON_RADIUS * 2);
        btn.setPreferredSize(new Dimension(180, 42));
        return btn;
    }

    /**
     * Creates a secondary button — transparent with gold border, gold text.
     * On hover, fills with semi-transparent gold.
     */
    public static JButton createSecondaryButton(String text) {
        RoundedButton btn = new RoundedButton(text, BG_CARD, ACCENT_GOLD, BUTTON_RADIUS * 2);
        btn.setBorderOnly(true, ACCENT_GOLD);
        btn.setPreferredSize(new Dimension(180, 42));
        return btn;
    }

    /**
     * Creates a danger button — red themed.
     */
    public static JButton createDangerButton(String text) {
        RoundedButton btn = new RoundedButton(text, ACCENT_RED, Color.WHITE, BUTTON_RADIUS * 2);
        btn.setPreferredSize(new Dimension(180, 42));
        return btn;
    }

    /**
     * Creates a button with an emoji icon prefix.
     */
    public static JButton createIconButton(String emoji, String text) {
        RoundedButton btn = new RoundedButton(emoji + "  " + text, ACCENT_GOLD, BG_DARK, BUTTON_RADIUS * 2);
        btn.setPreferredSize(new Dimension(200, 42));
        return btn;
    }

    /**
     * Creates a styled dark text field with placeholder text.
     */
    public static JTextField createStyledTextField(String placeholder) {
        PlaceholderTextField tf = new PlaceholderTextField(placeholder);
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_GOLD);
        tf.setBackground(BG_INPUT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        tf.setPreferredSize(new Dimension(250, 40));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_GOLD, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                tf.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                tf.repaint();
            }
        });

        return tf;
    }

    /**
     * Creates a styled combo box with dark theme.
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBackground(BG_INPUT);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        cb.setPreferredSize(new Dimension(200, 40));
        cb.setFocusable(false);
        cb.setOpaque(true);

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_BODY);
                if (index == -1) {
                    setBackground(BG_INPUT);
                    setForeground(TEXT_PRIMARY);
                } else if (isSelected) {
                    setBackground(ACCENT_GOLD);
                    setForeground(BG_DARK);
                } else {
                    setBackground(BG_INPUT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });

        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowBtn = new JButton("▾");
                arrowBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                arrowBtn.setForeground(TEXT_SECONDARY);
                arrowBtn.setBackground(BG_INPUT);
                arrowBtn.setBorder(BorderFactory.createEmptyBorder());
                arrowBtn.setFocusPainted(false);
                return arrowBtn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_INPUT);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        return cb;
    }

    /**
     * Creates a rounded card panel with BG_CARD background and subtle border.
     */
    public static JPanel createCard() {
        RoundedPanel card = new RoundedPanel(CARD_RADIUS, BG_CARD, BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        return card;
    }

    /**
     * Creates a stat card for the dashboard.
     */
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        RoundedPanel card = new RoundedPanel(CARD_RADIUS, BG_CARD, BORDER);
        card.setLayout(new BorderLayout());

        // Accent strip at top
        JPanel accentStrip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        accentStrip.setPreferredSize(new Dimension(0, 4));
        accentStrip.setOpaque(false);
        card.add(accentStrip, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_STAT_NUMBER);
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(valueLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(titleLabel);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    /**
     * Creates a gradient panel.
     */
    public static JPanel createGradientPanel(Color start, Color end) {
        return new GradientPanel(start, end);
    }

    /**
     * Style a scroll pane with dark theme.
     */
    public static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBackground(BG_DARK);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);

        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER;
                this.trackColor = BG_DARK;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    public static Color brighten(Color c, int amount) {
        return new Color(
                Math.min(255, c.getRed() + amount),
                Math.min(255, c.getGreen() + amount),
                Math.min(255, c.getBlue() + amount)
        );
    }

    public static Color darken(Color c, int amount) {
        return new Color(
                Math.max(0, c.getRed() - amount),
                Math.max(0, c.getGreen() - amount),
                Math.max(0, c.getBlue() - amount)
        );
    }
}
