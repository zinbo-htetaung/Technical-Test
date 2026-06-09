import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Encoder {

    private static final String REFERENCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789()*+,-./";

    // ── Theme ─────────────────────────────────────────────────────────────────────

    static boolean isDark = true;

    static Color bg()         { return isDark ? new Color(15,15,23)    : new Color(245,245,250); }
    static Color card()       { return isDark ? new Color(24,24,36)    : Color.WHITE; }
    static Color accent()     { return new Color(99,102,241); }
    static Color accentDark() { return new Color(79,82,221); }
    static Color fieldBg()    { return isDark ? new Color(32,32,48)    : new Color(235,235,245); }
    static Color border()     { return isDark ? new Color(50,50,70)    : new Color(210,210,225); }
    static Color text()       { return isDark ? new Color(240,240,255) : new Color(20,20,40); }
    static Color muted()      { return isDark ? new Color(140,140,170) : new Color(130,130,160); }
    static Color success()    { return new Color(52,211,153); }
    static Color error()      { return new Color(248,113,113); }

    // ── Logic ────────────────────────────────────────────────────────────────────

    public String encode(String plainText, char offsetChar) {
        int oi = REFERENCE.indexOf(Character.toUpperCase(offsetChar));
        if (oi == -1) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(REFERENCE.charAt(oi));
        for (char c : plainText.toUpperCase().toCharArray()) {
            int idx = REFERENCE.indexOf(c);
            sb.append(idx == -1 ? c : REFERENCE.charAt((idx - oi + REFERENCE.length()) % REFERENCE.length()));
        }
        return sb.toString();
    }

    public String decode(String encodedText) {
        if (encodedText.isEmpty()) return null;
        int oi = REFERENCE.indexOf(encodedText.charAt(0));
        if (oi == -1) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < encodedText.length(); i++) {
            char c = encodedText.charAt(i);
            int idx = REFERENCE.indexOf(c);
            sb.append(idx == -1 ? c : REFERENCE.charAt((idx + oi) % REFERENCE.length()));
        }
        return sb.toString();
    }

    // ── Custom Components ─────────────────────────────────────────────────────────

    static JTextField styledField(String placeholder, boolean editable) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fieldBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(muted());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? accent() : border());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setEditable(editable);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        field.setFont(editable ? new Font("SansSerif", Font.PLAIN, 14) : new Font("Monospaced", Font.BOLD, 15));
        field.setForeground(editable ? text() : success());
        field.setCaretColor(accent());
        field.setPreferredSize(new Dimension(0, 46));
        field.setMinimumSize(new Dimension(0, 46));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return field;
    }

    static JButton styledButton(String label) {
        JButton btn = new JButton(label) {
            private boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? accentDark() : accent());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setMinimumSize(new Dimension(0, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return btn;
    }

    static JLabel label(String text, boolean muted) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(muted ? muted() : text());
        lbl.setFont(new Font("SansSerif", muted ? Font.PLAIN : Font.BOLD, muted ? 12 : 13));
        return lbl;
    }

    // ── Theme Toggle Button ───────────────────────────────────────────────────────

    static JButton themeToggleBtn() {
        JButton btn = new JButton() {
            private boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? border() : fieldBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setFont(new Font("Apple Color Emoji", Font.PLAIN, 18));
                String icon = isDark ? "☀️" : "🌙";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon,
                    (getWidth()  - fm.stringWidth(icon)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setMinimumSize(new Dimension(40, 40));
        btn.setMaximumSize(new Dimension(40, 40));
        return btn;
    }

    // ── Form Builder ──────────────────────────────────────────────────────────────

    static JPanel buildForm(Encoder encoder, boolean isEncode) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.weightx   = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        JTextField inputField  = styledField(isEncode ? "e.g.  HELLO WORLD" : "e.g.  BGDKKN VNQKC", true);
        JTextField offsetField = styledField("e.g.  B", true);
        JTextField resultField = styledField("", false);

        // Fixed height error label — never collapses
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(error());
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setPreferredSize(new Dimension(0, 20));
        errorLabel.setMinimumSize(new Dimension(0, 20));
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JButton btn = styledButton(isEncode ? "Encode  →" : "Decode  →");

        ActionListener submitAction = e -> {
            errorLabel.setText(" ");
            String input = inputField.getText().trim();
            if (input.isEmpty()) { errorLabel.setText("⚠  Please enter some text."); return; }

            if (isEncode) {
                String offset = offsetField.getText().trim();
                if (offset.isEmpty()) { errorLabel.setText("⚠  Please enter an offset character."); return; }
                if (offset.length() > 1) { errorLabel.setText("⚠  Offset must be a single character only."); offsetField.requestFocus(); return; }
                String result = encoder.encode(input, offset.charAt(0));
                if (result == null) { errorLabel.setText("⚠  Offset not in reference table."); return; }
                resultField.setText(result);
            } else {
                String result = encoder.decode(input);
                if (result == null) { errorLabel.setText("⚠  Invalid encoded text."); return; }
                resultField.setText(result);
            }
        };

        btn.addActionListener(submitAction);
        inputField.addActionListener(submitAction);
        if (isEncode) offsetField.addActionListener(submitAction);

        c.insets = new Insets(0, 0, 6, 0);
        form.add(label(isEncode ? "Text to Encode" : "Encoded Text", false), c);
        c.insets = new Insets(0, 0, 16, 0);
        form.add(inputField, c);

        if (isEncode) {
            c.insets = new Insets(0, 0, 6, 0);
            form.add(label("Offset Character", false), c);
            c.insets = new Insets(0, 0, 4, 0);
            form.add(offsetField, c);
            c.insets = new Insets(0, 0, 20, 0);
            form.add(label("Any single character from the reference table (A–Z, 0–9, symbols)", true), c);
        } else {
            c.insets = new Insets(0, 0, 20, 0);
            form.add(label("Offset is read automatically from the first character", true), c);
        }

        c.insets = new Insets(0, 0, 4, 0);
        form.add(btn, c);
        c.insets = new Insets(0, 0, 16, 0);
        form.add(errorLabel, c);
        c.insets = new Insets(0, 0, 6, 0);
        form.add(label("Result", false), c);
        c.insets = new Insets(0, 0, 0, 0);
        form.add(resultField, c);

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.weighty = 1.0;
        spacer.gridwidth = GridBagConstraints.REMAINDER;
        spacer.fill = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), spacer);

        return form;
    }

    // ── Main Window ───────────────────────────────────────────────────────────────

    static void buildUI(Encoder encoder) {
        JFrame frame = new JFrame("Encoder / Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(380, 500));
        frame.setSize(520, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        Runnable[] rebuild = new Runnable[1];

        rebuild[0] = () -> {
            frame.getContentPane().removeAll();

            JPanel root = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(bg()); g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            root.setOpaque(false);

            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.setBorder(BorderFactory.createEmptyBorder(32, 24, 32, 24));

            GridBagConstraints wc = new GridBagConstraints();
            wc.fill = GridBagConstraints.BOTH;
            wc.weightx = wc.weighty = 1.0;

            JPanel inner = new JPanel(new BorderLayout(0, 20));
            inner.setOpaque(false);

            // Header
            JPanel headerRow = new JPanel(new BorderLayout());
            headerRow.setOpaque(false);

            JPanel titles = new JPanel();
            titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
            titles.setOpaque(false);
            JLabel titleLabel = new JLabel("🔐  Encoder / Decoder");
            titleLabel.setFont(new Font("Apple Color Emoji", Font.BOLD, 22));
            titleLabel.setForeground(text());
            JLabel subLabel = new JLabel("Shift cipher using a 44-character reference table");
            subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            subLabel.setForeground(muted());
            titles.add(titleLabel);
            titles.add(Box.createVerticalStrut(4));
            titles.add(subLabel);

            JButton toggleBtn = themeToggleBtn();
            toggleBtn.addActionListener(e -> { isDark = !isDark; rebuild[0].run(); });

            headerRow.add(titles,    BorderLayout.CENTER);
            headerRow.add(toggleBtn, BorderLayout.EAST);

            // Card
            JPanel cardPanel = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(card());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(border());
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                    g2.dispose();
                }
            };
            cardPanel.setOpaque(false);

            // Tab bar
            JPanel tabBar = new JPanel(new GridLayout(1, 2)) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(border()); g.fillRect(0, getHeight() - 1, getWidth(), 1);
                }
            };
            tabBar.setOpaque(false);

            JToggleButton encTab = new JToggleButton("Encode");
            JToggleButton decTab = new JToggleButton("Decode");
            ButtonGroup group = new ButtonGroup();
            group.add(encTab); group.add(decTab);
            encTab.setSelected(true);

            JPanel encForm = buildForm(encoder, true);
            JPanel decForm = buildForm(encoder, false);

            for (JToggleButton tb : new JToggleButton[]{encTab, decTab}) {
                tb.setContentAreaFilled(false); tb.setBorderPainted(false);
                tb.setFocusPainted(false);
                tb.setFont(new Font("SansSerif", Font.BOLD, 13));
                tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tb.setPreferredSize(new Dimension(0, 44));
                tb.setOpaque(false);
            }

            JPanel encTabWrap = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    if (encTab.isSelected()) { g.setColor(accent()); g.fillRect(0, getHeight()-2, getWidth(), 2); }
                }
            };
            JPanel decTabWrap = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    if (decTab.isSelected()) { g.setColor(accent()); g.fillRect(0, getHeight()-2, getWidth(), 2); }
                }
            };
            encTabWrap.setOpaque(false); encTabWrap.add(encTab);
            decTabWrap.setOpaque(false); decTabWrap.add(decTab);
            tabBar.add(encTabWrap); tabBar.add(decTabWrap);

            Runnable updateTabs = () -> {
                encTab.setForeground(encTab.isSelected() ? accent() : muted());
                decTab.setForeground(decTab.isSelected() ? accent() : muted());
                encTabWrap.repaint(); decTabWrap.repaint();
            };
            updateTabs.run();

            JPanel formArea = new JPanel(new CardLayout());
            formArea.setOpaque(false);
            formArea.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));
            formArea.add(encForm, "encode");
            formArea.add(decForm, "decode");

            encTab.addActionListener(e -> { ((CardLayout) formArea.getLayout()).show(formArea, "encode"); updateTabs.run(); });
            decTab.addActionListener(e -> { ((CardLayout) formArea.getLayout()).show(formArea, "decode"); updateTabs.run(); });

            cardPanel.add(tabBar,   BorderLayout.NORTH);
            cardPanel.add(formArea, BorderLayout.CENTER);

            inner.add(headerRow, BorderLayout.NORTH);
            inner.add(cardPanel, BorderLayout.CENTER);

            wrapper.add(inner, wc);
            root.add(wrapper, BorderLayout.CENTER);

            frame.setContentPane(root);
            frame.revalidate();
            frame.repaint();
        };

        String theme = UIManager.getLookAndFeelDefaults().getString("ColorScheme");
        isDark = theme == null || !theme.equalsIgnoreCase("light");

        rebuild[0].run();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Encoder encoder = new Encoder();
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> buildUI(encoder));
    }
}