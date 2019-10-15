package de.gamma.libvcs4j.gxl.export.analysis;

import de.gamma.libvcs4j.gxl.export.RevisionHandler;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Combines IFileAnalyzer for different filytypes in one single point.
 */
public class FileAnalyzer implements IFileAnalyzer {

    private final Logger logger = LoggerFactory.getLogger(FileAnalyzer.class);
    private final HashMap<String, IFileAnalyzer> analyzerMap = new HashMap<>();
    private final List<String> analyzableFileTypes = new ArrayList<>();

    public FileAnalyzer() {
        Arrays
                .asList(new JavaIFileAnalyzer())
                .forEach(analyzer -> {
                    analyzer.getFileTypes().forEach(fileType -> analyzerMap.put(fileType, analyzer));
                });
        analyzableFileTypes.addAll(analyzerMap.keySet());
    }

    @Override
    public void analyze(RevisionHandler revisionHandler, VCSFile vcsFile) {
        for (String fileType : getFileTypes()) {
            if (vcsFile.getRelativePath().endsWith(fileType)) {
                analyzerMap.get(fileType).analyze(revisionHandler, vcsFile);
                return;
            }
        }
        logger.error("There is no analyzer registered for the VCSFile " + vcsFile.getRelativePath());
    }

    @Override
    public List<String> getFileTypes() {
        return analyzableFileTypes;
    }
}
