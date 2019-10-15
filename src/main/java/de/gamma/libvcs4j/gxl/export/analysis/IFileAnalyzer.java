package de.gamma.libvcs4j.gxl.export.analysis;

import de.gamma.libvcs4j.gxl.export.RevisionHandler;
import de.unibremen.informatik.st.libvcs4j.VCSFile;

import java.util.List;

public interface IFileAnalyzer {

    void analyze(RevisionHandler revisionHandler, VCSFile vcsFile);

    List<String> getFileTypes();
}