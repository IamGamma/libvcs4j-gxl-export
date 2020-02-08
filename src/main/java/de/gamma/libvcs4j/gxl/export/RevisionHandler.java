package de.gamma.libvcs4j.gxl.export;

import de.gamma.libvcs4j.gxl.export.analysis.IFileAnalyzer;
import de.gamma.libvcs4j.gxl.export.gxl.GxlDir;
import de.gamma.libvcs4j.gxl.export.gxl.GxlEdge;
import de.gamma.libvcs4j.gxl.export.gxl.GxlFile;
import de.gamma.libvcs4j.gxl.export.gxl.GxlRoot;
import de.gamma.libvcs4j.gxl.export.gxl.util.DirNode;
import de.gamma.libvcs4j.gxl.export.gxl.util.IGxlId;
import de.gamma.libvcs4j.gxl.export.util.Check;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Processes a RevisionRange and stores the exported data in a gxl file.
 */
public class RevisionHandler {

    private final Logger logger = LoggerFactory.getLogger(RevisionHandler.class);

    /**
     * The RevisionRange for this RevisionHandler
     */
    private final RevisionRange range;

    /**
     * The updated SpoonModel for range
     */
    private final SpoonModel spoonModel;

    /**
     * The GxlRoot containing all gxl data
     */
    private final GxlRoot gxlRoot = new GxlRoot();

    /**
     * The underlying list containing all Edges. The list is not threadsafe.
     */
    private final List<GxlEdge> edgeListUnsafe = new ArrayList<>();

    /**
     * A counter, so that each Edge created has its own Id
     */
    private final AtomicInteger edgeCounter = new AtomicInteger(0);

    /**
     * A counter, so that each GxlDir or GxlFile created has its own Id
     */
    private final AtomicInteger nodeCounter = new AtomicInteger(0);

    /**
     * Contains all created GxlDir, with their path as key.
     */
    private final ConcurrentMap<String, GxlDir> dirMap = new ConcurrentHashMap<>();

    /**
     * Contains all created GxlFile, with their path as key.
     */
    private final ConcurrentMap<String, GxlFile> fileMap = new ConcurrentHashMap<>();

    /**
     * Contains all created GxlEdge.
     */
    private final List<GxlEdge> edgeList = Collections.synchronizedList(edgeListUnsafe);

    /**
     * The root GxlDir of the loaded repository.
     */
    private final GxlDir gxlDirRoot;

    private final IFileAnalyzer fileAnalyzer;

    /**
     * @return the GxlDir used als rot dir.
     */
    public GxlDir getGxlDirRoot() {
        return gxlDirRoot;
    }

    /**
     * @return a map containing all created GxlFiles.
     */
    public ConcurrentMap<String, GxlFile> getFileMap() {
        return fileMap;
    }

    /**
     * @return a map containing all created GxlDirs.
     */
    public ConcurrentMap<String, GxlDir> getDirMap() {
        return dirMap;
    }

    /**
     * @return a list containing all created GxlEdges.
     */
    public List<GxlEdge> getEdgeList() {
        return edgeList;
    }

    /**
     * Processes a RevisionRange and stores the exported data in a gxl file.
     * @param file where the gxl is saved
     * @param range the RevisionRange to process
     * @param projectName the name of the processed project
     */
    public static void writeToFile(File file, Path csvPath, RevisionRange range, String projectName, IFileAnalyzer fileAnalyzer, SpoonModel spoonModel) {
        Check.notNull(file, "file must not be null.");
        Check.notNull(range, "range must not be null");
        Check.notNullOrEmpty(projectName, "projectName must not be null or empty");
        Check.notNull(fileAnalyzer, "fileAnalyzer must not be null");
        //Check.notNull(spoonModel, "spoonModel must not be null");

        var handler = new RevisionHandler(range, projectName, fileAnalyzer, spoonModel);
        handler.run();
        handler.saveToFile(file, csvPath);
    }

    /**
     * Creates a new GxlEdge and saves it.
     * @param from the source
     * @param to the target
     * @param type the GxlEdge type
     */
    public void addNewEdge(IGxlId from, IGxlId to, String type) {
        var gxlEdge = new GxlEdge(edgeCounter.getAndIncrement(), from.getId(), to.getId(), type);
        edgeList.add(gxlEdge);
    }

    /**
     * Creates a new GxlFile and saves it.
     * @param path the path of the created GxlFile
     * @return the created GxlFile
     */
    public GxlFile addNewFile(Path path) {
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
     * Creates a new GxlDir and saves it.
     * @param path the path of the created GxlDir
     * @return the created GxlDir
     */
    public GxlDir addNewDir(Path path) {
        var gxlDir = new GxlDir(
                nodeCounter.getAndIncrement(),
                path.getFileName().toString(),
                path.toString()
        );
        dirMap.put(path.toString(), gxlDir);
        return gxlDir;
    }

    /**
     * @return the updated SpoonModel
     */
    public SpoonModel getSpoonModel() {
        return spoonModel;
    }

    @Override
    public String toString() {
        return "RevisionHandler for " + range.getOrdinal() + " - " + range.getRevision().getId();
    }

    private RevisionHandler(RevisionRange range, String projectName, IFileAnalyzer fileAnalyzer, SpoonModel spoonModel) {
        this.range = range;
        this.gxlRoot.graph.id = projectName + "-root";
        this.fileAnalyzer = fileAnalyzer;
        this.spoonModel = spoonModel;
        this.gxlDirRoot = new GxlDir(
                nodeCounter.getAndIncrement(),
                projectName,
                projectName + "-root"
        );
    }

    private List<VCSFile> getRevisionVCSFiles() {
        return range.getRevision().getFiles();
    }

    /**
     * Executes all functions to create the gxl data.
     */
    private void run() {
        extractGxlFilesAndBareGxlDir();
        logger.debug(toString() + ": correct gxl dir tree structure.");
        DirNode.calculateRealGxlDirectories(this);
        loadFileChangeInfoIntoGxlFiles();
        analyzeFiles();
        putLoadedDataIntoGxl();
    }

    /**
     * Creates all GxlFiles using the VCSFiles in the revision,
     * your GxlDir without the real folder structure
     * and the GxlEdge to the GxlDir.
     */
    private void extractGxlFilesAndBareGxlDir() {
        logger.debug(toString() + ": extract gxlfiles and bare gxldirs.");
        getRevisionVCSFiles().stream().filter(vcsFile -> fileAnalyzer.getFileTypes().stream().anyMatch(vcsFile.getRelativePath()::endsWith)).forEach(vcsFile -> {
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
                // if parent path is null its the root directory
                dir = gxlDirRoot;
            }
            addNewEdge(file, dir, GxlEdge.TYPE_ENCLOSING);
        });
    }

    /**
     * Adds information about file changes to the generated Gxl files.
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
     * Analyzes all GxlFiles.
     */
    private void analyzeFiles() {
        logger.debug(toString() + ": analyze code files.");
        getRevisionVCSFiles().forEach(vcsFile -> {
            var gxlFile = fileMap.get(vcsFile.getRelativePath());
            if (gxlFile == null) {
                return;
            }
            fileAnalyzer.analyze(this, vcsFile);
        });
    }

    /**
     * Puts all created gxl data in the correct position for export.
     */
    public void putLoadedDataIntoGxl() {
        gxlRoot.graph.files.addAll(fileMap.values());
        gxlRoot.graph.edges.addAll(edgeList);
        gxlRoot.graph.dirs.addAll(dirMap.values());
        gxlRoot.graph.dirs.add(gxlDirRoot);
    }

    /**
     * Saves all created gxl data into a given file.
     *
     * @param file where the data is saved
     */
    private void saveToFile(File file, Path csvPath) {
        logger.debug(toString() + ": save to file.");
        try {
            var nodeCount = gxlRoot.graph.files.size();
            var dirCount = gxlRoot.graph.dirs.size();
            var locCount = gxlRoot.graph.files.stream().mapToInt((GxlFile gxlFile) -> gxlFile.loc.data).sum();
            var formatString = "%d;%d;%d;%d;%d" + System.lineSeparator();
            Files.write(csvPath, String.format(formatString, range.getOrdinal(), nodeCount, dirCount, (nodeCount + dirCount), locCount).getBytes(), StandardOpenOption.APPEND);
            JAXBContext jaxbContext = JAXBContext.newInstance(GxlRoot.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "\n<!DOCTYPE gxl SYSTEM \"http://www.gupro.de/GXL/gxl-1.0.dtd\">");
            marshaller.marshal(gxlRoot, file);
        } catch (JAXBException e) {
            logger.error("Error while trying to save revision to gxl file:", e);
        } catch (IOException e) {
            logger.error("Error while trying to save data to csv file:", e);
        }
    }
}
