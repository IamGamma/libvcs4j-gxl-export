package de.gamma.libvcs4j.gxl.export.gxl;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the graph node inside a gxl file.
 * <graph id="" edgeids="true">
 * Needs to be inside a GxlRoot object to create
 * a correct structured gxl file.
 */
@XmlRootElement(name = "graph")
public class GxlGraph {

    @XmlAttribute(name = "id")
    public String id;

    @XmlAttribute(name = "edgeids")
    public Boolean edgeIds = true;

    @XmlElement(name = "node")
    public final List<GxlDir> dirs = new ArrayList<>();

    @XmlElement(name = "node")
    public final List<GxlFile> files = new ArrayList<>();

    @XmlElement(name = "edge")
    public final List<GxlEdge> edges = new ArrayList<>();

    /**
     * An empty constructor, required for JAXB.
     */
    public GxlGraph() {}

    public GxlGraph(String id) {
        this.id = id;
    }
}
