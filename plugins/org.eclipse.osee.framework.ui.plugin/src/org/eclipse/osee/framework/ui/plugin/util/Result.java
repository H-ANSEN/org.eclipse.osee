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

package org.eclipse.osee.framework.ui.plugin.util;

import org.eclipse.osee.framework.jdk.core.util.Strings;

public class Result {

   public static Result TrueResult = new Result(true);
   public static Result FalseResult = new Result(false);
   public static Result CancelledResult = new Result(false, true);
   private final boolean isTrue;
   private boolean cancelled = false;
   private String text = "";

   public Result(boolean valid, String text) {
      this.isTrue = valid;
      this.text = text;
   }

   public Result(String text) {
      this.isTrue = false;
      this.text = text;
   }

   public Result(boolean isTrue) {
      this.isTrue = isTrue;
   }

   public Result(boolean isTrue, boolean cancelled) {
      this.isTrue = isTrue;
      this.cancelled = cancelled;
   }

   public Result() {
      this.isTrue = true;
   }

   public boolean isTrue() {
      return isTrue;
   }

   public boolean isFalse() {
      return !isTrue;
   }

   public String getText() {
      return text;
   }

   @Override
   public String toString() {
      return String.valueOf(isTrue()) + (getText().equals("") ? "" : " - \"" + getText() + "\"");
   }

   public void popup() {
      AWorkbench.popup((isTrue ? "Success" : "ERROR"),
         Strings.isValid(text) ? text : isTrue ? "Success" : "Error Encountered.  See Error Log View");
   }

   public void setText(String text) {
      this.text = text;
   }

   public boolean isCancelled() {
      return cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}