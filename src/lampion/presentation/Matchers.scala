/*
 * Copyright 2005-2008 LAMP/EPFL
 * @author Sean McDirmid
 */
// $Id$

package lampion.presentation;

trait Matchers extends AutoEdits {
  type Project <: ProjectImpl
  trait ProjectImpl extends super.ProjectImpl with compiler.NewMatchers {
    type File <: FileImpl
    trait FileImpl extends super[ProjectImpl].FileImpl with super[NewMatchers].FileImpl {selfX : File => 
      def self : File
      override def doParsing = {
        if (isMatched) {
          super.doParsing
        } else {
          assert(true)
          assert(true)
        }
      }
      protected def overwriteOk(offset : Int) = !isNewline(content(offset)) && isCompleted(offset + 1)
      override def beforeEdit(edit : Edit) : Edit = {
        if (isMatched) {
          if (edit.text.length == 1 && edit.length == 0 && edit.offset < content.length && 
              edit.text.charAt(0) == content(edit.offset) && overwriteOk(edit.offset)) {
            val enclosing = FileImpl.this.enclosing(edit.offset)
            if (enclosing.isDefined && 
              edit.offset >= enclosing.get.until - enclosing.get.close.length) {
              unmakeCompleted(edit.offset + 1)
              return NoEdit
            }
            
          } else if (edit.text.length == 0 && edit.length == 1) {
            val enclosing = FileImpl.this.enclosing(edit.offset + 1)
            if (enclosing.isDefined &&
                edit.offset + 1 == enclosing.get.from + enclosing.get.kind.open.length &&
                edit.offset + 1 == enclosing.get.until - enclosing.get.close.length &&
                isCompleted(enclosing.get.until)) { 
              return new Edit(edit.offset, 1 + enclosing.get.close.length, "") {
                override def moveCursorTo = edit.offset
              }
            }
          }
        }
        super.beforeEdit(edit)
      }
      protected def indentFor(m : Match, sentry : Int, leading : Int) : Option[RandomAccessSeq[Char]] = None
      protected override def autoIndent(sentry : Int, leading : Int) : RandomAccessSeq[Char] = {
        enclosing(sentry,leading).map(m => indentFor(m,sentry,leading)) getOrElse None getOrElse
          super.autoIndent(sentry, leading)
      }  
      protected def completeOpen(offset : Int, kind : OpenMatch) = offset + kind.open.length
      override def afterEdit(offset : Int, added : Int, removed : Int) : Edits = {
        var edits = super.afterEdit(offset, added, removed)
        if (edits ne NoEdit) return edits
        if (added == 1 && removed == 0) {} else return edits
        rebalance(offset + added) match {
        case Some(CompleteBrace(kind, close)) => 
          val at = completeOpen(offset + 1 - kind.open.length, kind)
          val content0 = content.drop(at)
          val length = if (isUnmatchedClose(at + 1)) 1 else 0
          return edits * new Edit(at, length, close) {
            override def afterEdit = {
              super.afterEdit
              makeCompleted(at + close.length)
            }
          }
        case Some(DeleteClose(from,length)) =>
          if (isCompleted(from)) 
            return edits * new Edit(from - length, length, "")
        case _ => 
        }
        edits
      }
      type Token <: TokenImpl
      trait TokenImpl extends super.TokenImpl {
        def self : Token
        override def hover : Option[RandomAccessSeq[Char]] = super.hover orElse {
          if (syncUI{isUnmatchedOpen(offset)}) Some("unmatched open brace")
          else if (syncUI{isUnmatchedClose(extent)}) Some("unmatched close brace")
          else None 
        }

        // matches are made at the edge
        def border(dir : Dir) = {
          if (dir == NEXT) FileImpl.this.border(offset, dir).map(_.until)
          else FileImpl.this.border(extent, dir).map(_.from)
        }
        def findWithMatch(dir : Dir)(f : Token => Boolean) : Option[Token] = {
          var tok : Option[Token] = Some(self)
          while (true) {
            if (tok.isEmpty) return None
            else if (tok.get != self && 
                     f(tok.get)) return tok
            val nextBorder = tok.get.border(dir) 
            def prevBorder = tok.get.border(dir.reverse)
            if (!nextBorder.isEmpty) {
              tok = tokenFor(nextBorder.get).apply(dir)
            } else if (tok.get != self && !prevBorder.isEmpty) return None 
            else tok = tok.get.apply(dir)
          }
          None
        }
      }
    }
  }
}
