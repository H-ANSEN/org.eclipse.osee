/*********************************************************************
 * Copyright (c) 2023 Boeing
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

package org.eclipse.osee.framework.core.xml.publishing;

import java.util.Optional;

/**
 * Class to encapsulate a list of {@link WordRunStyle} elements.
 *
 * @author Loren K. Ashley
 */

public class WordRunStyleList extends AbstractElementList<WordRun, WordRunStyle> {

   /**
    * Creates a new open and empty list for {@link WordRunStyles}s.
    *
    * @param wordRun the {@link WordRun} that contains this {@link WordRunStyle}.
    * @throws NullPointerException when the parameter <code>wordRun</code> is <code>null</code>.
    */

   public WordRunStyleList(WordRun wordRun) {
      super(wordRun);
   }

   /**
    * Gets the containing (parent) {@link WordRun}.
    *
    * @return the containing {@link WordRun}.
    */

   @SuppressWarnings("cast")
   public Optional<WordRun> getWordRun() {
      var parent = this.getParent();
      //@formatter:off
      return
         (parent instanceof WordRun)
            ? Optional.of( parent )
            : Optional.empty();
      //@formatter:on
   }
}

/* EOF */