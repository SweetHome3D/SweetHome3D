/*
 * AutoCompleteDocument.java 6 aout 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.swing;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * Document able to autocomplete.
 * @author Emmanuel Puybaret
 */
class AutoCompleteDocument extends PlainDocument {
  private JTextComponent textComponent;
  private List<String>   autoCompletionStrings;
  private boolean        autoCompletionEnabled;

  public AutoCompleteDocument(JTextComponent textComponent, List<String> autoCompletionStrings) {
    this.textComponent = textComponent;
    this.autoCompletionStrings = autoCompletionStrings;
    this.autoCompletionEnabled = true;
    try {
      replace(0, 0, textComponent.getText(), null);
    } catch (BadLocationException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
    if (this.autoCompletionEnabled
        && string != null
        && string.length() > 0
        && string.indexOf('\n') < 0 // No completion on multi line texts
        && getText(0, getLength()).indexOf('\n') < 0) {
      int length = getLength();
      if (offset == length
          || (offset == this.textComponent.getSelectionStart()
               && length - 1 == this.textComponent.getSelectionEnd())) {
        String textAtOffset = getText(0, offset);
        String completion = autoComplete(textAtOffset + string);
        if (completion != null) {
          int completionIndex = offset + string.length();
          super.remove(offset, length - offset);
          super.insertString(offset, string, attr);
          super.insertString(completionIndex, completion.substring(completionIndex), attr);
          this.textComponent.select(completionIndex, getLength());
          return;
        }
      }
    }
    super.insertString(offset, string, attr);
  }

  private String autoComplete(String stringStart) {
    stringStart = stringStart.toLowerCase();
    // Keep suggestions in alphabetical order
    final Collator comparator = Collator.getInstance();
    comparator.setStrength(Collator.TERTIARY);
    TreeSet<String> matchingStrings = new TreeSet<String>(new Comparator<String>() {
        public int compare(String s1, String s2) {
          return comparator.compare(s1, s2);
        }
      });
    // Find matching strings
    for (String s : this.autoCompletionStrings) {
      if (s.toLowerCase().startsWith(stringStart)) {
        matchingStrings.add(s);
      }
    }
    if (matchingStrings.size() > 0) {
      // Return the first found one
      return matchingStrings.first();
    } else {
      return null;
    }
  }

  /**
   * Sets whether auto completion is enabled.
   */
  public void setAutoCompletionEnabled(boolean autoCompletionEnabled) {
    this.autoCompletionEnabled = autoCompletionEnabled;
  }

  /**
   * Returns whether auto completion is enabled.
   */
  public boolean isAutoCompletionEnabled() {
    return this.autoCompletionEnabled;
  }
}
