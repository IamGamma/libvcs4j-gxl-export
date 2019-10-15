package de.gamma.libvcs4j.gxl.export.gxl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//  <?xml version="1.0" encoding="UTF-8"?>
//  <!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.dtd">
//  <gxl xmlns:xlink="http://www.w3.org/1999/xlink">
//  <graph id="fs" edgeids="true">
@XmlRootElement(name = "gxl")
public class GxlRoot {

    @XmlAttribute(name = "xmlns:xlink")
    public String xlink = "http://www.w3.org/1999/xlink";

    @XmlElement(name = "graph")
    public final GxlGraph graph = new GxlGraph();

    public GxlRoot() {}
}