package de.gamma.libvcs4j.gxl.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

import static javax.swing.SwingUtilities.*;

/**
 * A simple GUI that simplifies loading and analyzing repositories with RepositoryHandler.
 */
public class DesktopApp {

    public static void main(String[] args) {
        invokeLater(() -> new DesktopApp().showGui());
    }

    private static Component spacingH(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    private static Component spacingW() {
        return Box.createRigidArea(new Dimension(10, 0));
    }

    private final Logger logger = LoggerFactory.getLogger(DesktopApp.class);

    private JTextField textFieldRepo = new JTextField();
    private final JTextField textFieldMaxRevision = new JTextField("0");
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel labelInfo = new JLabel("Info:", SwingConstants.LEFT);
    private final JButton button = new JButton("Export data");
    private boolean taskRunning = false;
    private Worker worker;

    private void showGui() {
        JFrame frame = new JFrame("Gxl export");//creating instance of JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT));

        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(600, 200));

        var labelRepo = new JLabel("Repository adresse (https clone adresse):");
        labelRepo.setAlignmentX(Component.LEFT_ALIGNMENT);

        var textFieldRepo = new JTextField();
        textFieldRepo.setAlignmentX(Component.LEFT_ALIGNMENT);

        var labelBranch = new JLabel("Repository branch (default: master):");
        labelBranch.setAlignmentX(Component.LEFT_ALIGNMENT);

        var textFieldBranch = new JTextField("master");
        textFieldBranch.setAlignmentX(Component.LEFT_ALIGNMENT);

        var labelMaxRevision = new JLabel("Number of revisions to load (0 for all):", SwingConstants.LEFT);
        labelMaxRevision.setAlignmentX(Component.LEFT_ALIGNMENT);

        var textFieldMaxRevision = new JTextField("0");
        textFieldMaxRevision.setAlignmentX(Component.LEFT_ALIGNMENT);

        labelInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        var panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
        panelButton.setAlignmentX(Component.LEFT_ALIGNMENT);


        button.addActionListener(event -> onButtonClick(textFieldRepo.getText(), textFieldBranch.getText(), textFieldMaxRevision.getText()));

        Arrays.asList(
                progressBar,
                spacingW(),
                button
        ).forEach(panelButton::add);
        Arrays.asList(
                labelRepo,
                spacingH(2),
                textFieldRepo,
                spacingH(2),
                labelBranch,
                spacingH(2),
                textFieldBranch,
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

    private void onButtonClick(String fieldRepo, String fieldBranch, String fieldMaxRevisions) {
        if (!taskRunning) {
            try {
                var maxRevisions = Integer.parseInt(fieldMaxRevisions);
                worker = new Worker(fieldRepo, fieldBranch, maxRevisions);
                worker.addPropertyChangeListener(event -> {
                    if ("progress".equals(event.getPropertyName()))
                        progressBar.setValue((Integer) event.getNewValue());
                });
                worker.execute();
                logger.info(String.format("Exporting %s datasets from %s.", maxRevisions, fieldRepo));
                setInfo("Loading data, this may take a moment.");
                taskRunning = true;
            }catch (NumberFormatException e) {
                logger.info("Maximum number of revisions is not a valid number.");
                setInfo("Field for maximum revisions is not a valid number.");
            }
        }
        onTaskChangeState();
    }

    private void onTaskChangeState() {
        if (!taskRunning) {
            progressBar.setValue(0);
            if (textFieldMaxRevision.getText().equals("")) {
                textFieldMaxRevision.setText("0");
            }
        }
    }

    /**
     * Loads repositorys in the background and sends progress updates.
     */
    private class Worker extends SwingWorker<Integer, Integer> {

        private final String repository;
        private final String branch;
        private final int maxRevisions;

        Worker(String repository, String branch, int maxRevisions) {
            this.repository = repository;
            this.branch = branch;
            this.maxRevisions = maxRevisions;
        }

        @Override
        protected Integer doInBackground() {
                var handler = new RepositoryHandler(repository, branch, maxRevisions);
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
            setInfo("Finished export");
        }
    }

}