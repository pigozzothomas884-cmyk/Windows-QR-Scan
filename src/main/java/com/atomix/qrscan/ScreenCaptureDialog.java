package com.atomix.qrscan;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Dialog;
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

final class ScreenCaptureDialog extends JDialog {
    private final SelectionPanel selectionPanel = new SelectionPanel();
    private BufferedImage capturedImage;
    private AWTException captureException;

    ScreenCaptureDialog(MainFrame owner) {
        super(owner, "Seleziona area QR", Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setContentPane(selectionPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));
        setBounds(getVirtualScreenBounds());
        selectionPanel.installMouseHandlers();
        selectionPanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        selectionPanel.getActionMap().put("cancel", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispose();
            }
        });
    }

    BufferedImage capture() throws AWTException {
        setVisible(true);
        if (captureException != null) {
            throw captureException;
        }
        return capturedImage;
    }

    private static Rectangle getVirtualScreenBounds() {
        Rectangle bounds = new Rectangle();
        for (var device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            bounds = bounds.union(device.getDefaultConfiguration().getBounds());
        }
        return bounds;
    }

    private final class SelectionPanel extends JPanel {
        private java.awt.Point start;
        private java.awt.Point end;

        private SelectionPanel() {
            setOpaque(false);
        }

        private void installMouseHandlers() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    start = event.getPoint();
                    end = start;
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    end = event.getPoint();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    end = event.getPoint();
                    Rectangle selection = getSelection();
                    if (selection.width <= 10 || selection.height <= 10) {
                        dispose();
                        return;
                    }

                    Rectangle screenSelection = new Rectangle(
                            ScreenCaptureDialog.this.getX() + selection.x,
                            ScreenCaptureDialog.this.getY() + selection.y,
                            selection.width,
                            selection.height);
                    setVisible(false);
                    try {
                        capturedImage = new Robot().createScreenCapture(screenSelection);
                    } catch (AWTException exception) {
                        captureException = exception;
                    } finally {
                        dispose();
                    }
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        private Rectangle getSelection() {
            if (start == null || end == null) {
                return new Rectangle();
            }
            return new Rectangle(
                    Math.min(start.x, end.x),
                    Math.min(start.y, end.y),
                    Math.abs(end.x - start.x),
                    Math.abs(end.y - start.y));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setColor(new Color(0, 0, 0, 115));
                g.fillRect(0, 0, getWidth(), getHeight());

                Rectangle selection = getSelection();
                if (start != null) {
                    g.setComposite(AlphaComposite.Clear);
                    g.fill(selection);
                    g.setComposite(AlphaComposite.SrcOver);
                    g.setColor(Color.RED);
                    g.drawRect(selection.x, selection.y, selection.width, selection.height);
                } else {
                    g.setColor(Color.WHITE);
                    g.drawString("Trascina per selezionare il codice QR (Esc per annullare)", 20, 35);
                }
            } finally {
                g.dispose();
            }
        }
    }
}
