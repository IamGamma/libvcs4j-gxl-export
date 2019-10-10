package de.gamma.libvcs4j.gxl.export;

import de.gamma.libvcs4j.gxl.export.gxl.GxlDir;
import de.gamma.libvcs4j.gxl.export.gxl.GxlRoot;
import de.gamma.libvcs4j.gxl.export.gxl.GxlEdge;
import de.gamma.libvcs4j.gxl.export.gxl.GxlFile;
import de.gamma.libvcs4j.gxl.export.gxl.util.DirNode;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RevisionHandler {

    private final Logger logger = LoggerFactory.getLogger(RevisionHandler.class);
    private final String projectName;
    private final GxlRoot gxlRoot;
    private final List<String> badFileTypes;

    static void writeToFile(File file, RevisionRange range, String projectName) {
        var handler = new RevisionHandler(range, projectName);
        handler.run();
        handler.saveToFile(file);
    }

    private RevisionRange range;

    private RevisionHandler(RevisionRange range, String projectName) {
        // TODO argument check
        this.range = range;
        this.projectName = projectName;
        gxlRoot = new GxlRoot();
        gxlRoot.graph.id = projectName;
        badFileTypes = Arrays.asList(".jar");
    }

    private void run() {
        logger.info("Handel Revision: " + range.getOrdinal());
        var dirMap = new ConcurrentHashMap<String, GxlDir>();
        var edgeListUnsafe = new ArrayList<GxlEdge>();
        var edgeList = Collections.synchronizedList(edgeListUnsafe);
        var revisionFiles = range.getRevision().getFiles();
        gxlRoot.graph.id = projectName;


        var edgeCounter = new AtomicInteger(0);
        var nodeCounter = new AtomicInteger(0);

        var dirRoot = new GxlDir(
                "N" + nodeCounter.getAndIncrement(),
                projectName,
                projectName
        );
        var files = revisionFiles.stream().filter(vcsFile -> badFileTypes.stream().noneMatch(vcsFile.getRelativePath()::endsWith)).map(vcsFile -> {
            var path = Paths.get(vcsFile.getRelativePath());
            logger.info("Handel Revision 2: " + path.toString());

            var file = new GxlFile(
                    "N" + nodeCounter.getAndIncrement(),
                    0,
                    0,
                    0,
                    path.getFileName().toString(),
                    vcsFile.getRelativePath(),
                    path.getFileName().toString(),
                    vcsFile.getRelativePath());
            try {
                file.loc.data = (int) Files.lines(vcsFile.toFile().toPath()).count();
                file.numberOfTokens.data = Files.lines(vcsFile.toFile().toPath())
                        .map(line -> new StringTokenizer(line).countTokens())
                        .reduce(0, Integer::sum);
            } catch (IOException e) {
                logger.error("Error in file strem von revision " + range.getOrdinal());
                e.printStackTrace();
            }
            var dirPath = path.getParent();
            if (dirPath != null) {
                var dir = dirMap.putIfAbsent(dirPath.toString(), new GxlDir(
                        "N" + nodeCounter.getAndIncrement(),
                        dirPath.getFileName().toString(),
                        dirPath.toString()
                ));
                if (dir == null) {
                    dir = dirMap.get(dirPath.toString());
                }

                edgeList.add(new GxlEdge(
                        "E" + edgeCounter.getAndIncrement(),
                        file.id,
                        dir.id,
                        GxlEdge.ENCLOSING
                ));
            } else {
                edgeList.add(new GxlEdge(
                        "E" + edgeCounter.getAndIncrement(),
                        file.id,
                        dirRoot.id,
                        GxlEdge.ENCLOSING
                ));
            }
            return file;
        }).collect(Collectors.toList());
        // Splitte und kombine die einzelnen path in ihre Ordnerstruktur
        var dirNodeRoot = new DirNode("", ""); // dirRoot.sourceName.data
        dirMap.values().forEach(dir -> {
            var actualDirNode = dirNodeRoot;
            for (Path subPath : Paths.get(dir.linkageName.data)) {
                var name = subPath.getFileName().toString();

                var newDirNode = actualDirNode.getChildren().getOrDefault(name, new DirNode(name, Paths.get(actualDirNode.getPath(), name).toString()));
                actualDirNode.getChildren().put(name, newDirNode);
                actualDirNode = newDirNode;
            }
            actualDirNode.setHasFiles(true);
        });
        dirNodeRoot.combineEmptyNodes();
        dirNodeRoot.putChildsIntoGraph(dirRoot, nodeCounter, edgeCounter, dirMap, edgeList);
        gxlRoot.graph.files.addAll(files);
        gxlRoot.graph.edges.addAll(edgeList);
        gxlRoot.graph.dirs.addAll(dirMap.values());
        gxlRoot.graph.dirs.add(dirRoot);
    }

    private void saveToFile(File file) {
        try {
            //var path = Paths.get("graph-revisions/" + root.graph.id + ".gxl");
            //Files.createDirectories(file);
            JAXBContext jaxbContext = JAXBContext.newInstance(GxlRoot.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "\n<!DOCTYPE gxl SYSTEM \"http://www.gupro.de/GXL/gxl-1.0.dtd\">");
            marshaller.marshal(gxlRoot, file);
            //marshaller.marshal(gxlRoot, System.out);
        } catch (JAXBException e) {
            logger.error("Error while trying to save revision to gxl file:", e);
        }
    }
}
