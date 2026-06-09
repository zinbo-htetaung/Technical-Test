import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

// ── 1. ThemeManager — Encapsulation ──────────────────────────────────────────
/**
 * Manages application theme colors.
 * Demonstrates ENCAPSULATION — isDark state is private,
 * only accessible and modifiable through public methods.
 */
class ThemeManager {
    private boolean isDark;

    public ThemeManager() {
        // Auto-detect system theme on startup
        String scheme = UIManager.getLookAndFeelDefaults().getString("ColorScheme");
        this.isDark = scheme == null || !scheme.equalsIgnoreCase("light");
    }

    public boolean isDark()     { return isDark; }
    public void toggle()        { isDark = !isDark; }

    public Color bg()           { return isDark ? new Color(15,15,23)    : new Color(245,245,250); }
    public Color card()         { return isDark ? new Color(24,24,36)    : Color.WHITE; }
    public Color accent()       { return new Color(99,102,241); }
    public Color accentDark()   { return new Color(79,82,221); }
    public Color fieldBg()      { return isDark ? new Color(32,32,48)    : new Color(235,235,245); }
    public Color border()       { return isDark ? new Color(50,50,70)    : new Color(210,210,225); }
    public Color text()         { return isDark ? new Color(240,240,255) : new Color(20,20,40); }
    public Color muted()        { return isDark ? new Color(140,140,170) : new Color(130,130,160); }
    public Color success()      { return new Color(52,211,153); }
    public Color error()        { return new Color(248,113,113); }
}


// ── 2. CipherEngine — Encapsulation ──────────────────────────────────────────
/**
 * Handles all encoding and decoding logic.
 * Demonstrates ENCAPSULATION — reference table is private and immutable,
 * only exposed through controlled public methods.
 */
class CipherEngine {

    /** The 44-character reference table: A-Z, 0-9, and symbols ( ) * + , - . / */
    private static final String REFERENCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789()*+,-./";

    /**
     * Encodes plaintext using the shift cipher.
     * Each character is shifted left by the offset character's index.
     * The offset character is prepended to the result for later decoding.
     *
     * @param plainText  text to encode (case-insensitive)
     * @param offsetChar shift offset — must be in the reference table
     * @return encoded string, or null if offsetChar is invalid
     */
    public String encode(String plainText, char offsetChar) {
        int oi = REFERENCE.indexOf(Character.toUpperCase(offsetChar));
        if (oi == -1) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(REFERENCE.charAt(oi)); // prepend offset as first character
        for (char c : plainText.toUpperCase().toCharArray()) {
            int idx = REFERENCE.indexOf(c);
            sb.append(idx == -1 ? c : REFERENCE.charAt((idx - oi + REFERENCE.length()) % REFERENCE.length()));
        }
        return sb.toString();
    }

    /**
     * Decodes an encoded string back to its original plaintext.
     * The first character of the encoded string is read as the offset.
     *
     * @param encodedText encoded string (first char must be the offset)
     * @return decoded plaintext, or null if input is invalid
     */
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
}


// ── 3. BaseForm — Abstraction + Inheritance ───────────────────────────────────
/**
 * Abstract base class for the encode and decode forms.
 * Demonstrates ABSTRACTION — defines the common form structure and layout,
 * but leaves handleSubmit() for subclasses to implement.
 * Demonstrates INHERITANCE — EncodeForm and DecodeForm extend this class.
 */
abstract class BaseForm extends JPanel {

    protected ThemeManager theme;
    protected JTextField   inputField;
    protected JTextField   resultField;
    protected JLabel       errorLabel;
    protected JButton      submitBtn;

    public BaseForm(ThemeManager theme) {
        this.theme = theme;
        setOpaque(false);
        setLayout(new GridBagLayout());
        buildLayout();
    }

    /**
     * Builds the common form layout shared by both encode and decode forms.
     * Subclasses add their own extra fields (e.g. offset) via addExtraFields().
     */
    private void buildLayout() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.weightx   = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        inputField  = createField(getInputPlaceholder(), true);
        resultField = createField("", false);
        submitBtn   = createButton(getButtonLabel());

        // Fixed-height error label — prevents layout jumping
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(theme.error());
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setPreferredSize(new Dimension(0, 20));
        errorLabel.setMinimumSize(new Dimension(0, 20));
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // Input field row
        c.insets = new Insets(0, 0, 6, 0);
        add(createLabel(getInputLabel(), false), c);
        c.insets = new Insets(0, 0, 16, 0);
        add(inputField, c);

        // Extra fields (e.g. offset — only in EncodeForm)
        addExtraFields(c);

        // Submit button + error + result
        c.insets = new Insets(0, 0, 4, 0);  add(submitBtn,   c);
        c.insets = new Insets(0, 0, 16, 0); add(errorLabel,  c);
        c.insets = new Insets(0, 0, 6, 0);  add(createLabel("Result", false), c);
        c.insets = new Insets(0, 0, 0, 0);  add(resultField, c);

        // Vertical spacer — pushes content to top when window is tall
        GridBagConstraints spacer = new GridBagConstraints();
        spacer.weighty   = 1.0;
        spacer.gridwidth = GridBagConstraints.REMAINDER;
        spacer.fill      = GridBagConstraints.VERTICAL;
        add(Box.createVerticalGlue(), spacer);

        // Attach submit to button and Enter key on input
        submitBtn.addActionListener(e -> handleSubmit());
        inputField.addActionListener(e -> handleSubmit());
    }

    /**
     * Abstract method — each subclass defines its own submit logic.
     * Demonstrates POLYMORPHISM — same method name, different behavior.
     */
    protected abstract void handleSubmit();

    /** @return placeholder text for the main input field */
    protected abstract String getInputPlaceholder();

    /** @return label text above the main input field */
    protected abstract String getInputLabel();

    /** @return label text on the submit button */
    protected abstract String getButtonLabel();

    /**
     * Hook for subclasses to inject extra fields into the layout.
     * EncodeForm uses this to add the offset character field.
     * DecodeForm adds a hint label instead.
     */
    protected abstract void addExtraFields(GridBagConstraints c);

    /** Clears error and result fields. */
    protected void clearState() {
        errorLabel.setText(" ");
        resultField.setText("");
    }

    /** Shows an inline error message below the submit button. */
    protected void showError(String msg) {
        errorLabel.setText(msg);
    }

    /** Shows the result in the result field. */
    protected void showResult(String result) {
        resultField.setText(result);
    }

    // ── Component factories ───────────────────────────────────────────────────

    protected JTextField createField(String placeholder, boolean editable) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.fieldBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(theme.muted());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? theme.accent() : theme.border());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setEditable(editable);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        field.setFont(editable ? new Font("SansSerif", Font.PLAIN, 14) : new Font("Monospaced", Font.BOLD, 15));
        field.setForeground(editable ? theme.text() : theme.success());
        field.setCaretColor(theme.accent());
        field.setPreferredSize(new Dimension(0, 46));
        field.setMinimumSize(new Dimension(0, 46));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return field;
    }

    protected JButton createButton(String label) {
        JButton btn = new JButton(label) {
            private boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? theme.accentDark() : theme.accent());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
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

    protected JLabel createLabel(String text, boolean muted) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(muted ? theme.muted() : theme.text());
        lbl.setFont(new Font("SansSerif", muted ? Font.PLAIN : Font.BOLD, muted ? 12 : 13));
        return lbl;
    }
}


// ── 4. EncodeForm — Inheritance + Polymorphism ────────────────────────────────
/**
 * Encode form — extends BaseForm.
 * Demonstrates INHERITANCE — reuses all layout and component logic from BaseForm.
 * Demonstrates POLYMORPHISM — overrides handleSubmit() with encode-specific logic.
 */
class EncodeForm extends BaseForm {

    private final CipherEngine engine;
    private JTextField offsetField;

    public EncodeForm(ThemeManager theme, CipherEngine engine) {
        super(theme);
        this.engine = engine;
    }

    @Override protected String getInputPlaceholder() { return "e.g.  HELLO WORLD"; }
    @Override protected String getInputLabel()       { return "Text to Encode"; }
    @Override protected String getButtonLabel()      { return "Encode  →"; }

    @Override
    protected void addExtraFields(GridBagConstraints c) {
        offsetField = createField("e.g.  B", true);
        offsetField.addActionListener(e -> handleSubmit()); // Enter key on offset field

        c.insets = new Insets(0, 0, 6, 0);
        add(createLabel("Offset Character", false), c);
        c.insets = new Insets(0, 0, 4, 0);
        add(offsetField, c);
        c.insets = new Insets(0, 0, 20, 0);
        add(createLabel("Any single character from the reference table (A–Z, 0–9, symbols)", true), c);
    }

    /**
     * Encode-specific submit logic.
     * Validates input and offset, then calls CipherEngine.encode().
     * Demonstrates POLYMORPHISM — same method name as DecodeForm.handleSubmit(),
     * completely different behaviour.
     */
    @Override
    protected void handleSubmit() {
        clearState();
        String input  = inputField.getText().trim();
        String offset = offsetField.getText().trim();

        if (input.isEmpty())  { showError("⚠  Please enter some text.");           return; }
        if (offset.isEmpty()) { showError("⚠  Please enter an offset character."); return; }
        if (offset.length() > 1) {
            showError("⚠  Offset must be a single character only.");
            offsetField.requestFocus();
            return;
        }

        String result = engine.encode(input, offset.charAt(0));
        if (result == null) { showError("⚠  Offset not in reference table."); return; }
        showResult(result);
    }
}


// ── 5. DecodeForm — Inheritance + Polymorphism ────────────────────────────────
/**
 * Decode form — extends BaseForm.
 * Demonstrates INHERITANCE — reuses all layout and component logic from BaseForm.
 * Demonstrates POLYMORPHISM — overrides handleSubmit() with decode-specific logic.
 */
class DecodeForm extends BaseForm {

    private final CipherEngine engine;

    public DecodeForm(ThemeManager theme, CipherEngine engine) {
        super(theme);
        this.engine = engine;
    }

    @Override protected String getInputPlaceholder() { return "e.g.  BGDKKN VNQKC"; }
    @Override protected String getInputLabel()       { return "Encoded Text"; }
    @Override protected String getButtonLabel()      { return "Decode  →"; }

    @Override
    protected void addExtraFields(GridBagConstraints c) {
        // No offset field needed — offset is read from first character automatically
        c.insets = new Insets(0, 0, 20, 0);
        add(createLabel("Offset is read automatically from the first character", true), c);
    }

    /**
     * Decode-specific submit logic.
     * Validates input then calls CipherEngine.decode().
     * Demonstrates POLYMORPHISM — same method name as EncodeForm.handleSubmit(),
     * completely different behaviour.
     */
    @Override
    protected void handleSubmit() {
        clearState();
        String input = inputField.getText().trim();

        if (input.isEmpty()) { showError("⚠  Please enter some text."); return; }

        String result = engine.decode(input);
        if (result == null) { showError("⚠  Invalid encoded text."); return; }
        showResult(result);
    }
}


// ── 6. MainWindow — Assembles everything ─────────────────────────────────────
/**
 * Builds and manages the main application window.
 * Wires together ThemeManager, CipherEngine, EncodeForm and DecodeForm.
 */
class MainWindow {

    private final JFrame       frame;
    private final ThemeManager theme;
    private final CipherEngine engine;

    public MainWindow() {
        this.frame  = new JFrame("Encoder / Decoder");
        this.theme  = new ThemeManager();
        this.engine = new CipherEngine();
    }

    public void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(380, 500));
        frame.setSize(520, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        rebuild();
        frame.setVisible(true);
    }

    /** Rebuilds the entire UI — called on startup and on theme toggle. */
    private void rebuild() {
        frame.getContentPane().removeAll();

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(theme.bg());
                g.fillRect(0, 0, getWidth(), getHeight());
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
        inner.add(buildHeader(), BorderLayout.NORTH);
        inner.add(buildCard(),   BorderLayout.CENTER);

        wrapper.add(inner, wc);
        root.add(wrapper, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel buildHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel title = new JLabel("🔐  Encoder / Decoder");
        title.setFont(new Font("Apple Color Emoji", Font.BOLD, 22));
        title.setForeground(theme.text());

        JLabel sub = new JLabel("Shift cipher using a 44-character reference table");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(theme.muted());

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(sub);

        JButton toggleBtn = buildThemeToggle();
        row.add(titles,    BorderLayout.CENTER);
        row.add(toggleBtn, BorderLayout.EAST);
        return row;
    }

    private JButton buildThemeToggle() {
        JButton btn = new JButton() {
            private boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? theme.border() : theme.fieldBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setFont(new Font("Apple Color Emoji", Font.PLAIN, 18));
                String icon = theme.isDark() ? "☀️" : "🌙";
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
        btn.addActionListener(e -> { theme.toggle(); rebuild(); });
        return btn;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.card());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(theme.border());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        JToggleButton encTab = new JToggleButton("Encode");
        JToggleButton decTab = new JToggleButton("Decode");
        new ButtonGroup() {{ add(encTab); add(decTab); }};
        encTab.setSelected(true);

        EncodeForm encForm = new EncodeForm(theme, engine);
        DecodeForm decForm = new DecodeForm(theme, engine);

        for (JToggleButton tb : new JToggleButton[]{encTab, decTab}) {
            tb.setContentAreaFilled(false); tb.setBorderPainted(false);
            tb.setFocusPainted(false);
            tb.setFont(new Font("SansSerif", Font.BOLD, 13));
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tb.setPreferredSize(new Dimension(0, 44));
            tb.setOpaque(false);
        }

        JPanel encWrap = tabWrap(encTab); JPanel decWrap = tabWrap(decTab);

        JPanel tabBar = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(theme.border()); g.fillRect(0, getHeight()-1, getWidth(), 1);
            }
        };
        tabBar.setOpaque(false);
        tabBar.add(encWrap); tabBar.add(decWrap);

        JPanel formArea = new JPanel(new CardLayout());
        formArea.setOpaque(false);
        formArea.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));
        formArea.add(encForm, "encode");
        formArea.add(decForm, "decode");

        Runnable updateTabs = () -> {
            encTab.setForeground(encTab.isSelected() ? theme.accent() : theme.muted());
            decTab.setForeground(decTab.isSelected() ? theme.accent() : theme.muted());
            encWrap.repaint(); decWrap.repaint();
        };
        updateTabs.run();

        encTab.addActionListener(e -> { ((CardLayout) formArea.getLayout()).show(formArea, "encode"); updateTabs.run(); });
        decTab.addActionListener(e -> { ((CardLayout) formArea.getLayout()).show(formArea, "decode"); updateTabs.run(); });

        card.add(tabBar,   BorderLayout.NORTH);
        card.add(formArea, BorderLayout.CENTER);
        return card;
    }

    private JPanel tabWrap(JToggleButton tb) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                if (tb.isSelected()) { g.setColor(theme.accent()); g.fillRect(0, getHeight()-2, getWidth(), 2); }
            }
        };
        p.setOpaque(false);
        p.add(tb);
        return p;
    }
}


// ── 7. Encoder — Entry Point ──────────────────────────────────────────────────
/**
 * Application entry point.
 * Launches the MainWindow on the Event Dispatch Thread.
 */
public class Encoder {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainWindow().show());
    }
}