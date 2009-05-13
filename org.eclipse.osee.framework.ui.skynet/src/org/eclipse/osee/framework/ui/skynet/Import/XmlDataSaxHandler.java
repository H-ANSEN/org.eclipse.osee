/**
 * 
 */
package org.eclipse.osee.framework.ui.skynet.Import;

import org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Ryan D. Brooks
 */
public class XmlDataSaxHandler extends AbstractSaxHandler {
   private int level = 0;
   private RoughArtifact roughArtifact;
   private final Branch branch;
   private final AbstractArtifactExtractor extractor;

   /**
    * @param branch
    */
   public XmlDataSaxHandler(AbstractArtifactExtractor extractor, Branch branch) {
      super();
      this.branch = branch;
      this.extractor = extractor;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler#endElementFound(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void endElementFound(String uri, String localName, String name) throws SAXException {
      if (level == 3) {
         roughArtifact.addAttribute(localName, getContents());
      }
      level--;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler#startElementFound(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
   @Override
   public void startElementFound(String uri, String localName, String name, Attributes attributes) throws SAXException {
      level++;

      if (level == 2) {
         roughArtifact = new RoughArtifact(RoughArtifactKind.PRIMARY, branch);
         extractor.addRoughArtifact(roughArtifact);
      }
   }
}