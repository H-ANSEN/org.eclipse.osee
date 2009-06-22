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
package org.eclipse.osee.framework.manager.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.data.OseeSession;
import org.eclipse.osee.framework.core.server.OseeHttpServlet;
import org.eclipse.osee.framework.core.server.SessionData;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.HttpProcessor;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class SystemManagerServlet extends OseeHttpServlet {

   private static final long serialVersionUID = 3334123351267606890L;

   private static enum Command {
      user, delete, invalid, overview;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.core.server.OseeHttpServlet#checkAccessControl(javax.servlet.http.HttpServletRequest)
    */
   @Override
   protected void checkAccessControl(HttpServletRequest request) throws OseeCoreException {
      // Allow access to all
   }

   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try {
         Command command = Command.overview;
         String cmd = request.getParameter("cmd");
         if (Strings.isValid(cmd)) {
            command = Command.valueOf(cmd);
         }
         switch (command) {
            case user:
               displayUser(request, response);
               break;
            case delete:
               deleteSession(request, response);
               break;
            default:
               displayOverview(request, response);
               break;
         }
      } catch (Exception ex) {
         OseeLog.log(InternalSystemManagerServletActivator.class, Level.SEVERE, String.format(
               "Error processing request for protocols [%s]", request.toString()), ex);
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         response.setContentType("text/plain");
         response.getWriter().write(Lib.exceptionToString(ex));
      } finally {
         response.getWriter().flush();
         response.getWriter().close();
      }
   }

   private void displayOverview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      String requestAddress = request.getLocalAddr();
      String requestPort = String.valueOf(request.getLocalPort());

      StringBuffer sb = new StringBuffer(1000);
      try {
         sb.append(AHTML.heading(2, "OSEE Dashboard"));
         sb.append(createAnchor(AnchorType.MANAGER_HOME_ANCHOR, null, requestAddress, requestPort));
         sb.append(AHTML.newline(2));
         sb.append(getSessionByUserIdEntry(request, response));
         sb.append(getSessions(requestAddress, requestPort));
      } catch (Exception ex) {
         sb.append("Exception: ");
         sb.append(Lib.exceptionToString(ex));
      }
      displayResults(sb.toString(), request, response);
   }

   private String getSessionByUserIdEntry(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      StringBuffer sb = new StringBuffer(1000);
      try {
         sb.append("<form METHOD=GET ACTION=\"http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/osee/manager\">");
         sb.append("By UserId: <input TYPE=\"text\" NAME=\"userId\" SIZE=\"10\" MAXLENGTH=\"10\">");
         sb.append("<input TYPE=\"hidden\" NAME=\"operation\" VALUE=\"user\">");
         sb.append("<INPUT TYPE=SUBMIT></form>");
      } catch (Exception ex) {
         sb.append("Exception: ");
         sb.append(Lib.exceptionToString(ex));
      }
      return sb.toString();
   }

   private void displayUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      StringBuffer sb = new StringBuffer(1000);
      try {
         HttpSystemManagerCreationInfo info = new HttpSystemManagerCreationInfo(request);
         String requestAddress = request.getLocalAddr();
         String requestPort = String.valueOf(request.getLocalPort());
         String userId = info.userId;
         if (!Strings.isValid(userId)) {
            sb.append("Invalid userId [" + userId + "]");
         } else {
            sb.append(AHTML.heading(2, "OSEE System Manager"));
            sb.append(createAnchor(AnchorType.MANAGER_HOME_ANCHOR, null, requestAddress, requestPort));
            sb.append(AHTML.newline(1));
            sb.append(getSessionsByUserId(userId, requestAddress, requestPort));
         }
      } catch (Exception ex) {
         sb.append("Exception: ");
         sb.append(Lib.exceptionToString(ex));
      }
      displayResults(sb.toString(), request, response);
   }

   private void deleteSession(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      StringBuffer sb = new StringBuffer(1000);
      try {
         HttpSystemManagerCreationInfo info = new HttpSystemManagerCreationInfo(request);
         if (!Strings.isValid(info.sessionId)) {
            sb.append("Invalid userId [" + info.sessionId + "]");
         } else {
            InternalSystemManagerServletActivator.getSessionManager().releaseSessionImmediate(info.sessionId);
            sb.append("Deleted session [" + info.sessionId + "]");
         }
      } catch (OseeCoreException ex) {
         sb.append("Exception: " + ex.getLocalizedMessage());
      }
      displayResults(sb.toString(), request, response);
   }

   private void displayResults(String results, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try {
         response.setStatus(HttpServletResponse.SC_OK);
         response.setContentType("text/html");
         response.setCharacterEncoding("UTF-8");
         response.getWriter().write(results + AHTML.newline() + "As of: " + new Date());
      } catch (Exception ex) {
         OseeLog.log(InternalSystemManagerServletActivator.class, Level.SEVERE, String.format(
               "Error processing request for protocols [%s]", request.toString()), ex);
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         response.setContentType("text/plain");
         response.getWriter().write(Lib.exceptionToString(ex));
      } finally {
         response.getWriter().flush();
         response.getWriter().close();
      }
   }

   private String getSessions(String requestAddress, String requestPort) throws Exception {
      Collection<SessionData> sessionData =
            InternalSystemManagerServletActivator.getSessionManager().getAllSessions(true);
      return createSessionTable(sessionData, "Sessions", requestAddress, requestPort);
   }

   private String getSessionsByUserId(String userId, String requestAddress, String requestPort) throws Exception {
      Collection<SessionData> sessionData =
            InternalSystemManagerServletActivator.getSessionManager().getSessionsByUserId(userId, true);
      return createSessionTable(sessionData, "Sessions for [" + userId + "]", requestAddress, requestPort);
   }

   private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm a");

   enum AnchorType {
      INFO_ANCHOR, LOG_ANCHOR, DELETE_ANCHOR, MANAGER_HOME_ANCHOR;
   }

   private String createAnchor(AnchorType anchorType, String sessionId, String address, String port) throws UnsupportedEncodingException {
      String toReturn = Strings.emptyString();
      switch (anchorType) {
         case INFO_ANCHOR:
            toReturn = String.format("<a href=\"http://%s:%s/osee/request?cmd=info\">info</a>", address, port);
            break;
         case LOG_ANCHOR:
            toReturn = String.format("<a href=\"http://%s:%s/osee/request?cmd=log\">log</a>", address, port);
            break;
         case DELETE_ANCHOR:
            String encodedSessionId = URLEncoder.encode(sessionId, "UTF-8");
            toReturn =
                  String.format("<a href=\"http://%s:%s/%s?operation=delete&sessionId=%s\">delete</a>", address, port,
                        OseeServerContext.MANAGER_CONTEXT, encodedSessionId);
            break;
         case MANAGER_HOME_ANCHOR:
            toReturn =
                  String.format("<a href=\"http://%s:%s/%s\">Home</a>", address, port,
                        OseeServerContext.MANAGER_CONTEXT);
            break;
         default:
            break;
      }
      return toReturn;
   }

   private String createSessionTable(Collection<SessionData> sessionDatas, String title, String requestAddress, String requestPort) throws Exception {
      StringBuffer sb = new StringBuffer(1000);
      sb.append(AHTML.heading(3, title));
      sb.append(AHTML.beginMultiColumnTable(100, 1));
      sb.append(AHTML.addHeaderRowMultiColumnTable(new String[] {"Created", "Alive", "User", "Version", "Machine",
            "Info", "Log", "Last Interaction", "IP", "Port", "Delete"}));

      List<String> items = new ArrayList<String>();
      for (SessionData sessionData : sessionDatas) {
         OseeSession session = sessionData.getSession();
         String sessionId = session.getSessionId();
         String clientAddress = session.getClientAddress();
         String clientPort = String.valueOf(session.getPort());
         boolean isOk = false;
         try {
              // TODO this check takes too long
             //            isOk = HttpProcessor.isAlive(session.getClientAddress(), session.getPort());
         } catch (Exception ex) {
            // OseeLog.log(this.getClass(), Level.SEVERE, ex);
         }
         items.add(AHTML.addRowMultiColumnTable(new String[] {dateFormat.format(session.getCreation()),
               "unknown", session.getUserId(), session.getVersion(), session.getClientMachineName(),
               createAnchor(AnchorType.INFO_ANCHOR, sessionId, clientAddress, clientPort),
               createAnchor(AnchorType.LOG_ANCHOR, sessionId, clientAddress, clientPort),
               dateFormat.format(session.getLastInteractionDate()), clientAddress, clientPort,
               createAnchor(AnchorType.DELETE_ANCHOR, sessionId, requestAddress, requestPort)}));
      }
      Arrays.sort(items.toArray(new String[items.size()]));
      Collections.reverse(items);
      for (String item : items) {
         sb.append(item);
      }
      sb.append(AHTML.endMultiColumnTable());
      return sb.toString();
   }
}
