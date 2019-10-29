package de.gamma.libvcs4j.gxl.export.gxl;

import de.gamma.libvcs4j.gxl.export.gxl.attr.GxlString;
import de.gamma.libvcs4j.gxl.export.gxl.util.GxlType;
import de.gamma.libvcs4j.gxl.export.gxl.util.IGxlId;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a directory node inside a gxl file.
 * For example <node id="N2">
 * Needs to be inside a GxlGraph object to create
 * a correct structured gxl file.
 */
@XmlRootElement(name = "node")
public class GxlDir implements IGxlId {

    @XmlAttribute(name = "id")
    public String id;

    @XmlElement(name = "type")
    private GxlType type = new GxlType("Directory");

    @XmlElement(name = "attr")
    public GxlString sourceName;

    @XmlElement(name = "attr")
    public GxlString linkageName;

    /**
     * An empty constructor, required for JAXB.
     */
    public GxlDir() {
    }

    public GxlDir(int id,
                  String sourceName,
                  String linkageName) {
        this.id = "N" + id;
        this.sourceName = new GxlString("Source.Name", sourceName);
        this.linkageName = new GxlString("Linkage.Name", linkageName);
    }

    @Override
    public String getId() {
        return id;
    }
}