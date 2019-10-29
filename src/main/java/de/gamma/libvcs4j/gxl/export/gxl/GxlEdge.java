package de.gamma.libvcs4j.gxl.export.gxl;

import de.gamma.libvcs4j.gxl.export.gxl.util.GxlType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a edge node inside a gxl file.
 * For example <edge id="E1" from="N3" to="N4">
 * Needs to be inside a GxlGraph object to create
 * a correct structured gxl file.
 */
@XmlRootElement(name = "edge")
public class GxlEdge {

    @XmlAttribute(name = "id")
    public String id;

    @XmlAttribute(name = "from")
    public String from;

    @XmlAttribute(name = "to")
    public String to;

    @XmlElement(name = "type")
    public GxlType type;

    /**
     * An empty constructor, required for JAXB.
     */
    public GxlEdge() {
    }

    public GxlEdge(int id, String from, String to, String type) {
        this.id = "E" + id;
        this.from = from;
        this.to = to;
        this.type = new GxlType(type);
    }

    public static final String TYPE_ENCLOSING = "Enclosing";
    public static final String TYPE_CLONE = "Clone";
}