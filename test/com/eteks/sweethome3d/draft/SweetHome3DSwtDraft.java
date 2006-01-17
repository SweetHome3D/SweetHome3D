/*
 * SweetHome3DSwtDraft.java 6 janv. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.draft;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Draft application for Sweet Home 3D GUI using SWT.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DSwtDraft {
  private Shell             sShell                  = null;
  private CoolBar           coolBar                 = null;
  private SashForm          mainSashForm            = null;
  private SashForm          leftSashForm            = null;
  private SashForm          rightSashForm           = null;
  private Tree              defaultFurnitureTree    = null;
  private Table             furnitureTable          = null;
  private Composite         editionComposite        = null;
  private Button            cutButton               = null;
  private Button            copyButton              = null;
  private Button            pasteButton             = null;
  private ScrolledComposite planScrolledComposite   = null;
  private ScrolledComposite view3DScrolledComposite = null;
  private Label             planLabel               = null;
  private Label             view3DLabel             = null;
  private Menu              shellMenuBar            = null;
  private Menu              fileMenu                = null;
  private Menu              editMenu                = null;
  private Menu              furnitureMenu           = null;
  private Menu              planMenu                = null;
  private Menu              helpMenu                = null;

  /**
   * This method initializes coolBar
   * 
   */
  private void createCoolBar() {
    GridData gridData = new GridData();
    gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    coolBar = new CoolBar(sShell, SWT.NONE);
    coolBar.setLayout(new RowLayout());
    createEditionComposite();
    coolBar.setLayoutData(gridData);
    CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
    coolItem.setControl(editionComposite);
    // Calcul de la taille du CoolItem
    Point size = coolItem.getControl().computeSize(SWT.DEFAULT,
        SWT.DEFAULT);
    coolItem.setSize(coolItem.computeSize(size.x, size.y));
  }

  /**
   * This method initializes mainSashForm
   * 
   */
  private void createMainSashForm() {
    GridData gridData1 = new GridData();
    gridData1.grabExcessVerticalSpace = true;
    gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
    gridData1.grabExcessHorizontalSpace = true;
    mainSashForm = new SashForm(sShell, SWT.NONE);
    createLeftSashForm();
    mainSashForm.setLayoutData(gridData1);
    createRightSashForm();
    mainSashForm.setWeights(new int [] {30, 70});
  }

  /**
   * This method initializes leftSashForm
   * 
   */
  private void createLeftSashForm() {
    leftSashForm = new SashForm(mainSashForm, SWT.NONE);
    leftSashForm.setOrientation(org.eclipse.swt.SWT.VERTICAL);
    createDefaultFurnitureTree();
    createFurnitureTable();
  }

  /**
   * This method initializes rightSashForm
   * 
   */
  private void createRightSashForm() {
    rightSashForm = new SashForm(mainSashForm, SWT.NONE);
    rightSashForm.setOrientation(org.eclipse.swt.SWT.VERTICAL);
    createPlanScrolledComposite();
    createView3DScrolledComposite();
  }

  /**
   * This method initializes defaultFurnitureTree
   * 
   */
  private void createDefaultFurnitureTree() {
    defaultFurnitureTree = new Tree(leftSashForm, SWT.NONE);

    TreeItem bedroom = new TreeItem(defaultFurnitureTree, SWT.NONE);
    bedroom.setText("Bedroom");
    TreeItem bed140x90 = new TreeItem(bedroom, SWT.NONE);
    bed140x90.setText("Bed 140x90");
    TreeItem chest = new TreeItem(bedroom, SWT.NONE);
    chest.setText("Chest");
    TreeItem bedsideTable = new TreeItem(bedroom, SWT.NONE);
    bedsideTable.setText("Bedside table");

    TreeItem livingRoom = new TreeItem(defaultFurnitureTree,
        SWT.NONE);
    livingRoom.setText("Living Room");
    TreeItem bookcase = new TreeItem(livingRoom, SWT.NONE);
    bookcase.setText("Bookcase");
    TreeItem chair = new TreeItem(livingRoom, SWT.NONE);
    chair.setText("Chair");
    TreeItem roundTable = new TreeItem(livingRoom, SWT.NONE);
    roundTable.setText("Round table");
  }

  /**
   * This method initializes furnitureTable
   * 
   */
  private void createFurnitureTable() {
    furnitureTable = new Table(leftSashForm, SWT.NONE);
    furnitureTable.setHeaderVisible(true);
    furnitureTable.setLinesVisible(true);
    TableColumn nameTableColumn = new TableColumn(furnitureTable,
        SWT.NONE);
    nameTableColumn.setWidth(60);
    nameTableColumn.setText("Name");
    TableColumn lTableColumn = new TableColumn(furnitureTable,
        SWT.NONE);
    lTableColumn.setWidth(30);
    lTableColumn.setText("W");
    TableColumn dtableColumn = new TableColumn(furnitureTable,
        SWT.NONE);
    dtableColumn.setWidth(30);
    dtableColumn.setText("D");
    TableColumn hTableColumn = new TableColumn(furnitureTable,
        SWT.NONE);
    hTableColumn.setWidth(30);
    hTableColumn.setText("H");
    TableItem item = new TableItem(furnitureTable, SWT.NONE);
    item.setText(new String [] {"Bed", "140", "190", "50"});
    item = new TableItem(furnitureTable, SWT.NONE);
    item.setText(new String [] {"Chest", "100", "80", "80"});
    item = new TableItem(furnitureTable, SWT.NONE);
    item.setText(new String [] {"Table", "110", "110", "75"});
    item = new TableItem(furnitureTable, SWT.NONE);
    item.setText(new String [] {"Chair", "45", "45", "90"});
    item = new TableItem(furnitureTable, SWT.NONE);
    item.setText(new String [] {"Bookcase", "90", "30", "180"});
  }

  /**
   * This method initializes editionComposite
   * 
   */
  private void createEditionComposite() {
    editionComposite = new Composite(coolBar, SWT.NONE);
    editionComposite.setLayout(new RowLayout());
    cutButton = new Button(editionComposite, SWT.NONE);
    cutButton.setImage(new Image(Display.getCurrent(), getClass()
        .getResourceAsStream(
            "/com/eteks/sweethome3d/draft/resources/Cut16.gif")));
    copyButton = new Button(editionComposite, SWT.NONE);
    copyButton.setImage(new Image(Display.getCurrent(), getClass()
        .getResourceAsStream(
            "/com/eteks/sweethome3d/draft/resources/Copy16.gif")));
    pasteButton = new Button(editionComposite, SWT.NONE);
    pasteButton
        .setImage(new Image(
            Display.getCurrent(),
            getClass()
                .getResourceAsStream(
                    "/com/eteks/sweethome3d/draft/resources/Paste16.gif")));
  }

  /**
   * This method initializes planScrolledComposite
   * 
   */
  private void createPlanScrolledComposite() {
    planScrolledComposite = new ScrolledComposite(rightSashForm,
        SWT.V_SCROLL | SWT.H_SCROLL);
    planScrolledComposite.setExpandHorizontal(true);
    planScrolledComposite.setExpandVertical(true);
    planLabel = new Label(planScrolledComposite, SWT.NONE);
    planLabel.setText("Label");
    planLabel.setImage(new Image(Display.getCurrent(), getClass()
        .getResourceAsStream(
            "/com/eteks/sweethome3d/draft/resources/plan.png")));
    Point size = planLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    planScrolledComposite.setMinSize(size.x, size.y);
    planScrolledComposite.setContent(planLabel);
  }

  /**
   * This method initializes view3DScrolledComposite
   * 
   */
  private void createView3DScrolledComposite() {
    view3DScrolledComposite = new ScrolledComposite(rightSashForm,
        SWT.V_SCROLL | SWT.H_SCROLL);
    view3DScrolledComposite.setExpandHorizontal(true);
    view3DScrolledComposite.setExpandVertical(true);
    view3DLabel = new Label(view3DScrolledComposite, SWT.NONE);
    view3DLabel.setText("Label");
    view3DLabel.setImage(new Image(Display.getCurrent(),
        getClass().getResourceAsStream(
            "/com/eteks/sweethome3d/draft/resources/view3D.jpg")));
    Point size = view3DLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    view3DScrolledComposite.setMinSize(size.x, size.y);
    view3DScrolledComposite.setContent(view3DLabel);
  }

  /**
   * @param args
   */
  public static void main(String [] args) {
    /*
     * Before this is run, be sure to set up the launch configuration
     * (Arguments->VM Arguments) for the correct SWT library path in order to
     * run with the SWT dlls. The dlls are located in the SWT plugin jar. For
     * example, on Windows the Eclipse SWT 3.1 plugin jar is:
     * installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
     */
    Display display = Display.getDefault();
    SweetHome3DSwtDraft thisClass = new SweetHome3DSwtDraft();
    thisClass.createSShell();
    thisClass.sShell.open();
    while (!thisClass.sShell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  /**
   * This method initializes sShell
   */
  private void createSShell() {
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    sShell = new Shell();
    sShell.setText("Sweet Home 3D");
    createCoolBar();
    createMainSashForm();
    sShell.setLayout(gridLayout);
    sShell.setSize(new org.eclipse.swt.graphics.Point(800, 700));
    shellMenuBar = new Menu(sShell, SWT.BAR);
    MenuItem fileMenuItem = new MenuItem(shellMenuBar, SWT.CASCADE);
    fileMenuItem.setText("File");
    MenuItem editMenuItem = new MenuItem(shellMenuBar, SWT.CASCADE);
    editMenuItem.setText("Edit");
    MenuItem furnitureMenuItem = new MenuItem(shellMenuBar,
        SWT.CASCADE);
    furnitureMenuItem.setText("Furniture");
    MenuItem planMenuItem = new MenuItem(shellMenuBar, SWT.CASCADE);
    planMenuItem.setText("Plan");
    MenuItem helpMenuItem = new MenuItem(shellMenuBar, SWT.CASCADE);
    helpMenuItem.setText("Help");
    helpMenu = new Menu(helpMenuItem);
    MenuItem aboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
    aboutMenuItem.setText("About");
    aboutMenuItem
        .addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
          public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            MessageBox aboutMessageBox = new MessageBox(sShell,
                SWT.OK);
            aboutMessageBox
                .setMessage("Sweet Home 3D Draft\n Copyrights 2006 eTeks");
            aboutMessageBox.setText("About");
            aboutMessageBox.open();
          }

          public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
          }
        });
    helpMenuItem.setMenu(helpMenu);
    planMenu = new Menu(planMenuItem);
    MenuItem importImageMenuItem = new MenuItem(planMenu, SWT.PUSH);
    importImageMenuItem.setText("Import image...");
    MenuItem showRulesMenuItem = new MenuItem(planMenu, SWT.PUSH);
    showRulesMenuItem.setText("Show rules");
    planMenuItem.setMenu(planMenu);
    furnitureMenu = new Menu(furnitureMenuItem);
    MenuItem addMenuItem = new MenuItem(furnitureMenu, SWT.PUSH);
    addMenuItem.setText("Add");
    MenuItem deleteMenuItem = new MenuItem(furnitureMenu, SWT.PUSH);
    deleteMenuItem.setText("Delete");
    furnitureMenuItem.setMenu(furnitureMenu);
    editMenu = new Menu(editMenuItem);
    MenuItem undoMenuItem = new MenuItem(editMenu, SWT.PUSH);
    undoMenuItem.setText("Undo");
    MenuItem redoMenuItem = new MenuItem(editMenu, SWT.PUSH);
    redoMenuItem.setText("Redo");
    MenuItem cutMenuItem = new MenuItem(editMenu, SWT.PUSH);
    cutMenuItem.setText("Cut");
    MenuItem copyMenuItem = new MenuItem(editMenu, SWT.PUSH);
    copyMenuItem.setText("Copy");
    MenuItem pasteMenuItem = new MenuItem(editMenu, SWT.PUSH);
    pasteMenuItem.setText("Paste");
    editMenuItem.setMenu(editMenu);
    fileMenu = new Menu(fileMenuItem);
    MenuItem newMenuItem = new MenuItem(fileMenu, SWT.PUSH);
    newMenuItem.setText("New");
    MenuItem openMenuItem = new MenuItem(fileMenu, SWT.PUSH);
    openMenuItem.setText("Open...");
    MenuItem saveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
    saveMenuItem.setText("Save...");
    MenuItem saveAsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
    saveAsMenuItem.setText("Save as...");
    MenuItem preferencesMenuItem = new MenuItem(fileMenu, SWT.PUSH);
    preferencesMenuItem.setText("Preferences...");
    MenuItem exitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
    exitMenuItem.setText("Exit");
    exitMenuItem
        .addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
          public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            System.exit(0);
          }

          public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
          }
        });
    fileMenuItem.setMenu(fileMenu);
    sShell.setMenuBar(shellMenuBar);
  }

}
