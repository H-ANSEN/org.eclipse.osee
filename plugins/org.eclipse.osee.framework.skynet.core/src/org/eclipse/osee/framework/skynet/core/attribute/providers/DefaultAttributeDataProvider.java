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
package org.eclipse.osee.framework.skynet.core.attribute.providers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.utils.AttributeResourceProcessor;
import org.eclipse.osee.framework.skynet.core.attribute.utils.BinaryContentUtils;
import org.eclipse.osee.jdbc.JdbcConstants;

/**
 * @author Roberto E. Escobar
 */
public class DefaultAttributeDataProvider extends AbstractAttributeDataProvider implements ICharacterAttributeDataProvider {
   private Object rawValue;

   private final DataStore dataStore;

   public DefaultAttributeDataProvider(Attribute<?> attribute) {
      super(attribute);
      this.dataStore = new DataStore(new AttributeResourceProcessor(attribute));
      this.rawValue = "";
   }

   @Override
   public String getDisplayableString() throws OseeCoreException {
      return getValueAsString();
   }

   @Override
   public void setDisplayableString(String toDisplay) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String getValueAsString() throws OseeCoreException {
      String fromStorage = null;
      byte[] data = null;
      try {
         data = dataStore.getContent();
         if (data != null) {
            data = Lib.decompressBytes(new ByteArrayInputStream(data));
            fromStorage = new String(data, "UTF-8");
         }
      } catch (IOException ex) {
         OseeCoreException.wrapAndThrow(ex);
      }
      if (fromStorage != null) {
         return fromStorage;
      } else if (rawValue != null) {
         return rawValue.toString();
      }

      return "";
   }

   @Override
   public boolean setValue(Object value) throws OseeCoreException {
      boolean response = false;
      if (getValueAsString() == value || getValueAsString() != null && getValueAsString().equals(value)) {
         response = false;
      } else {
         storeValue(value);
         response = true;
      }
      return response;
   }

   private String getInternalFileName() throws OseeCoreException {
      return BinaryContentUtils.generateFileName(getAttribute());
   }

   private void storeValue(Object value) throws OseeCoreException {
      if (value != null && value instanceof String && ((String) value).length() > JdbcConstants.JDBC__MAX_VARCHAR_LENGTH) {
         try {
            byte[] compressed =
               Lib.compressStream(new ByteArrayInputStream(((String) value).getBytes("UTF-8")), getInternalFileName());
            dataStore.setContent(compressed, "zip", "application/zip", "ISO-8859-1");
            this.rawValue = "";
         } catch (IOException ex) {
            OseeCoreException.wrapAndThrow(ex);
         }
      } else {
         this.rawValue = value;
         dataStore.clear();
      }
   }

   @Override
   public Object[] getData() {
      return new Object[] {rawValue, dataStore.getLocator()};
   }

   @Override
   public void loadData(Object... objects) throws OseeCoreException {
      if (objects != null && objects.length > 1) {
         storeValue(objects[0]);
         dataStore.setLocator((String) objects[1]);
      }
   }

   @Override
   public void persist(int storageId) throws OseeCoreException {
      dataStore.persist(storageId);
   }

   @Override
   public void purge() throws OseeCoreException {
      dataStore.purge();
   }

   @Override
   public Object getValue() {
      return rawValue;
   }
}
