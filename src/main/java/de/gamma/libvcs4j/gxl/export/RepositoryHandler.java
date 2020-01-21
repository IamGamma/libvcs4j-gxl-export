package de.gamma.libvcs4j.gxl.export;

import de.gamma.libvcs4j.gxl.export.analysis.CombinedFileAnalyzer;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import de.unibremen.informatik.st.libvcs4j.engine.AbstractVSCEngine;
import de.unibremen.informatik.st.libvcs4j.spoon.BuildException;
import de.unibremen.informatik.st.libvcs4j.spoon.SpoonModel;
import de.unibremen.informatik.st.libvcs4j.spoon.SpoonModelBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Pulls information from a repository.
 */
public class RepositoryHandler {

    private final Logger logger = LoggerFactory.getLogger(RepositoryHandler.class);

    /**
     * The default folder in which data loaded from a repository is stored.
     */
    private final String GRAPH_DATA_PATH = "graph-data/";

    /**
     * A file analyzer that unites different file types.
     */
    private final CombinedFileAnalyzer fileAnalyzer = new CombinedFileAnalyzer();

    /**
     * The repository from which data is to be loaded.
     */
    private final String repository;

    /**
     * The branch from which data is to be loaded.
     */
    private final String branch;

    /**
     * Specifies how many revisions are to be loaded.
     */
    private int maxRevisions;

    /**
     * A function that is called for every revision with the new revision number.
     */
    private Consumer<Integer> progressCallback;

    /**
     * Creates a new RepositoryHandler that extracts gxl data from a
     * repository with the given address. In doing so, maxRevisions
     * determines the number of revisions to extract.
     * @param repository The repository from which data is to be loaded.
     * @param maxRevisions Specifies how many revisions are to be loaded.
     */
    public RepositoryHandler(String repository, String branch, int maxRevisions) {
        this.repository = repository;
        this.maxRevisions = maxRevisions;
        this.branch = branch;
    }

    /**
     * Sets the progress callback, that is updated with the revision number,
     * that is being exported.
     * @param progressCallback The function that is called, when the next revision is loaded.
     */
    public void setProgressCallback(Consumer<Integer> progressCallback) {
        this.progressCallback = progressCallback;
    }

    /**
     * When called loads als data from the set repository and starts analyzing it.
     */
    public void start() {
        // gets the project name of a git repository
        var projectName = StringUtils.substringAfterLast(repository, "/");
        projectName = StringUtils.substringBefore(projectName, ".");

        logger.info("Delete old generated data");
        var pathData = Paths.get(GRAPH_DATA_PATH, projectName, branch);
        // delete old data from the reposioty, if exists
        if (Files.exists(pathData)) {
            try (var walk = Files.walk(pathData))
            {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            var result = file.delete();
                            if (!result) {
                                logger.warn("Could not delete found file " + file.getPath());
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            logger.info("Loading Vcs: " + repository);
            VCSEngine vcs = VCSEngineBuilder
                    .ofGit(repository)
                    .withBranch(branch)
                    .build();

            logger.info(repository + ": check maxRevisions");
            if (maxRevisions == 0) {
                // load all existing revisions
                maxRevisions = ((AbstractVSCEngine) vcs).listRevisions().size();
            }

            logger.info(repository + ": create spoon model.");
            // prepare directory for the generated files and SpoonModel
            var spoonModelBuilder = new SpoonModelBuilder();
            Files.createDirectories(pathData);

            logger.info(repository + ": start parsing " + maxRevisions + " revisions.");
            var revisionCounter = 0;
            var startMillis = System.currentTimeMillis();
            logger.info("Start loading repodata at (system millis): " + startMillis);
            for (RevisionRange range : vcs) {
                logger.info("Analyzing revision: " + range.getOrdinal());
                if (progressCallback != null) {
                    if (maxRevisions == 0) {
                        progressCallback.accept((int) ((revisionCounter / (float) ((AbstractVSCEngine) vcs).listRevisions().size()) * 100));
                    } else {
                        progressCallback.accept((int) ((revisionCounter / (float) maxRevisions) * 100));
                    }
                }

                SpoonModel spoonModel = null;
                try {
                    spoonModel = spoonModelBuilder.update(range);
                } catch (BuildException e) {
                    revisionCounter++;
                    logger.error("Error while tring to update SpoonModel for revision " + range.getOrdinal(), e);
                    continue;
                }
                // select output file and handle revision
                var path = Paths.get(pathData.toString(), String.format("%s-%s.gxl", projectName, range.getOrdinal()));
                var saveFile = new File(path.toAbsolutePath().toString());
                RevisionHandler.writeToFile(saveFile, range, projectName, fileAnalyzer, spoonModel);

                revisionCounter++;
                if (revisionCounter >= maxRevisions) {
                    break;
                }
            }
            logger.info("Finished exporting data in (seconds): " + ((System.currentTimeMillis() - startMillis) / 1000));
        } catch (IOException e) {
            logger.error("Error when iterating revisions:", e);
        }
    }
}
