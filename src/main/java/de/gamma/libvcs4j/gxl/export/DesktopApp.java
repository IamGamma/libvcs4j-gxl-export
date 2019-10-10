package de.gamma.libvcs4j.gxl.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class DesktopApp {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DesktopApp().showGui();
            }
        });
    }

    private static Component spacingH(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    private static Component spacingW(int width) {
        return Box.createRigidArea(new Dimension(width, 0));
    }

    private final Logger logger = LoggerFactory.getLogger(DesktopApp.class);

    private JTextField textFieldRepo = new JTextField();
    private JTextField textFieldMaxRevision = new JTextField("0");
    private JProgressBar progressBar = new JProgressBar();
    private JLabel labelInfo = new JLabel("Info:", SwingConstants.LEFT);
    private JButton button = new JButton("Daten exportieren");
    private boolean taskRunning = false;
    private Worker worker;

    private void showGui() {
        JFrame frame = new JFrame("Gxl Export");//creating instance of JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT));

        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(600, 180));

        var labelRepo = new JLabel("Repository Adresse (https clone Adresse):");
        labelRepo.setAlignmentX(Component.LEFT_ALIGNMENT);

        var textFieldRepo = new JTextField();
        textFieldRepo.setAlignmentX(Component.LEFT_ALIGNMENT);

        var labelMaxRevision = new JLabel("Anzahl an maximaler Revisionen (0 für alle):", SwingConstants.LEFT);
        labelMaxRevision.setAlignmentX(Component.LEFT_ALIGNMENT);

        var textFieldMaxRevision = new JTextField("0");
        textFieldMaxRevision.setAlignmentX(Component.LEFT_ALIGNMENT);

        labelInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        var panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
        panelButton.setAlignmentX(Component.LEFT_ALIGNMENT);


        button.addActionListener(event -> {
            onButtonClick(textFieldRepo.getText(), textFieldMaxRevision.getText());
        });

        Arrays.asList(
                progressBar,
                spacingW(10),
                button
        ).forEach(panelButton::add);
        Arrays.asList(
                labelRepo,
                spacingH(2),
                textFieldRepo,
                spacingH(10),
                labelMaxRevision,
                spacingH(2),
                textFieldMaxRevision,
                spacingH(10),
                labelInfo,
                spacingH(5),
                new JSeparator(SwingConstants.HORIZONTAL),
                spacingH(5),
                panelButton
        ).forEach(panel::add);
        frame.add(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);//making the frame visible
    }

    private void setInfo(String text) {
        labelInfo.setText("Info: " + text);
    }

    private void onButtonClick(String fieldRepo, String fieldMaxRevisions) {
        if (taskRunning) {
            taskRunning = false;
            /* TODO loading is not canceled
            if (worker != null) {
                worker.cancel(true);
                worker = null;
            }
            */
        } else {
            try {
                var maxRevisions = Integer.parseInt(fieldMaxRevisions);
                worker = new Worker(fieldRepo, maxRevisions);
                worker.addPropertyChangeListener(event -> {
                    if ("progress".equals(event.getPropertyName()))
                        progressBar.setValue((Integer) event.getNewValue());
                });
                worker.execute();
                logger.info(String.format("Exportiere %s Datensätze von %s.", maxRevisions, fieldRepo));
                setInfo("Lade Daten, das kann ein Moment dauern");
                taskRunning = true;
            }catch (NumberFormatException e) {
                logger.info("Anzahl der maximalen Revisionen ist keine gültige Zahl.");
                setInfo("Anzahl der maximalen Revisionen ist keine gültige Zahl.");
            }
        }
        onTaskChangeState();
    }

    private void onTaskChangeState() {
        //textFieldRepo.setEnabled(!taskRunning);
        //textFieldMaxRevision.setEnabled(!taskRunning);
        if (!taskRunning) {
            progressBar.setValue(0);
            if (textFieldMaxRevision.getText().equals("")) {
                textFieldMaxRevision.setText("0");
            }
            button.setText("Daten exportieren");
        } else {
            button.setText("Abbrechen");
        }
    }

    private class Worker extends SwingWorker<Integer, Integer> {

        private String repository;
        private int maxRevisions;

        Worker(String repository, int maxRevisions) {
            this.repository = repository;
            this.maxRevisions = maxRevisions;
        }

        @Override
        protected Integer doInBackground() throws Exception {
                var handler = new RepositoryHandler(repository, maxRevisions);
                handler.setProgressCallback(this::setProgress);
                handler.start();
                handler.setProgressCallback(null);
            return 1;
        }

        @Override
        protected void done() {
            taskRunning = false;
            onTaskChangeState();
            worker = null;
            setInfo("Fertig exportiert");
        }
    }

}