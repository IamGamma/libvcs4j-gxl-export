package de.gamma.libvcs4j.gxl.export.gxl.util;

import de.gamma.libvcs4j.gxl.export.gxl.GxlDir;
import de.gamma.libvcs4j.gxl.export.gxl.GxlEdge;
import de.gamma.libvcs4j.gxl.export.util.Check;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 */
public class DirNode {

    /**
     * TODO
     */
    private String name;

    /**
     * TODO
     */
    private String path;

    /**
     * TODO
     */
    private HashMap<String, DirNode> children = new HashMap<>();

    /**
     * TODO
     */
    public DirNode(String name, String path) {
        Check.notNullOrEmpty(name, "Name can not be null or empty!");
        Check.notNullOrEmpty(path, "Path can not be null or empty!");
        this.name = name;
        this.path = path;
    }

    /**
     * TODO
     */
    public String getName() {
        return name;
    }

    /**
     * TODO
     */
    public void setName(String name) {
        Check.notNullOrEmpty(name, "Name can not be null or empty!");
        this.name = name;
    }

    /**
     * TODO
     */
    public String getPath() {
        return path;
    }

    /**
     * TODO
     */
    public void setPath(String path) {
        Check.notNullOrEmpty(name, "Name can not be null or empty!");
        this.path = path;
    }

    /**
     * TODO
     */
    public HashMap<String, DirNode> getChildren() {
        return children;
    }

    /**
     * TODO
     */
    public void combineEmptyNodes() {
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
     * TODO use RevisionRange or else
     * @param root
     * @param nodeCounter
     * @param edgeCounter
     * @param dirMap
     * @param edgeList
     */
    public void putChildsIntoGraph(GxlDir root, AtomicInteger nodeCounter, AtomicInteger edgeCounter, ConcurrentMap<String, GxlDir> dirMap, List<GxlEdge> edgeList) {
        for (DirNode child : children.values()) {
            child.putIntoGraph(root, nodeCounter, edgeCounter, dirMap, edgeList);
        }
    }

    /**
     * TODO use RevisionRange or else
     * @param parent
     * @param nodeCounter
     * @param edgeCounter
     * @param dirMap
     * @param edgeList
     */
    private void putIntoGraph(GxlDir parent, AtomicInteger nodeCounter, AtomicInteger edgeCounter, ConcurrentMap<String, GxlDir> dirMap, List<GxlEdge> edgeList) {
        var gxlDir = dirMap.get(path);
        if (gxlDir == null) {
            gxlDir = new GxlDir(
                    nodeCounter.getAndIncrement(),
                    name,
                    path
            );
            dirMap.put(path, gxlDir);
        } else {
            // Wenn er schon existiert wurden beim Combinen vll der Name angepasst, daher diese naupdatedn
            gxlDir.sourceName.data = name;
        }
        if (parent != null) {
            edgeList.add(new GxlEdge(
                    edgeCounter.getAndIncrement(),
                    gxlDir.id,
                    parent.id,
                    GxlEdge.TYPE_ENCLOSING
            ));
        }
        for (DirNode child : children.values()) {
            child.putIntoGraph(gxlDir, nodeCounter, edgeCounter, dirMap, edgeList);
        }
    }
}