package de.gamma.libvcs4j.gxl.export;

import de.gamma.libvcs4j.gxl.export.analysis.FileAnalyzer;
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
 * TODO
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
    private final FileAnalyzer fileAnalyzer = new FileAnalyzer();

    /**
     * The repository from which data is to be loaded.
     */
    private final String repository;

    /**
     * Specifies how many revisions are to be loaded.
     */
    private final int maxRevisions;

    /**
     * A function that is called for every revision with the new revision number.
     */
    private Consumer<Integer> progressCallback;

    /**
     * TODO
     * @param repository The repository from which data is to be loaded.
     * @param maxRevisions Specifies how many revisions are to be loaded.
     */
    public RepositoryHandler(String repository, int maxRevisions) {
        this.repository = repository;
        this.maxRevisions = maxRevisions;
    }

    public void setProgressCallback(Consumer<Integer> progressCallback) {
        this.progressCallback = progressCallback;
    }

    /**
     * TODO
     */
    public void start() {
        var projectName = StringUtils.substringAfterLast(repository, "/");
        projectName = StringUtils.substringBefore(projectName, ".");

        logger.debug("Delete old generated data");
        var pathData = Paths.get(GRAPH_DATA_PATH, projectName);
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

        logger.debug("Loading Vcs: " + repository);
        VCSEngine vcs = VCSEngineBuilder
                .ofGit(repository)
                .build();

        try {
            // TODO ignore Revisions where no analyzable file was changed in any way
            var revisionsPath = Paths.get(GRAPH_DATA_PATH, projectName);
            var spoonModelBuilder = new SpoonModelBuilder();
            Files.createDirectories(revisionsPath);
            var revisionCounter = 0;
            for (RevisionRange range : vcs) {
                if (++revisionCounter > maxRevisions) {
                    break;
                }
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
                    logger.error("Error while tring to update SpoonModel for revision " + range.getOrdinal(), e);
                }
                var path = Paths.get(revisionsPath.toString(), String.format("%s-%s.gxl", projectName, range.getOrdinal()));
                var saveFile = new File(path.toAbsolutePath().toString());
                RevisionHandler.writeToFile(saveFile, range, projectName, fileAnalyzer, spoonModel);
            }
        } catch (IOException e) {
            logger.error("Error when iterating revisions:", e);
        }
        logger.info("Finished exporting data.");
    }

}
