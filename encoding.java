import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Encoder {

    private static final String REFERENCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789()*+,-./";

    private static final Color BG          = new Color(15, 15, 23);
    private static final Color CARD        = new Color(24, 24, 36);
    private static final Color ACCENT      = new Color(99, 102, 241);
    private static final Color ACCENT_DARK = new Color(79, 82, 221);
    private static final Color FIELD_BG    = new Color(32, 32, 48);
    private static final Color BORDER_COL  = new Color(50, 50, 70);
    private static final Color TEXT        = new Color(240, 240, 255);
    private static final Color MUTED       = new Color(140, 140, 170);
    private static final Color SUCCESS     = new Color(52, 211, 153);
    private static final Color ERROR       = new Color(248, 113, 113);

    // ── Logic ────────────────────────────────────────────────────────────────────

    public String encode(String plainText, char offsetChar) {
        int offsetIndex = REFERENCE.indexOf(Character.toUpperCase(offsetChar));
        if (offsetIndex == -1) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(REFERENCE.charAt(offsetIndex));
        for (char c : plainText.toUpperCase().toCharArray()) {
            int idx = REFERENCE.indexOf(c);
            sb.append(idx == -1 ? c : REFERENCE.charAt((idx - offsetIndex + REFERENCE.length()) % REFERENCE.length()));
        }
        return sb.toString();
    }

    public String decode(String encodedText) {
        if (encodedText.isEmpty()) return null;
        int offsetIndex = REFERENCE.indexOf(encodedText.charAt(0));
        if (offsetIndex == -1) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < encodedText.length(); i++) {
            char c = encodedText.charAt(i);
            int idx = REFERENCE.indexOf(c);
            sb.append(idx == -1 ? c : REFERENCE.charAt((idx + offsetIndex) % REFERENCE.length()));
        }
        return sb.toString();
    }

    // ── Custom Components ─────────────────────────────────────────────────────────

    static JTextField styledField(String placeholder, boolean editable) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(MUTED);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_COL);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setEditable(editable);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        field.setFont(editable ? new Font("SansSerif", Font.PLAIN, 14) : new Font("Monospaced", Font.BOLD, 15));
        field.setForeground(editable ? TEXT : SUCCESS);
        field.setCaretColor(ACCENT);
        return field;
    }

    static JButton styledButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? ACCENT_DARK : ACCENT);
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
        return btn;
    }

    static JLabel label(String text, boolean muted) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(muted ? MUTED : TEXT);
        lbl.setFont(new Font("SansSerif", muted ? Font.PLAIN : Font.BOLD, muted ? 12 : 13));
        return lbl;
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
        JLabel     errorLabel  = label("", true);
        errorLabel.setForeground(ERROR);
        JButton btn = styledButton(isEncode ? "Encode  →" : "Decode  →");

        inputField.setPreferredSize(new Dimension(0, 46));
        offsetField.setPreferredSize(new Dimension(0, 46));
        resultField.setPreferredSize(new Dimension(0, 46));
        btn.setPreferredSize(new Dimension(0, 46));

        btn.addActionListener(e -> {
            errorLabel.setText("");
            String input = inputField.getText().trim();
            if (input.isEmpty()) { errorLabel.setText("⚠  Please enter some text."); return; }
            if (isEncode) {
                String offset = offsetField.getText().trim();
                if (offset.isEmpty()) { errorLabel.setText("⚠  Please enter an offset character."); return; }
                String result = encoder.encode(input, offset.charAt(0));
                if (result == null) { errorLabel.setText("⚠  Offset not in reference table."); return; }
                resultField.setText(result);
            } else {
                String result = encoder.decode(input);
                if (result == null) { errorLabel.setText("⚠  Invalid encoded text."); return; }
                resultField.setText(result);
            }
        });

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

        c.insets = new Insets(0, 0, 8, 0);
        form.add(btn, c);
        c.insets = new Insets(0, 0, 20, 0);
        form.add(errorLabel, c);
        c.insets = new Insets(0, 0, 6, 0);
        form.add(label("Result", false), c);
        c.insets = new Insets(0, 0, 0, 0);
        form.add(resultField, c);

        // Push everything up when window is tall
        GridBagConstraints spacer = new GridBagConstraints();
        spacer.weighty   = 1.0;
        spacer.gridwidth = GridBagConstraints.REMAINDER;
        spacer.fill      = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), spacer);

        return form;
    }

    // ── Main Window ───────────────────────────────────────────────────────────────

    static void buildUI(Encoder encoder) {
        JFrame frame = new JFrame("Encoder / Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(360, 480));
        frame.setSize(520, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true); // ← responsive: drag to any size

        // Root background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);

        // Centered wrapper — card stays centered and max 560px wide
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(36, 24, 36, 24));

        GridBagConstraints wc = new GridBagConstraints();
        wc.fill    = GridBagConstraints.BOTH;
        wc.weightx = 1.0;
        wc.weighty = 1.0;

        // Inner panel
        JPanel inner = new JPanel(new BorderLayout(0, 20));
        inner.setOpaque(false);
        inner.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("🔐  Encoder / Decoder");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT);

        JLabel subLabel = new JLabel("Shift cipher using a 44-character reference table");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLabel.setForeground(MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(subLabel);

        // Card with rounded corners
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COL);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);

        // Tab bar
        JPanel tabBar = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_COL);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
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

        // Tab styling + underline indicator
        for (JToggleButton tb : new JToggleButton[]{encTab, decTab}) {
            tb.setContentAreaFilled(false);
            tb.setBorderPainted(false);
            tb.setFocusPainted(false);
            tb.setFont(new Font("SansSerif", Font.BOLD, 13));
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tb.setPreferredSize(new Dimension(0, 44));
            tb.setOpaque(false);
        }

        Runnable updateTabs = () -> {
            encTab.setForeground(encTab.isSelected() ? ACCENT : MUTED);
            decTab.setForeground(decTab.isSelected() ? ACCENT : MUTED);
            tabBar.repaint();
        };
        updateTabs.run();

        // Draw underline on selected tab
        JPanel encTabWrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                if (encTab.isSelected()) {
                    g.setColor(ACCENT);
                    g.fillRect(0, getHeight() - 2, getWidth(), 2);
                }
            }
        };
        JPanel decTabWrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                if (decTab.isSelected()) {
                    g.setColor(ACCENT);
                    g.fillRect(0, getHeight() - 2, getWidth(), 2);
                }
            }
        };
        encTabWrap.setOpaque(false); encTabWrap.add(encTab);
        decTabWrap.setOpaque(false); decTabWrap.add(decTab);
        tabBar.add(encTabWrap);
        tabBar.add(decTabWrap);

        // Form switcher
        JPanel formArea = new JPanel(new CardLayout());
        formArea.setOpaque(false);
        formArea.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));
        formArea.add(encForm, "encode");
        formArea.add(decForm, "decode");

        encTab.addActionListener(e -> {
            ((CardLayout) formArea.getLayout()).show(formArea, "encode");
            updateTabs.run(); encTabWrap.repaint(); decTabWrap.repaint();
        });
        decTab.addActionListener(e -> {
            ((CardLayout) formArea.getLayout()).show(formArea, "decode");
            updateTabs.run(); encTabWrap.repaint(); decTabWrap.repaint();
        });

        cardPanel.add(tabBar,    BorderLayout.NORTH);
        cardPanel.add(formArea,  BorderLayout.CENTER);

        inner.add(header,    BorderLayout.NORTH);
        inner.add(cardPanel, BorderLayout.CENTER);

        wrapper.add(inner, wc);
        root.add(wrapper, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Encoder encoder = new Encoder();
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> buildUI(encoder));
    }
}