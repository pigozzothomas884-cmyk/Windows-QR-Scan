package com.atomix.qrscan;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                System.err.println("Impossibile applicare il tema del sistema: " + exception.getMessage());
            }
            new MainFrame().setVisible(true);
        });
    }
}
