package org.eclipse.osee.ats.artifact;

import java.util.List;
import org.eclipse.osee.framework.core.util.WorkPageAdapter;
import org.eclipse.osee.framework.core.util.WorkPageType;

public class GoalState extends WorkPageAdapter {
   public static GoalState InWork = new GoalState("InWork", WorkPageType.Working);
   public static GoalState Completed = new GoalState("Completed", WorkPageType.Completed);
   public static GoalState Cancelled = new GoalState("Cancelled", WorkPageType.Cancelled);

   private GoalState(String pageName, WorkPageType workPageType) {
      super(GoalState.class, pageName, workPageType);
   }

   public static GoalState valueOf(String pageName) {
      return WorkPageAdapter.valueOfPage(GoalState.class, pageName);
   }

   public List<GoalState> values() {
      return WorkPageAdapter.pages(GoalState.class);
   }

};
