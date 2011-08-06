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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A text field that suggests to the user strings stored in auto completion strings in the user preferences.
 * From the code released in public domain by Samuel Sjoberg on his 
 * <a href="http://samuelsjoberg.com/archive/2009/10/autocompletion-in-swing">blog</a>.
 * @author Emmanuel Puybaret
 */
public class AutoCompleteTextField extends JTextField {
  private UserPreferences preferences;
  
  public AutoCompleteTextField(String text, int preferredLength, UserPreferences preferences) {
    super(preferredLength);
    this.preferences = preferences;    
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
        String text = getText(0, offset); 
        String completion = autoComplete(text + string);
        int length = offset + string.length();
        if (completion != null && text.length() > 0) {
          string = completion.substring(length - 1);
          super.insertString(offset, string, attr);
          select(length, getLength());
        } else {
          super.insertString(offset, string, attr);
        }
      }
    }

    private String autoComplete(String stringStart) {
      stringStart = stringStart.toLowerCase();
      String matchingString = null;
      for (String s : preferences.getAutoCompletionStrings()) {
        if (s.toLowerCase().startsWith(stringStart)) {
          if (matchingString == null) {
            matchingString = s;
          } else {
            return null;
          }
        }
      }
      return matchingString;
    }
  }
}
