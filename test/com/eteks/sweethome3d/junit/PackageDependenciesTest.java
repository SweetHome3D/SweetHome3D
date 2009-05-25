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
    // Ignore Java packages and Swing sub packages
    packageFilter.addPackage("java.*");
    packageFilter.addPackage("javax.swing.*");
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
    JavaPackage imageio = constraint.addPackage("javax.imageio");
    // Java 3D
    JavaPackage java3d = constraint.addPackage("javax.media.j3d");
    JavaPackage vecmath = constraint.addPackage("javax.vecmath");
    JavaPackage sun3dLoaders = constraint.addPackage("com.sun.j3d.loaders");
    JavaPackage sun3dLoadersLw3d = constraint.addPackage("com.sun.j3d.loaders.lw3d");
    JavaPackage sun3dUtilsGeometry = constraint.addPackage("com.sun.j3d.utils.geometry");
    JavaPackage sun3dUtilsImage = constraint.addPackage("com.sun.j3d.utils.image");
    JavaPackage sun3dUtilsUniverse = constraint.addPackage("com.sun.j3d.utils.universe");
    JavaPackage loader3ds = constraint.addPackage("com.microcrowd.loader.java3d.max3ds");
    // SunFlow
    JavaPackage sunflow = constraint.addPackage("org.sunflow");
    JavaPackage sunflowCore = constraint.addPackage("org.sunflow.core");
    JavaPackage sunflowCoreLight = constraint.addPackage("org.sunflow.core.light");
    JavaPackage sunflowImage = constraint.addPackage("org.sunflow.image");
    JavaPackage sunflowMath = constraint.addPackage("org.sunflow.math");
    JavaPackage sunflowSystem = constraint.addPackage("org.sunflow.system");
    JavaPackage sunflowSystemUI = constraint.addPackage("org.sunflow.system.ui");
    // iText for PDF
    JavaPackage iText = constraint.addPackage("com.lowagie.text");
    JavaPackage iTextPdf = constraint.addPackage("com.lowagie.text.pdf");
    // FreeHEP Vector Graphics for SVG
    JavaPackage vectorGraphicsUtil = constraint.addPackage("org.freehep.util");
    JavaPackage vectorGraphicsSvg = constraint.addPackage("org.freehep.graphicsio.svg");
    // Java JNLP
    JavaPackage jnlp = constraint.addPackage("javax.jnlp");
    // Mac OS X specific interfaces
    JavaPackage eawt = constraint.addPackage("com.applet.eawt");
    JavaPackage eio = constraint.addPackage("com.applet.eio");

    // Describe dependencies : model don't have any dependency on
    // other packages, IO and View/Controller packages ignore each other
    // and Swing components and Java 3D use is isolated in sweetHome3DSwing
    sweetHome3DTools.dependsUpon(sweetHome3DModel);
    
    sweetHome3DPlugin.dependsUpon(sweetHome3DModel);
    sweetHome3DPlugin.dependsUpon(sweetHome3DTools);
    
    sweetHome3DViewController.dependsUpon(sweetHome3DModel);
    sweetHome3DViewController.dependsUpon(sweetHome3DTools);
    sweetHome3DViewController.dependsUpon(sweetHome3DPlugin);   
    
    sweetHome3DJava3D.dependsUpon(sweetHome3DModel);
    sweetHome3DJava3D.dependsUpon(sweetHome3DTools);
    sweetHome3DJava3D.dependsUpon(java3d);
    sweetHome3DJava3D.dependsUpon(vecmath);
    sweetHome3DJava3D.dependsUpon(sun3dLoaders);
    sweetHome3DJava3D.dependsUpon(sun3dLoadersLw3d);
    sweetHome3DJava3D.dependsUpon(sun3dUtilsGeometry);
    sweetHome3DJava3D.dependsUpon(sun3dUtilsImage);
    sweetHome3DJava3D.dependsUpon(sun3dUtilsUniverse);
    sweetHome3DJava3D.dependsUpon(loader3ds);
    sweetHome3DJava3D.dependsUpon(imageio);
    sweetHome3DJava3D.dependsUpon(sunflow);
    sweetHome3DJava3D.dependsUpon(sunflowCore);
    sweetHome3DJava3D.dependsUpon(sunflowCoreLight);
    sweetHome3DJava3D.dependsUpon(sunflowImage);
    sweetHome3DJava3D.dependsUpon(sunflowMath);
    sweetHome3DJava3D.dependsUpon(sunflowSystem);
    sweetHome3DJava3D.dependsUpon(sunflowSystemUI);
    
    sweetHome3DSwing.dependsUpon(sweetHome3DModel);
    sweetHome3DSwing.dependsUpon(sweetHome3DTools);
    sweetHome3DSwing.dependsUpon(sweetHome3DPlugin);   
    sweetHome3DSwing.dependsUpon(sweetHome3DViewController);
    sweetHome3DSwing.dependsUpon(sweetHome3DJava3D);
    sweetHome3DSwing.dependsUpon(swing);
    sweetHome3DSwing.dependsUpon(imageio);
    sweetHome3DSwing.dependsUpon(java3d);
    sweetHome3DSwing.dependsUpon(vecmath);
    sweetHome3DSwing.dependsUpon(sun3dUtilsGeometry);
    sweetHome3DSwing.dependsUpon(sun3dUtilsUniverse);
    sweetHome3DSwing.dependsUpon(iText);
    sweetHome3DSwing.dependsUpon(iTextPdf);
    sweetHome3DSwing.dependsUpon(vectorGraphicsUtil);
    sweetHome3DSwing.dependsUpon(vectorGraphicsSvg);
    sweetHome3DSwing.dependsUpon(jnlp);
    
    sweetHome3DIO.dependsUpon(sweetHome3DModel);
    sweetHome3DIO.dependsUpon(sweetHome3DTools);
    sweetHome3DIO.dependsUpon(eio);

    // Describe application and applet assembly packages
    sweetHome3DApplication.dependsUpon(sweetHome3DModel);
    sweetHome3DApplication.dependsUpon(sweetHome3DTools);
    sweetHome3DApplication.dependsUpon(sweetHome3DPlugin);
    sweetHome3DApplication.dependsUpon(sweetHome3DViewController);
    sweetHome3DApplication.dependsUpon(sweetHome3DJava3D);
    sweetHome3DApplication.dependsUpon(sweetHome3DSwing);
    sweetHome3DApplication.dependsUpon(sweetHome3DIO);
    sweetHome3DApplication.dependsUpon(swing);
    sweetHome3DApplication.dependsUpon(imageio);
    sweetHome3DApplication.dependsUpon(java3d);
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
    sweetHome3DApplet.dependsUpon(java3d);
    sweetHome3DApplet.dependsUpon(jnlp);
    
    jdepend.analyze();

    assertTrue("Dependency mismatch", jdepend.dependencyMatch(constraint));
  }
}
