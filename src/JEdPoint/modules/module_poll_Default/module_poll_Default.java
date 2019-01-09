package JEdPoint.modules.module_poll_Default;

import JEdPoint.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * The default poll module.
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_poll_Default extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.modulePoll );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Poll Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Poll Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default poll module. ";
  private static final String infoLongDescription   = "The default poll module, used when there are no other available.";

  private String settingsDir;
  private static final String configFilename      = "module_poll_default.conf";

  private String exportDir;

  Poller poller = new Poller(this);
  Thread PollThread;

  public module_poll_Default()
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
      case JEdPointMessage.pollFullPoll:
        PollThread = new Thread( poller );
        PollThread.start();
      break;
      case JEdPointMessage.pollSendMail:
        PollThread = new Thread( poller );
        PollThread.start();
      break;
      case JEdPointMessage.pollGetMail:
        PollThread = new Thread( poller );
        PollThread.start();
      break;
      case JEdPointMessage.pollCancel:
        poller.threadAlive = false;
        PollThread = null;
      break;
      case JEdPointMessage.pollGetStatus:
        if (PollThread != null)
        {
          JPM.setResponse("actions", poller.getOuputVector() );
          JPM.setResponse("cancancel", poller.canCancel() );
          JPM.setResponse("isready", poller.isReady() );
        }
        else
        {
          JPM.setResponse("actions", new Vector() );
          JPM.setResponse("cancancel", false );
          JPM.setResponse("isready", true );
        }
      break;
    }
    return JPM;
  }

  private class Poller implements Runnable
  {
    JEdPointModule jpm;
    private Vector actionVector = new Vector(); // Stores our current action (plus action history)
    private boolean canCancel = false;          // Can this action be stopped?
    private boolean isReady = true;             // Is the importer ready to be started?
    private boolean threadAlive;

    public Poller(JEdPointModule newjpm)
    {
      this.jpm = newjpm;
    }

    public Vector getOuputVector()
    {
      Vector returnVector = new Vector();

      while (!actionVector.isEmpty())
      {
        returnVector.add( actionVector.elementAt(0) );
        actionVector.remove(0);
      }
      return returnVector;
    }

    public boolean canCancel()
    {
      return canCancel;
    }

    public boolean isReady()
    {
      return isReady;
    }

    public void run()
    {
      Vector packetFiles;

      this.isReady = false;
      this.canCancel = true;

      threadAlive = true;

      actionVector.add("Done!");

      this.isReady = true;
      this.canCancel = false;
    }
  } // IMPORTER
}
