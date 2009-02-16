package scala.tools.eclipse.javaelements

import org.eclipse.contribution.jdt.imagedescriptor.IImageDescriptorSelector
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;

class ScalaImageDescriptorSelector extends IImageDescriptorSelector {

  def getTypeImageDescriptor(isInner : Boolean, isInInterfaceOrAnnotation : Boolean, flags : Int, useLightIcons : Boolean, element : AnyRef) : ImageDescriptor = {
    element match {
      case _ : ScalaCompilationUnit => ScalaImages.SCALA_FILE 
      case _ => null
    }
  }
    
  def createCompletionProposalImageDescriptor(proposal : LazyJavaCompletionProposal) : ImageDescriptor = {
    null
  } 
}
