/**
 * PackageDependenciesTest.java
 */
package com.eteks.sweethome3d.junit;

import java.io.IOException;

import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import junit.framework.TestCase;

/**
 * Tests if dependencies between Sweet Home 3D packages are met.
 * @author Emmanuel Puybaret
 */
public class PackageDependenciesTest extends TestCase {
  /**
   * Tests that the package dependencies constraint is met for the analyzed packages.
   */
  public void testPackageDependencies() throws IOException {
    PackageFilter packageFilter = new PackageFilter();
    // Ignore Java packages 
    packageFilter.addPackage("java.*");
    // Ignore JUnit tests
    packageFilter.addPackage("com.eteks.sweethome3d.junit");
    
    JDepend jdepend = new JDepend(packageFilter);
    jdepend.addDirectory("classes");

    DependencyConstraint constraint = new DependencyConstraint();
    // Sweet Home 3D packages
    JavaPackage sweetHome3DModel = constraint.addPackage("com.eteks.sweethome3d.model");
    JavaPackage sweetHome3DTools = constraint.addPackage("com.eteks.sweethome3d.tools");
    JavaPackage sweetHome3DPlugin = constraint.addPackage("com.eteks.sweethome3d.plugin");
    JavaPackage sweetHome3DViewController = constraint.addPackage("com.eteks.sweethome3d.viewcontroller");
    JavaPackage sweetHome3DSwing = constraint.addPackage("com.eteks.sweethome3d.swing");
    JavaPackage sweetHome3DJava3D = constraint.addPackage("com.eteks.sweethome3d.j3d");
    JavaPackage sweetHome3DIO = constraint.addPackage("com.eteks.sweethome3d.io");
    JavaPackage sweetHome3DApplication = constraint.addPackage("com.eteks.sweethome3d");
    JavaPackage sweetHome3DApplet = constraint.addPackage("com.eteks.sweethome3d.applet");
    // Swing components packages
    JavaPackage swing = constraint.addPackage("javax.swing");
    JavaPackage swingEvent = constraint.addPackage("javax.swing.event");
    JavaPackage swingUndo = constraint.addPackage("javax.swing.undo");
    JavaPackage swingText = constraint.addPackage("javax.swing.text");
    JavaPackage swingTextHtml = constraint.addPackage("javax.swing.text.html");
    JavaPackage swingTable = constraint.addPackage("javax.swing.table");
    JavaPackage swingTree = constraint.addPackage("javax.swing.tree");
    JavaPackage swingBorder = constraint.addPackage("javax.swing.border");
    JavaPackage swingColorchooser = constraint.addPackage("javax.swing.colorchooser");
    JavaPackage swingFilechooser = constraint.addPackage("javax.swing.filechooser");
    JavaPackage swingPlaf = constraint.addPackage("javax.swing.plaf");
    JavaPackage swingPlafSynth = constraint.addPackage("javax.swing.plaf.synth");
    JavaPackage imageio = constraint.addPackage("javax.imageio");
    JavaPackage imageioStream = constraint.addPackage("javax.imageio.stream");
    // Java 3D
    JavaPackage java3d = constraint.addPackage("javax.media.j3d");
    JavaPackage vecmath = constraint.addPackage("javax.vecmath");
    JavaPackage sun3dLoaders = constraint.addPackage("com.sun.j3d.loaders");
    JavaPackage sun3dLoadersLw3d = constraint.addPackage("com.sun.j3d.loaders.lw3d");
    JavaPackage sun3dUtilsGeometry = constraint.addPackage("com.sun.j3d.utils.geometry");
    JavaPackage sun3dUtilsImage = constraint.addPackage("com.sun.j3d.utils.image");
    JavaPackage sun3dUtilsUniverse = constraint.addPackage("com.sun.j3d.utils.universe");
    JavaPackage sun3dExpSwing = constraint.addPackage("com.sun.j3d.exp.swing");
    // XML
    JavaPackage xmlParsers = constraint.addPackage("javax.xml.parsers");
    JavaPackage xmlSax = constraint.addPackage("org.xml.sax");
    JavaPackage xmlSaxHelpers = constraint.addPackage("org.xml.sax.helpers");
    // JMF
    JavaPackage jmf = constraint.addPackage("javax.media");
    JavaPackage jmfControl = constraint.addPackage("javax.media.control");
    JavaPackage jmfDataSink = constraint.addPackage("javax.media.datasink");
    JavaPackage jmfFormat = constraint.addPackage("javax.media.format");
    JavaPackage jmfProtocol = constraint.addPackage("javax.media.protocol");
    // SunFlow
    JavaPackage sunflow = constraint.addPackage("org.sunflow");
    JavaPackage sunflowCore = constraint.addPackage("org.sunflow.core");
    JavaPackage sunflowCoreLight = constraint.addPackage("org.sunflow.core.light");
    JavaPackage sunflowCorePrimitive = constraint.addPackage("org.sunflow.core.primitive");
    JavaPackage sunflowImage = constraint.addPackage("org.sunflow.image");
    JavaPackage sunflowMath = constraint.addPackage("org.sunflow.math");
    JavaPackage sunflowSystem = constraint.addPackage("org.sunflow.system");
    JavaPackage sunflowSystemUI = constraint.addPackage("org.sunflow.system.ui");
    // iText for PDF
    JavaPackage iText = constraint.addPackage("com.lowagie.text");
    JavaPackage iTextPdf = constraint.addPackage("com.lowagie.text.pdf");
    // FreeHEP Vector Graphics for SVG
    JavaPackage vectorGraphicsUtil = constraint.addPackage("org.freehep.util");
    JavaPackage vectorGraphics = constraint.addPackage("org.freehep.graphicsio");
    JavaPackage vectorGraphicsSvg = constraint.addPackage("org.freehep.graphicsio.svg");
    // Batik for SVG path parsing
    JavaPackage orgApacheBatikParser = constraint.addPackage("org.apache.batik.parser");
    // Java JNLP
    JavaPackage jnlp = constraint.addPackage("javax.jnlp");
    // Mac OS X specific interfaces
    JavaPackage eawt = constraint.addPackage("com.apple.eawt");
    JavaPackage eio = constraint.addPackage("com.apple.eio");

    // Describe dependencies : com.eteks.sweethome3d.model don't have any dependency on
    // other packages, IO and View/Controller packages ignore each other
    // and Swing components and Java 3D use are isolated in sweetHome3DSwing
    sweetHome3DTools.dependsUpon(sweetHome3DModel);
    sweetHome3DTools.dependsUpon(eio);
    
    sweetHome3DViewController.dependsUpon(sweetHome3DModel);
    sweetHome3DViewController.dependsUpon(sweetHome3DTools);
    sweetHome3DViewController.dependsUpon(swingEvent);
    sweetHome3DViewController.dependsUpon(swingUndo);
    sweetHome3DViewController.dependsUpon(swingText);
    sweetHome3DViewController.dependsUpon(swingTextHtml);
    sweetHome3DViewController.dependsUpon(xmlParsers);
    sweetHome3DViewController.dependsUpon(xmlSax);
    sweetHome3DViewController.dependsUpon(xmlSaxHelpers);

    sweetHome3DPlugin.dependsUpon(sweetHome3DModel);
    sweetHome3DPlugin.dependsUpon(sweetHome3DTools);
    sweetHome3DPlugin.dependsUpon(sweetHome3DViewController);   
    sweetHome3DPlugin.dependsUpon(swingUndo);
    
    sweetHome3DJava3D.dependsUpon(sweetHome3DModel);
    sweetHome3DJava3D.dependsUpon(sweetHome3DTools);
    sweetHome3DJava3D.dependsUpon(sweetHome3DViewController);
    sweetHome3DJava3D.dependsUpon(java3d);
    sweetHome3DJava3D.dependsUpon(vecmath);
    sweetHome3DJava3D.dependsUpon(sun3dLoaders);
    sweetHome3DJava3D.dependsUpon(sun3dLoadersLw3d);
    sweetHome3DJava3D.dependsUpon(sun3dUtilsGeometry);
    sweetHome3DJava3D.dependsUpon(sun3dUtilsImage);
    sweetHome3DJava3D.dependsUpon(sun3dUtilsUniverse);
    sweetHome3DJava3D.dependsUpon(imageio);
    sweetHome3DJava3D.dependsUpon(swing);
    sweetHome3DJava3D.dependsUpon(sunflow);
    sweetHome3DJava3D.dependsUpon(sunflowCore);
    sweetHome3DJava3D.dependsUpon(sunflowCoreLight);
    sweetHome3DJava3D.dependsUpon(sunflowCorePrimitive);
    sweetHome3DJava3D.dependsUpon(sunflowImage);
    sweetHome3DJava3D.dependsUpon(sunflowMath);
    sweetHome3DJava3D.dependsUpon(sunflowSystem);
    sweetHome3DJava3D.dependsUpon(sunflowSystemUI);
    sweetHome3DJava3D.dependsUpon(xmlParsers);
    sweetHome3DJava3D.dependsUpon(xmlSax);
    sweetHome3DJava3D.dependsUpon(xmlSaxHelpers);
    sweetHome3DJava3D.dependsUpon(orgApacheBatikParser);
    
    sweetHome3DSwing.dependsUpon(sweetHome3DModel);
    sweetHome3DSwing.dependsUpon(sweetHome3DTools);
    sweetHome3DSwing.dependsUpon(sweetHome3DPlugin);   
    sweetHome3DSwing.dependsUpon(sweetHome3DViewController);
    sweetHome3DSwing.dependsUpon(sweetHome3DJava3D);
    sweetHome3DSwing.dependsUpon(swing);
    sweetHome3DSwing.dependsUpon(swingEvent);
    sweetHome3DSwing.dependsUpon(swingText);
    sweetHome3DSwing.dependsUpon(swingTextHtml);
    sweetHome3DSwing.dependsUpon(swingTable);
    sweetHome3DSwing.dependsUpon(swingTree);
    sweetHome3DSwing.dependsUpon(swingBorder);
    sweetHome3DSwing.dependsUpon(swingColorchooser);
    sweetHome3DSwing.dependsUpon(swingFilechooser);
    sweetHome3DSwing.dependsUpon(swingPlaf);
    sweetHome3DSwing.dependsUpon(swingPlafSynth);
    sweetHome3DSwing.dependsUpon(imageio);
    sweetHome3DSwing.dependsUpon(imageioStream);
    sweetHome3DSwing.dependsUpon(java3d);
    sweetHome3DSwing.dependsUpon(vecmath);
    sweetHome3DSwing.dependsUpon(sun3dUtilsGeometry);
    sweetHome3DSwing.dependsUpon(sun3dUtilsUniverse);
    sweetHome3DSwing.dependsUpon(sun3dExpSwing);
    sweetHome3DSwing.dependsUpon(jmf);
    sweetHome3DSwing.dependsUpon(jmfControl);
    sweetHome3DSwing.dependsUpon(jmfDataSink);
    sweetHome3DSwing.dependsUpon(jmfFormat);
    sweetHome3DSwing.dependsUpon(jmfProtocol);
    sweetHome3DSwing.dependsUpon(iText);
    sweetHome3DSwing.dependsUpon(iTextPdf);
    sweetHome3DSwing.dependsUpon(vectorGraphicsUtil);
    sweetHome3DSwing.dependsUpon(vectorGraphics);
    sweetHome3DSwing.dependsUpon(vectorGraphicsSvg);
    sweetHome3DSwing.dependsUpon(jnlp);
    
    sweetHome3DIO.dependsUpon(sweetHome3DModel);
    sweetHome3DIO.dependsUpon(sweetHome3DTools);
    sweetHome3DIO.dependsUpon(xmlParsers);
    sweetHome3DIO.dependsUpon(xmlSax);
    sweetHome3DIO.dependsUpon(xmlSaxHelpers);

    // Describe application and applet assembly packages
    sweetHome3DApplication.dependsUpon(sweetHome3DModel);
    sweetHome3DApplication.dependsUpon(sweetHome3DTools);
    sweetHome3DApplication.dependsUpon(sweetHome3DPlugin);
    sweetHome3DApplication.dependsUpon(sweetHome3DViewController);
    sweetHome3DApplication.dependsUpon(sweetHome3DJava3D);
    sweetHome3DApplication.dependsUpon(sweetHome3DSwing);
    sweetHome3DApplication.dependsUpon(sweetHome3DIO);
    sweetHome3DApplication.dependsUpon(swing);
    sweetHome3DApplication.dependsUpon(swingEvent);
    sweetHome3DApplication.dependsUpon(swingBorder);
    sweetHome3DApplication.dependsUpon(imageio);
    sweetHome3DApplication.dependsUpon(java3d);
    sweetHome3DApplication.dependsUpon(sun3dExpSwing);
    sweetHome3DApplication.dependsUpon(eawt);
    sweetHome3DApplication.dependsUpon(jnlp);
    
    sweetHome3DApplet.dependsUpon(sweetHome3DModel);
    sweetHome3DApplet.dependsUpon(sweetHome3DTools);
    sweetHome3DApplet.dependsUpon(sweetHome3DPlugin);
    sweetHome3DApplet.dependsUpon(sweetHome3DViewController);
    sweetHome3DApplet.dependsUpon(sweetHome3DJava3D);
    sweetHome3DApplet.dependsUpon(sweetHome3DSwing);
    sweetHome3DApplet.dependsUpon(sweetHome3DIO);
    sweetHome3DApplet.dependsUpon(swing);
    sweetHome3DApplet.dependsUpon(swingEvent);
    sweetHome3DApplet.dependsUpon(swingTable);
    sweetHome3DApplet.dependsUpon(java3d);
    sweetHome3DApplet.dependsUpon(jnlp);
    
    jdepend.analyze();

    assertTrue("Dependency mismatch", jdepend.dependencyMatch(constraint));
  }
}
