/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.search;

import org.eclipse.osee.ats.api.components.AtsSearchHeaderComponent;
import org.eclipse.osee.display.api.components.SearchResultsListComponent;
import org.eclipse.osee.display.api.data.WebId;
import org.eclipse.osee.display.api.search.SearchPresenter;

/*
 * @author John Misinco
 */
public interface AtsWebSearchPresenter extends SearchPresenter {

   void selectSearch(WebId program, WebId build, boolean nameOnly, String searchPhrase);

   void programSelected(AtsSearchHeaderComponent headerComponent, WebId program);

   //overloaded to avoid casting
   void initSearchHome(AtsSearchHeaderComponent headerComponent);

   //overloaded to avoid casting
   void initSearchResults(String url, AtsSearchHeaderComponent searchHeaderComponent, SearchResultsListComponent resultsComponent);
}
