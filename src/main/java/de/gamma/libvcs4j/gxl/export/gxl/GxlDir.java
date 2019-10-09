package de.gamma.libvcs4j.gxl.export.gxl;

import de.gamma.libvcs4j.gxl.export.gxl.attr.GxlString;
import de.gamma.libvcs4j.gxl.export.gxl.util.GxlType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//    <node id="N2">
//      <type xlink:href="Directory"/>
//      <de.gamma.libvcs4jgxlexport.attr name="Source.Name">
//        <string>9p</string>
//      </de.gamma.libvcs4jgxlexport.attr>
//      <de.gamma.libvcs4jgxlexport.attr name="Linkage.Name">
//        <string>9p/fs</string>
//      </de.gamma.libvcs4jgxlexport.attr>
//    </node>
@XmlRootElement(name = "node")
public class GxlDir {

    @XmlAttribute(name = "id")
    public String id;

    @XmlElement(name = "type")
    private GxlType type = new GxlType("Directory");

    @XmlElement(name = "attr")
    public GxlString sourceName;

    @XmlElement(name = "attr")
    public GxlString linkageName;

    public GxlDir() {

    }

    public GxlDir(String id,
                  String sourceName,
                  String linkageName) {
        this.id = id;
        this.sourceName = new GxlString("Source.Name", sourceName);
        this.linkageName = new GxlString("Linkage.Name", linkageName);
    }

}