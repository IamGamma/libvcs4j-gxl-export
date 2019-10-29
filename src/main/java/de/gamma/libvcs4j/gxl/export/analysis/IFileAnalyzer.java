package de.gamma.libvcs4j.gxl.export.analysis;

import de.gamma.libvcs4j.gxl.export.RevisionHandler;
import de.unibremen.informatik.st.libvcs4j.VCSFile;

import java.util.List;

/**
 * An interface for analyzing specific file types;
 */
public interface IFileAnalyzer {

    /**
     * Analyzes a passed VCSFile based on the information in a RevisionHander.
     * @param revisionHandler The RevisionHandler which information is used.
     * @param vcsFile The file to analyze.
     */
    void analyze(RevisionHandler revisionHandler, VCSFile vcsFile);

    /**
     * Returns all file extensions, in the form ".filetype", that can be analyzed.
     * @return A list of all supported file types
     */
    List<String> getFileTypes();
}