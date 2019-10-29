package de.gamma.libvcs4j.gxl.export.gxl.util;

import de.gamma.libvcs4j.gxl.export.RevisionHandler;
import de.gamma.libvcs4j.gxl.export.gxl.GxlDir;
import de.gamma.libvcs4j.gxl.export.gxl.GxlEdge;
import de.gamma.libvcs4j.gxl.export.util.Check;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to calculate the correct tree structure in Gxl graphs.
 */
public class DirNode {

    /**
     * The name of the folder that is being represented.
     */
    private String name;

    /**
     * The path of the folder that is being represented.
     */
    private String path;

    /**
     * Child nodes representing the directory structure.
     */
    private HashMap<String, DirNode> children = new HashMap<>();

    /**
     * Creates a new DirNode with a given name and path.
     * @param name The DirNode name, may not be null.
     * @param path The DirNode path, may not be null.
     */
    public DirNode(String name, String path) {
        Check.notNull(name, "Name can not be null!");
        Check.notNull(path, "Path can not be null!");
        this.name = name;
        this.path = path;
    }

    /**
     * @return The name of this DirNode.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name new name of this DirNode, may not be null.
     */
    public void setName(String name) {
        Check.notNullOrEmpty(name, "Name can not be null or empty!");
        this.name = name;
    }

    /**
     * @return path of this DirNode.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path new path to be set.
     */
    public void setPath(String path) {
        Check.notNullOrEmpty(name, "Name can not be null or empty!");
        this.path = path;
    }

    /**
     * @return the children of this DirNode.
     */
    public HashMap<String, DirNode> getChildren() {
        return children;
    }

    /**
     * Connects empty nodes that have only one child node.
     * For example NodeA("src/main", ...) and NodeB("src/main/test")
     * are combined if NodeA has only NodeB as child.
     */
    private void combineEmptyNodes() {
        if (children.size() == 1) {
            var onlyChild = children.values().iterator().next();
            name = Paths.get(name, onlyChild.name).toString();
            path = onlyChild.path;
            children = onlyChild.children;
            combineEmptyNodes();
        } else {
            children.values().forEach(DirNode::combineEmptyNodes);
        }
    }

    /**
     * Converts existing GxlDir from a given RevisionHandler without a correct tree structure into a tree structure out of DirNode.
     * For example [GxlDir("src/main/test"), GxlDir("src/main/code"), GxlDir("src/main/resources")]
     * is converted into
     * [DirNode("src/main/", [Node("test"), DirNode("code"), DirNode("resources")])].
     * @param revisionHandler the RevisionHandler
     */
    public static void calculateRealGxlDirectories(RevisionHandler revisionHandler) {
        // create a DirNode as root
        var dirNodeRoot = new DirNode("", "");
        // get path from all existing GxlDir, split into folder and create new DirNode if not already exists
        revisionHandler.getDirMap().values().forEach(dir -> {
            var actualDirNode = dirNodeRoot;
            for (Path subPath : Paths.get(dir.linkageName.data)) {
                var name = subPath.getFileName().toString();

                var newDirNode = actualDirNode.getChildren().getOrDefault(name, new DirNode(name, Paths.get(actualDirNode.getPath(), name).toString()));
                actualDirNode.getChildren().put(name, newDirNode);
                actualDirNode = newDirNode;
            }
        });

        dirNodeRoot.combineEmptyNodes();

        // create and modify GxlDir based on generated NodeDirs
        for (DirNode child : dirNodeRoot.children.values()) {
            child.putIntoGraph(revisionHandler, revisionHandler.getGxlDirRoot());
        }
    }

    /**
     * A recursive function that converts previously created DirNodes to GxlDir,
     * adds them to the existing GxlDir and create an enclosing Edge to the parent,
     * from a given RevisionHandler.
     * @param revisionHandler the RevisionHandler
     * @param parent the enclosing parent
     */
    private void putIntoGraph(RevisionHandler revisionHandler, GxlDir parent) {
        var dirMap = revisionHandler.getDirMap();
        var gxlDir = dirMap.get(path);

        if (gxlDir == null) {
            gxlDir = revisionHandler.addNewDir(Path.of(path));
        } else {
            // if the GxlDir already exists, update the name
            gxlDir.sourceName.data = name;
        }

        revisionHandler.addNewEdge(gxlDir, parent, GxlEdge.TYPE_ENCLOSING);

        for (DirNode child : children.values()) {
            child.putIntoGraph(revisionHandler, gxlDir);
        }
    }
}