package de.gamma.libvcs4j.gxl.export.analysis;

import de.gamma.libvcs4j.gxl.export.RevisionHandler;
import de.gamma.libvcs4j.gxl.export.gxl.GxlEdge;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import de.unibremen.informatik.st.libvcs4j.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an IFileAnalyzer that can analyze .java files.
 */
class JavaFileAnalyzer implements IFileAnalyzer {

    private final Logger logger = LoggerFactory.getLogger(JavaFileAnalyzer.class);
    private final Metrics metrics = new Metrics();

    @Override
    public void analyze(RevisionHandler revisionHandler, VCSFile vcsFile) {
        var fileMap = revisionHandler.getFileMap();
        var gxlFile = fileMap.get(vcsFile.getRelativePath());
        if (gxlFile == null) {
            logger.error("There is no gxl node registered for the VCSFile " + vcsFile.getRelativePath());
            return;
        }
        try {
            metrics.computeSize(vcsFile).ifPresent(size -> {
                gxlFile.loc.data = size.getLOC();
                gxlFile.numberOfTokens.data = size.getNOT();
            });

            metrics.computeComplexity(vcsFile).ifPresent(complexity -> {
                // TODO Put analyzed Data in CSV File
            });

            // find all referenced files and create edges representing the reference
            revisionHandler.getSpoonModel().findReferencedFiles(vcsFile).stream()
                    .map(referencedVcsFile -> fileMap.get(referencedVcsFile.getRelativePath()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(referencedGxlFile -> referencedGxlFile != gxlFile)
                    .forEach(referencedGxlFile -> revisionHandler.addNewEdge(gxlFile, referencedGxlFile, GxlEdge.TYPE_CLONE));
        } catch (IOException e) {
            logger.error("Error when analyzing file " + vcsFile.getRelativePath() + " from revision " + revisionHandler.toString(), e);
        }
    }

    @Override
    public List<String> getFileTypes() {
        return Collections.singletonList(".java");
    }
}
