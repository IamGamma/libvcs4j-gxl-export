package de.gamma.libvcs4jgxlexport;

import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class Main {

    private final Logger logger = LoggerFactory.getLogger(Main.class);

    private void onStart() {
        logger.debug("Delete old generated data");
        var pathData = Paths.get("graph-revisions/");
        if (Files.exists(pathData)) {
            try {

                Files.walk(pathData)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        var projectName = "streamex";
        var maxRevisions = 40;

        logger.debug("Loading Vcs");
        VCSEngine vcs = VCSEngineBuilder
                .ofGit("https://github.com/amaembo/streamex.git")
                .build();

        try {
            var revisionsPath = Paths.get("graph-revisions/");
            Files.createDirectories(revisionsPath);
            var revisionCounter = 0;
            for (RevisionRange range : vcs) {
                if (revisionCounter++ >= maxRevisions) {
                    break;
                }
                var path = Paths.get(String.format("graph-revisions/%s-%s.gxl", projectName, range.getOrdinal()));
                var saveFile = new File(path.toAbsolutePath().toString());
                RevisionHandler.writeToFile(saveFile, range, projectName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main().onStart();
    }
}