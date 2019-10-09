package de.gamma.libvcs4jgxlexport.gxl.attr;

import javax.xml.bind.annotation.XmlElement;

public class GxlString extends GxlAttr {

    @XmlElement(name = "string")
    public String data;

    public GxlString(String name, String data) {
        this.name = name;
        this.data = data;
    }

}
