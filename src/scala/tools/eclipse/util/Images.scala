package scala.tools.eclipse.util

import java.net.URL

import scala.collection.jcl.LinkedHashMap

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.graphics.Image

import scala.tools.eclipse.ScalaPlugin

object Images {
  
  private val images = new LinkedHashMap[ImageDescriptor,Image]

  def image(desc : ImageDescriptor) = images.get(desc) match {
    case Some(image) => image
    case None => 
      val image = desc.createImage
      images(desc) = image
      image
  }

  def fullIcon(name : String) = 
    image(ImageDescriptor.createFromURL(new URL(ScalaPlugin.plugin.getBundle.getEntry("/icons/full/"), name)))
}
