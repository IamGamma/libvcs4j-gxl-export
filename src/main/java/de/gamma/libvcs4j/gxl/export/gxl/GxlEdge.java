package de.gamma.libvcs4j.gxl.export.gxl;

import de.gamma.libvcs4j.gxl.export.gxl.util.GxlType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//    <edge id="E520" from="N598" to="N585">
//      <type xlink:href="Enclosing"/>
//    </edge>
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

    public GxlEdge() {

    }

    public GxlEdge(int id, String from, String to, String type) {
        this.id = "E" + id;
        this.from = from;
        this.to = to;
        this.type = new GxlType(type);
    }

    public static String TYPE_ENCLOSING = "Enclosing";
    public static String TYPE_CLONE = "Clone";
}