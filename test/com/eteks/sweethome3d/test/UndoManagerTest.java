/*
 * UndoManagerTest.java 17 mai 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import junit.framework.TestCase;

/**
 * Tests undo / redo Swing framework.
 * @author Emmanuel Puybaret
 */
public class UndoManagerTest extends TestCase {
  public void testUndoManager() {
    UndoManager manager = new UndoManager(); 
    List<String> list = new ArrayList<String>();
    // Add some undoable edits
    manager.addEdit(new AddWordToListEdit(list, "a"));
    manager.addEdit(new AddWordToListEdit(list, "b"));
    assertEquals(Arrays.asList(new String [] {"a", "b"}), list);
    // Test undo / redo
    manager.undo();
    assertEquals(Arrays.asList(new String [] {"a"}), list);
    // Forget "b" and replace it by "c"
    manager.addEdit(new AddWordToListEdit(list, "c"));
    assertEquals(Arrays.asList(new String [] {"a", "c"}), list);
    manager.undo();
    assertEquals(Arrays.asList(new String [] {"a"}), list);
    manager.redo();
    assertEquals(Arrays.asList(new String [] {"a", "c"}), list);
    // Check canUndo / canRedo
    assertTrue(manager.canUndo());
    assertFalse(manager.canRedo());
  }
  
  public void testInsignificantUndoableEdits() {
    UndoManager manager = new UndoManager(); 
    List<String> list = new ArrayList<String>();
       // Add some significant undoable edits and an insignificant undoable edit
    manager.addEdit(new AddWordToListEdit(list, "a"));
    manager.addEdit(new InsignificantEdit(list, " "));
    manager.addEdit(new AddWordToListEdit(list, "b"));
    assertEquals(Arrays.asList(new String [] {"a", " ", "b"}), list);
    // Test undo / redo
    manager.undo();
    assertEquals(Arrays.asList(new String [] {"a", " "}), list);
    manager.undo();
    assertEquals(Collections.EMPTY_LIST, list);
  }
  
  public void testUndoableEditSupport() {
    UndoableEditSupport editSupport = new UndoableEditSupport();
    UndoManager manager = new UndoManager(); 
    // Add manager as a listener of editSupport
    editSupport.addUndoableEditListener(manager);

    List<String> list = new ArrayList<String>();
    final UndoableEdit edit = new AddWordToListEdit(list, "a");    
    // Add an other listener to editSupport
    editSupport.addUndoableEditListener(new UndoableEditListener () {
      public void undoableEditHappened(UndoableEditEvent ev) {
        assertEquals(edit, ev.getEdit());
      }
    });
    // Add a significant undoable edit
    assertFalse(manager.canUndo());
    editSupport.postEdit(edit);    
    assertTrue(manager.canUndo());
  }

  public void testCompoundUndoableEdits() {
    UndoManager manager = new UndoManager(); 
    List<String> list = new ArrayList<String>();
    // Add some coumpound undoable edits
    CompoundEdit compoundEdit = new CompoundEdit();
    compoundEdit.addEdit(new AddWordToListEdit(list, "a"));
    compoundEdit.addEdit(new InsignificantEdit(list, " "));
    compoundEdit.end();
    manager.addEdit(compoundEdit);
    compoundEdit = new CompoundEdit();
    compoundEdit.addEdit(new AddWordToListEdit(list, "b"));
    compoundEdit.addEdit(new InsignificantEdit(list, " "));
    compoundEdit.end();
    manager.addEdit(compoundEdit);
    // Test undo / redo
    assertEquals(Arrays.asList(new String [] {"a", " ", "b", " "}), list);
    manager.undo();
    assertEquals(Arrays.asList(new String [] {"a", " "}), list);
    manager.redo();
    assertEquals(Arrays.asList(new String [] {"a", " ", "b", " "}), list);
    assertFalse(manager.canRedo());
  }

  private static class AddWordToListEdit extends AbstractUndoableEdit {
    private List<String> list;
    private String       word;

    public AddWordToListEdit(List<String> list, String word) {
      this.list = list;
      this.word = word;
      doEdit();
    }
    
    private void doEdit() {
      this.list.add(this.word);
    }
    
    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      int endIndex  = list.size() - 1;
      this.list.remove(endIndex);
    }
    
    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doEdit();
    }
  }
  
  private static class InsignificantEdit extends AddWordToListEdit {
    public InsignificantEdit(List<String> list, String word) {
      super(list, word);
    }

    @Override
    public boolean isSignificant() {
      return false;
    }
  }
}
