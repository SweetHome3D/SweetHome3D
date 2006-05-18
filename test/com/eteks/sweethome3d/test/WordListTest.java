/*
 * WordListTest.java 17 mai 2006
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
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests undo with no framework.
 * @author Emmanuel Puybaret
 */
public class WordListTest extends TestCase {
  public void testWordListWithListCopy() {
    WordListWithListCopy list = new WordListWithListCopy();
    list.addWord("a");
    list.addWord("b");
    assertEquals("[a, b]", list.toString());
    list.undoAddWord();
    assertEquals("[a]", list.toString());
  }

  public void testWordListWithReverseAdd() {
    WordListWithReverseAdd list = new WordListWithReverseAdd();
    list.addWord("a");
    list.addWord("b");
    assertEquals("[a, b]", list.toString());
    list.undoAddWord();
    assertEquals("[a]", list.toString());
    list.undoAddWord();
  }
}

class WordListWithListCopy {
  private List<String> list = new ArrayList<String>();
  private List<String> listCopy;
  
  public void addWord(String word) {
    this.listCopy = new ArrayList<String>(list); 
    this.list.add(word);
  }
  
  public void undoAddWord() {
    if (this.listCopy == null)
      throw new IllegalStateException();
    this.list.clear(); 
    this.list.addAll(this.listCopy);
    this.listCopy = null;
  }

  @Override
  public String toString() {
    return this.list.toString();
  }
}

class WordListWithReverseAdd {
  private List<String> list = new ArrayList<String>();

  public void addWord(String word) {
    this.list.add(word); 
  }

  public void undoAddWord() {
    if (this.list.isEmpty())
      throw new IllegalStateException();
    int endIndex = this.list.size() - 1; 
    this.list.remove(endIndex); 
  }

  @Override
  public String toString() {
    return list.toString();
  }
}
