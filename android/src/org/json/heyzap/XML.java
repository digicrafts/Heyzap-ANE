package org.json.heyzap;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.util.Iterator;

import org.json.heyzap.JSONArray;
import org.json.heyzap.JSONException;
import org.json.heyzap.JSONObject;


/**
 * This provides static methods to convert an XML text into a JSONObject,
 * and to covert a JSONObject into an XML text.
 * @author JSON.org
 * @version 2010-12-23
 */
public class XML {

    /** The Character '&'. */
    public static final Character AMP   = new Character('&');

    /** The Character '''. */
    public static final Character APOS  = new Character('\'');

    /** The Character '!'. */
    public static final Character BANG  = new Character('!');

    /** The Character '='. */
    public static final Character EQ    = new Character('=');

    /** The Character '>'. */
    public static final Character GT    = new Character('>');

    /** The Character '<'. */
    public static final Character LT    = new Character('<');

    /** The Character '?'. */
    public static final Character QUEST = new Character('?');

    /** The Character '"'. */
    public static final Character QUOT  = new Character('"');

    /** The Character '/'. */
    public static final Character SLASH = new Character('/');

    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    public static String escape(String string) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Throw an exception if the string contains whitespace. 
     * Whitespace is not allowed in tagNames and attributes.
     * @param string
     * @throws JSONException
     */
    public static void noSpace(String string) throws JSONException {
    	int i, length = string.length();
    	if (length == 0) {
    		throw new JSONException("Empty string.");
    	}
    	for (i = 0; i < length; i += 1) {
		    if (Character.isWhitespace(string.charAt(i))) {
		    	throw new JSONException("'" + string + 
		    			"' contains a space character.");
		    }
		}
    }

    /**
     * Scan the content following the named tag, attaching it to the context.
     * @param x       The XMLTokener containing the source string.
     * @param context The JSONObject that will include the new material.
     * @param name    The tag name.
     * @return true if the close tag is processed.
     * @throws JSONException
     */
    private static boolean parse(XMLTokener x, JSONObject context,
                                 String name) throws JSONException {
        char       c;
        int        i;
        String     n;
        JSONObject o = null;
        String     s;
        Object     t;

// Test for and skip past these forms:
//      <!-- ... -->
//      <!   ...   >
//      <![  ... ]]>
//      <?   ...  ?>
// Report errors for these forms:
//      <>
//      <=
//      <<

        t = x.nextToken();

// <!

        if (t == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                t = x.nextToken();
                if (t.equals("CDATA")) {
                    if (x.next() == '[') {
                        s = x.nextCDATA();
                        if (s.length() > 0) {
                            context.accumulate("content", s);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                t = x.nextMeta();
                if (t == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (t == LT) {
                    i += 1;
                } else if (t == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (t == QUEST) {

// <?

            x.skipPast("?>");
            return false;
        } else if (t == SLASH) {

// Close tag </

        	t = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag" + t);
            }            
            if (!t.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + t);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;

        } else if (t instanceof Character) {
            throw x.syntaxError("Misshaped tag");

// Open tag <

        } else {
            n = (String)t;
            t = null;
            o = new JSONObject();
            for (;;) {
                if (t == null) {
                    t = x.nextToken();
                }

// attribute = value

                if (t instanceof String) {
                    s = (String)t;
                    t = x.nextToken();
                    if (t == EQ) {
                        t = x.nextToken();
                        if (!(t instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }
                        o.accumulate(s, XML.stringToValue((String)t));
                        t = null;
                    } else {
                        o.accumulate(s, "");
                    }

// Empty tag <.../>

                } else if (t == SLASH) {
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (o.length() > 0) {
                        context.accumulate(n, o);
                    } else {
                    	context.accumulate(n, "");
                    }
                    return false;

// Content, between <...> and </...>

                } else if (t == GT) {
                    for (;;) {
                        t = x.nextContent();
                        if (t == null) {
                            if (n != null) {
                                throw x.syntaxError("Unclosed tag " + n);
                            }
                            return false;
                        } else if (t instanceof String) {
                            s = (String)t;
                            if (s.length() > 0) {
                                o.accumulate("content", XML.stringToValue(s));
                            }

// Nested element

                        } else if (t == LT) {
                            if (parse(x, o, n)) {
                                if (o.length() == 0) {
                                    context.accumulate(n, "");
                                } else if (o.length() == 1 &&
                                       o.opt("content") != null) {
                                    context.accumulate(n, o.opt("content"));
                                } else {
                                    context.accumulate(n, o);
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }


    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string. This is much less ambitious than
     * JSONObject.stringToValue, especially because it does not attempt to
     * convert plus forms, octal forms, hex forms, or E forms lacking decimal 
     * points.
     * @param string A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

// If it might be a number, try converting it. If that doesn't work, 
// return the string.

        try {
	        char initial = string.charAt(0);
	        boolean negative = false;
	        if (initial == '-') {
	        	initial = string.charAt(1);
	        	negative = true;
	        }
	        if (initial == '0' && string.charAt(negative ? 2 : 1) == '0') {
	        	return string;
	        }
	        if ((initial >= '0' && initial <= '9')) {
                if (string.indexOf('.') >= 0) {
                    return Double.valueOf(string);
                } else if (string.indexOf('e') < 0 && string.indexOf('E') < 0) {
                    Long myLong = new Long(string);
                    if (myLong.longValue() == myLong.intValue()) {
                        return new Integer(myLong.intValue());
                    } else {
                        return myLong;
                    }
                }
	        }
        }  catch (Exception ignore) {
        }
        return string;
    }

    
    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation
     * because JSON is a data format and XML is a document format. XML uses
     * elements, attributes, and content text, while JSON uses unordered
     * collections of name/value pairs and arrays of values. JSON does not
     * does not like to distinguish between elements and attributes.
     * Sequences of similar elements are represented as JSONArrays. Content
     * text may be placed in a "content" member. Comments, prologs, DTDs, and
     * <code>&lt;[ [ ]]></code> are ignored.
     * @param string The source string.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException
     */
    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject o = new JSONObject();
        XMLTokener x = new XMLTokener(string);
        while (x.more() && x.skipPast("<")) {
            parse(x, o, null);
        }
        return o;
    }


    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     * @param o A JSONObject.
     * @return  A string.
     * @throws  JSONException
     */
    public static String toString(Object o) throws JSONException {
        return toString(o, null);
    }


    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     * @param o A JSONObject.
     * @param tagName The optional name of the enclosing tag.
     * @return A string.
     * @throws JSONException
     */
    public static String toString(Object o, String tagName)
            throws JSONException {
        StringBuffer b = new StringBuffer();
        int          i;
        JSONArray    ja;
        JSONObject   jo;
        String       k;
        Iterator     keys;
        int          len;
        String       s;
        Object       v;
        if (o instanceof JSONObject) {

// Emit <tagName>

            if (tagName != null) {
                b.append('<');
                b.append(tagName);
                b.append('>');
            }

// Loop thru the keys.

            jo = (JSONObject)o;
            keys = jo.keys();
            while (keys.hasNext()) {
                k = keys.next().toString();
                v = jo.opt(k);
                if (v == null) {
                	v = "";
                }
                if (v instanceof String) {
                    s = (String)v;
                } else {
                    s = null;
                }

// Emit content in body

                if (k.equals("content")) {
                    if (v instanceof JSONArray) {
                        ja = (JSONArray)v;
                        len = ja.length();
                        for (i = 0; i < len; i += 1) {
                            if (i > 0) {
                                b.append('\n');
                            }
                            b.append(escape(ja.get(i).toString()));
                        }
                    } else {
                        b.append(escape(v.toString()));
                    }

// Emit an array of similar keys

                } else if (v instanceof JSONArray) {
                    ja = (JSONArray)v;
                    len = ja.length();
                    for (i = 0; i < len; i += 1) {
                    	v = ja.get(i);
                    	if (v instanceof JSONArray) {
                            b.append('<');
                            b.append(k);
                            b.append('>');
                    		b.append(toString(v));
                            b.append("</");
                            b.append(k);
                            b.append('>');
                    	} else {
                    		b.append(toString(v, k));
                    	}
                    }
                } else if (v.equals("")) {
                    b.append('<');
                    b.append(k);
                    b.append("/>");

// Emit a new tag <k>

                } else {
                    b.append(toString(v, k));
                }
            }
            if (tagName != null) {

// Emit the </tagname> close tag

                b.append("</");
                b.append(tagName);
                b.append('>');
            }
            return b.toString();

// XML does not have good support for arrays. If an array appears in a place
// where XML is lacking, synthesize an <array> element.

        } else if (o instanceof JSONArray) {
            ja = (JSONArray)o;
            len = ja.length();
            for (i = 0; i < len; ++i) {
            	v = ja.opt(i);
                b.append(toString(v, (tagName == null) ? "array" : tagName));
            }
            return b.toString();
        } else {
            s = (o == null) ? "null" : escape(o.toString());
            return (tagName == null) ? "\"" + s + "\"" :
                (s.length() == 0) ? "<" + tagName + "/>" :
                "<" + tagName + ">" + s + "</" + tagName + ">";
        }
    }
}