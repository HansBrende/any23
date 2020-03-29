package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.DocumentType;

import java.io.IOException;
import java.io.Reader;

/**
 * This class is internal API only and may be changed on a whim. Use at your own risk!
 *
 * @author Hans Brende (hansbrende@apache.org)
 */
public abstract class InternalAny23Ext {
    protected abstract void doctype(DocumentType docType);
    protected abstract void comment(String data);
    protected abstract void text(String text);
    protected abstract void startTag(String name, Attributes attributes, boolean selfClosing);
    protected abstract void endTag(String name);

    private Tokeniser tokeniser;

    protected final void init(Reader in) {
        this.tokeniser = new Tokeniser(new CharacterReader(in), ParseErrorList.noTracking());
    }

    protected final void extractText() {
        String currentDataTagName = null;
        Token t;
        while ((t = tokeniser.read()) != null && !t.isEOF()) {
            switch (t.type) {
                case Doctype:
                    Token.Doctype d = t.asDoctype();
                    doctype(new DocumentType(d.getName(), d.getPublicIdentifier(), d.getSystemIdentifier()));
                    break;
                case Comment:
                    comment(t.asComment().getData());
                    break;
                case StartTag:
                    Token.StartTag startTag = t.asStartTag();
                    String startTagName = startTag.normalName();
                    boolean isSelfClosing = startTag.isSelfClosing();
                    startTag(startTagName, startTag.getAttributes(), isSelfClosing);

                    if (currentDataTagName == null && !isSelfClosing) {
                        if ("script".equals(startTagName)) {
                            // otherwise will create new element for <n.length ... in e.g., /html/rdfa/rdfa-issue268-and-317.html
                            tokeniser.transition(TokeniserState.ScriptData);
                            currentDataTagName = startTagName;
                        } else if ("style".equals(startTagName)) {
                            tokeniser.transition(TokeniserState.Rawtext);
                            currentDataTagName = startTagName;
                        }
                        // TODO: HtmlTreeBuilder also transitions "plaintext" to TokeniserState.PLAINTEXT,
                        //  "noframes", "xmp", "iframe", and "noembed" to TokeniserState.Rawtext, and
                        //  "textarea" and "title" to TokeniserState.Rcdata. Need we follow suit?
                        //  plaintext, xmp, noframes and noembed are obsolete so don't worry about them
                        //  leaving textarea, title, iframe
                    } else {
                        currentDataTagName = null;
                    }
                    break;
                case EndTag:
                    String endTagName = t.asEndTag().normalName();
                    endTag(endTagName);
                    if (endTagName.equals(currentDataTagName)) {
                        currentDataTagName = null;
                    }
                    break;
                case Character:
                    if (currentDataTagName == null) {
                        text(t.asCharacter().getData());
                    }
                    break;
                default:
                    throw new AssertionError("unrecognized jsoup token type: " + t.type);
            }
        }
    }
}
