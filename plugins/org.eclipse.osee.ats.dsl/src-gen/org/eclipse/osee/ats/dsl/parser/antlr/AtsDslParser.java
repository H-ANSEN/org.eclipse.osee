/*
 * generated by Xtext
 */
package org.eclipse.osee.ats.dsl.parser.antlr;

import com.google.inject.Inject;
import org.eclipse.osee.ats.dsl.services.AtsDslGrammarAccess;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;

public class AtsDslParser extends org.eclipse.xtext.parser.antlr.AbstractAntlrParser {

   @Inject
   private AtsDslGrammarAccess grammarAccess;

   @Override
   protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
      tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
   }

   @Override
   protected org.eclipse.osee.ats.dsl.parser.antlr.internal.InternalAtsDslParser createParser(XtextTokenStream stream) {
      return new org.eclipse.osee.ats.dsl.parser.antlr.internal.InternalAtsDslParser(stream, getGrammarAccess());
   }

   @Override
   protected String getDefaultRuleName() {
      return "AtsDsl";
   }

   public AtsDslGrammarAccess getGrammarAccess() {
      return this.grammarAccess;
   }

   public void setGrammarAccess(AtsDslGrammarAccess grammarAccess) {
      this.grammarAccess = grammarAccess;
   }

}
