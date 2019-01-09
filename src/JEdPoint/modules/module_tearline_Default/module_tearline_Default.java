package JEdPoint.modules.module_tearline_Default;

import JEdPoint.*;
import java.io.*;
import java.util.*;

/**
 * The default tearline module.
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_tearline_Default extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleTearline );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Tearline Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Tearline Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default tearline module. ";
  private static final String infoLongDescription   = "The default tearline module, used when there are no other available.";

//  private static final String configFilename    = "module_tearline_default.conf";

  public module_tearline_Default()
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
      case JEdPointMessage.tlGetTearline:
        tempstring = (String)JPM.getRequest("file");
        tempstring = SearchFile( parseSetting(tempstring) );
        JPM.setResponse("tearline", tempstring);
      break;
      case JEdPointMessage.tlAssemble:
        JPM.setResponse("tearline", this.AssembleATearline((String)JPM.getRequest("tearline")) );
      break;
    }
    return JPM;
  }

  private String AssembleATearline( String tearline )
  {
    int maxValue = Math.min(tearline.length(), 80-4 );
    return "--- " + tearline.substring(0, maxValue);
  }

  // Checks to see if a file exists. Returns true or false.
  private boolean FileExists(String Filename)
  {
    File tempfile = new File(Filename);
    return tempfile.exists();
  }

  private String SearchFile(String Filename) throws JEdPointException
  {
    int linecounter = 0;
    int counter = 0;
    int linechosen;
    String tempstring = Filename;

    // Why would we be getting a null anyways?
    if (Filename == null) return "";

    if ( !FileExists(Filename) ) return Filename;

    try
    {
      java.io.BufferedReader br = new BufferedReader( new java.io.FileReader(new File(Filename) ) );

      while (br.readLine() != null)
        linecounter++;

      br.close();

      linechosen = (int)(Math.random() * (double)linecounter);

      br = new BufferedReader( new java.io.FileReader(new File(Filename) ) );

      for (counter=0; counter<linechosen; counter++)
      {
        tempstring = br.readLine();
      }
      tempstring = br.readLine();
    }
    catch (Exception e)
    {
      // File not found?
      // Null point exception?
      throw new JEdPointException(e,
        JEdPointException.severityError,
        "Could not read the tearline file \"" + Filename + "\".",
        "File I/O Error",
        "Check to see if the file can be read");
    }
    return tempstring;
  }

  /**
   * Asks JEdPoint to parse some settings for us.
   */
  private String parseSetting( String settingToParse )
  {
    JEdPointMessage JPM = new JEdPointMessage();
    JPM = this.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkParseSetting, "parse", settingToParse );
    return JPM.getResponseString("parsed");
  }

}
