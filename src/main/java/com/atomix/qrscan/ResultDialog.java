package com.atomix.qrscan;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

final class ResultDialog extends JDialog {
    private static final String APP_NAME = "QR Scan";
    private static final Color BACKGROUND = new Color(30, 31, 34);
    private static final Color SURFACE = new Color(37, 37, 38);
    private static final Color INPUT_BACKGROUND = new Color(24, 24, 25);
    private static final Color OUTLINE = new Color(65, 65, 66);
    private static final Color TEXT = new Color(235, 235, 235);

    ResultDialog(MainFrame owner, String resultText) {
        super(owner, "Risultato QR Code", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 17));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(23, 23, 20, 23));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Risultato della scansione");
        title.setFont(new Font(Font.DIALOG, Font.BOLD, 19));
        title.setForeground(TEXT);
        JLabel description = new JLabel("Contenuto rilevato dal codice QR");
        description.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        description.setForeground(new Color(180, 180, 180));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(description);
        content.add(heading, BorderLayout.NORTH);

        JTextArea result = new JTextArea(resultText, 5, 38);
        result.setEditable(false);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setBackground(INPUT_BACKGROUND);
        result.setForeground(TEXT);
        result.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        result.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scrollPane = new JScrollPane(result);
        scrollPane.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        scrollPane.getViewport().setBackground(INPUT_BACKGROUND);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        buttons.setOpaque(false);
        if (isHttpUrl(resultText)) {
            JButton openUrl = new JButton("Apri link");
            styleButton(openUrl);
            openUrl.addActionListener(event -> openUrl(resultText));
            buttons.add(openUrl);
        }

        JButton copy = new JButton("Copia");
        styleButton(copy);
        copy.addActionListener(event -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(resultText), null);
                dispose();
            } catch (IllegalStateException exception) {
                JOptionPane.showMessageDialog(
                        this,
                        "Impossibile copiare il contenuto:\n\n" + exception.getMessage(),
                        APP_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        buttons.add(copy);

        JButton close = new JButton("Chiudi");
        styleButton(close);
        close.addActionListener(event -> dispose());
        buttons.add(close);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setMinimumSize(new java.awt.Dimension(460, getHeight()));
        setLocationRelativeTo(owner);
    }

    private static boolean isHttpUrl(String text) {
        try {
            String scheme = URI.create(text).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void openUrl(String text) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new UnsupportedOperationException("Il sistema non supporta l'apertura dei collegamenti.");
            }
            Desktop.getDesktop().browse(URI.create(text));
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Impossibile aprire il collegamento:\n\n" + exception.getMessage(),
                    APP_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        button.setBackground(SURFACE);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE),
                BorderFactory.createEmptyBorder(6, 11, 6, 11)));
    }
}
