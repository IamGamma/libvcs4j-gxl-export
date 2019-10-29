package de.gamma.libvcs4j.gxl.export.gxl.util;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Represents the <type xlink:href=""/> node in a gxl file.
 */
public class GxlType {

    @XmlAttribute(name = "xlink:href")
    public String href;


    public GxlType(String href) {
        this.href = href;
    }
}
