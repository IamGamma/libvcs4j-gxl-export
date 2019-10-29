package de.gamma.libvcs4j.gxl.export;

import de.gamma.libvcs4j.gxl.export.analysis.IFileAnalyzer;
import de.gamma.libvcs4j.gxl.export.gxl.GxlDir;
import de.gamma.libvcs4j.gxl.export.gxl.GxlEdge;
import de.gamma.libvcs4j.gxl.export.gxl.GxlFile;
import de.gamma.libvcs4j.gxl.export.gxl.GxlRoot;
import de.gamma.libvcs4j.gxl.export.gxl.util.DirNode;
import de.gamma.libvcs4j.gxl.export.gxl.util.IGxlId;
import de.unibremen.informatik.st.libvcs4j.FileChange;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import de.unibremen.informatik.st.libvcs4j.spoon.SpoonModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 */
public class RevisionHandler {

    private final Logger logger = LoggerFactory.getLogger(RevisionHandler.class);

    /**
     * TODO private
     */
    private final RevisionRange range;

    /**
     * TODO private
     */
    private final SpoonModel spoonModel;

    /**
     * TODO
     */
    private final GxlRoot gxlRoot = new GxlRoot();

    /**
     * TODO
     */
    private final List<GxlEdge> edgeListUnsafe = new ArrayList<>();

    /**
     * TODO
     */
    private final AtomicInteger edgeCounter = new AtomicInteger(0);

    /**
     * TODO
     */
    private final AtomicInteger nodeCounter = new AtomicInteger(0);

    /**
     * TODO
     */
    private final ConcurrentMap<String, GxlDir> dirMap = new ConcurrentHashMap<>();

    /**
     * TODO private
     */
    public final ConcurrentMap<String, GxlFile> fileMap = new ConcurrentHashMap<>();

    /**
     * TODO
     */
    private final List<GxlEdge> edgeList = Collections.synchronizedList(edgeListUnsafe);

    /**
     * TODO
     */
    private final GxlDir dirRoot;

    /**
     * TODO
     */
    private final List<VCSFile> revisionFiles;

    /**
     * TODO
     */
    private final IFileAnalyzer fileAnalyzer;

    /**
     * TODO
     */
    private final String projectName;

    /**
     * TODO
     *
     * @param file
     * @param range
     * @param projectName
     */
    public static void writeToFile(File file, RevisionRange range, String projectName, IFileAnalyzer fileAnalyzer, SpoonModel spoonModel) {
        // TODO argument check
        var handler = new RevisionHandler(range, projectName, fileAnalyzer, spoonModel);
        handler.run();
        handler.saveToFile(file);
    }

    /**
     * TODO
     * @param from
     * @param to
     * @param type
     */
    public void addNewEdge(IGxlId from, IGxlId to, String type) {
        var gxlEdge = new GxlEdge(edgeCounter.getAndIncrement(), from.getId(), to.getId(), type);
        edgeList.add(gxlEdge);
    }

    /**
     * TODO
     * @return
     */
    public SpoonModel getSpoonModel() {
        return spoonModel;
    }

    @Override
    public String toString() {
        return "RevisionHandler for " + range.getOrdinal() + " - " + range.getRevision().getId();
    }

    /**
     * TODo
     *
     * @param range
     * @param projectName
     */
    private RevisionHandler(RevisionRange range, String projectName, IFileAnalyzer fileAnalyzer, SpoonModel spoonModel) {
        this.range = range;
        this.projectName = projectName;
        this.gxlRoot.graph.id = projectName;
        this.revisionFiles = range.getRevision().getFiles();
        this.fileAnalyzer = fileAnalyzer;
        this.spoonModel = spoonModel;
        this.dirRoot = new GxlDir(
                nodeCounter.getAndIncrement(),
                projectName,
                projectName
        );
    }

    /**
     * TODo extensive inline doc
     */
    private void run() {
        extractGxlFilesAndBareGxlDir();
        correctGxlDirTreeStructure();
        loadFileChangeInfoIntoGxlFiles();
        analyzeFiles();
        putLoadedDataIntoGxl();
    }

    /**
     * TODO
     */
    private void extractGxlFilesAndBareGxlDir() {
        logger.debug(toString() + ": extract gxlfiles and bare gxldirs.");
        revisionFiles.stream().filter(vcsFile -> fileAnalyzer.getFileTypes().stream().anyMatch(vcsFile.getRelativePath()::endsWith)).forEach(vcsFile -> {
            var path = Paths.get(vcsFile.getRelativePath());
            var file = addNewFile(path);
            // create lowest directory that only contain files an no other directorys
            // so that the created GxlFile are enclosed
            GxlDir dir;
            var parentPath = path.getParent();
            if (parentPath != null) {
                dir = dirMap.get(parentPath.toString());
                if (dir == null) {
                    dir = addNewDir(parentPath);
                }
            } else {
                // Wenn es kein parentPath gibt, liegt die Datei im Hauptverzeichnis eines Projekts
                dir = dirRoot;
            }
            addNewEdge(file, dir, GxlEdge.TYPE_ENCLOSING);
        });
    }

    /**
     * TODO
     */
    private void correctGxlDirTreeStructure() {
        logger.debug(toString() + ": correct gxl dir tree structure.");
        // formats the generated DirNodes to represent the real filestructure
        var dirNodeRoot = new DirNode("", "");
        dirMap.values().forEach(dir -> {
            var actualDirNode = dirNodeRoot;
            for (Path subPath : Paths.get(dir.linkageName.data)) {
                var name = subPath.getFileName().toString();

                var newDirNode = actualDirNode.getChildren().getOrDefault(name, new DirNode(name, Paths.get(actualDirNode.getPath(), name).toString()));
                actualDirNode.getChildren().put(name, newDirNode);
                actualDirNode = newDirNode;
            }
        });

        dirNodeRoot.putChildsIntoGraph(dirRoot, nodeCounter, edgeCounter, dirMap, edgeList);
    }

    /**
     * TODO
     */
    private void loadFileChangeInfoIntoGxlFiles() {
        logger.debug("Load filechange info into gxlfiles.");
        range.getAddedFiles().stream()
                .map(FileChange::getNewFile)
                .flatMap(Optional::stream)
                .map(vcsFile -> fileMap.get(vcsFile.getRelativePath()))
                .filter(Objects::nonNull)
                .forEach(gxlFile -> gxlFile.setWasAdded(true));
        range.getModifiedFiles().stream()
                .map(FileChange::getNewFile)
                .flatMap(Optional::stream)
                .map(vcsFile -> fileMap.get(vcsFile.getRelativePath()))
                .filter(Objects::nonNull)
                .forEach(gxlFile -> gxlFile.setWasModified(true));
        range.getRelocatedFiles()
                .forEach(fileChange -> {
                    fileChange.getOldFile().ifPresent(oldFile -> {
                        fileChange
                                .getNewFile()
                                .ifPresent(newFile -> {
                                    Optional
                                            .ofNullable(fileMap.get(newFile.getRelativePath()))
                                            .ifPresent(gxlFile -> {
                                                gxlFile.setWasRelocatedFrom(oldFile.getRelativePath());
                                            });
                                });
                    });
                });
        range.getRemovedFiles().stream()
                .map(FileChange::getNewFile)
                .flatMap(Optional::stream)
                .map(vcsFile -> fileMap.get(vcsFile.getRelativePath()))
                .filter(Objects::nonNull)
                .forEach(gxlFile -> gxlFile.setWasRemoved(true));
    }

    /**
     * TODO
     */
    private void analyzeFiles() {
        logger.debug(toString() + ": analyze code files.");
        revisionFiles.forEach(vcsFile -> {
            var gxlFile = fileMap.get(vcsFile.getRelativePath());
            if (gxlFile == null) {
                return;
            }
            fileAnalyzer.analyze(this, vcsFile);
        });
    }

    /**
     * TODO
     */
    private void putLoadedDataIntoGxl() {
        gxlRoot.graph.files.addAll(fileMap.values());
        gxlRoot.graph.edges.addAll(edgeList);
        gxlRoot.graph.dirs.addAll(dirMap.values());
        gxlRoot.graph.dirs.add(dirRoot);
    }

    /**
     * TODO
     * @param path
     * @return
     */
    private GxlFile addNewFile(Path path) {
        var gxlFile = new GxlFile(
                nodeCounter.getAndIncrement(),
                0,
                0,
                0,
                path.getFileName().toString(),
                path.toString(),
                path.getFileName().toString(),
                path.toString());

        fileMap.put(gxlFile.linkageName.data, gxlFile);
        return gxlFile;
    }

    /**
     * TODO
     * @param path
     * @return
     */
    private GxlDir addNewDir(Path path) {
        var gxlDir = new GxlDir(
                nodeCounter.getAndIncrement(),
                path.getFileName().toString(),
                path.toString()
        );
        dirMap.put(path.toString(), gxlDir);
        return gxlDir;
    }

    /**
     * TODO
     *
     * @param file
     */
    private void saveToFile(File file) {
        logger.debug(toString() + ": save to file.");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(GxlRoot.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "\n<!DOCTYPE gxl SYSTEM \"http://www.gupro.de/GXL/gxl-1.0.dtd\">");
            marshaller.marshal(gxlRoot, file);
        } catch (JAXBException e) {
            logger.error("Error while trying to save revision to gxl file:", e);
        }
    }
}
