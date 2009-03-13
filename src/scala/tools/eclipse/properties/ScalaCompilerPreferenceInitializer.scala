/*
 * Copyright 2005-2009 LAMP/EPFL
 * @author Josh Suereth
 */
// $Id$

package scala.tools.eclipse.properties

import org.eclipse.core.runtime.preferences.{ AbstractPreferenceInitializer, DefaultScope }
//import org.eclipse.core.runtime.preferences._
//import org.eclipse.jface.preference._

import scala.tools.nsc.Settings

import scala.tools.eclipse.SettingConverterUtil._
import scala.tools.eclipse.util.IDESettings

/**
 * This is responsible for initializing Scala Compiler
 * Preferences to their default values.
 */
class ScalaCompilerPreferenceInitializer extends AbstractPreferenceInitializer {
  
  /** Actually initializes preferences */
  def initializeDefaultPreferences() : Unit = {
	  
    ScalaPlugin.plugin.check {
      val settings = new Settings(null)
      val node = new DefaultScope().getNode(ScalaPlugin.plugin.pluginId)
      val store = ScalaPlugin.plugin.getPluginPreferences

      IDESettings.shownSettings(settings).foreach {
        setting =>
          val preferenceName = convertNameToProperty(setting.name)
          setting.value match {
            case bool : Boolean  => node.put(preferenceName, bool.toString)
            case string : String => node.put(preferenceName, string)
          }
      }
    }
  }
}
