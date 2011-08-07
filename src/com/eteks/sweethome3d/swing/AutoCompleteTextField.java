/*
 * AutoCompleteTextField.java 6 aout 2011
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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A text field that suggests to the user strings stored in auto completion strings in the user preferences.
 * Inspired from the code released in public domain by Samuel Sjoberg on his 
 * <a href="http://samuelsjoberg.com/archive/2009/10/autocompletion-in-swing">blog</a>.
 * @author Emmanuel Puybaret
 */
public class AutoCompleteTextField extends JTextField {
  private List<String> autoCompletionStrings;
  
  public AutoCompleteTextField(String text, int preferredLength, List<String> autoCompletionStrings) {
    super(preferredLength);
    this.autoCompletionStrings = autoCompletionStrings;
    setDocument(new AutoCompleteDocument(text));    
  }
  
  /**
   * Document able to autocomplete.
   */
  private class AutoCompleteDocument extends PlainDocument {
    public AutoCompleteDocument(String text) {
      try {
        replace(0, 0, text, null);
      } catch (BadLocationException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    @Override
    public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
      if (string != null && string.length() > 0) {
        int length = getLength();
        if (offset == length || (offset == getSelectionStart() && length - 1 == getSelectionEnd())) {
          String textAtOffset = getText(0, offset); 
          String completion = autoComplete(textAtOffset + string);
          if (completion != null) {
            int completionIndex = offset + string.length();
            super.remove(offset, length - offset);
            super.insertString(offset, string, attr);
            super.insertString(completionIndex, completion.substring(completionIndex), attr);
            select(completionIndex, getLength());
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
      for (String s : autoCompletionStrings) {
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
  }
}
