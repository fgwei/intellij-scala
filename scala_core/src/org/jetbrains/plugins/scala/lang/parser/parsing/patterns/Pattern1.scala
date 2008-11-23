package org.jetbrains.plugins.scala.lang.parser.parsing.patterns

import com.intellij.lang.PsiBuilder
import lexer.ScalaTokenTypes

/**
* @author Alexander Podkhalyuzin
* Date: 28.02.2008
*/

/*
 * Pattern1 ::= varid ':' TypePat
 *            | '_' ':' TypePat
 *            | Pattern2
 */

object Pattern1 {
  def parse(builder: PsiBuilder): Boolean = {

    def isVarId = builder.getTokenText.substring(0, 1).toLowerCase != builder.getTokenText.substring(0, 1) || (
            builder.getTokenText.apply(0) == '`' && builder.getTokenText.apply(builder.getTokenText.length - 1) == '`'
            )

    val pattern1Marker = builder.mark
    val backupMarker = builder.mark
    builder.getTokenType match {
      case ScalaTokenTypes.tIDENTIFIER => {
        if (isVarId) {
          backupMarker.rollbackTo
        }
        else {
          builder.advanceLexer //Ate id
          builder.getTokenType match {
            case ScalaTokenTypes.tCOLON => {
              builder.advanceLexer //Ate :
              backupMarker.drop
              if (!TypePattern.parse(builder)) {
                builder error ScalaBundle.message("wrong.type")
              }
              pattern1Marker.done(ScalaElementTypes.TYPED_PATTERN)
              return true
            }

            case _ => {
              backupMarker.rollbackTo
            }
          }
        }
      }
      case ScalaTokenTypes.tUNDER => {
        builder.advanceLexer //Ate _
        builder.getTokenType match {
          case ScalaTokenTypes.tCOLON => {
            builder.advanceLexer //Ate :
            backupMarker.drop
            if (!TypePattern.parse(builder)) {
              builder error ScalaBundle.message("wrong.type")
            }
            pattern1Marker.done(ScalaElementTypes.TYPED_PATTERN)
            return true
          }
          case _ => {
            backupMarker.rollbackTo
          }
        }
      }
      case _ => {
        backupMarker.drop
      }
    }
    pattern1Marker.drop
    Pattern2.parse(builder, false)
  }
}