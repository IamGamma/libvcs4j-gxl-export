package de.gamma.libvcs4j.gxl.export.gxl.util;

import javax.xml.bind.annotation.XmlAttribute;

public class GxlType {

    @XmlAttribute(name = "xlink:href")
    public String href;


    public GxlType(String href) {
        this.href = href;
    }
}
