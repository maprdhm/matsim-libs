//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.20 at 07:21:05 PM MESZ 
//


package org.matsim.jaxb.signalcontrol20;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * This type can be used for all georeferenced data within the 
 *     framework. As we try to avoid reimplementing GIS functionality this is a very 
 *     limited basic type without spatial reference system information. However it
 *     seems to be useful to have a common type for such information, which can
 *     be extended if needed.
 * 
 * <p>Java class for coordinateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="coordinateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="xCoord" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="yCoord" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "coordinateType", propOrder = {
    "xCoord",
    "yCoord"
})
final class XMLCoordinateType {

    @XmlElement(required = true)
    private BigDecimal xCoord;
    @XmlElement(required = true)
    private BigDecimal yCoord;

    /**
     * Gets the value of the xCoord property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getXCoord() {
        return xCoord;
    }

    /**
     * Sets the value of the xCoord property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setXCoord(BigDecimal value) {
        this.xCoord = value;
    }

    /**
     * Gets the value of the yCoord property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getYCoord() {
        return yCoord;
    }

    /**
     * Sets the value of the yCoord property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setYCoord(BigDecimal value) {
        this.yCoord = value;
    }

}
