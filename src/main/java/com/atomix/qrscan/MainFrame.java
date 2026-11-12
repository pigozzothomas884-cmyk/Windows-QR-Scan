package com.atomix.qrscan;

import com.google.zxing.WriterException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class MainFrame extends JFrame {
    private static final Color BACKGROUND = new Color(30, 31, 34);
    private static final Color INSET = new Color(24, 24, 25);
    private static final Color OUTLINE = new Color(65, 65, 66);
    private static final Color TEXT = new Color(235, 235, 235);
    private static final String APP_NAME = "QR Scan";
    private static final String LOGO_RESOURCE = "/QRScanLogo.png";

    private final QrCodeService qrCodeService = new QrCodeService();
    private final JTextField input = new JTextField();
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JButton saveButton = new JButton("Salva QR in PNG");
    private BufferedImage currentQrImage;
    private TrayIcon trayIcon;

    MainFrame() {
        super(APP_NAME);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(620, 620);
        setMinimumSize(new Dimension(500, 500));
        setLocationRelativeTo(null);
        loadApplicationIcon();
        buildUi();
        installTrayIcon();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                exitApplication();
            }

            @Override
            public void windowIconified(java.awt.event.WindowEvent event) {
                if (trayIcon != null) {
                    setVisible(false);
                }
            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                updatePreviewIcon();
            }
        });
    }

    private void loadApplicationIcon() {
        try (var stream = MainFrame.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (stream == null) {
                System.err.println("Risorsa icona non trovata: " + LOGO_RESOURCE);
                return;
            }
            BufferedImage logo = ImageIO.read(stream);
            if (logo == null) {
                throw new IOException("Il file PNG non contiene un'immagine valida.");
            }
            setIconImage(logo);
        } catch (IOException exception) {
            System.err.println("Impossibile caricare il logo dell'applicazione: " + exception.getMessage());
        }
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createTitleBar(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel inputSection = new JPanel();
        inputSection.setOpaque(false);
        inputSection.setLayout(new BoxLayout(inputSection, BoxLayout.Y_AXIS));
        JLabel inputHeading = label("Testo o link", 13, Font.PLAIN, TEXT);
        inputHeading.setAlignmentX(0.0f);
        inputSection.add(inputHeading);
        inputSection.add(Box.createVerticalStrut(7));

        input.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        input.setForeground(TEXT);
        input.setCaretColor(TEXT);
        input.setSelectionColor(new Color(38, 79, 120));
        input.setBackground(INSET);
        input.setToolTipText("Scrivi un testo o un link per generare un codice QR");
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        input.setAlignmentX(0.0f);
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateQrPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateQrPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateQrPreview();
            }
        });
        inputSection.add(input);
        content.add(inputSection, BorderLayout.NORTH);

        JPanel previewSection = new JPanel(new BorderLayout(0, 8));
        previewSection.setOpaque(false);
        JLabel heading = label("Anteprima", 13, Font.PLAIN, TEXT);
        previewSection.add(heading, BorderLayout.NORTH);

        JPanel imageArea = new JPanel(new BorderLayout());
        imageArea.setBackground(INSET);
        imageArea.setBorder(BorderFactory.createLineBorder(OUTLINE));
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.setVerticalAlignment(SwingConstants.CENTER);
        imageArea.add(preview, BorderLayout.CENTER);
        previewSection.add(imageArea, BorderLayout.CENTER);
        content.add(previewSection, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);

        JPanel scanActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        scanActions.setOpaque(false);

        JButton screenScanButton = new JButton("Scansiona dallo schermo");
        screenScanButton.setToolTipText("Seleziona sullo schermo l'area che contiene il codice QR");
        screenScanButton.addActionListener(event -> startCapture());
        scanActions.add(screenScanButton);

        JButton scanImageButton = new JButton("Apri immagine...");
        scanImageButton.setToolTipText("Scansiona un codice QR da un file immagine");
        scanImageButton.addActionListener(event -> scanImage());
        scanActions.add(scanImageButton);
        actions.add(scanActions, BorderLayout.WEST);

        saveButton.setEnabled(false);
        saveButton.addActionListener(event -> saveQrCode());
        actions.add(saveButton, BorderLayout.EAST);
        content.add(actions, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(37, 37, 38));
        titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));

        JLabel title = label(APP_NAME, 12, Font.PLAIN, TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        titleBar.add(title, BorderLayout.CENTER);

        JPanel windowButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        windowButtons.setOpaque(false);
        JButton minimize = createWindowButton("−");
        minimize.setToolTipText("Riduci a icona");
        minimize.addActionListener(event -> setExtendedState(JFrame.ICONIFIED));
        windowButtons.add(minimize);

        JButton maximize = createWindowButton("□");
        maximize.setToolTipText("Ingrandisci o ripristina");
        maximize.addActionListener(event -> toggleMaximized());
        windowButtons.add(maximize);

        JButton close = createWindowButton("×");
        close.setToolTipText("Chiudi");
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                close.setBackground(new Color(196, 43, 28));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                close.setBackground(titleBar.getBackground());
            }
        });
        close.addActionListener(event -> exitApplication());
        windowButtons.add(close);
        titleBar.add(windowButtons, BorderLayout.EAST);

        MouseAdapter dragHandler = new MouseAdapter() {
            private Point dragOffset;

            @Override
            public void mousePressed(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    toggleMaximized();
                    dragOffset = null;
                    return;
                }
                dragOffset = SwingUtilities.convertPoint(
                        event.getComponent(),
                        event.getPoint(),
                        titleBar);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (dragOffset == null || (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
                    return;
                }
                Point screenPoint = event.getLocationOnScreen();
                setLocation(screenPoint.x - dragOffset.x, screenPoint.y - dragOffset.y);
            }
        };
        titleBar.addMouseListener(dragHandler);
        titleBar.addMouseMotionListener(dragHandler);
        title.addMouseListener(dragHandler);
        title.addMouseMotionListener(dragHandler);
        return titleBar;
    }

    private JButton createWindowButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
        button.setForeground(TEXT);
        button.setBackground(new Color(37, 37, 38));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(44, 32));
        return button;
    }

    private void toggleMaximized() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.DIALOG, style, size));
        label.setForeground(color);
        return label;
    }

    private void installTrayIcon() {
        if (!SystemTray.isSupported()) {
            return;
        }

        Image logo = getTrayImage();
        if (logo == null) {
            return;
        }

        try {
            PopupMenu menu = new PopupMenu();
            MenuItem scanImage = new MenuItem("Scansiona QR da immagine...");
            scanImage.addActionListener(event -> scanImage());
            menu.add(scanImage);

            MenuItem capture = new MenuItem("Cattura QR dallo schermo");
            capture.addActionListener(event -> startCapture());
            menu.add(capture);

            MenuItem open = new MenuItem("Apri QR Scan");
            open.addActionListener(event -> restoreWindow());
            menu.add(open);
            menu.addSeparator();

            MenuItem exit = new MenuItem("Esci");
            exit.addActionListener(event -> exitApplication());
            menu.add(exit);

            trayIcon = new TrayIcon(logo, APP_NAME, menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(event -> restoreWindow());
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception exception) {
            trayIcon = null;
            JOptionPane.showMessageDialog(
                    this,
                    "Impossibile creare l'icona nell'area di notifica:\n\n" + exception.getMessage(),
                    APP_NAME,
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private Image getTrayImage() {
        try (var stream = MainFrame.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (stream == null) {
                System.err.println("Risorsa logo non trovata: " + LOGO_RESOURCE);
                return null;
            }
            Image logo = ImageIO.read(stream);
            if (logo == null) {
                throw new IOException("Il file PNG non contiene un'immagine valida.");
            }
            return logo;
        } catch (IOException exception) {
            System.err.println("Impossibile caricare il logo dell'applicazione: " + exception.getMessage());
            return null;
        }
    }

    private void updateQrPreview() {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            currentQrImage = null;
            preview.setIcon(null);
            preview.setText("");
            saveButton.setEnabled(false);
            return;
        }

        try {
            currentQrImage = qrCodeService.generate(text, 400, 400);
            preview.setText(null);
            updatePreviewIcon();
            saveButton.setEnabled(true);
        } catch (WriterException exception) {
            currentQrImage = null;
            preview.setIcon(null);
            preview.setText("Impossibile generare il codice QR: " + exception.getMessage());
            saveButton.setEnabled(false);
        }
    }

    private void updatePreviewIcon() {
        if (currentQrImage == null || preview.getWidth() <= 0 || preview.getHeight() <= 0) {
            return;
        }
        int width = Math.max(1, preview.getWidth() - 24);
        int height = Math.max(1, preview.getHeight() - 24);
        double scale = Math.min((double) width / currentQrImage.getWidth(),
                (double) height / currentQrImage.getHeight());
        int scaledWidth = Math.max(1, (int) (currentQrImage.getWidth() * scale));
        int scaledHeight = Math.max(1, (int) (currentQrImage.getHeight() * scale));
        preview.setIcon(new ImageIcon(currentQrImage.getScaledInstance(
                scaledWidth,
                scaledHeight,
                Image.SCALE_SMOOTH)));
    }

    private void scanImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleziona un'immagine con un codice QR");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Immagini (*.png, *.jpg, *.jpeg, *.bmp, *.gif)",
                "png",
                "jpg",
                "jpeg",
                "bmp",
                "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(selectedFile);
            if (image == null) {
                throw new IOException("Il formato del file non è supportato.");
            }
            String text = qrCodeService.decode(image);
            if (text == null || text.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nessun codice QR rilevato nell'immagine selezionata.",
                        APP_NAME,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                new ResultDialog(this, text).setVisible(true);
            }
        } catch (IOException exception) {
            showError("Impossibile leggere l'immagine selezionata", exception);
        } catch (RuntimeException exception) {
            showError("Impossibile decodificare il codice QR nell'immagine", exception);
        }
    }

    private void saveQrCode() {
        if (currentQrImage == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salva QR Code");
        chooser.setFileFilter(new FileNameExtensionFilter("Immagine PNG (*.png)", "png"));
        chooser.setSelectedFile(new File("QRCode.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
        }
        try {
            if (!ImageIO.write(currentQrImage, "png", selectedFile)) {
                throw new IOException("Non è disponibile un encoder PNG.");
            }
            JOptionPane.showMessageDialog(
                    this,
                    "QR Code salvato con successo.",
                    APP_NAME,
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showError("Impossibile salvare il codice QR", exception);
        }
    }

    private void startCapture() {
        setVisible(false);
        try {
            BufferedImage capturedImage = new ScreenCaptureDialog(this).capture();
            if (capturedImage == null) {
                return;
            }
            String text = qrCodeService.decode(capturedImage);
            if (text == null || text.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nessun codice QR rilevato nell'area selezionata.",
                        APP_NAME,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                new ResultDialog(this, text).setVisible(true);
            }
        } catch (Exception exception) {
            showError("Impossibile catturare o leggere l'area selezionata", exception);
        } finally {
            setVisible(true);
            toFront();
        }
    }

    private void restoreWindow() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
        toFront();
        requestFocus();
    }

    private void exitApplication() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        dispose();
        System.exit(0);
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(
                this,
                message + ":\n\n" + exception.getMessage(),
                APP_NAME,
                JOptionPane.ERROR_MESSAGE);
    }
}
