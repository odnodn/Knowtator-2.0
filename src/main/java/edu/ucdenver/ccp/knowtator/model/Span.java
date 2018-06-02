package edu.ucdenver.ccp.knowtator.model;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLAttributes;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLTags;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Span implements Savable, KnowtatorTextBoundObject, Comparable<Span> {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(KnowtatorController.class);

  private int start;

  private int end;
  private Annotation annotation;
  private String spannedText;
  private String id;
  private TextSource textSource;

  public Span(
      String id, int start, int end, TextSource textSource, KnowtatorController controller) {

    this.textSource = textSource;
    this.start = start;
    this.end = end;

    controller.verifyId(id, this, false);

    if (start > end) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is greater than the end of it: start="
              + start
              + " end="
              + end);
    }
    if (start < 0) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is less than zero: start=" + start);
    }
    this.spannedText = textSource.getContent().substring(start, end);
  }

  public Span(int start, int end) {
    this.start = start;
    this.end = end;
    if (start > end) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is greater than the end of it: start="
              + start
              + " end="
              + end);
    }
    if (start < 0) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is less than zero: start=" + start);
    }
  }

  public static boolean intersects(TreeSet<Span> spans1, TreeSet<Span> spans2) {
    for (Span span1 : spans1) {
      for (Span span2 : spans2) {
        if (span1.intersects(span2)) return true;
      }
    }
    return false;
  }

  /**
   * This method assumes that the both lists of spans are sorted the same way and that a Span in one
   * list at the same index as a Span in the other list should be the same.
   *
   * @param spans1 sorted list of c
   * @param spans2 sorted list of spans
   * @return true if the two lists of spans are the same.
   */
  public static boolean spansMatch(TreeSet<Span> spans1, TreeSet<Span> spans2) {
    if (spans1.size() == spans2.size()) {
      Iterator<Span> spans1Iterator = spans1.iterator(), spans2Iterator = spans2.iterator();
      while (spans1Iterator.hasNext()) {
        if (!spans1Iterator.next().equalStartAndEnd(spans2Iterator.next())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public static String substring(String string, Span span) {
    int start = Math.max(0, span.getStart());
    start = Math.min(start, string.length() - 1);
    int end = Math.max(0, span.getEnd());
    end = Math.min(end, string.length() - 1);
    return string.substring(start, end);
  }

  //  public static int compare(Span span1, Span span2) {
  //    if (span1 == span2) {
  //      return 0;
  //    }
  //
  //    int compare = span1.getStart().compareTo(span2.getStart());
  //    if (compare == 0) {
  //      compare = span1.getEnd().compareTo(span2.getEnd());
  //    }
  //    if (compare == 0) {
  //      return -1;
  //    }
  //    return compare;
  //  }

  public int compare(Span span2) {
    if (span2 == null) {
      return 1;
    }
    int compare = getStart().compareTo(span2.getStart());
    if (compare == 0) {
      compare = getEnd().compareTo(span2.getEnd());
    }
    if (compare == 0) {
      compare = id.compareTo(span2.getId());
    }
    return compare;
  }

  private boolean equalStartAndEnd(Object object) {
    if (!(object instanceof Span)) {
      return false;
    }
    Span span = (Span) object;
    return Objects.equals(getStart(), span.getStart()) && Objects.equals(getEnd(), span.getEnd());
  }

  public int hashCode() {
    return ((this.start << 16) | (0x0000FFFF | this.end));
  }

  private boolean contains(Span span) {
    return (getStart() <= span.getStart() && span.getEnd() <= getEnd());
  }

  public boolean contains(int i) {
    return (getStart() <= i && i < getEnd());
  }

  /** we need some junit tests */
  public boolean intersects(Span span) {
    int spanStart = span.getStart();
    // either Span's start is in this or this' start is in Span
    return this.contains(span)
        || span.contains(this)
        || (getStart() <= spanStart && spanStart < getEnd()
            || spanStart <= getStart() && getStart() < span.getEnd());
  }

  void shrinkEnd() {
    if (end > start) end -= 1;
  }

  void shrinkStart() {
    if (start < end) start += 1;
  }

  void growEnd(int limit) {
    if (end < limit) end += 1;
  }

  void growStart() {
    if (start > 0) start -= 1;
  }

  public String toString() {
    return String.format("Start: %d, End: %d", start, end);
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public void setAnnotation(Annotation annotation) {
    this.annotation = annotation;
  }

  public String getSpannedText() {
    return spannedText;
  }

  public Integer getStart() {
    return start;
  }

  public Integer getEnd() {
    return end;
  }

  public int getSize() {
    return getEnd() - getStart();
  }

  /**
  These methods are intended to correct for Java's handling of supplementary unicode characters.
   */
  public int getStartCodePoint() { return Character.codePointCount(textSource.getContent(), 0, start); }

  public int getEndCodePoint() { return Character.codePointCount(textSource.getContent(), 0, end); }

  public void writeToKnowtatorXML(Document dom, Element annotationElem) {
    Element spanElement = dom.createElement(KnowtatorXMLTags.SPAN);
    spanElement.setAttribute(KnowtatorXMLAttributes.SPAN_START, String.valueOf(getStart()));
    spanElement.setAttribute(KnowtatorXMLAttributes.SPAN_END, String.valueOf(getEnd()));
    spanElement.setAttribute(KnowtatorXMLAttributes.ID, id);
    spanElement.setTextContent(getSpannedText());
    annotationElem.appendChild(spanElement);
  }

  @Override
  public void readFromKnowtatorXML(File file, Element parent) {}

  @Override
  public void readFromOldKnowtatorXML(File file, Element parent) {}

  @Override
  public void readFromBratStandoff(
      File file, Map<Character, List<String[]>> annotationMap, String content) {}

  @Override
  public void writeToBratStandoff(Writer writer, Map<String, Map<String, String>> annotationsConfig, Map<String, Map<String, String>> visualConfig) throws IOException {
    String[] spanLines = getSpannedText().split("\n");
    int spanStart = getStart();
    for (int j = 0; j < spanLines.length; j++) {
      writer.append(String.format("%d %d", spanStart, spanStart + spanLines[j].length()));
      if (j != spanLines.length -1) {
        writer.append(";");
      }
      spanStart += spanLines[j].length() + 1;
    }

  }

  @Override
  public void readFromGeniaXML(Element parent, String content) {}

  @Override
  public void writeToGeniaXML(Document dom, Element parent) {}

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public TextSource getTextSource() {
    return textSource;
  }

  public void dispose() {
  }

  @Override
  public int compareTo(Span o) {
    return compare(o);
  }


  //	public static Span shortest(List<Span> spans) {
  //		if (spans.size() == 0)
  //			return null;
  //		if (spans.size() == 1)
  //			return spans.get(0);
  //
  //		Span shortestSpan = spans.get(0);
  //		int shortestSize = shortestSpan.getSize();
  //		for (int i = 1; i < spans.size(); i++) {
  //			if (spans.get(i).getSize() < shortestSize) {
  //				shortestSpan = spans.get(i);
  //				shortestSize = shortestSpan.getSize();
  //			}
  //		}
  //
  //		return shortestSpan;
  //	}

  //	public int compareTo(Span span) {
  //		if (getStart() < span.getStart()) {
  //			return -1;
  //		} else if (getStart() == span.getStart()) {
  //			return Integer.compare(span.getEnd(), getEnd());
  //		} else {
  //			return 1;
  //		}
  //	}

  //	public boolean crosses(Span span) {
  //		int spanStart = span.getStart();
  //
  //		// either s's start is in this or this' start is in s
  //		return !this.contains(span)
  //				&& !span.contains(this)
  //				&& (getStart() <= spanStart && spanStart < getEnd() || spanStart <= getStart()
  //						&& getStart() < span.getEnd());
  //	}

  //	public boolean lessThan(Span span) {
  //		return getStart() < span.getStart() && getEnd() < span.getEnd();
  //	}

  //	public boolean greaterThan(Span span) {
  //		return getStart() > span.getStart() && getEnd() > span.getEnd();
  //	}

  //	public static Span parseSpan(String spanString) {
  //		String startString = spanString.substring(0, spanString.indexOf("|"));
  //		String endString = spanString.substring(spanString.indexOf("|") + 1);
  //		int start = Integer.parseInt(startString);
  //		int end = Integer.parseInt(endString);
  //		return new Span(start, end);
  //	}

  //	public static boolean isValid(int start, int end) {
  //		return start <= end && start >= 0;
  //	}
}
