/*
 * AutoSelectSpinner.java 10 sept. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.text.DefaultFormatter;

/**
 * A spinner which commits its value during edition and selects 
 * the value displayed in its editor when it gains focus.
 * @author Emmanuel Puybaret
 */
public class AutoCommitSpinner extends JSpinner {
  /**
   * Creates a spinner with a given <code>model</code>.
   */
  public AutoCommitSpinner(SpinnerModel model) {
    super(model);    
    JComponent editor = getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
      final JFormattedTextField textField = ((JSpinner.DefaultEditor)editor).getTextField();
      // Commit text during edition
      if (textField.getFormatter() instanceof DefaultFormatter) {
        ((DefaultFormatter)textField.getFormatter()).setCommitsOnValidEdit(true);
      }
    }
  }
}
