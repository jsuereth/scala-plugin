/*
 * Copyright 2005-2008 LAMP/EPFL
 * @author Sean McDirmid
 */
// $Id$

package scala.tools.eclipse.launching
import org.eclipse.jdt.core._
import org.eclipse.jdt.launching._
import org.eclipse.core.runtime._

object ContainerInitializer {
  def plugin = ScalaPlugin.plugin
  def libPath : String = plugin.bundlePath + "lib/"
    
    /*
    plugin.check{
    val bundle = plugin.getBundle 
    val bpath = bundle.getEntry("/")
    val rpath = FileLocator.resolve(bpath)
    rpath.getPath + "lib/"
  } getOrElse "lib/"
    */
    
  def decode(path : IPath) : (List[(IPath,IPath)], String) = {
    val plugin = this.plugin
    path.lastSegment.split('.').last match {
    case plugin.scalaHome =>
      var home = System.getProperty("scala.home")
      if (home == null) home = System.getenv("SCALA_HOME")
      assert(home != null)
      val lib = new java.io.File(home, "lib")
      val src = new java.io.File(home, "src")
      assert(lib.exists && lib.isDirectory)
      if (!lib.exists || !lib.isDirectory) {
        plugin.logError("Scala home " + lib + " does not exist", null)
      }
      val jars = lib.list(new java.io.FilenameFilter {
        def accept(dir : java.io.File, name : String) = 
          name.endsWith(".jar") && !name.endsWith("-src.jar")
      }).toList
      val ce = jars.map{jar =>
        val libJar = new java.io.File(lib, jar)
        val libPath = Path.fromOSString(libJar.getAbsolutePath)
        val srcJarName = jar.substring(0, jar.length() - (".jar").length()) + "-src.jar"
          var srcJar = new java.io.File(lib, srcJarName);
        if (!srcJar.exists) srcJar = new java.io.File(src, jar);
        val srcPath = if (!srcJar.exists) null else Path.fromOSString(srcJar.getAbsolutePath)
        (libPath, srcPath)
      }
      (ce, "Scala Home")
    case plugin.scalaLib =>
      val classes = Path.fromPortableString(libPath + "scala-library.jar")
      val sources = Path.fromPortableString(libPath + "scala-library-src.jar")
      ((classes, sources) :: Nil, "Scala Library " + scala.util.Properties.versionString)
    }
  }
}

/** for resolving variables */
class ContainerInitializer extends ClasspathContainerInitializer {
  import ContainerInitializer._
  def initialize(cpath : IPath, project : IJavaProject) = plugin.check{
    val de = decode(cpath)
    val ce = de._1.map{
      case (classes,sources) => JavaCore.newLibraryEntry(classes, sources, null)
    }.toArray
    JavaCore.setClasspathContainer(cpath, Array(project), Array(new IClasspathContainer {
      override def getPath = cpath
      override def getClasspathEntries = ce
      override def getDescription = de._2
      override def getKind = IClasspathContainer.K_DEFAULT_SYSTEM
    }), null)
  }
}
