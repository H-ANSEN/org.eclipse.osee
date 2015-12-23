//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2012.09.14 at 05:06:42 PM MST
//

package org.eclipse.osee.framework.messaging.event.res.msgs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.osee.framework.messaging.event.res.RemoteEvent;

/**
 * <p>
 * Java class for RemoteBasicGuidRelation1 complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoteBasicGuidRelation1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="branchGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="relTypeGuid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="relationId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="gammaId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="artAId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="artA" type="{}RemoteBasicGuidArtifact1"/>
 *         &lt;element name="artBId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="artB" type="{}RemoteBasicGuidArtifact1"/>
 *         &lt;element name="rationale" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteBasicGuidRelation1", propOrder = {
   "modTypeGuid",
   "branchGuid",
   "relTypeGuid",
   "relationId",
   "gammaId",
   "artAId",
   "artA",
   "artBId",
   "artB",
   "rationale"})
public class RemoteBasicGuidRelation1 extends RemoteEvent {

   @XmlElement(required = true)
   protected String modTypeGuid;
   @XmlElement(required = true)
   protected String branchGuid;
   protected long relTypeGuid;
   protected int relationId;
   protected int gammaId;
   protected int artAId;
   @XmlElement(required = true)
   protected RemoteBasicGuidArtifact1 artA;
   protected int artBId;
   @XmlElement(required = true)
   protected RemoteBasicGuidArtifact1 artB;
   @XmlElement(required = true)
   protected String rationale;

   /**
    * Gets the value of the modTypeGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getModTypeGuid() {
      return modTypeGuid;
   }

   /**
    * Sets the value of the modTypeGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setModTypeGuid(String value) {
      this.modTypeGuid = value;
   }

   /**
    * Gets the value of the branchGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getBranchGuid() {
      return branchGuid;
   }

   /**
    * Sets the value of the branchGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setBranchGuid(String value) {
      this.branchGuid = value;
   }

   /**
    * Gets the value of the relTypeGuid property.
    */
   public long getRelTypeGuid() {
      return relTypeGuid;
   }

   /**
    * Sets the value of the relTypeGuid property.
    */
   public void setRelTypeGuid(long value) {
      this.relTypeGuid = value;
   }

   /**
    * Gets the value of the relationId property.
    */
   public int getRelationId() {
      return relationId;
   }

   /**
    * Sets the value of the relationId property.
    */
   public void setRelationId(int value) {
      this.relationId = value;
   }

   /**
    * Gets the value of the gammaId property.
    */
   public int getGammaId() {
      return gammaId;
   }

   /**
    * Sets the value of the gammaId property.
    */
   public void setGammaId(int value) {
      this.gammaId = value;
   }

   /**
    * Gets the value of the artAId property.
    */
   public int getArtAId() {
      return artAId;
   }

   /**
    * Sets the value of the artAId property.
    */
   public void setArtAId(int value) {
      this.artAId = value;
   }

   /**
    * Gets the value of the artA property.
    * 
    * @return possible object is {@link RemoteBasicGuidArtifact1 }
    */
   public RemoteBasicGuidArtifact1 getArtA() {
      return artA;
   }

   /**
    * Sets the value of the artA property.
    * 
    * @param value allowed object is {@link RemoteBasicGuidArtifact1 }
    */
   public void setArtA(RemoteBasicGuidArtifact1 value) {
      this.artA = value;
   }

   /**
    * Gets the value of the artBId property.
    */
   public int getArtBId() {
      return artBId;
   }

   /**
    * Sets the value of the artBId property.
    */
   public void setArtBId(int value) {
      this.artBId = value;
   }

   /**
    * Gets the value of the artB property.
    * 
    * @return possible object is {@link RemoteBasicGuidArtifact1 }
    */
   public RemoteBasicGuidArtifact1 getArtB() {
      return artB;
   }

   /**
    * Sets the value of the artB property.
    * 
    * @param value allowed object is {@link RemoteBasicGuidArtifact1 }
    */
   public void setArtB(RemoteBasicGuidArtifact1 value) {
      this.artB = value;
   }

   /**
    * Gets the value of the rationale property.
    * 
    * @return possible object is {@link String }
    */
   public String getRationale() {
      return rationale;
   }

   /**
    * Sets the value of the rationale property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setRationale(String value) {
      this.rationale = value;
   }

}
