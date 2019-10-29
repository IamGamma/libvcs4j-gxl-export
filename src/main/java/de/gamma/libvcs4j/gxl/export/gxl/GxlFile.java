package de.gamma.libvcs4j.gxl.export.gxl;

import de.gamma.libvcs4j.gxl.export.gxl.attr.GxlFloat;
import de.gamma.libvcs4j.gxl.export.gxl.attr.GxlInt;
import de.gamma.libvcs4j.gxl.export.gxl.attr.GxlString;
import de.gamma.libvcs4j.gxl.export.gxl.util.GxlType;
import de.gamma.libvcs4j.gxl.export.gxl.util.IGxlId;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a file node inside a gxl file.
 * For example <node id="N1">
 * Needs to be inside a GxlGraph object to create
 * a correct structured gxl file.
 */
@XmlRootElement(name = "node")
public class GxlFile implements IGxlId {

    @XmlAttribute(name = "id")
    public String id;

    @XmlElement(name = "type")
    private GxlType type = new GxlType("File");

    @XmlElement(name = "attr")
    public GxlInt numberOfTokens;

    @XmlElement(name = "attr")
    public GxlInt loc;

    @XmlElement(name = "attr")
    public GxlFloat cloneRate;

    @XmlElement(name = "attr")
    public GxlString sourceName;

    @XmlElement(name = "attr")
    public GxlString linkageName;

    @XmlElement(name = "attr")
    public GxlString sourceFile;

    @XmlElement(name = "attr")
    public GxlString sourcePath;

    @XmlElement(name = "attr")
    public GxlInt wasAdded;

    @XmlElement(name = "attr")
    public GxlInt wasModified;

    @XmlElement(name = "attr")
    public GxlString wasRelocatedFrom;

    @XmlElement(name = "attr")
    public GxlInt wasRemoved;

    /**
     * An empty constructor, required for JAXB.
     */
    public GxlFile() {
    }

    public GxlFile(int id,
                   int loc,
                   float cloneRate,
                   int numberOfTokens,
                   String sourceName,
                   String linkageName,
                   String sourceFile,
                   String sourcePath) {
        this.id = "N" + id;
        this.loc = new GxlInt("Metric.LOC", loc);
        this.cloneRate = new GxlFloat("Metric.Clone_Rate", cloneRate);
        this.numberOfTokens = new GxlInt("Metric.Number_of_Tokens", numberOfTokens);
        this.sourceName = new GxlString("Source.Name", sourceName);
        this.linkageName = new GxlString("Linkage.Name", linkageName);
        this.sourceFile = new GxlString("Source.File", sourceFile);
        this.sourcePath = new GxlString("Source.Path", sourcePath);
    }

    public void setWasAdded(boolean wasAdded) {
        if (wasAdded) {
            this.wasAdded = new GxlInt("CodeHistory.WasAdded", 1);
        } else {
            this.wasAdded = null;
        }
    }

    public void setWasModified(boolean wasModified) {
        if (wasModified) {
            this.wasModified = new GxlInt("CodeHistory.WasModified", 1);
        } else {
            this.wasModified = null;
        }
    }

    public void setWasRelocatedFrom(String wasRelocatedFrom) {
        if (wasRelocatedFrom != null) {
            this.wasRelocatedFrom = new GxlString("CodeHistory.WasRelocated", wasRelocatedFrom);
        } else {
            this.wasRelocatedFrom = null;
        }
    }

    public void setWasRemoved(boolean wasRemoved) {
        if (wasRemoved) {
            this.wasRemoved = new GxlInt("CodeHistory.WasRemoved", 1);
        } else {
            this.wasRemoved = null;
        }
    }

    @Override
    public String getId() {
        return id;
    }
}