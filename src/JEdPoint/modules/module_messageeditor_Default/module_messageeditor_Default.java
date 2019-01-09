package JEdPoint.modules.module_messageeditor_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;

public class module_messageeditor_Default extends JEdPointModule implements Serializable
{
  private JEdPointModule thisModule = this;
  DialogMessageEditor dialog;

  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleMessageEditor );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Message Editor Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Message Editor Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default message editor module. ";
  private static final String infoLongDescription   = "The default message editor module, used when there are no other available.";

  /**Construct the application*/
  public module_messageeditor_Default()
  {
  }

  /**
   * Process the Message.
   */
  public JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
  {
    String tempstring;

    switch (messageType)
    {
      case JEdPointMessage.moduleInit:
      break;
      case JEdPointMessage.moduleGetInformation:
        JPM.setResponse("author", this.infoAuthor);
        JPM.setResponse("modulename", this.infoModuleName);
        JPM.setResponse("modulenameversion", this.infoModuleNameVersion);
        JPM.setResponse("shortdescription", this.infoShortDescription);
        JPM.setResponse("longdescription", this.infoLongDescription);
        JPM.setResponse("type", this.infoType);
        JPM.setResponse("versionhigh", this.infoVersionHigh);
        JPM.setResponse("versionlow", this.infoVersionLow);
        JPM.setResponse("apiversion", this.infoApiVersion);
      break;
      case JEdPointMessage.meEditMessage:
        startFrame( (FidonetMessage)JPM.getRequest("message"), (JFrame)JPM.getRequest("jframe") );
        // Retrieve the message from the frame and put it in the response.
        JPM.setResponse( "cancel", dialog.editingCanceled() );
        JPM.setResponse( "message", dialog.getMessage() );
      break;
      case JEdPointMessage.meGetStatus:
        JPM.setResponse( "active", new Boolean(dialog.isDisplayable()) );
    }
    return JPM;
  }

  private void startFrame(FidonetMessage messageToEdit, JFrame parentFrame) throws JEdPointException
  {
    try
    {
      dialog = new DialogMessageEditor(parentFrame, "Message Editor", true);
      dialog.initialize(this);
      dialog.setMessage(messageToEdit);
      dialog.setVisible(true);
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal, "Could not start the message editor.",
        "See the stack trace.",
        "See the stack trace.");
    }
  }
}