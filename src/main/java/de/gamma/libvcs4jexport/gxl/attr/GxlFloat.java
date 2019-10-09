package de.gamma.libvcs4jexport.gxl.attr;

import javax.xml.bind.annotation.XmlElement;

public class GxlFloat extends GxlAttr {

    @XmlElement(name = "float")
    public float data;

    public GxlFloat(String name, float data) {
        this.name = name;
        this.data = data;
    }

}
