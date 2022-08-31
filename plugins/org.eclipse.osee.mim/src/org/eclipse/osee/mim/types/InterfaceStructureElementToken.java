/*********************************************************************
 * Copyright (c) 2021 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
package org.eclipse.osee.mim.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactReadable;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;

/**
 * @author Luciano T. Vaglienti
 */
public class InterfaceStructureElementToken extends PLGenericDBObject {
   public static final InterfaceStructureElementToken SENTINEL = new InterfaceStructureElementToken();

   private String Name;

   private Boolean InterfaceElementAlterable;

   private String Notes;

   private String Description;

   private Integer InterfaceElementIndexStart;

   private Integer InterfaceElementIndexEnd;

   private String Units;

   private Long PlatformTypeId;
   private String PlatformTypeName;

   private Double beginByte = 0.0;
   private Double beginWord = 0.0;

   private ApplicabilityToken applicability;

   private String logicalType;
   private String InterfacePlatformTypeMinval;
   private String InterfacePlatformTypeMaxval;
   private String InterfacePlatformTypeDefaultValue;
   private String InterfacePlatformTypeBitSize;
   private String InterfacePlatformTypeDescription;
   private boolean autogenerated = false;
   private boolean includedInCounts = true;
   private boolean hasNegativeEndByteOffset = false;
   private PlatformTypeToken platformType;
   private ArtifactReadable artifactReadable;
   /**
    * @param art
    */
   public InterfaceStructureElementToken(ArtifactToken art) {
      this((ArtifactReadable) art);
   }

   /**
    * @param art
    */
   public InterfaceStructureElementToken(ArtifactReadable art) {
      this();
      this.setId(art.getId());
      this.setName(art.getSoleAttributeValue(CoreAttributeTypes.Name));
      this.setInterfaceElementAlterable(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceElementAlterable, false));
      this.setInterfaceElementIndexStart(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceElementIndexStart, 0));
      this.setInterfaceElementIndexEnd(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceElementIndexEnd, 0));
      this.setNotes(art.getSoleAttributeValue(CoreAttributeTypes.Notes, ""));
      this.setDescription(art.getSoleAttributeValue(CoreAttributeTypes.Description, ""));
      ArtifactReadable pTypeArt =
         art.getRelated(CoreRelationTypes.InterfaceElementPlatformType_PlatformType).getOneOrDefault(
            ArtifactReadable.SENTINEL);
      if (pTypeArt.isValid() && !pTypeArt.getExistingAttributeTypes().isEmpty()) {
         PlatformTypeToken pType = new PlatformTypeToken(pTypeArt);
         this.setPlatformType(pType);
         this.setInterfacePlatformTypeBitSize(pType.getInterfacePlatformTypeBitSize());
         this.setPlatformTypeId(pType.getId());
         this.setPlatformTypeName(pType.getName());
         this.setLogicalType(pType.getInterfaceLogicalType() != null ? pType.getInterfaceLogicalType() : "");
         this.setInterfacePlatformTypeMinval(
            pType.getInterfacePlatformTypeMinval() != null ? pType.getInterfacePlatformTypeMinval() : "");
         this.setInterfacePlatformTypeMaxval(
            pType.getInterfacePlatformTypeMaxval() != null ? pType.getInterfacePlatformTypeMaxval() : "");
         this.setInterfacePlatformTypeDefaultValue(
            pType.getInterfacePlatformTypeDefaultValue() != null ? pType.getInterfacePlatformTypeDefaultValue() : "");
         this.setUnits(pType.getInterfacePlatformTypeUnits() != null ? pType.getInterfacePlatformTypeUnits() : "");
         this.setInterfacePlatformTypeDescription(pType.getDescription() != null ? pType.getDescription() : "");
         this.setApplicability(!art.getApplicabilityToken().getId().equals(
            -1L) ? art.getApplicabilityToken() : ApplicabilityToken.SENTINEL);
      } else {
         this.setPlatformType(PlatformTypeToken.SENTINEL);
         this.setInterfacePlatformTypeBitSize("0");
         this.setPlatformTypeId(-1L);
         this.setPlatformTypeName("");
         this.setLogicalType("");
         this.setInterfacePlatformTypeMinval("0");
         this.setInterfacePlatformTypeMaxval("0");
         this.setInterfacePlatformTypeDefaultValue("0");
         this.setUnits("");
         this.setInterfacePlatformTypeDescription("");
      }
      this.artifactReadable = art;
   }

   public InterfaceStructureElementToken(String name, String description, Double beginByte, Double beginWord, Integer size) {
      this(name, description, beginByte, beginWord, size, false);
   }

   public InterfaceStructureElementToken(String name, String description, Double beginByte, Double beginWord, Integer size, boolean offset) {
      super((long) -1, name);
      this.setId((long) -1);
      this.setName(name);
      this.setDescription(description);
      this.setInterfaceElementAlterable(false);
      this.setInterfaceElementIndexStart(0);
      this.setInterfacePlatformTypeBitSize("8");
      this.setInterfaceElementIndexEnd(size - 1);
      this.setNotes("");
      this.setBeginByte(beginByte);
      this.setBeginWord(beginWord);
      this.setApplicability(ApplicabilityToken.BASE);
      this.setPlatformTypeId((long) -1);
      this.setPlatformTypeName("spare");
      this.setUnits("");
      this.setLogicalType("autogenerated");
      this.setInterfacePlatformTypeDefaultValue("");
      this.setInterfacePlatformTypeMaxval("0");
      this.setInterfacePlatformTypeMinval("0");
      this.setAutogenerated(true);
      this.setIncludedInCounts(false); // Spares should not be included in structure byte counts
      this.setHasNegativeEndByteOffset(offset);
      this.setInterfacePlatformTypeDescription("Autogenerated upon page load");
   }

   public InterfaceStructureElementToken(Long id, String name, ApplicabilityToken applicability, PlatformTypeToken pType) {
      super(id, name);
      this.setDescription("");
      this.setNotes("");
      this.setInterfaceElementAlterable(false);
      this.setInterfaceElementIndexStart(0);
      this.setInterfaceElementIndexEnd(0);
      this.setApplicability(applicability);
      this.setPlatformType(pType);
      this.setInterfacePlatformTypeBitSize(pType.getInterfacePlatformTypeBitSize());
      this.setPlatformTypeId(pType.getId());
      this.setPlatformTypeName(pType.getName());
      this.setLogicalType(pType.getInterfaceLogicalType());
      this.setInterfacePlatformTypeMinval(pType.getInterfacePlatformTypeMinval());
      this.setInterfacePlatformTypeMaxval(pType.getInterfacePlatformTypeMaxval());
      this.setInterfacePlatformTypeDefaultValue(pType.getInterfacePlatformTypeDefaultValue());
      this.setUnits(pType.getInterfacePlatformTypeUnits());
      this.setInterfacePlatformTypeDescription(pType.getDescription());
      this.setAutogenerated(true);
      this.setInterfacePlatformTypeDescription("Autogenerated upon page load");
   }

   /**
    * @param id
    * @param name
    */
   public InterfaceStructureElementToken(Long id, String name) {
      super(id, name);
   }

   /**
    *
    */
   public InterfaceStructureElementToken() {
      super();
   }

   /**
    * @return the description
    */
   public String getDescription() {
      return Description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description) {
      Description = description;
   }

   /**
    * @return the notes
    */
   public String getNotes() {
      return Notes;
   }

   /**
    * @param notes the notes to set
    */
   public void setNotes(String notes) {
      Notes = notes;
   }

   /**
    * @return the interfaceElementAlterable
    */
   public Boolean getInterfaceElementAlterable() {
      return InterfaceElementAlterable;
   }

   /**
    * @param interfaceElementAlterable the interfaceElementAlterable to set
    */
   public void setInterfaceElementAlterable(Boolean interfaceElementAlterable) {
      InterfaceElementAlterable = interfaceElementAlterable;
   }

   /**
    * @return the interfaceElementIndexStart
    */
   public Integer getInterfaceElementIndexStart() {
      return InterfaceElementIndexStart;
   }

   /**
    * @param interfaceElementIndexStart the interfaceElementIndexStart to set
    */
   public void setInterfaceElementIndexStart(Integer interfaceElementIndexStart) {
      InterfaceElementIndexStart = interfaceElementIndexStart;
   }

   /**
    * @return the interfaceElementIndexEnd
    */
   public Integer getInterfaceElementIndexEnd() {
      return InterfaceElementIndexEnd;
   }

   /**
    * @param interfaceElementIndexEnd the interfaceElementIndexEnd to set
    */
   public void setInterfaceElementIndexEnd(Integer interfaceElementIndexEnd) {
      InterfaceElementIndexEnd = interfaceElementIndexEnd;
   }

   /**
    * @return the platformTypeId
    */
   public Long getPlatformTypeId() {
      return PlatformTypeId;
   }

   /**
    * @param platformTypeId the platformTypeId to set
    */
   public void setPlatformTypeId(Long platformTypeId) {
      PlatformTypeId = platformTypeId;
   }

   /**
    * @return the platformTypeName
    */
   public String getPlatformTypeName2() {
      return PlatformTypeName;
   }

   /**
    * @param platformTypeName the platformTypeName to set
    */
   public void setPlatformTypeName(String platformTypeName) {
      PlatformTypeName = platformTypeName;
   }

   /**
    * @return the beginByte
    */
   public Double getBeginByte() {
      return beginByte;
   }

   /**
    * @param beginByte the beginByte to set
    */
   public void setBeginByte(Double beginByte) {
      this.beginByte = beginByte;
   }

   /**
    * @return the endByte
    */
   public Double getEndByte() {
      return (this.beginByte + (this.getInterfacePlatformTypeByteSize() * Math.max(1, this.getArrayLength())) - 1) % 4;
   }

   /**
    * @return the beginWord
    */
   public Double getBeginWord() {
      return beginWord;
   }

   /**
    * @param beginWord the beginWord to set
    */
   public void setBeginWord(Double beginWord) {
      this.beginWord = beginWord;
   }

   /**
    * @return the endWord
    */
   public Double getEndWord() {
      return Math.ceil(
         ((this.getBeginWord() * 4) + this.getBeginByte() + (this.getInterfacePlatformTypeByteSize() * Math.max(1,
            this.getArrayLength()))) / 4) - 1;
   }

   /**
    * @return the endByte, without resetting counter per word
    */
   @JsonIgnore
   public Double getEndingByteNoReset() {
      return this.getEndWord() * 4;
   }

   /**
    * @return the endbit, without resetting counter per word
    */
   @JsonIgnore
   public Double getEndingBitNoReset() {
      return this.getEndingByteNoReset() * 8;
   }

   /**
    * @return the applicability
    */
   public ApplicabilityToken getApplicability() {
      return applicability;
   }

   /**
    * @param applicability the applicability to set
    */
   public void setApplicability(ApplicabilityToken applicability) {
      this.applicability = applicability;
   }

   /**
    * @return the logicalType
    */
   public String getLogicalType() {
      return logicalType;
   }

   /**
    * @param logicalType the logicalType to set
    */
   public void setLogicalType(String logicalType) {
      this.logicalType = logicalType;
   }

   /**
    * @return the interfacePlatformTypeMinval
    */
   public String getInterfacePlatformTypeMinval() {
      return InterfacePlatformTypeMinval;
   }

   /**
    * @param interfacePlatformTypeMinval the interfacePlatformTypeMinval to set
    */
   public void setInterfacePlatformTypeMinval(String interfacePlatformTypeMinval) {
      InterfacePlatformTypeMinval = interfacePlatformTypeMinval;
   }

   /**
    * @return the interfacePlatformTypeMaxval
    */
   public String getInterfacePlatformTypeMaxval() {
      return InterfacePlatformTypeMaxval;
   }

   /**
    * @param interfacePlatformTypeMaxval the interfacePlatformTypeMaxval to set
    */
   public void setInterfacePlatformTypeMaxval(String interfacePlatformTypeMaxval) {
      InterfacePlatformTypeMaxval = interfacePlatformTypeMaxval;
   }

   /**
    * @return the interfacePlatformTypeDefaultValue
    */
   public String getInterfacePlatformTypeDefaultValue() {
      return InterfacePlatformTypeDefaultValue;
   }

   /**
    * @param interfacePlatformTypeDefaultValue the interfacePlatformTypeDefaultValue to set
    */
   public void setInterfacePlatformTypeDefaultValue(String interfacePlatformTypeDefaultValue) {
      InterfacePlatformTypeDefaultValue = interfacePlatformTypeDefaultValue;
   }

   /**
    * @return the units
    */
   public String getUnits() {
      return Units;
   }

   /**
    * @param units the units to set
    */
   public void setUnits(String units) {
      Units = units;
   }

   /**
    * @return the autogenerated
    */
   public boolean isAutogenerated() {
      return autogenerated;
   }

   /**
    * @param autogenerated the autogenerated to set
    */
   public void setAutogenerated(boolean autogenerated) {
      this.autogenerated = autogenerated;
   }

   /**
    * @return the includedInCounts
    */
   @JsonIgnore
   public boolean isIncludedInCounts() {
      return includedInCounts;
   }

   /**
    * @param autogenerated the includedInCounts to set
    */
   @JsonIgnore
   public void setIncludedInCounts(boolean includedInCounts) {
      this.includedInCounts = includedInCounts;
   }

   /**
    * @return the interfacePlatformTypeBitSize
    */
   @JsonIgnore
   public double getInterfacePlatformTypeBitSize() {
      return Double.parseDouble(InterfacePlatformTypeBitSize);
   }

   /**
    * @return the interfacePlatformTypeByteSize
    */
   @JsonIgnore
   public double getInterfacePlatformTypeByteSize() {
      return Double.parseDouble(InterfacePlatformTypeBitSize) / 8;

   }

   /**
    * @return the interfacePlatformTypeWordSize
    */
   @JsonIgnore
   public double getInterfacePlatformTypeWordSize() {
      return Math.floor(this.getInterfacePlatformTypeByteSize() / 4);
   }

   /**
    * @return the length of array
    */
   @JsonIgnore
   public int getArrayLength() {
      return this.getInterfaceElementIndexEnd() - this.getInterfaceElementIndexStart() + 1;
   }

   /**
    * return size of element using array and type size
    */
   public double getElementSizeInBits() {
      return (this.getArrayLength() * this.getInterfacePlatformTypeBitSize());
   }

   /**
    * return size of element using array and type size
    */
   public double getElementSizeInBytes() {
      return this.getArrayLength() * this.getInterfacePlatformTypeBitSize() / 8;
   }

   /**
    * @param interfacePlatformTypeBitSize the interfacePlatformTypeBitSize to set
    */
   public void setInterfacePlatformTypeBitSize(String interfacePlatformTypeBitSize) {
      InterfacePlatformTypeBitSize = interfacePlatformTypeBitSize;
   }

   /**
    * @return the hasNegativeEndByteOffset
    */
   @JsonIgnore
   public boolean isHasNegativeEndByteOffset() {
      return hasNegativeEndByteOffset;
   }

   /**
    * @param hasNegativeEndByteOffset the hasNegativeEndByteOffset to set
    */
   @JsonIgnore
   public void setHasNegativeEndByteOffset(boolean hasNegativeEndByteOffset) {
      this.hasNegativeEndByteOffset = hasNegativeEndByteOffset;
   }

   /**
    * @return the interfacePlatformTypeDescription
    */
   public String getInterfacePlatformTypeDescription() {
      return InterfacePlatformTypeDescription;
   }

   /**
    * @param interfacePlatformTypeDescription the interfacePlatformTypeDescription to set
    */
   public void setInterfacePlatformTypeDescription(String interfacePlatformTypeDescription) {
      InterfacePlatformTypeDescription = interfacePlatformTypeDescription;
   }

   /**
    * @return the platformType
    */
   @JsonIgnore
   public PlatformTypeToken getPlatformType() {
      return platformType;
   }

   /**
    * @param platformType the platformType to set
    */
   @JsonIgnore
   public void setPlatformType(PlatformTypeToken platformType) {
      this.platformType = platformType;
   }

   @JsonIgnore
   public ArtifactReadable getArtifactReadable() {
      return this.artifactReadable;
   }

}
