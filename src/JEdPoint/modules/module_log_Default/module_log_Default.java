package JEdPoint.modules.module_log_Default;

import JEdPoint.*;
import java.io.*;
import java.util.*;

/**
 * The default logging module.
 *
 * This module is a very simple logging module that is used until the user loads
 * a proper logging module. If he doesn't specify one then this one will be used.
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_log_Default extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleLog );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Logging Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Logging Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default logging module. ";
  private static final String infoLongDescription   = "The default logging module, used when there are no other available.";

  private String configFilename    = "module_log_default.conf";

  // Log Levels
  private static final String loglevelStrings[] =
    { "debug", "info", "warning", "error", "fatal" };
  private static final int loglevelDebug    = 0;
  private static final int loglevelInfo     = 1;
  private static final int loglevelWarning  = 2;
  private static final int loglevelError    = 3;
  private static final int loglevelFatal    = 4;

  RandomAccessFile logfile[] = new RandomAccessFile[ loglevelStrings.length ];
  private boolean showMessages[] = new boolean[ loglevelStrings.length ];

  public module_log_Default()
  {
  }

  /**
   * Process the Message.
   */
  public JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
  {
    int counter;
    String tempString;
    JEdPointException jpe;

    switch (messageType)
    {
      case JEdPointMessage.moduleInit:
        // Load our settings from the config file.
        loadSettings(this.getSettingsDir(), this.configFilename);
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
        for (counter=0; counter<this.loglevelStrings.length; counter++)
        {
          if ( (String)JPM.getRequest(this.loglevelStrings[counter]) != null)
          {
            try
            {
                // Check that the directory structure exists. If it doesn't, create it.
                tempString = (String)JPM.getRequest(this.loglevelStrings[counter]);

                // Try to get the directory name by extracting everything up to the slash
                if (tempString.indexOf("\\") != -1)
                  tempString = tempString.substring(0, tempString.lastIndexOf("\\"));
                if (tempString.indexOf("/") != -1)
                  tempString = tempString.substring(0, tempString.lastIndexOf("/"));

                checkDirectory(tempString);
                // Set the logfile
                logfile[counter] = new RandomAccessFile((String)JPM.getRequest(this.loglevelStrings[counter]), "rw");
            }
           catch (Exception e)
           {
             throw new JEdPointException(e, JEdPointException.severityFatal,
               "Could not open/read the log file for the level of: " + this.loglevelStrings[counter],
               "This is most probably caused by an illegal filename.",
               "Please check the JEdPoint config file to see if there is a legal filename specified.");
           }
          }
       }
      break;
      case JEdPointMessage.logDebug:
        tempString = (String)JPM.getRequest("message");
        if (showMessages[loglevelDebug])
        {
          javax.swing.JOptionPane jop = new javax.swing.JOptionPane();
          jop.setMessageType(jop.INFORMATION_MESSAGE);
          jop.setOptionType(jop.DEFAULT_OPTION);
          jop.setMessage( tempString );
          JEdPointMessage newJPM = new JEdPointMessage();
          newJPM.setRequest("joptionpane", jop);
          newJPM.setRequest("title", "Debug");
          sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiDisplayJOptionPane, newJPM);
        }
        compactLog(loglevelDebug, tempString);
      break;
      case JEdPointMessage.logInfo:
        tempString = (String)JPM.getRequest("message");
        if (showMessages[loglevelInfo])
        {
          javax.swing.JOptionPane jop = new javax.swing.JOptionPane();
          jop.setMessageType(jop.INFORMATION_MESSAGE);
          jop.setOptionType(jop.DEFAULT_OPTION);
          jop.setMessage( tempString );
          JEdPointMessage newJPM = new JEdPointMessage();
          newJPM.setRequest("joptionpane", jop);
          newJPM.setRequest("title", "Info");
          sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiDisplayJOptionPane, newJPM);
        }
        compactLog(loglevelInfo, tempString);
      break;
      case JEdPointMessage.logWarning:
        tempString = (String)JPM.getRequest("message");
        if (showMessages[loglevelWarning])
        {
          javax.swing.JOptionPane jop = new javax.swing.JOptionPane();
          jop.setMessageType(jop.WARNING_MESSAGE);
          jop.setOptionType(jop.DEFAULT_OPTION);
          jop.setMessage( tempString );
          JEdPointMessage newJPM = new JEdPointMessage();
          newJPM.setRequest("joptionpane", jop);
          newJPM.setRequest("title", "Warning");
          sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiDisplayJOptionPane, newJPM);
        }
        compactLog(loglevelWarning, tempString);
      break;
      case JEdPointMessage.logError:
        jpe = (JEdPointException)JPM.getRequest("exception");
        if (showMessages[loglevelError])
        {
          javax.swing.JOptionPane jop = new javax.swing.JOptionPane();
          jop.setMessageType(jop.ERROR_MESSAGE);
          jop.setOptionType(jop.DEFAULT_OPTION);
          jop.setMessage( jpe.toString() );
          JEdPointMessage newJPM = new JEdPointMessage();
          newJPM.setRequest("joptionpane", jop);
          newJPM.setRequest("title", "JEdPoint encountered an error that isn't necessarily fatal");
          sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiDisplayJOptionPane, newJPM);
        }
        fullLog(loglevelError,(JEdPointException)JPM.getRequest("exception"));
      break;
      case JEdPointMessage.logFatal:
        jpe = (JEdPointException)JPM.getRequest("exception");
        if (showMessages[loglevelFatal])
        {
          javax.swing.JOptionPane jop = new javax.swing.JOptionPane();
          jop.setMessageType(jop.ERROR_MESSAGE);
          jop.setOptionType(jop.DEFAULT_OPTION);
          jop.setMessage( jpe.toString() );
          JEdPointMessage newJPM = new JEdPointMessage();
          newJPM.setRequest("joptionpane", jop);
          newJPM.setRequest("title", "JEdPoint encountered a fatal error");
          sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiDisplayJOptionPane, newJPM);
        }
        fullLog(loglevelFatal,(JEdPointException)JPM.getRequest("exception"));
      break;
    }
    return JPM;
  }

  /**
   * Returns the settingsdir variable from the mk.
   */
  private String getSettingsDir()
  {
    JEdPointMessage tempJPM = new JEdPointMessage();

    tempJPM = sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkParseSetting, "parse", "%settingsdir%" );

    return tempJPM.getResponseString("parsed");
  }

  /**
   * Loads the settings from the confile file specified.
   */
  private void loadSettings(String settingsDir, String configFilename) throws JEdPointException
  {
    JEdPointMessage tempJPM = new JEdPointMessage();
    JEdPointMessage logJPM = new JEdPointMessage();
    Properties tempProp = new Properties();
    int counter;
    String tempString;

    String fullConfigFilename = settingsDir + File.separator + configFilename;

    // Load the properties from disk...
    try
    {
      tempProp.load(new FileInputStream(fullConfigFilename));
    }
    catch (java.io.FileNotFoundException fnfe)
    {
      System.out.println("Could not find the file " + fullConfigFilename);
      System.out.println("This file is necessary for proper funtioning of JEdPoint's internal log module.");
      System.out.println("Please consult the manual as to how to make a proper config file.");
      System.exit(1);
    }
    catch (Exception e)
    {
      System.out.println("Could not read the file " + fullConfigFilename);
      System.out.println("This file is necessary for proper funtioning of JEdPoint's internal log module.");
      System.out.println("Please consult the manual as to how to make a proper config file.");
      System.exit(1);
    }

    tempJPM = new JEdPointMessage();
    for (counter=0; counter<loglevelStrings.length; counter++)
    {
      tempString = (String)tempProp.get( loglevelStrings[counter] );
      if (tempString != null)
      {
        tempJPM = this.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkParseSetting, "parse", tempString );
        tempString = tempJPM.getResponseString("parsed");
        logJPM.setRequest( loglevelStrings[counter] , tempString);
      }

      tempString = (String)tempProp.getProperty( "show" + loglevelStrings[counter], "false" );
      if (tempString.compareToIgnoreCase("false") == 0)
        showMessages[ counter ] = false;
      else
        showMessages[ counter ] = true;
    }

    // Set all of the files at once.
    processMessage(JEdPointMessage.logSetLogfile, logJPM);
  }

  /**
   * Checks to see if a directory exists.
   * <br><br>
   * If the directory doesn't exist it will try to create one.
   * If a file exists that has the directory name, it will route an exception (and terminate).
   */
  private void checkDirectory(String dirToCheck)
  {
    File newDir = new File(dirToCheck);

    if (newDir.exists() == false)
    {
      // Try to create the directory
      if (!newDir.mkdirs())
        this.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception",
          new JEdPointException(new Exception("Could not create the directory " + dirToCheck),
          JEdPointException.severityFatal,
          "Unable to create the directory: " + dirToCheck,
          "No permission?",
          "Try and create the directory yourself."));
    }
    if (!newDir.isDirectory())
    {
        this.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception",
          new JEdPointException(new Exception("Directory " + dirToCheck + " is a file."),
          JEdPointException.severityFatal,
          "Unable to create the directory: " + dirToCheck,
          "The directory is a file.",
          "Delete the file."));
    }
  }

  /**
   * Replaces a string in a string with another string.
   * <br><br>
   * Just a quick string replacement method so that you don't have to sit and roll your own.
   * <br>
   * Replaces all occurrences of stringToFind.
   */
  public static String replaceString(String stringToUse, String stringToFind, String stringReplaceWith)
  {
    while (stringToUse.indexOf(stringToFind)!=-1)
    {
      stringToUse = stringToUse.substring(0,stringToUse.indexOf(stringToFind)) + stringReplaceWith + stringToUse.substring(stringToUse.indexOf(stringToFind)+stringToFind.length(), stringToUse.length() );
    }
    return stringToUse;
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

    if (this.logfile[loglevel] != null)
      addToFile( loglevel, sb.toString() );
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


    if (this.logfile[loglevel] != null)
      this.addToFile( loglevel, sb.toString() );
  }

  private void addToFile(int loglevel, String stringToAdd)
  {
    try
    {
      this.logfile[loglevel].seek(this.logfile[loglevel].length());
      this.logfile[loglevel].writeBytes(stringToAdd);
    }
    catch (Exception e)
    {
    }
  }

}