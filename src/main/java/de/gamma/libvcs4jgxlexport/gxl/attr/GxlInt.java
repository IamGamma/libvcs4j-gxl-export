package de.gamma.libvcs4jgxlexport.gxl.attr;

import javax.xml.bind.annotation.XmlElement;

public class GxlInt extends GxlAttr {

    @XmlElement(name = "int")
    public int data;

    public GxlInt(String name, int data) {
        this.name = name;
        this.data = data;
    }

}
