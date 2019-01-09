package JEdPoint.modules.module_log;

import JEdPoint.*;
import java.io.*;
import java.util.*;

/**
 * The primitive logging module.
 * <br><br>
 * This module is a very primitive logging module that is used until Jedpoint loads
 * the default log module.
 *
 * This modules logs to system.out...
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_log extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleLog );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Primitive JEdPoint Logging Module";
  private static final String infoModuleNameVersion = "Primitive JEdPoint Logging Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The primitive logging module. ";
  private static final String infoLongDescription   = "The primitive logging module, loaded before all the other modules.";

  // Log Levels
  private static final String loglevelStrings[] =
    { "debug", "info", "warning", "error", "fatal" };
  private static final int loglevelDebug    = 0;
  private static final int loglevelInfo     = 1;
  private static final int loglevelWarning  = 2;
  private static final int loglevelError    = 3;
  private static final int loglevelFatal    = 4;

  public module_log()
  {
  }

  /**
   * Process the Message.
   */
  public JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
  {
    int counter;
    String tempString;

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
      case JEdPointMessage.logSetLogfile:
      break;
      case JEdPointMessage.logDebug:
        this.compactLog(this.loglevelDebug, (String)JPM.getRequest("message"));
      break;
      case JEdPointMessage.logInfo:
        this.compactLog(this.loglevelInfo, (String)JPM.getRequest("message"));
      break;
      case JEdPointMessage.logWarning:
        this.compactLog(this.loglevelWarning, (String)JPM.getRequest("message"));
      break;
      case JEdPointMessage.logError:
        fullLog(this.loglevelError,(JEdPointException)JPM.getRequest("exception"));
      break;
      case JEdPointMessage.logFatal:
        fullLog(this.loglevelFatal,(JEdPointException)JPM.getRequest("exception"));
      break;
    }
    return JPM;
  }

  private String dateAndTime()
  {
    java.util.Date newDate = new java.util.Date();
    return newDate.toString();
  }

  private void compactLog(int loglevel, String message)
  {
    StringBuffer sb = new StringBuffer();

    // First add the date and time
    sb.append( this.dateAndTime() + " ");

    // Now add a short string about what kind of error we're talking about.
    sb.append("[" + this.loglevelStrings[loglevel] + "] " + message + "\n");

    System.out.println( sb.toString() );
  }

  private void fullLog(int loglevel, JEdPointException JPE)
  {
    StringBuffer sb = new StringBuffer();

    if (loglevel==this.loglevelError)
      sb.append("-------------------------------------------------------------------------------\n");
    else
      sb.append("===============================================================================\n");

    // First add the date and time
    sb.append( this.dateAndTime() + " ");

    // Now add a short string about what kind of error we're talking about.
    sb.append("[" + this.loglevelStrings[loglevel] + "]\n");

    // Add the big exception message
    sb.append( JPE.toString() );

    if (loglevel==this.loglevelError)
      sb.append("-------------------------------------------------------------------------------\n");
    else
      sb.append("===============================================================================\n");

    System.out.println( sb.toString() );
  }

}