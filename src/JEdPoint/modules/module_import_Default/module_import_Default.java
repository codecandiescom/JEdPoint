package JEdPoint.modules.module_import_Default;

import JEdPoint.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * The default importer module.
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_import_Default extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleImport );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Message Import Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Message Import Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default message import module. ";
  private static final String infoLongDescription   = "The default message import module, used when there are no other available.";

  private String settingsDir;
  private static final String configFilename      = "module_import_default.conf";

  private String importDir;
  private String defaultCodepage;
  private String copyZips;
  private boolean deleteZips;
  private String copyPackets;
  private boolean deletePackets;
  private String badPacketDirectory;

  Importer pktImporter = new Importer(this);
  Thread ImportThread;

  public module_import_Default()
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
      case JEdPointMessage.importStart:
        ImportThread = new Thread( pktImporter );
        ImportThread.start();
      break;
      case JEdPointMessage.importCancel:
        pktImporter.threadAlive = false;
        ImportThread = null;
      break;
      case JEdPointMessage.importGetStatus:
        if (ImportThread != null)
        {
          JPM.setResponse("actions", pktImporter.getOuputVector() );
          JPM.setResponse("cancancel", pktImporter.canCancel() );
          JPM.setResponse("isready", pktImporter.isReady() );
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
    String tempString;

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
    importDir = JPM.getResponseString("importdir");
    defaultCodepage = JPM.getResponseString("codepage");

    // Load the directory where to put bad packets
    if (props.getProperty("badpacketdirectory", "").length() != 0)
    {
      badPacketDirectory = parseSetting( props.getProperty("badpacketdirectory") );
      // if this directory doesn't exist, create it
      checkDirectory( badPacketDirectory );
    }
    else
      throw new JEdPointException( new Exception("Setting: \"badpacketdirectory\" not found."), JEdPointException.severityFatal,
        "Could not find the setting in the file: " + configFilename,
        "This setting does not exist in the file.",
        "Consult the manual for the correct settings file format." );

    // Get the codepage. If there isn't such an option specified, use the user's
    // codepage.
    if (props.get("defaultcodepage") != null)
      defaultCodepage = (String)props.get("defaultcodepage");

    // Not yet implemented
    if (props.get("copyzips") != null)
      copyZips = (String)props.get("copyzips");

    // DeleteZips
    tempString = props.getProperty("deletezips", "true");
    if (tempString.compareToIgnoreCase("true") == 0)
      deleteZips = true;
    else
      deleteZips = false;

    // Not yet implemented
    if (props.get("copypackets") != null)
      copyPackets = (String)props.get("copypackets");

    // DeletePackets
    tempString = props.getProperty("deletepackets", "true");
    if (tempString.compareToIgnoreCase("true") == 0)
      deletePackets = true;
    else
      deletePackets = false;
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

  /**
   * Checks to see if a directory exists.
   * <br><br>
   * If the directory doesn't exist it will try to create one.
   * If a file exists that has the directory name, it will throw an exception (and terminate).
   */
  private void checkDirectory(String dirToCheck) throws JEdPointException
  {
    File newDir = new File(dirToCheck);

    if (newDir.exists() == false)
    {
      // Try to create the directory
      if (!newDir.mkdirs())
        throw new JEdPointException(new Exception("Could not create the directory " + dirToCheck),
          JEdPointException.severityFatal,
          "Unable to create the directory: " + dirToCheck,
          "No permission?",
          "Try and create the directory yourself.");
    }
    if (!newDir.isDirectory())
    {
      throw new JEdPointException(new Exception("Directory " + dirToCheck + " is a file."),
          JEdPointException.severityFatal,
          "Unable to create the directory: " + dirToCheck,
          "The directory is a file.",
          "Delete the file.");
    }
  }

  // ---------------------------------------------------------------------------
  // IMPORTER
  // ---------------------------------------------------------------------------
  private class Importer implements Runnable
  {
    JEdPointModule jpm;
    private Vector actionVector = new Vector(); // Stores our current action (plus action history)
    private boolean canCancel = false;          // Can this action be stopped?
    private boolean isReady = true;             // Is the importer ready to be started?
    private boolean threadAlive;

    public Importer(JEdPointModule newjpm)
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

      isReady = false;
      canCancel = true;

      threadAlive = true;

      // Clear all of the newimported attributes
      jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbClearImported, new JEdPointMessage() );

      unzipImportDir( importDir );

      packetFiles = findAllPacketFiles( importDir );

      while (!packetFiles.isEmpty())
      {
        actionVector.add("Processing file: " + (String)packetFiles.elementAt(0) );

        importPacket( (String)packetFiles.elementAt(0) );

        if (deletePackets)
        {
          try
          {
            if ( new File( (String)packetFiles.elementAt(0) ).exists() )
              if ( new File( (String)packetFiles.elementAt(0) ).delete() == false )
                // We couldn't delete the file...
                jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Import: Could not delete the file: " + (String)packetFiles.elementAt(0) );
          }
          catch (NullPointerException npe)
          {
          } // TRY
        } // IF

        packetFiles.remove(0);

        if (!threadAlive)
          break;
      }
      actionVector.add("Done!");

      isReady = true;
      canCancel = false;
    }

    /**
     * Finds all the zip files in the import dir and unzips them to importDir
     */
    private void unzipImportDir(String directory)
    {
      File files[] = new File( importDir ).listFiles();
      ZipFile zf;
      int counter;

      // Find all the files in the importDir and try to unzip them.
      for (counter=0; counter<files.length; counter++)
      {
        try
        {
          // If the file is a packet file, skip it
          if (files[counter].getName().toLowerCase().endsWith(".pkt"))
            continue;

          zf = new ZipFile( files[counter], ZipFile.OPEN_READ );

          unzipFile( zf.getName() );

          zf.close();

          if (deleteZips)
            files[counter].delete();
        }
        catch (ZipException ze)
        {
          // This isn't a zip file, ignore it.
        }
        catch (Exception e)
        {
          // File could not be read, for some reason.
          // This is more serious than a simple not-a-zipfile-exception, so let's log it
          jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Import: " + files[counter].getName() + " isn't a zip file.");
        }
      }
    }

    private void unzipFile( String zipFilename )
    {
      final int bufferSize = 8192;
      try
      {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilename));
        FileOutputStream fos;
        ZipEntry ze;
        byte byteBuffer[] = new byte[bufferSize];
        int readbytes;

        //process sequentially
        while ( (ze = zis.getNextEntry()) != null)
        {
          actionVector.add("Unzipping packet: " + ze.getName() + " from file " + zipFilename + "...");
          fos = new FileOutputStream( importDir + "/" + ze.getName() );

          // Copy everything from the zis to the fos
          readbytes = zis.read(byteBuffer, 0, byteBuffer.length);
          while (readbytes > 0)
          {
            fos.write(byteBuffer, 0, readbytes);
            readbytes = zis.read(byteBuffer, 0, byteBuffer.length);
          }

          fos.close();
        }
        zis.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    private Vector findAllPacketFiles( String directoryToSearch )
    {
      Vector returnVector = new Vector();
      int counter;
      File files[];

      files = new File( directoryToSearch ).listFiles();

      for (counter=0; counter<files.length; counter++)
      {
        // All these ifs are just qualifications to see if it is a .pkt FILE

        if (files[counter].getName().toLowerCase().indexOf(".pkt") != -1)
        // This filename has the word ".pkt" in it
          if ( files[counter].isFile() )
          // This is a file
            if ( files[counter].canRead() )
            // And it can be read
              returnVector.add( files[counter].getAbsolutePath() );
      }

      return returnVector;
    }

    private void importPacket( String PacketToImport )
    {
      int messageCounter;
      int counter;
      FidonetPacket fp = new FidonetPacket();
      FidonetMessage fm;
      String area;
      String tempString;
      GregorianCalendar gc = new GregorianCalendar();
      JEdPointMessage JPM = new JEdPointMessage();
      File sourceFile, destinationFile;

      try
      {
        fp = fp.readPacket( PacketToImport );
      }
      catch (Exception e)
      {
        actionVector.add("Error trying to read " + PacketToImport + ". Putting it aside...");

        // Move the file to the badpackets directory
        sourceFile = new File( PacketToImport );
        destinationFile = new File( badPacketDirectory + "/" + sourceFile.getName() );
        sourceFile.renameTo(destinationFile);
        return;
      }

      messageCounter = fp.getMessageCount();

      for (counter=0; counter<messageCounter; counter++)
      {
        try
        {
          fm = new FidonetMessage();

          fp.loadMessage(counter);

          fp = convertFidonetPacketMessageToUnicode( fp );

          tempString = fp.getMessageDataString(fp.Message_Text);

          if ( tempString.indexOf("AREA:") != -1)
            try
            {
              area = tempString.substring( tempString.indexOf("AREA:")+5, tempString.indexOf("\n") );
            }
            catch (Exception exceptionArea)
            {
              area = "BAD";
            }
          else
            area = "NETMAIL";

          // Before we take into account which area the messages are in, take the
          // stuff that's common to all areas. Names, Subjects, etc...
          fm.setMessageData("usernamefrom", (String)fp.getMessageData(fp.Message_FromUsername) );
          fm.setMessageData("usernameto", (String)fp.getMessageData(fp.Message_ToUsername) );
          fm.setMessageData("subject", (String)fp.getMessageData(fp.Message_Subject) );

          // TODO: Some time in the future we'll actually care about attributes.
          // Until then, ignore those attribute thingies in the FidonetPacket.

          // Get the message id
          try
          {
            tempString = (String)fp.getMessageData(fp.Message_Text);
            if (tempString.indexOf("\u263AMSGID: ") != -1)
            {
              tempString = tempString.substring( tempString.indexOf("\u263AMSGID: ") );
              tempString = tempString.substring( tempString.indexOf(" ") +1 );
              tempString = tempString.substring( tempString.indexOf(" ") +1 );
              tempString = tempString.substring( 0, tempString.indexOf("\n") ).toLowerCase();
            }
            else
              tempString = "";
          }
          catch (Exception exceptionMsgID)
          {
            throw new Exception("Error extracting the message id");
          }
          fm.setMessageData( "msgid", tempString );

          // Get the date
          try
          {
            tempString = (String)fp.getMessageData(fp.Message_DateTime);
            gc.set( Calendar.DAY_OF_MONTH, Integer.parseInt(tempString.substring(0, 2)) );
            // Yes, isn't it lovely to have to deal with fucking dates that only have two digits?
            // "Hi, I'm the guy who designed the packet format. I didn't think we'd ever reach the
            // year 2000, so I didn't figure we'd ever need 4 digit years."
            // I hope to god he DIDN'T make it to the year 2000.
            if ( Integer.parseInt(tempString.substring( 7, 9)) < 50 )
              gc.set( Calendar.YEAR, Integer.parseInt(tempString.substring(7, 9)) + 2000 );
            else
              gc.set( Calendar.YEAR, Integer.parseInt(tempString.substring(7, 9)) + 1900 );
            // Get the month...
            tempString = tempString.substring(3, 6);
            if (tempString.compareTo("Jan") == 0) gc.set( Calendar.MONTH, Calendar.JANUARY);
            else
            if (tempString.compareTo("Feb") == 0) gc.set( Calendar.MONTH, 1);
            else
            if (tempString.compareTo("Mar") == 0) gc.set( Calendar.MONTH, 2);
            else
            if (tempString.compareTo("Apr") == 0) gc.set( Calendar.MONTH, 3);
            else
            if (tempString.compareTo("May") == 0) gc.set( Calendar.MONTH, 4);
            else
            if (tempString.compareTo("Jun") == 0) gc.set( Calendar.MONTH, 5);
            else
            if (tempString.compareTo("Jul") == 0) gc.set( Calendar.MONTH, 6);
            else
            if (tempString.compareTo("Aug") == 0) gc.set( Calendar.MONTH, 7);
            else
            if (tempString.compareTo("Sep") == 0) gc.set( Calendar.MONTH, 8);
            else
            if (tempString.compareTo("Oct") == 0) gc.set( Calendar.MONTH, 9);
            else
            if (tempString.compareTo("Nov") == 0) gc.set( Calendar.MONTH, 10);
            else
            if (tempString.compareTo("Dec") == 0) gc.set( Calendar.MONTH, 11);
            tempString = (String)fp.getMessageData(fp.Message_DateTime);
            gc.set( Calendar.HOUR_OF_DAY, Integer.parseInt(tempString.substring(11, 13)) );
            gc.set( Calendar.MINUTE, Integer.parseInt(tempString.substring(14, 16)) );
            gc.set( Calendar.SECOND, Integer.parseInt(tempString.substring(17, 19)) );
          }
          catch (Exception exceptionDateTime)
          {
            gc = new GregorianCalendar();
          }
          // Finally... We've got the date and everything...
          fm.setMessageData( "datetime", gc );

          // Remove the "AREA" kludge from the message text...
          try
          {
            fp.setMessageData(fp.Message_Text, JEdPointUtilities.removeLine((String)fp.getMessageData(fp.Message_Text), "AREA:") );
          }
          catch (Exception exceptionRemoveLine)
          {
          }

          tempString = (String)fp.getMessageData(fp.Message_Text);

          try
          {
            fm.setMessageData("tearline", FidonetMessageUtils.extractTearline(tempString) );
          }
          catch (Exception exceptionTearline)
          {
            throw new Exception("Error extracting the tearline");
          }

          try
          {
            fm.setMessageData("prekludges", FidonetMessageUtils.extractPreKludges(tempString) );
          }
          catch (Exception exceptionPreKludges)
          {
            throw new Exception("Error extracting prekludges");
          }

          try
          {
            fm.setMessageData("postkludges", FidonetMessageUtils.extractPostKludges(tempString) );
          }
          catch (Exception exceptionPostKludges)
          {
            throw new Exception("Error extracting postkludges");
          }

          try
          {
            fm.setMessageData("message", FidonetMessageUtils.extractMessageBody(tempString) );
          }
          catch (Exception exceptionMessageBody)
          {
            throw new Exception("Error extracting message body");
          }

          if (area.compareTo("NETMAIL") == 0)
          {
            // This is netmail. Treat it differently.

            // First the FMPT
            try
            {
              tempString = (String)fp.getMessageData(fp.Message_Text);

              // Yes, that's right boys and girls... I have to have a fucking FMPT check
              // here in case the netmail is from a boss. WHY couldn't FMPT _always_ be included?
              if (tempString.indexOf("FMPT") != -1)
                tempString = JEdPointUtilities.getLine(tempString, "INTL") + "\n"
                  + JEdPointUtilities.getLine(tempString, "FMPT") + "\n";
              else
                tempString = JEdPointUtilities.getLine(tempString, "INTL") + "\n" + "FMPT 0" + "\n";

              tempString = tempString.substring( tempString.indexOf(" ")+1 );
              tempString = tempString.substring( tempString.indexOf(" ")+1 );
              tempString = tempString.substring( 0, tempString.indexOf("\n") )
                + "."
                + tempString.substring( tempString.lastIndexOf(" ")+1, tempString.lastIndexOf("\n") );
            }
            catch (Exception eFMPT)
            {
              throw new Exception("Could not extract the frompoint");
            }

            fm.setMessageData( "pointfrom", PointNumber.parsePointNumber(tempString) );

            // Now the TOPT
            try
            {
              tempString = (String)fp.getMessageData(fp.Message_Text);

              // Another fucking check...
              if (tempString.indexOf("TOPT") != -1)
                tempString = JEdPointUtilities.getLine(tempString, "INTL") + "\n"
                  + JEdPointUtilities.getLine(tempString, "TOPT") + "\n";
              else
                tempString = JEdPointUtilities.getLine(tempString, "INTL") + "\n" + "TOPT 0" + "\n";

              tempString = tempString.substring( tempString.indexOf(" ")+1 );
              tempString = tempString.substring( 0, tempString.indexOf(" ") )
                + "."
                + tempString.substring( tempString.lastIndexOf(" ")+1, tempString.lastIndexOf("\n") );
            }
            catch (Exception eTOPT)
            {
              throw new Exception("Could not extract the topoint");
            }

            fm.setMessageData( "pointto", PointNumber.parsePointNumber(tempString) );

            // Set an empty origin
            fm.setMessageData("origin", "" );
          }
          else
          {
            // Retrieve the (from) point number from the origin
            try
            {
              tempString = JEdPointUtilities.getLastLine( (String)fp.getMessageData(fp.Message_Text), " * Origin");

              tempString = tempString.substring( tempString.lastIndexOf("(") + 1, tempString.lastIndexOf(")") );

              tempString = minimizePointnumberString( tempString );

              fm.setMessageData("pointfrom", PointNumber.parsePointNumber(tempString) );
            }
            catch (Exception ePointFrom)
            {
              // Obviously some form av point number we couldn't parse. So fuck it.
              fm.setMessageData("pointfrom", PointNumber.parsePointNumber("0:0/0.0") );
            }

            // If it's a reply, we can extract the "to" point number
            tempString = (String)fp.getMessageData(fp.Message_Text);
            if ( tempString.indexOf("\u263AREPLY: ") != -1 )
            {
              tempString = tempString.substring( tempString.indexOf("\u263AREPLY: ") +8 );
              tempString = tempString.substring( 0, tempString.indexOf(" ") );

              tempString = minimizePointnumberString( tempString );

              try
              {
                fm.setMessageData("pointto", PointNumber.parsePointNumber(tempString) );
              }
              catch (Exception pointtoReply)
              {
                // Obviously some form av point number we couldn't parse. So fuck it.
                fm.setMessageData("pointto", new PointNumber(0,0,0,0) );
              }
            }
            else
              fm.setMessageData("pointto", new PointNumber(0,0,0,0) );

            // Extract the origin
            try
            {
              fm.setMessageData("origin", FidonetMessageUtils.extractOrigin( (String)fp.getMessageData(fp.Message_Text) ) );
            }
            catch (Exception e)
            {
            }
          }

          // Finally, add the message to the messagebase...
          JPM.setRequest("id", area);
          JPM.setRequest("message", fm);
          jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbImportMessage, JPM);

          actionVector.add( area + ": Message from " + (String)fp.getMessageData(fp.Message_FromUsername) + " to " +  (String)fp.getMessageData(fp.Message_ToUsername) );
        }
        catch (Exception e)
        {
          actionVector.add( "Error trying to import message #" + (counter+1) + " in packet " + PacketToImport );
          sourceFile = new File( PacketToImport );
          destinationFile = new File( badPacketDirectory + "/" + sourceFile.getName() );
          sourceFile.renameTo(destinationFile);
          jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logError, "exception", new JEdPointException(
            e,
            JEdPointException.severityError,
            "Error trying to import message #" + (counter+1) + " in packet " + PacketToImport,
            "An error in the message in the packet.",
            "Try to find the error yourself?") );
        }
      }
    }

    /**
     * Removes as much bullshit from a string as possible, leaving (hopefully) a parseable point number.
     *
     * Removes things like @Fidonet and text in front of or before the numbers.
     */
    private String minimizePointnumberString( String pointNumberString )
    {
      int counter;
      String returnValue = "";
      char tempChar;
      boolean addTheChar;
      String doubleCheck[] = { "::", "//", ".." };

      // Remove whatever is after the @, because that is usually the bitch in most
      // non standard point numbers
      if (pointNumberString.indexOf("@") != -1)
        pointNumberString = pointNumberString.substring(0, pointNumberString.indexOf("@"));

      // Copy all the numbers, colons, slashes and points from pointnumberstring.
      for (counter=0; counter<pointNumberString.length(); counter++)
      {
        addTheChar = false;

        tempChar = pointNumberString.charAt(counter);

        if ( ( Character.isDigit( tempChar ) )
          || ( ( tempChar == ':' ) )
          || ( ( tempChar == '/' ) )
          || ( ( tempChar == '.' ) )
        )
          addTheChar = true;

        if (addTheChar)
          returnValue += tempChar;
      }

      // We now remove doubles ( double colons and double points etc ... )
      for (counter=0; counter<doubleCheck.length; counter++)
      {
        while ( returnValue.indexOf(doubleCheck[counter]) != -1 )
          returnValue = JEdPointUtilities.replaceString( returnValue, doubleCheck[counter], doubleCheck[counter].substring(0, 1) );
      }

      return returnValue;
    }

    /**
     * Converts the currently loaded FidonetPacket message to Unicode.
     * Will automatically find the codepage that the message uses.
     */
    private FidonetPacket convertFidonetPacketMessageToUnicode( FidonetPacket fp )
    {
      int dataToConvert[] = { fp.Message_FromUsername,
                              fp.Message_ToUsername,
                              fp.Message_Subject,
                              fp.Message_Text };
      String tempString;
      String codepage;
      int counter;


      tempString = (String)fp.getMessageData(fp.Message_Text);
      codepage = FidonetMessageUtils.extractCHRS(tempString, defaultCodepage);

      for (counter=0; counter<dataToConvert.length; counter++)
      {
        tempString = JEdPointUtilities.unicodeConvert ( codepage, fp.getMessageDataString(dataToConvert[counter]), true) ;
        fp.setMessageData( dataToConvert[counter], tempString );
      }
      return fp;
    }
  }

}
