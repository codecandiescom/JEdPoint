package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class module_ui_Default extends JEdPointModule implements Serializable
{
  boolean packFrame = false;
  UIFrame frame;

  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleUI );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint User Interface Module";
  private static final String infoModuleNameVersion = "Default JEdPoint User Interface Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default user interface module. ";
  private static final String infoLongDescription   = "The default user interface module, used when there are no other available.";

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
      case JEdPointMessage.moduleDeInit:
        if (frame != null)
          frame.shutDown();
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
      case JEdPointMessage.uiStartUI:
        startFrame();
      break;
      case JEdPointMessage.uiGetStatus:
        JPM.setResponse( "active", new Boolean(frame.isDisplayable()) );
      break;
      case JEdPointMessage.uiDisplayJOptionPane:
        frame.displayJOptionPane( (JOptionPane)JPM.getRequest("joptionpane"),  JPM.getRequestString("title"));
      break;
    }
    return JPM;
  }

  private void startFrame() throws JEdPointException
  {
    frame = new UIFrame();

    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame)
    {
      frame.pack();
    }
    else
    {
      frame.validate();
    }

    frame.initialize(this);

    frame.setVisible(true);
  }
}