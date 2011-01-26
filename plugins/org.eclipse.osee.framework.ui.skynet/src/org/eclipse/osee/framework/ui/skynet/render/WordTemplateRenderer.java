/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.framework.ui.skynet.render;

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERAL_REQUESTED;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.linking.LinkType;
import org.eclipse.osee.framework.skynet.core.linking.OseeLinkBuilder;
import org.eclipse.osee.framework.skynet.core.linking.WordMlLinkHandler;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;
import org.eclipse.osee.framework.ui.skynet.render.compare.WordTemplateCompare;
import org.eclipse.osee.framework.ui.skynet.render.word.AttributeElement;
import org.eclipse.osee.framework.ui.skynet.render.word.Producer;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;
import org.eclipse.osee.framework.ui.skynet.render.word.WordTemplateProcessor;
import org.eclipse.osee.framework.ui.skynet.templates.TemplateManager;
import org.eclipse.osee.framework.ui.skynet.util.WordUiUtil;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.w3c.dom.Element;

/**
 * Renders WordML content.
 * 
 * @author Jeff C. Phillips
 */
public class WordTemplateRenderer extends WordRenderer implements ITemplateRenderer {
   public static final String DEFAULT_SET_NAME = "Default";
   public static final String ARTIFACT_SCHEMA = "http://eclipse.org/artifact.xsd";
   private static final String EMBEDDED_OBJECT_NO = "w:embeddedObjPresent=\"no\"";
   private static final String EMBEDDED_OBJECT_YES = "w:embeddedObjPresent=\"yes\"";
   private static final String STYLES_END = "</w:styles>";
   private static final String OLE_START = "<w:docOleData>";
   private static final String OLE_END = "</w:docOleData>";
   private static final QName fo = new QName("ns0", "unused_localname", ARTIFACT_SCHEMA);
   public static final String UPDATE_PARAGRAPH_NUMBER_OPTION = "updateParagraphNumber";

   private final WordTemplateProcessor templateProcessor = new WordTemplateProcessor(this);

   private final IComparator comparator;

   public WordTemplateRenderer() {
      super();
      this.comparator = new WordTemplateCompare(this);
   }

   @Override
   public List<String> getCommandIds(CommandGroup commandGroup) {
      ArrayList<String> commandIds = new ArrayList<String>(2);

      if (commandGroup.isPreview()) {
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wordpreview.command");
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wordpreviewChildren.command");
      }
      if (commandGroup.isEdit()) {
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wordeditor.command");
      }

      return commandIds;
   }

   @Override
   public WordTemplateRenderer newInstance() {
      return new WordTemplateRenderer();
   }

   public void publish(VariableMap variableMap, Artifact masterTemplateArtifact, Artifact slaveTemplateArtifact, List<Artifact> artifacts) throws OseeCoreException {
      templateProcessor.publishWithExtensionTemplates(variableMap, masterTemplateArtifact, slaveTemplateArtifact,
         artifacts);
   }

   /**
    * Displays a list of artifacts in the Artifact Explorer that could not be multi edited because they contained
    * artifacts that had an OLEData attribute.
    */
   private void displayNotMultiEditArtifacts(final Collection<Artifact> artifacts, final String warningString) {
      if (!artifacts.isEmpty()) {
         Displays.ensureInDisplayThread(new Runnable() {

            @Override
            public void run() {
               WordUiUtil.displayUnhandledArtifacts(artifacts, warningString);
            }
         });
      }
   }

   public static QName getFoNamespace() {
      return fo;
   }

   public static byte[] getFormattedContent(Element formattedItemElement) {
      ByteArrayOutputStream data = new ByteArrayOutputStream((int) Math.pow(2, 10));
      OutputFormat format = Jaxp.getCompactFormat(formattedItemElement.getOwnerDocument());
      format.setOmitDocumentType(true);
      format.setOmitXMLDeclaration(true);
      XMLSerializer serializer = new XMLSerializer(data, format);

      try {
         for (Element e : Jaxp.getChildDirects(formattedItemElement)) {
            serializer.serialize(e);
         }
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }

      return data.toByteArray();
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, Artifact artifact) throws OseeCoreException {
      if (!presentationType.matches(GENERALIZED_EDIT, GENERAL_REQUESTED)) {
         if (artifact.isAttributeTypeValid(CoreAttributeTypes.WordTemplateContent)) {
            return PRESENTATION_SUBTYPE_MATCH;
         }
         if (!presentationType.matches(PresentationType.SPECIALIZED_EDIT, PresentationType.PRODUCE_ATTRIBUTE)) {
            return IRenderer.DEFAULT_MATCH;
         }
      }
      return NO_MATCH;
   }

   @Override
   public void renderAttribute(IAttributeType attributeType, Artifact artifact, PresentationType presentationType, Producer producer, AttributeElement attributeElement) throws OseeCoreException {
      WordMLProducer wordMl = (WordMLProducer) producer;

      if (attributeType.equals(CoreAttributeTypes.WordTemplateContent)) {
         Attribute<?> wordTempConAttr = artifact.getSoleAttribute(attributeType);
         String data = (String) wordTempConAttr.getValue();

         if (attributeElement.getLabel().length() > 0) {
            wordMl.addParagraph(attributeElement.getLabel());
         }

         if (data != null) {
            //Change the BinData Id so images do not get overridden by the other images
            data = WordUtil.reassignBinDataID(data);

            LinkType linkType = (LinkType) getOption("linkType");
            data = WordMlLinkHandler.link(linkType, artifact, data);
            data = WordUtil.reassignBookMarkID(data);
         }

         if (presentationType == PresentationType.SPECIALIZED_EDIT) {
            OseeLinkBuilder linkBuilder = new OseeLinkBuilder();
            wordMl.addEditParagraphNoEscape(linkBuilder.getStartEditImage(artifact.getGuid()));
            wordMl.addWordMl(data);
            wordMl.addEditParagraphNoEscape(linkBuilder.getEndEditImage(artifact.getGuid()));

         } else {
            wordMl.addWordMl(data);
         }
         wordMl.resetListValue();
      } else {
         super.renderAttribute(attributeType, artifact, PresentationType.SPECIALIZED_EDIT, wordMl, attributeElement);
      }
   }

   @Override
   public InputStream getRenderInputStream(PresentationType presentationType, List<Artifact> artifacts) throws OseeCoreException {
      final List<Artifact> notMultiEditableArtifacts = new LinkedList<Artifact>();
      String template;

      if (artifacts.isEmpty()) {
         //  Still need to get a default template with a null artifact list
         template = getTemplate(null, presentationType);
      } else {
         Artifact firstArtifact = artifacts.iterator().next();
         template = getTemplate(firstArtifact, presentationType);

         if (presentationType == PresentationType.SPECIALIZED_EDIT && artifacts.size() > 1) {
            // currently we can't support the editing of multiple artifacts with OLE data
            for (Artifact artifact : artifacts) {
               if (!artifact.getSoleAttributeValue(CoreAttributeTypes.WordOleData, "").equals("")) {
                  notMultiEditableArtifacts.add(artifact);
               }
            }
            displayNotMultiEditArtifacts(notMultiEditableArtifacts,
               "Do not support editing of multiple artifacts with OLE data");
            artifacts.removeAll(notMultiEditableArtifacts);
         } else { // support OLE data when appropriate
            if (!firstArtifact.getSoleAttributeValue(CoreAttributeTypes.WordOleData, "").equals("")) {
               template = template.replaceAll(EMBEDDED_OBJECT_NO, EMBEDDED_OBJECT_YES);
               template =
                  template.replaceAll(
                     STYLES_END,
                     STYLES_END + OLE_START + firstArtifact.getSoleAttributeValue(CoreAttributeTypes.WordOleData, "") + OLE_END);
            }
         }
      }

      template = WordUtil.removeGUIDFromTemplate(template);
      return templateProcessor.applyTemplate(getOptions(), artifacts, template, null,
         getOptions() != null ? getOptions().getString("paragraphNumber") : null,
         getOptions() != null ? getOptions().getString("outlineType") : null, presentationType);
   }

   protected String getTemplate(Artifact artifact, PresentationType presentationType) throws OseeCoreException {
      Artifact templateArtifact =
         TemplateManager.getTemplate(this, artifact, presentationType, getStringOption(TEMPLATE_OPTION));
      return templateArtifact.getSoleAttributeValue(CoreAttributeTypes.WholeWordContent);
   }

   @Override
   public IComparator getComparator() {
      return comparator;
   }

   @Override
   protected IOperation getUpdateOperation(File file, List<Artifact> artifacts, Branch branch, PresentationType presentationType) throws OseeCoreException {
      return new UpdateArtifactOperation(file, artifacts, branch, false);
   }
}