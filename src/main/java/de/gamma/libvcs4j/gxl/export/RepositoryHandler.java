package de.gamma.libvcs4j.gxl.export;

import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import de.unibremen.informatik.st.libvcs4j.engine.AbstractVSCEngine;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class RepositoryHandler {

    final static int ALL_REVISIONS = 0;
    private static final int DEFAULT_MAX_REVISIONS = 40;
    private static final String DEFAULT_GIT_REPO = "https://github.com/amaembo/streamex.git";
    private static final String GRAPH_DATA_PATH = "graph-data/";

    private final Logger logger = LoggerFactory.getLogger(RepositoryHandler.class);
    private final String repository;
    private final int maxRevisions;
    private Consumer<Integer> progressCallback;

    public RepositoryHandler(String repository, int maxRevisions) {
        this.repository = repository;
        this.maxRevisions = maxRevisions;
    }

    public RepositoryHandler(String repository) {
        this(repository, 0);
    }

    public RepositoryHandler() {
        this(DEFAULT_GIT_REPO, DEFAULT_MAX_REVISIONS);
    }

    public void setProgressCallback(Consumer<Integer> progressCallback) {
        this.progressCallback = progressCallback;
    }

    private void verifyData() {
        // TODO verify Data
    }

    public void start() {
        verifyData();

        var projectName = StringUtils.substringAfterLast(repository, "/");
        projectName = StringUtils.substringBefore(projectName, ".");

        logger.debug("Delete old generated data");
        var pathData = Paths.get(GRAPH_DATA_PATH, projectName);
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

        logger.debug("Loading Vcs: " + repository);
        VCSEngine vcs = VCSEngineBuilder
                .ofGit(repository)
                .build();

        try {
            var revisionsPath = Paths.get(GRAPH_DATA_PATH, projectName);
            Files.createDirectories(revisionsPath);
            var revisionCounter = 0;
            for (RevisionRange range : vcs) {
                if (++revisionCounter >= maxRevisions) {
                    break;
                }
                if (progressCallback != null) {
                    if (maxRevisions == 0) {
                        progressCallback.accept((int) ((revisionCounter / (float) ((AbstractVSCEngine) vcs).listRevisions().size()) * 100));
                    } else {
                        progressCallback.accept((int) ((revisionCounter / (float) maxRevisions) * 100));
                    }
                }
                var path = Paths.get(revisionsPath.toString(), String.format("%s-%s.gxl", projectName, range.getOrdinal()));
                var saveFile = new File(path.toAbsolutePath().toString());
                RevisionHandler.writeToFile(saveFile, range, projectName);
            }
        } catch (IOException e) {
            logger.error("Error when iterating revisions:", e);
        }
    }

}
