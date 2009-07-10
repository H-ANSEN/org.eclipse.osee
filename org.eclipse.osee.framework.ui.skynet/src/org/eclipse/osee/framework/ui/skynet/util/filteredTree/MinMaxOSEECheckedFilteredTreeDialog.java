/*
 * Created on Jul 10, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.util.filteredTree;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * @author Donald G. Dunne
 */
public class MinMaxOSEECheckedFilteredTreeDialog extends OSEECheckedFilteredTreeDialog {

   private final int maxSelectionRequired;
   private final int minSelectionRequired;

   public MinMaxOSEECheckedFilteredTreeDialog(String dialogTitle, String dialogMessage, PatternFilter patternFilter, IContentProvider contentProvider, IBaseLabelProvider labelProvider, ViewerSorter viewerSorter, int minSelectionRequired, int maxSelectionRequired) {
      super(dialogTitle, dialogMessage, patternFilter, contentProvider, labelProvider, viewerSorter);
      this.minSelectionRequired = minSelectionRequired;
      this.maxSelectionRequired = maxSelectionRequired;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.util.filteredTree.OSEECheckedFilteredTreeDialog#isComplete()
    */
   @Override
   protected Result isComplete() {
      int numberSelected = getResult().length;
      if (minSelectionRequired <= numberSelected && maxSelectionRequired >= numberSelected) {
         return Result.TrueResult;
      } else {
         List<String> message = new ArrayList<String>();
         if (numberSelected < minSelectionRequired) {
            message.add(String.format("Must select at least [%s]", minSelectionRequired));
         }
         if (numberSelected > maxSelectionRequired) {
            message.add(String.format("Can't select more than [%s]", maxSelectionRequired));
         }
         return new Result(Collections.toString(" &&", message));
      }
   }
}
