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
package org.eclipse.osee.ats.util.widgets.dialog;

/**
 * @author Donald G. Dunne
 */
public class SimpleTaskResolutionOptionsRule extends TaskResolutionOptionRule {

   public final static String ID = "ats.simpleTaskResolutionOptions";
   public static enum States {
      None,
      In_Work,
      Waiting,
      Coded,
      Awaiting_Review,
      No_Change,
      Complete
   };
   public static String resolutionOptionsXml =
      "<AtsTaskOptions> " +
      //
      "<AtsTaskOption name=\"" + States.None.name() + "\" desc=\"Nothing has been done.\" complete=\"false\" percent=\"0\"/> " +
      //
      "<AtsTaskOption name=\"In_Work\" desc=\"Working on task.\" complete=\"false\" percent=\"15\"/> " +
      //
      "<AtsTaskOption name=\"Waiting\" desc=\"Waiting on some other dependency.\" complete=\"false\" percent=\"15\" color=\"DARK_RED\"/> " +
      //
      "<AtsTaskOption name=\"Coded\" desc=\"Code is completed but not tested.\" complete=\"false\" percent=\"75\"/> " +
      //
      "<AtsTaskOption name=\"Awaiting_Review\" desc=\"Awaiting a review on changes made.\" complete=\"false\" percent=\"95\" color=\"DARK_RED\"/> " +
      //
      "<AtsTaskOption name=\"No_Change\" desc=\"No change to code is necessary.\" complete=\"true\" percent=\"100\" color=\"DARK_GREEN\"/> " +
      //
      "<AtsTaskOption name=\"Complete\" desc=\"Code finished.\" complete=\"true\" percent=\"100\" color=\"DARK_GREEN\"/> " +
      //
      "</AtsTaskOptions>";

}
