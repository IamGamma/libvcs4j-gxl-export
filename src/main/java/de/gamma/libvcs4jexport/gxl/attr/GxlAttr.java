package de.gamma.libvcs4jexport.gxl.attr;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "attr")
public class GxlAttr {

    @XmlAttribute(name = "name")
    public String name;

}
