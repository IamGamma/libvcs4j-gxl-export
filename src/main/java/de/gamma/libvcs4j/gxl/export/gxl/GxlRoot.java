package de.gamma.libvcs4j.gxl.export.gxl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the root element <gxl xmlns:xlink="http://www.w3.org/1999/xlink">
 * in a gxl file. An object of this class can be automatically
 * converted to xml with JAXB and stored in any destination.
 *
 */
@XmlRootElement(name = "gxl")
public class GxlRoot {

    @XmlAttribute(name = "xmlns:xlink")
    public String xlink = "http://www.w3.org/1999/xlink";

    @XmlElement(name = "graph")
    public final GxlGraph graph = new GxlGraph();

    /**
     * An empty constructor, required for JAXB.
     */
    public GxlRoot() {}
}