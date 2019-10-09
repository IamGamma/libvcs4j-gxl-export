package de.gamma.libvcs4j.gxl.export.gxl;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "graph")
public class GxlGraph {

    @XmlAttribute(name = "id")
    public String id;

    @XmlAttribute(name = "edgeids")
    public Boolean edgeIds = true;

    @XmlElement(name = "node")
    public List<GxlDir> dirs = new ArrayList<>();

    @XmlElement(name = "node")
    public List<GxlFile> files = new ArrayList<>();

    @XmlElement(name = "edge")
    public List<GxlEdge> edges = new ArrayList<>();

    public GxlGraph() {}

    public GxlGraph(String id) {
        this.id = id;
    }
}
