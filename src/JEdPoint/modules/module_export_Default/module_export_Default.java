package JEdPoint.modules.module_export_Default;

import JEdPoint.*;
import java.io.*;
import java.util.*;

/**
 * The default exporter module.
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_export_Default extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleExport );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Message Export Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Message Export Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default message export module. ";
  private static final String infoLongDescription   = "The default message export module, used when there are no other available.";

  private String settingsDir;
  private static final String configFilename      = "module_export_default.conf";

  private String exportDir;
  private String defaultCodepage = "";
  private PointNumber userPointNumber;
  private String packetPassword;

  Exporter pktExporter = new Exporter(this);
  Thread ExportThread;

  public module_export_Default()
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
        // Read our settings from disk
        loadSettings();
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
      case JEdPointMessage.exportStart:
        ExportThread = new Thread( pktExporter );
        ExportThread.start();
      break;
      case JEdPointMessage.exportCancel:
        pktExporter.threadAlive = false;
        ExportThread = null;
      break;
      case JEdPointMessage.exportGetStatus:
        if (ExportThread != null)
        {
          JPM.setResponse("actions", pktExporter.getOuputVector() );
          JPM.setResponse("cancancel", pktExporter.canCancel() );
          JPM.setResponse("isready", pktExporter.isReady() );
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

  private void loadSettings() throws JEdPointException
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Properties props = new Properties();

    JPM = this.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, new JEdPointMessage());
    settingsDir = (String)JPM.getResponse("settingsdir");

    String fullConfigFilename = settingsDir+ File.separator + configFilename;

    try
    {
      props.load( new FileInputStream(fullConfigFilename));
    }
    catch (FileNotFoundException fnfe)
    {
      throw new JEdPointException(
        fnfe,
        JEdPointException.severityFatal,
        "Could not find the settings file: " + fullConfigFilename,
        "File does not exist.",
        "Create a new settings file for the UI module.");
    }
    catch (IOException ioe)
    {
      throw new JEdPointException(
        ioe,
        JEdPointException.severityFatal,
        "Could not read the settings file: " + fullConfigFilename,
        "No read permission?",
        "Check to see that the file can be read.");
    }

    JPM = this.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, new JEdPointMessage());
    exportDir = JPM.getResponseString("exportdir");
    userPointNumber = (PointNumber)JPM.getResponse("pointnumber");
    packetPassword = JPM.getResponseString("packetpassword");
    defaultCodepage = JPM.getResponseString("codepage");
  }

  private class Exporter implements Runnable
  {
    JEdPointModule jpm;
    private Vector actionVector = new Vector(); // Stores our current action (plus action history)
    private boolean canCancel = false;          // Can this action be stopped?
    private boolean isReady = true;             // Is the exporter ready to be started?

    private boolean threadAlive;

    public Exporter(JEdPointModule newjpm)
    {
      jpm = newjpm;
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
      Vector areas = null;

      this.isReady = false;
      this.canCancel = true;

      threadAlive = true;

      areas = findAllAreasThatHaveMessagesToExport();

      actionVector.add(areas.size() + " areas have messages to export.");

      while (!areas.isEmpty())
      {
        actionVector.add("Processing area: " + (String)areas.elementAt(0) );
        addFilenameToClo( exportArea( (String) areas.elementAt(0)) );
        areas.remove(0);
        if (!threadAlive)
          break;
      }

      actionVector.add("Done!");

      this.isReady = true;
      this.canCancel = false;
    }

    private void addFilenameToClo( String filenameToAdd )
    {
      String tempString;
      String filename;
      FileWriter fw;
      File tempFile;

      // Make a .CLO filename out of the net + node info
      tempString = Integer.toHexString( userPointNumber.getNet() );
      while (tempString.length() != 4)
        tempString = "0" + tempString;
      filename = tempString;

      tempString = Integer.toHexString( userPointNumber.getNode() );
      while (tempString.length() != 4)
        tempString = "0" + tempString;
      filename = exportDir + "/" + filename + tempString + ".CLO";

      try
      {
        fw = new FileWriter( filename, true );

        tempFile = new File( exportDir + "/" + filenameToAdd );

        fw.write("#" + tempFile.getCanonicalPath() + "\n");

        fw.close();
      }
      catch (Exception e)
      {
      }
    }

    /**
     * I think the method name speaks for itself.
     */
    private Vector findAllAreasThatHaveMessagesToExport()
    {
      Vector areaList;
      Vector returnVector = new Vector();
      String tempString;
      JEdPointMessage JPM = new JEdPointMessage();
      MessageAreaData MAD;

      // Get a list of all the areas
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbListAreas, JPM);
      areaList = (Vector) JPM.getResponse("areas");
      // Now go through all the areas, fetching their MAD and check to see if exportqueue != 0
      while (!areaList.isEmpty())
      {
        tempString = (String)areaList.elementAt(0);
        JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", tempString);
        MAD = (MessageAreaData)JPM.getResponse("messageareadata");

        // Check the exportqueue
        if (MAD.getMessageAreaDataLong("exportqueue") != 0)
          returnVector.add(tempString);

        areaList.remove(0);
      }
      return returnVector;
    }

    /**
     * Exports all of an areas messages into a packet file.
     *
     * It returns the packet's filename.
     */
    private String exportArea( String areaID )
    {
      FidonetPacket fp = new FidonetPacket();
      Vector messageVector;
      JEdPointMessage JPM;
      FidonetMessage fm;
      Long tempLong;
      GregorianCalendar tempGC = new GregorianCalendar();
      PointNumber tempPN;
      int Attribute;
      String tempString = "";
      Vector tempVector;

      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbListMessagesExport, "id", areaID);
      messageVector = (Vector)JPM.getResponse("vector");

      // Set the header data
      fp.setHeaderData( FidonetPacket.Header_OriginNode, userPointNumber.getNode() );
      fp.setHeaderData( FidonetPacket.Header_DestinationNode, userPointNumber.getNode() );
      fp.setHeaderData( FidonetPacket.Header_Year, tempGC.get(Calendar.YEAR) );
      fp.setHeaderData( FidonetPacket.Header_Month, tempGC.get(Calendar.MONTH) );
      fp.setHeaderData( FidonetPacket.Header_Day, tempGC.get(Calendar.DAY_OF_MONTH) );
      fp.setHeaderData( FidonetPacket.Header_Hour, tempGC.get(Calendar.HOUR_OF_DAY) );
      fp.setHeaderData( FidonetPacket.Header_Minute, tempGC.get(Calendar.MINUTE) );
      fp.setHeaderData( FidonetPacket.Header_Second, tempGC.get(Calendar.SECOND) );
      fp.setHeaderData( FidonetPacket.Header_Baud, 0);
      // We don't have to set the packet version
      fp.setHeaderData( FidonetPacket.Header_OriginNet, userPointNumber.getNet() );
      fp.setHeaderData( FidonetPacket.Header_DestinationNet, userPointNumber.getNet() );
      fp.setHeaderData( FidonetPacket.Header_ProductCodeLow, 0);  // WTF is this anyways?
      fp.setHeaderData( FidonetPacket.Header_PVMajor, 0);  // And WTF is this?
      fp.setHeaderData( FidonetPacket.Header_Password, packetPassword);
      fp.setHeaderData( FidonetPacket.Header_QOriginZone, userPointNumber.getZone() );
      fp.setHeaderData( FidonetPacket.Header_QDestinationZone, userPointNumber.getZone() );
      fp.setHeaderData( FidonetPacket.Header_Filler, 0 );
      fp.setHeaderData( FidonetPacket.Header_ProductCodeHigh, 0);  // WTF is this anyways?
      fp.setHeaderData( FidonetPacket.Header_PVMinor, 0);  // And WTF is this?
      fp.setHeaderData( FidonetPacket.Header_OriginZone, userPointNumber.getZone() );
      fp.setHeaderData( FidonetPacket.Header_DestinationZone, userPointNumber.getZone() );
      fp.setHeaderData( FidonetPacket.Header_OriginPoint, userPointNumber.getPoint() );
      fp.setHeaderData( FidonetPacket.Header_DestinationPoint, 0 );

      fp.setHeaderData( FidonetPacket.Header_ProdData, 0);  // And WTF is this shit?

      while (!messageVector.isEmpty())
      {
        tempLong = (Long)messageVector.elementAt(0);
        JPM.setRequest("id", areaID);
        JPM.setRequest("index", tempLong);
        JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbExportMessage, JPM );
        actionVector.add("Exporting message #" + tempLong.longValue() + " in area " + areaID);

        fm = (FidonetMessage)JPM.getResponse("message");

        tempPN = (PointNumber)fm.getMessageData("pointto");

        Attribute = 0;

        fp.setMessageData( FidonetPacket.Message_OriginNode, userPointNumber.getNode() );
        fp.setMessageData( FidonetPacket.Message_DestinationNode, userPointNumber.getNode() );
        fp.setMessageData( FidonetPacket.Message_OriginNet, userPointNumber.getNet() );
        fp.setMessageData( FidonetPacket.Message_DestinationNet, userPointNumber.getNet());
        fp.setMessageData( FidonetPacket.Message_Attribute, Attribute);
        fp.setMessageData( FidonetPacket.Message_Cost, 0);

        // Set the datetime
        tempGC = (GregorianCalendar)fm.getMessageData("datetime");
        tempString = digits( tempGC.get(Calendar.DAY_OF_MONTH), 2) + " ";
        if (tempGC.get(Calendar.MONTH) == Calendar.JANUARY )
          tempString = tempString + "Jan";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.FEBRUARY )
          tempString = tempString + "Feb";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.MARCH )
          tempString = tempString + "Mar";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.APRIL )
          tempString = tempString + "Apr";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.MAY )
          tempString = tempString + "May";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.JUNE )
          tempString = tempString + "Jun";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.JULY )
          tempString = tempString + "Jul";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.AUGUST )
          tempString = tempString + "Aug";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.SEPTEMBER )
          tempString = tempString + "Sep";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.OCTOBER )
          tempString = tempString + "Oct";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.NOVEMBER )
          tempString = tempString + "Nov";
        else
        if (tempGC.get(Calendar.MONTH) == Calendar.DECEMBER )
          tempString = tempString + "Dec";

        // Let's be naughty and assume that all our dates are from after the year 2000.
        tempString = tempString + " " + digits(tempGC.get(Calendar.YEAR)- 2000, 2) + "  " +
          digits(tempGC.get(Calendar.HOUR_OF_DAY), 2) + ":" + digits(tempGC.get(Calendar.MINUTE), 2) + ":" + digits(tempGC.get(Calendar.SECOND),2);

        fp.setMessageData( FidonetPacket.Message_DateTime, tempString);
        fp.setMessageData( FidonetPacket.Message_ToUsername, fm.getMessageDataString("usernameto") );
        fp.setMessageData( FidonetPacket.Message_FromUsername, fm.getMessageDataString("usernamefrom") );
        fp.setMessageData( FidonetPacket.Message_Subject, fm.getMessageDataString("subject") );

        // ADDING OF MESSAGE TEXT
        tempString = "";
        // Add the prekludges first
        tempVector = (Vector)fm.getMessageData("prekludges");
        while (!tempVector.isEmpty())
        {
          tempString = tempString + tempVector.elementAt(0) + "\n";
          tempVector.remove(0);
        }

        // If there isn't a CHRS kludge, add one.
        if ( tempString.indexOf("\u263ACHRS") == -1 )
        {
          tempString = "\u263ACHRS: " + defaultCodepage + " 2\n" + tempString;
        }

        // If we're in netmail, treat the body differently
        if (areaID.compareToIgnoreCase("netmail") == 0)
        {
          // Set the attribute to "personal"
          fp.setMessageData(FidonetPacket.Message_Attribute, fp.getMessageDataInt(FidonetPacket.Message_Attribute) +1 );

          tempString = "\u263AINTL " + tempPN.getZone()+":"+tempPN.getNet()+"/"+tempPN.getNode() + " " +
            userPointNumber.getZone()+":"+userPointNumber.getNet()+"/"+userPointNumber.getNode() + "\n" + tempString;
          tempString = "\u263AFMPT " + userPointNumber.getPoint() + "\n" + tempString;
          tempString = "\u263ATOPT " + tempPN.getPoint() + "\n" + tempString;
          // Now add the message text
          tempString = tempString + fm.getMessageDataString("message");

          // Add the tearline
          JPM = jpm.sendMessage(JEdPointModule.moduleTearline, JEdPointMessage.tlAssemble, "tearline", fm.getMessageDataString("tearline") );
          if (!tempString.trim().endsWith("\n"))
            tempString = tempString + "\n";
          tempString = tempString + "\n" + JPM.getResponseString("tearline");
        }
        else
        {
          tempString = "AREA:" + areaID + "\n" + tempString;
          // Now add the message text
          tempString = tempString + fm.getMessageDataString("message");

          // Add the tearline
          JPM = jpm.sendMessage(JEdPointModule.moduleTearline, JEdPointMessage.tlAssemble, "tearline", fm.getMessageDataString("tearline") );

          if (!tempString.trim().endsWith("\n"))
            tempString = tempString + "\n";

          tempString = tempString + JPM.getResponseString("tearline");

          // Add the origin
          JPM = new JEdPointMessage();
          JPM.setRequest("origin", fm.getMessageDataString("origin") );
          JPM.setRequest("point", userPointNumber );
          JPM = jpm.sendMessage(JEdPointModule.moduleOrigin, JEdPointMessage.originAssemble, JPM );
          tempString = tempString + "\n" + JPM.getResponseString("origin");
        }
        if (!tempString.trim().endsWith("\n"))
          tempString = tempString + "\n";
        // Add the postkludges
        tempVector = (Vector)fm.getMessageData("postkludges");
        while (!tempVector.isEmpty())
        {
          tempString = tempString + tempVector.elementAt(0) + "\n";
          tempVector.remove(0);
        }

        fp.setMessageData( FidonetPacket.Message_Text, tempString);

        convertFidonetPacketMessageFromUnicode(fp);

        fp.storeMessage( fp.getMessageCount() );

        messageVector.remove(0);
      }
      try
      {
        tempString = FidonetMessageUtils.generateMSGID() + ".pkt";
        fp.writePacket(exportDir + "/" + tempString ,fp);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      return tempString;
    }

    /**
     * Returns digits amount of digits of an int in String form.
     */
    private String digits(int number, int digits)
    {
      String returnString = String.valueOf(number);
      while (returnString.length() != digits)
        returnString = "0" + returnString;
      return returnString;
    }

    /**
     * Converts the currently loaded FidonetPacket message from Unicode.
     * Will automatically find the codepage that the message uses.
     * If it doesn't find a codepage, the default codepage will be used.
     */
    private FidonetPacket convertFidonetPacketMessageFromUnicode( FidonetPacket fp )
    {
      int dataToConvert[] = { FidonetPacket.Message_FromUsername,
                              FidonetPacket.Message_ToUsername,
                              FidonetPacket.Message_Subject,
                              FidonetPacket.Message_Text };
      String tempString;
      String codepage;
      int counter;

      tempString = (String)fp.getMessageData(fp.Message_Text);

      codepage = FidonetMessageUtils.extractCHRS(tempString, defaultCodepage);

      for (counter=0; counter<dataToConvert.length; counter++)
      {
        tempString = JEdPointUtilities.unicodeConvert ( codepage, (String)fp.getMessageData(dataToConvert[counter]), false) ;
        fp.setMessageData( dataToConvert[counter], tempString );
      }
      return fp;
    }

  }

}
