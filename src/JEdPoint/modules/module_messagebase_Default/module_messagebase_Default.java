package JEdPoint.modules.module_messagebase_Default;

import JEdPoint.*;
import java.io.*;
import java.util.*;

/**
 * The default message base module.
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class module_messagebase_Default extends JEdPointModule
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleMessageBase );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "Default JEdPoint Message Base Module";
  private static final String infoModuleNameVersion = "Default JEdPoint Message Base Module v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The default message base module.";
  private static final String infoLongDescription   = "The default message base, used when there are no other available.";

  private static final String configFilename    = "module_messagebase_default.conf";

  private byte headerIdentifier[] = { 1, 0x4A, 0x50 , 2 };
  private byte reverseIdentifier[] = { headerIdentifier[3], headerIdentifier[2], headerIdentifier[1], headerIdentifier[0] };
  private final int closeMsg = 20;
  private final int bufferSize = 65536;

  private final String fileExtensions[] = {
    ".data",
    ".attributes"
  };
  private final int extData = 0;
  private final int extAttr = 1;

  private final String statistics[] = {
    "totalmessages",
    "totalmessagesunread",
    "personalmessagestotal",
    "personalmessagesunread",
    "newmessagestotal",
    "newmessagesunread",
    "newpersonalmessagestotal",
    "newpersonalmessagesunread",
    "lastread",
    "exportqueue",
    "deletequeue"
  };
  private final int statTotalMessages             =  0;
  private final int statTotalMessagesUnread       =  1;
  private final int statPersonalMessagesTotal     =  2;
  private final int statPersonalMessagesUnread    =  3;
  private final int statNewMessagesTotal          =  4;
  private final int statNewMessagesUnread         =  5;
  private final int statNewPersonalMessagesTotal  =  6;
  private final int statNewPersonalMessagesUnread =  7;
  private final int statLastRead                  =  8;
  private final int statExportQueue               =  9;
  private final int statDeleteQueue               = 10;

  private final int attrExport        = 1;
  private final int attrPersonal      = 2;
  private final int attrNewlyImported = 4;
  private final int attrRead          = 8;
  private final int attrDelete        = 16;
  private final int attributeCount    = 1;    // How many bytes are the attributes?
  private final int attributedefault  = 0;

  private String settingCurrentUser;
  private String settingSettingsDir;
  private String settingDataDir;
  private String settingNetmailDir;
  private String settingMessageBaseDir;
  private boolean settingCompressHeader;
  private boolean settingCompressBody;
  private int settingHeaderCompressionLevel;
  private int settingBodyCompressionLevel;
  private String settingDefaultAreaTearline;
  private String settingDefaultAreaOrigin;

  private String netmail;
  private String MADFile;

  private static Vector MAD;

  private String openArea = "";
  private RandomAccessFile fileData;
  private RandomAccessFile fileAttr;

  private long lastMessage;
  private long lastMessagePosition;
  private int lastHeaderSize;
  private int lastBodySize;
  private long lastAttribute;
  private int lastAttributeRead;

  private String lastMADArea = "";
  private MessageAreaData lastMAD;
  private int lastMADIndex;

  /**
   * The MADAutoSave class takes care of saving my MAD whenever it's needed.
   * But it doesn't save the file _too_ often.
   *
   * It waits <CheckTheClassSource> seconds after having received the last req to save before
   * saving it.
   */
  MADAutoSave MADAutoSaver = new MADAutoSave();
  Thread autosaverThread = new Thread(MADAutoSaver);

  /**
   * Process the Message.
   */
  public JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
  {
    MessageAreaData tempMAD;
    FidonetMessage fm;

    String areaID;
    long index;

    Vector indexes;
    Vector tempVector;
    String tempString;
    Long tempLong;
    long counter;

    long rangeStart, rangeStop; // Used only for mbGetMessageHeaders


    switch (messageType)
    {
      case JEdPointMessage.moduleInit:
        loadSettings();
        init();
        autosaverThread.start();
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
      case JEdPointMessage.moduleDeInit:
        areaClose(openArea);
        MADSave();
        sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logDebug, "message", "Messagebase: Shut down.");
      break;
      // -----------------------------------------------------------------------
      // AREA
      // -----------------------------------------------------------------------
      case JEdPointMessage.mbAddArea:
        tempMAD = (MessageAreaData)JPM.getRequest("messageareadata");
        areaID = tempMAD.getMessageAreaDataString("id");

        if (MADFind( areaID ) == -1)
          createArea( tempMAD );
      break;
      case JEdPointMessage.mbCatchUpArea:
        areaID = JPM.getRequestString("id");
        // Get a list of all the unread messages
        tempVector = listMessagesAttribute(areaID, statGet(areaID, statTotalMessagesUnread), attrRead, false);
        // And mark each one as read
        while (!tempVector.isEmpty())
        {
          tempLong = (Long)tempVector.elementAt(0);
          setMessageAttributes(areaID, tempLong.longValue(), null, null, null, new Boolean(true), null);
          tempVector.removeElementAt(0);
        }
        // Set the lastread of the area to the last message
        statSet(areaID, statLastRead, statGet(areaID, statTotalMessages));
      break;
      case JEdPointMessage.mbChangeArea:
        changeArea( JPM.getRequestString("id"), (MessageAreaData)JPM.getRequest("messageareadata") );
      break;
      case JEdPointMessage.mbClearImported:
        if (JPM.getRequest("id") != null)
        {
          // Clear just one area
          areaID = JPM.getRequestString("id");
          clearImported(areaID);
        }
        else
        {
          // Clear ALL the areas
          for (counter=0; counter<MAD.size(); counter++)
          {
            tempMAD = (MessageAreaData)MAD.elementAt((int)counter);
            areaID = tempMAD.getMessageAreaDataString("id");
            clearImported(areaID);
          }
        }
      break;
      case JEdPointMessage.mbGetAreaInformation:
        JPM.setResponse( "messageareadata", getAreaInformation(JPM.getRequestString("id")) );
      break;
      case JEdPointMessage.mbListAreas:
        JPM.setResponse( "areas", listAreas() );
      break;
      case JEdPointMessage.mbPackArea:
        areaID = JPM.getRequestString("id");
        if ( JPM.getRequest("repair") != null )
          if ( JPM.getRequestBoolean("repair") == true )
            packAreaRepair(areaID);
        else
          packAreaQuick(areaID);
      break;
      case JEdPointMessage.mbRemoveArea:
        if (JPM.getRequest("id") != null)
        {
          tempString = JPM.getRequestString("id");
          removeArea( JPM.getRequestString("id") );
        }
        if (JPM.getRequest("ids") != null)
        {
          tempVector = (Vector)JPM.getRequest("ids");
          while (!tempVector.isEmpty())
          {
            tempString = (String)tempVector.elementAt(0);
            removeArea( tempString );
            tempVector.remove(0);
          }
        }
      break;
      // -----------------------------------------------------------------------
      // MESSAGE
      // -----------------------------------------------------------------------
      case JEdPointMessage.mbAddMessage:
        fm = (FidonetMessage)JPM.getRequest("message");
        areaAddMessage( JPM.getRequestString("id"), fm, false, true, false );
      break;
      case JEdPointMessage.mbDeleteMessage:
        areaID = JPM.getRequestString("id");
        // Delete only one message
        if (JPM.getRequest("index") != null)
        {
          index = JPM.getRequestLong("index");
          deleteMessage(areaID, index);
        }
        // Delete several messages
        if (JPM.getRequest("indexes") != null)
        {
          tempVector = (Vector) JPM.getRequest("indexes");
          while (!tempVector.isEmpty())
          {
            tempLong = (Long)tempVector.elementAt(tempVector.size()-1);
            deleteMessage(areaID, tempLong.longValue());
            tempVector.remove( tempVector.size()-1 );
          }
        }
      break;
      case JEdPointMessage.mbChangeMessage:
        areaID = JPM.getRequestString("id");
        index = JPM.getRequestLong("index");
        fm = (FidonetMessage)JPM.getRequest("message");
        changeMessage(areaID, index, fm);
      break;
      case JEdPointMessage.mbExportMessage:
        index = JPM.getRequestLong("index");
        areaID = JPM.getRequestString("id");
        setMessageExported( areaID, index );
        fm = assembleHeaderAndBody( areaGetHeader( areaID, index ), areaGetBody( areaID, index ) );
        JPM.setResponse("message", fm);
      break;
      case JEdPointMessage.mbGetMessageHeader:
        fm = areaGetHeader( JPM.getRequestString("id"), JPM.getRequestLong("index") );
        JPM.setResponse("message", fm);
      break;
      case JEdPointMessage.mbGetMessageHeaders:
        areaID = JPM.getRequestString("id");
        if (JPM.getRequest("rangestart") != null)
          rangeStart = JPM.getRequestLong("rangestart");
        else
          rangeStart = 1;
        if (JPM.getRequest("rangestop") != null)
          rangeStop = JPM.getRequestLong("rangestop");
        else
          rangeStop = areaGetCount(areaID);
        tempVector = new Vector();
        for (counter=rangeStart; counter<=rangeStop; counter++)
        {
          tempVector.add( areaGetHeader(areaID, counter) );
        }
        JPM.setResponse("messagevector", tempVector);
      break;
      // LISTMESSAGES
      case JEdPointMessage.mbListMessagesDelete:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statDeleteQueue), attrDelete, true) );
      break;
      case JEdPointMessage.mbListMessagesExport:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statExportQueue), attrExport, true) );
      break;
      case JEdPointMessage.mbListMessagesNew:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statNewMessagesTotal), attrNewlyImported, true) );
      break;
      case JEdPointMessage.mbListMessagesNewUnread:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statNewMessagesUnread), attrNewlyImported, true, attrRead, false) );
      break;
      case JEdPointMessage.mbListMessagesPersonal:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statPersonalMessagesTotal), attrPersonal, true) );
      break;
      case JEdPointMessage.mbListMessagesPersonalUnread:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statPersonalMessagesUnread), attrPersonal, true, attrRead, false) );
      break;
      case JEdPointMessage.mbListMessagesPersonalNew:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statNewPersonalMessagesTotal), attrPersonal, true, attrNewlyImported, true) );
      break;
      case JEdPointMessage.mbListMessagesPersonalNewUnread:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statNewPersonalMessagesUnread), attrPersonal, true, attrNewlyImported, true, attrRead, false) );
      break;
      case JEdPointMessage.mbListMessagesUnread:
        areaID = JPM.getRequestString("id");
        JPM.setResponse( "vector", listMessagesAttribute(areaID, statGet(areaID, statTotalMessagesUnread), attrRead, false) );
      break;
      // end of LISTMESSAGES
      case JEdPointMessage.mbGetMessage:
        fm = assembleHeaderAndBody( areaGetHeader( JPM.getRequestString("id"), JPM.getRequestLong("index") ), areaGetBody( JPM.getRequestString("id"), JPM.getRequestLong("index") ) );
        JPM.setResponse("message", fm);
      break;
      case JEdPointMessage.mbReadMessage:
        index = JPM.getRequestLong("index");
        areaID = JPM.getRequestString("id");
        setMessageRead( areaID, index );
        statSet(areaID, statLastRead, index);
        fm = assembleHeaderAndBody( areaGetHeader( areaID, index ), areaGetBody( areaID, index ) );
        JPM.setResponse("message", fm);
      break;
      case JEdPointMessage.mbImportMessage:
        fm = (FidonetMessage)JPM.getRequest("message");
        areaAddMessage( JPM.getRequestString("id"), fm, true, false, false );
      break;
      case JEdPointMessage.mbSetMessageAttributes:
        areaID = JPM.getRequestString("id");
        index = JPM.getRequestLong("index");
        setMessageAttributes( areaID, index,
          (Boolean)JPM.getRequest("attributeexport"),
          (Boolean)JPM.getRequest("attributepersonal"),
          (Boolean)JPM.getRequest("attributenew"),
          (Boolean)JPM.getRequest("attributeread"),
          (Boolean)JPM.getRequest("attributedelete") );
      break;
      case JEdPointMessage.mbWriteMessage:
        fm = (FidonetMessage)JPM.getRequest("message");
        areaID = JPM.getRequestString("id");
        areaAddMessage( areaID, fm, false, true, true );
      break;
    }
    return JPM;
  }

  // ---------------------------------------------------------------------------
  // AREAS
  // ---------------------------------------------------------------------------
  private void changeArea( String areaID, MessageAreaData newMAD ) throws JEdPointException
  {
    int areaToChange = MADFind( areaID );
    int counter;

    // Take oldMAD out for a walk...
    MessageAreaData oldMAD = (MessageAreaData)MAD.elementAt(areaToChange);

    // Start changing all the values

    // If the area ID changes then we'll have to rename all the files. Which isn't a problem.
    if (newMAD.getMessageAreaDataString("id").compareToIgnoreCase( oldMAD.getMessageAreaDataString("id") ) != 0)
    {
      renameArea( getCorrectDirectory(oldMAD), oldMAD.getMessageAreaDataString("id"), getCorrectDirectory(newMAD), newMAD.getMessageAreaDataString("id") );
    }

    // Copy all the RO fields to the oldMAD.
    for (counter=0; counter<oldMAD.fieldsrw[oldMAD.version].length; counter++)
    {
      oldMAD.setMessageAreaData( oldMAD.fieldsrw[oldMAD.version][counter], newMAD.getMessageAreaData(oldMAD.fieldsrw[oldMAD.version][counter]) );
    }

    // ... and put oldMAD back in after the walk.
    MAD.setElementAt(oldMAD, areaToChange);

    // And finally resort the MAD
    MADSort();
  }

  private void clearImported( String areaID ) throws JEdPointException
  {
    long counter;
    long newlyImported;


    newlyImported = statGet(areaID, statNewMessagesTotal);
    counter=statGet(areaID, statTotalMessages);

    while (newlyImported > 0)
    {
      if (getAttribute(areaID, counter, attrNewlyImported) == true)
      {
        setAttribute(areaID, counter, attrNewlyImported, false);
        newlyImported--;
      }
      counter--;
    }

    statSet(areaID, statNewMessagesTotal, 0);
    statSet(areaID, statNewMessagesUnread, 0);
    statSet(areaID, statNewPersonalMessagesTotal, 0);
    statSet(areaID, statNewPersonalMessagesUnread, 0);
  }

  /**
   * Will rename all the files that have to do with a messagebase to something else.
   */
  private void renameArea( String oldDirectory, String oldID, String newDirectory, String newID ) throws JEdPointException
  {
    File oldFile, newFile;
    int counter;

    for (counter=0; counter<fileExtensions.length; counter++)
    {
      areaClose(oldID);
      oldFile = new File( oldDirectory + File.separator + oldID + fileExtensions[counter] );
      newFile = new File( newDirectory + File.separator + newID + fileExtensions[counter] );
      oldFile.renameTo( newFile );
    }
  }

  /**
   * Returns the MAD of areaID
   * Returns null if the area doesn't exist
   */
  private MessageAreaData getAreaInformation( String areaID )
  {
    int counter;

    counter = MADFind(areaID);
    if (counter != -1)
      return (MessageAreaData) MAD.elementAt(counter);
    else
      return null;
  }

  /**
   * Examines a MAD area ID to see if it's netmail or not.
   * <br><br>
   * If it's the netmail area, return the netmail directory, else return
   * the messagebase directory.
   */
  private String getCorrectDirectory( String areaID )
  {
    if (areaID.compareToIgnoreCase("netmail") == 0)
      return this.settingNetmailDir;
    else
      return this.settingMessageBaseDir;
  }

  private Vector listAreas()
  {
    int counter;
    Vector returnVector = new Vector( this.MAD.size() );
    MessageAreaData tempMAD;

    for (counter=0; counter<MAD.size(); counter++)
    {
      tempMAD = (MessageAreaData) MAD.elementAt(counter);
      returnVector.add( tempMAD.getMessageAreaDataString("id") );
    }

    return returnVector;
  }

  private void loadSettings() throws JEdPointException
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Properties props = new Properties();

    // Retrieve settings from the mk
    JPM = sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, JPM );

    settingCurrentUser =          JPM.getResponseString("username");
    settingSettingsDir =          JPM.getResponseString("settingsdir");
    settingDataDir =              JPM.getResponseString("datadir");
    settingNetmailDir =           JPM.getResponseString("netmaildir");
    settingDefaultAreaTearline =  JPM.getResponseString("defaultareatearline");
    settingDefaultAreaOrigin =    JPM.getResponseString("defaultareaorigin");

    String fullConfigFilename = settingSettingsDir+ File.separator + configFilename;

    netmail = "Netmail";

    // Load our settings from disk
    try
    {
      props.load(new FileInputStream(fullConfigFilename));
    }
    catch (java.io.FileNotFoundException fnfe)
    {
      throw new JEdPointException( fnfe, JEdPointException.severityFatal,
        "Could not find the settings file: " + fullConfigFilename,
        "This file does not exist.",
        "Please consult the manual as to how to make a proper config file." );
    }
    catch (Exception e)
    {
      throw new JEdPointException( e, JEdPointException.severityFatal,
        "Could not read the settings file: " + fullConfigFilename,
        "Do we have permission to read the file? Is it opened by another program?",
        "Try reading / writing to the file using an editor." );
    }


    // Load the directory of our message base.
    if (props.getProperty("messagebasedirectory", "").length() != 0)
    {
      this.settingMessageBaseDir = this.parseSetting( props.getProperty("messagebasedirectory") );
      // if this directory doesn't exist, create it
      this.checkDirectory( this.settingMessageBaseDir );

      this.MADFile = this.settingMessageBaseDir + "/MessageAreaData.vector";
    }
    else
      throw new JEdPointException( new Exception("Setting: \"messagebasedirectory\" not found."), JEdPointException.severityFatal,
        "Could not find the setting in the file: " + fullConfigFilename,
        "This setting does not exist in the file.",
        "Consult the manual for the correct settings file format." );


    // Load the compression settings
    if (props.getProperty("compressheader", "false").toLowerCase().compareTo("false") == 0)
      settingCompressHeader = false;
    else
      settingCompressHeader = true;

    if (props.getProperty("compressbody", "false").toLowerCase().compareTo("false") == 0)
      settingCompressBody = false;
    else
      settingCompressBody = true;


    // Load the compression levels.
    // Assume maximum compression if the keys aren't set or aren't valid (numberformatexception).
    try
    {
      settingHeaderCompressionLevel =  Integer.parseInt( props.getProperty("headercompressionlevel", "9") );
    }
    catch (NumberFormatException nfe)
    {
      settingHeaderCompressionLevel = 9;
    }

    try
    {
      settingBodyCompressionLevel =  Integer.parseInt( props.getProperty("bodycompressionlevel", "9") );
    }
    catch (NumberFormatException nfe)
    {
      settingBodyCompressionLevel = 9;
    }
    return;
  }

  private void init() throws JEdPointException
  {
    int counter;
    MessageAreaData mad;

    // Load the MAD from disk
    this.MAD = (Vector) JEdPointUtilities.loadObject( MADFile );

    if (MAD == null)
    {
      MAD = new Vector();
      // File could not be read.
      // If we can't write to it either, then something's wrong.
      if (!JEdPointUtilities.saveObject( MADFile, MAD ))
      {
        throw new JEdPointException( new Exception("Could not read/write our message base MessageAreaData file."), JEdPointException.severityFatal,
          "The file: " + MADFile + " could not be read nor written.",
          "No r/w in that directory?",
          "Check to see that we have rw permission in that directory" );
      }

      // By now we've proven to ourselves that we can write the file.

      // Create a basic MAD
      createMAD();
    }

    // Find all unknown areas, add them to the MAD. Check that the existing areas'
    // attributes are ok, etc.
    findOrphanedAreas();
    checkData();
    checkAttributes();
    checkStats();
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
   * Searches for areas on disk that aren't included in the MAD.
   */
  private void findOrphanedAreas() throws JEdPointException
  {
    File tempFile;
    File fileList[];
    int counter;
    String tempString;

    // Search for his netmail data
    if (MADFind(netmail) == -1)
    {
      // If there is no .data file at all, create one
      if (!fileExists(settingNetmailDir + File.separator + netmail + fileExtensions[extData]) )
      {
        // Create a completely new netmail database.
        createArea(netmail);
      }
      else
      {
        // Add it to the MAD
        MADAdd(netmail);
      }
    }

    tempFile = new File( settingMessageBaseDir + "/" );
    fileList = tempFile.listFiles();
    for (counter=0; counter<fileList.length; counter++)
    {
      tempString = fileList[counter].getName();
      if (tempString.toLowerCase().endsWith(fileExtensions[extData]) )
      {
        // We've found ourselves a .data file
        // Now the question is whether we know about it

        // Get _just_ the filename
        tempString = tempString.substring(0, tempString.indexOf(fileExtensions[extData]) );

        if (MADFind(tempString) == -1)
        {
          // Add the file to the vector.
          createArea(tempString);
        }
      } // if (tempString.toLowerCase().endsWith(fileExtensions[extData]) )
    } // for (counter=0; counter<fileList.length; counter++)
  }

  /**
   * Checks that the .data file for this areaID exists.
   */
  private void checkData(String areaID) throws JEdPointException
  {
    // By opening this area, the .data and .attributes files are automatically created
    areaOpen(areaID);
  }

  /**
   * Checks the data file of the whole MAD
   */
  private void checkData() throws JEdPointException
  {
    int counter;
    MessageAreaData tempMAD;

    for (counter=0; counter<MAD.size(); counter++)
    {
      tempMAD = (MessageAreaData)MAD.elementAt(counter);
      checkData( tempMAD.getMessageAreaDataString("id") );
    }
  }

  /**
   * Checks that the attribute files for the data files are the same size as
   * the data files themselves (have the same message count).
   */
  private void checkAttributes() throws JEdPointException
  {
    int counter;
    MessageAreaData tempMAD;
    String areaID;

    for (counter=0; counter<MAD.size(); counter++)
    {
      tempMAD = (MessageAreaData)MAD.elementAt(counter);
      areaID = tempMAD.getMessageAreaDataString("id");

      try
      {
        areaOpen(areaID);
        fileAttr.seek( fileAttr.length() );
        while (fileAttr.length() < (attributeCount * areaGetCount(areaID)) )
        {
          fileAttr.write((byte)attributedefault);
        }
      }
      catch (JEdPointException JPE)
      {
        throw JPE;
      }
      catch (Exception e)
      {
      }
    }
  }

  /**
   * Checks the statistics of the message areas
   */
  private void checkStats() throws JEdPointException
  {
    int counter;
    MessageAreaData mad;
    String areaID;

    for (counter=0; counter<this.MAD.size(); counter++)
    {
      mad = (MessageAreaData) MAD.elementAt(counter);
      areaID = mad.getMessageAreaDataString("id");
      if ( !statisticsMatchDatabase(mad) )
        gatherStatistics(areaID );
    }
  }

  /**
   * Checks that the area data matches the message base file on disk
   */
  private boolean statisticsMatchDatabase( MessageAreaData mad )
  {
    String areaID;

    // If the message count doesn't match totalmessages (in the mad)
    // regather the statistics
    areaID = mad.getMessageAreaDataString("id");

    if ( areaGetCount(areaID) != mad.getMessageAreaDataLong("totalmessages") )
      return false;
    else
      return true;
  }

  /**
   * Creates a completely new MAD.
   *
   * Adds the netmail area to it.
   */
  private void createMAD() throws JEdPointException
  {
    MessageAreaData mad = new MessageAreaData();
    File files[];
    int counter;

    generateNetmailMAD(mad);

    MADAdd( mad );
  }

  private void generateNetmailMAD(MessageAreaData mad)
  {
    mad.setMessageAreaData("id", "Netmail");
    mad.setMessageAreaData("description", "Private mail");
    mad.setMessageAreaData("tearline", "JEdPoint (http://www.sourceforge.net/projects/jedpoint)");
    mad.setMessageAreaData("origin", "Netmail does not use origins.");
    mad.setMessageAreaData("readonly", new Boolean(false));
    mad.setMessageAreaData("replyin", "");
  }

  /**
   * Examines a MAD area ID to see if it's netmail or not.
   * <br><br>
   * If it's the netmail area, return the netmail directory, else return
   * the messagebase directory.
   */
  private String getCorrectDirectory( MessageAreaData mad )
  {
    return getCorrectDirectory( mad.getMessageAreaDataString("id") );
  }

  private static FidonetMessage assembleHeaderAndBody( FidonetMessage header, FidonetMessage body )
  {
    int counter;
    FidonetMessage returnValue = new FidonetMessage();

    for (counter=0; counter<header.fieldsHeader[header.version].length; counter++)
      returnValue.setMessageData( header.fieldsHeader[header.version][counter], header.getMessageData(header.fieldsHeader[header.version][counter]) );

    for (counter=0; counter<body.fieldsBody[body.version].length; counter++)
      returnValue.setMessageData( body.fieldsBody[body.version][counter], body.getMessageData(body.fieldsBody[body.version][counter]) );

    return returnValue;
  }

  // ---------------------------------------------------------------------------
  // STATISTICS
  // ---------------------------------------------------------------------------

  /**
   * Marks a message as read (if it hasn't been read already).
   *
   * Updates the stats for the area.
   */
  private void setMessageRead( String areaID, long index ) throws JEdPointException
  {
    boolean personal, newlyImported;

    if (getAttribute(areaID, index, attrRead) == false)
    {
      personal = getAttribute(areaID, index, attrPersonal);
      newlyImported = getAttribute(areaID, index, attrNewlyImported);

      statDecrease(areaID, statTotalMessagesUnread);
      if (personal)
        statDecrease(areaID, statPersonalMessagesUnread);
      if (newlyImported)
        statDecrease(areaID, statNewMessagesUnread);
      if (personal & newlyImported)
        statDecrease(areaID, statNewPersonalMessagesUnread);

      setAttribute(areaID, index, attrRead, true);
    }
  }

  /**
   * Marks a message as exported (if it hasn't been exported already).
   *
   * Updates the stats for the area.
   */
  private void setMessageExported( String areaID, long index ) throws JEdPointException
  {
    if (getAttribute(areaID, index, attrExport) == true)
    {
      statDecrease(areaID, statExportQueue);

      setAttribute(areaID, index, attrExport, false);
    }
  }

  /**
   * Reads the .msgbase file and generates stats from it.
   */
  private void gatherStatistics( String areaID ) throws JEdPointException
  {
    long totalmessages;
    long totalmessagesunread         = 0;
    long personalmessagestotal       = 0;
    long personalmessagesunread      = 0;
    long newmessagestotal            = 0;
    long newmessagesunread           = 0;
    long newpersonalmessagestotal    = 0;
    long newpersonalmessagesunread   = 0;
    long exportqueue                 = 0;
    long counter;

    totalmessages = areaGetCount(areaID);

    // The zero is important here, because when statAdding gets to work, it increases
    // TotalMessages too.
    statSet(areaID, statTotalMessages,              0);
    statSet(areaID, statTotalMessagesUnread,        totalmessagesunread);
    statSet(areaID, statPersonalMessagesTotal,      personalmessagestotal);
    statSet(areaID, statPersonalMessagesUnread,     personalmessagesunread);
    statSet(areaID, statNewMessagesTotal,           newmessagestotal);
    statSet(areaID, statNewMessagesUnread,          newmessagesunread);
    statSet(areaID, statNewPersonalMessagesTotal,   newpersonalmessagestotal);
    statSet(areaID, statNewPersonalMessagesUnread,  newpersonalmessagesunread);

    for (counter=1; counter<=totalmessages; counter++)
    {
      statAdding(areaID,
        getAttribute(areaID, counter, attrExport),
        getAttribute(areaID, counter, attrPersonal),
        getAttribute(areaID, counter, attrNewlyImported),
        getAttribute(areaID, counter, attrRead),
        getAttribute(areaID, counter, attrDelete)
      );
    }
  }

  private long statGet( String areaID, int statistic ) throws JEdPointException
  {
    // Load the MAD for this area
    MessageAreaData mad = (MessageAreaData)MAD.elementAt( MADFind(areaID) );

    // Return the statistic requested...
    return mad.getMessageAreaDataLong( statistics[statistic] );
  }

  private void statSet( String areaID, int statistic, long newValue ) throws JEdPointException
  {
    int index = MADFind(areaID);
    // Load the MAD for this area
    MessageAreaData mad = (MessageAreaData)MAD.elementAt( index );

    // Return the statistic requested...
    mad.setMessageAreaData( statistics[statistic], newValue );

    MAD.setElementAt( mad, index );
  }

  /**
   * Increases whichever stats that need to be changed, given all four attributes.
   */
  private void statAdding( String areaID, boolean toExport, boolean personal, boolean newlyImported, boolean read, boolean delete ) throws JEdPointException
  {
    if (delete)
      statIncrease(areaID, statDeleteQueue);
    if (toExport)
      statIncrease(areaID, statExportQueue);

    statIncrease(areaID, statTotalMessages);

    if (!read)
      statIncrease(areaID, statTotalMessagesUnread);
    if (personal)
      statIncrease(areaID, statPersonalMessagesTotal);
    if (personal & !read)
      statIncrease(areaID, statPersonalMessagesUnread);
    if (newlyImported)
      statIncrease(areaID, statNewMessagesTotal);
    if (newlyImported & !read)
      statIncrease(areaID, statNewMessagesUnread);
    if (personal & newlyImported)
      statIncrease(areaID, statNewPersonalMessagesTotal);
    if (personal & newlyImported & !read)
      statIncrease(areaID, statNewPersonalMessagesUnread);
  }

  /**
   * Decreases whichever stats that need to be changed, given all four attributes.
   */
  private void statSubtracting( String areaID, boolean toExport, boolean personal, boolean newlyImported, boolean read, boolean delete ) throws JEdPointException
  {
    if (delete)
      statDecrease(areaID, statDeleteQueue);
    if (toExport)
      statDecrease(areaID, statExportQueue);

    statDecrease(areaID, statTotalMessages);

    if (!read)
      statDecrease(areaID, statTotalMessagesUnread);
    if (personal)
      statDecrease(areaID, statPersonalMessagesTotal);
    if (personal & !read)
      statDecrease(areaID, statPersonalMessagesUnread);
    if (newlyImported)
      statDecrease(areaID, statNewMessagesTotal);
    if (newlyImported & !read)
      statDecrease(areaID, statNewMessagesUnread);
    if (personal & newlyImported)
      statDecrease(areaID, statNewPersonalMessagesTotal);
    if (personal & newlyImported & !read)
      statDecrease(areaID, statNewPersonalMessagesUnread);
  }

  /**
   * Increases a specified statistic by one
   */
  private void statIncrease( String areaID, int statistic) throws JEdPointException
  {
    if (areaID.compareTo(lastMADArea) != 0)
    {
      lastMADIndex = MADFind(areaID);
      // Load the MAD for this area
      lastMAD = (MessageAreaData)MAD.elementAt( lastMADIndex );
      lastMADArea = areaID;
    }

    // Return the statistic requested...
    lastMAD.setMessageAreaData( statistics[statistic], lastMAD.getMessageAreaDataLong(statistics[statistic])+1 );

    MAD.setElementAt( lastMAD, lastMADIndex );
  }

  /**
   * Decreases a specified statistic by one
   */
  private void statDecrease( String areaID, int statistic ) throws JEdPointException
  {
    int index = MADFind(areaID);
    // Load the MAD for this area
    MessageAreaData mad = (MessageAreaData)MAD.elementAt( index );

    // Return the statistic requested...
    mad.setMessageAreaData( statistics[statistic], mad.getMessageAreaDataLong(statistics[statistic])-1 );

    MAD.setElementAt( mad, index );
  }

  // ---------------------------------------------------------------------------
  // AREA
  // ---------------------------------------------------------------------------
  private void changeMessage( String areaID, long index, FidonetMessage newFM ) throws JEdPointException
  {
    byte buffer[] = new byte[bufferSize];
    byte headerBuffer[];
    byte bodyBuffer[];
    int bufferRead;
    File fromFile, toFile;
    long counter;
    long messagePosition;         // Where the message starts
    long messageAfterPosition;    // Where the message ends
    long messageEndPosition;      // Where the file ends
    int smallcounter;

    FidonetMessage header = new FidonetMessage();
    FidonetMessage body = new FidonetMessage();

    String filenameInput = getCorrectDirectory(areaID) + File.separator + areaID + fileExtensions[extData];
    String filenameOutput = getCorrectDirectory(areaID+".tmp") + File.separator + areaID+".tmp" + fileExtensions[extData];

    try
    {
      messageEndPosition = areaGetFileSize(areaID);
      messagePosition = areaSeekMessage(areaID, index);
      if (index + 1 > areaGetCount(areaID) )
        messageAfterPosition = messageEndPosition;
      else
        messageAfterPosition = areaSeekMessage(areaID, index+1);

      areaClose(areaID);

      BufferedInputStream bis = new BufferedInputStream( new FileInputStream( filenameInput ) );
      RandomAccessFile raf = new RandomAccessFile( filenameOutput, "rw" );

      counter = 0;

      while (counter<messagePosition)
      {
        bufferRead = bis.read(buffer, 0, Math.min(buffer.length, (int)(messagePosition-counter)) );
        raf.write(buffer, 0, bufferRead);
        counter += bufferRead;
      }

      // Move all of the header data from fm -> header
      for (smallcounter=0; smallcounter<newFM.fieldsHeader[newFM.version].length; smallcounter++)
        header.setMessageData( newFM.fieldsHeader[newFM.version][smallcounter], newFM.getMessageData(newFM.fieldsHeader[newFM.version][smallcounter]) );

      // Move all of the body data from fm -> body
      for (smallcounter=0; smallcounter<newFM.fieldsBody[newFM.version].length; smallcounter++)
        body.setMessageData( newFM.fieldsBody[newFM.version][smallcounter], newFM.getMessageData(newFM.fieldsBody[newFM.version][smallcounter]) );

      headerBuffer = JEdPointUtilities.objectToByteArray(header);
      if (settingCompressHeader) headerBuffer = JEdPointUtilities.compressByteArray(headerBuffer, true, settingHeaderCompressionLevel);

      bodyBuffer = JEdPointUtilities.objectToByteArray(body);
      if (settingCompressBody) bodyBuffer = JEdPointUtilities.compressByteArray(bodyBuffer, true, settingBodyCompressionLevel);

      // Write the ID at the beginning
      raf.write(headerIdentifier);       // 4 bytes
      raf.writeLong(index);            // 8 bytes
      raf.writeInt(headerBuffer.length); // 4 bytes
      raf.writeInt(bodyBuffer.length);   // 4 bytes  = 20 bytes total.

      // Write the header and body
      raf.write(headerBuffer);
      raf.write(bodyBuffer);

      // Write the ID at the end
      raf.write(reverseIdentifier);
      raf.writeLong(index);
      raf.writeInt(headerBuffer.length);
      raf.writeInt(bodyBuffer.length);

      // Skip the message
      bis.skip( messageAfterPosition - messagePosition );
      counter += ( messageAfterPosition - messagePosition );

      while (counter<messageEndPosition)
      {
        bufferRead = bis.read(buffer, 0, Math.min(buffer.length, (int)(messageEndPosition-counter)) );
        raf.write(buffer, 0, bufferRead);
        counter += bufferRead;
      }

      bis.close();
      raf.close();

      fromFile = new File( filenameOutput );
      toFile = new File( filenameInput );

      // Delete the original file and move the new file to the old file
      toFile.delete();
      fromFile.renameTo( toFile );
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityError, "Could not change the message!",
        "", "" );
    }
  }

  private void deleteMessage( String areaID, long index ) throws JEdPointException
  {
    // Set's the attribute to the ! of whatever it was
    if ( getAttribute(areaID, index, attrDelete) )
    {
      setAttribute(areaID, index, attrDelete, false);
      // Increase the stat
      statDecrease(areaID, statDeleteQueue );
    }
    else
    {
      setAttribute(areaID, index, attrDelete, true);
      // Decrease the stat
      statIncrease(areaID, statDeleteQueue );
    }
  }

  /**
   * Returns a vector of message whose attributes match those specified.
   */
  private Vector listMessagesAttribute(String areaID, long total, int attribute, boolean attributeStatus) throws JEdPointException
  {
    return listMessagesAttribute(areaID, total,
      attribute, attributeStatus,
      attribute, attributeStatus,
      attribute, attributeStatus,
      attribute, attributeStatus);
  }

  private Vector listMessagesAttribute(String areaID, long total,
    int attribute1, boolean attributeStatus1,
    int attribute2, boolean attributeStatus2 ) throws JEdPointException
  {
    return listMessagesAttribute(areaID, total,
      attribute1, attributeStatus1,
      attribute2, attributeStatus2,
      attribute1, attributeStatus1,
      attribute2, attributeStatus2 );
  }

  private Vector listMessagesAttribute(String areaID, long total,
    int attribute1, boolean attributeStatus1,
    int attribute2, boolean attributeStatus2,
    int attribute3, boolean attributeStatus3 ) throws JEdPointException
  {
    return listMessagesAttribute(areaID, total,
      attribute1, attributeStatus1,
      attribute2, attributeStatus2,
      attribute3, attributeStatus3,
      attribute3, attributeStatus3 );
  }

  private Vector listMessagesAttribute(String areaID, long total,
    int attribute1, boolean attributeStatus1,
    int attribute2, boolean attributeStatus2,
    int attribute3, boolean attributeStatus3,
    int attribute4, boolean attributeStatus4 ) throws JEdPointException
  {
    Vector returnVector = new Vector();
    long counter;

    counter = statGet(areaID, statTotalMessages);
    while (total != 0)
    {
      if (
        (getAttribute(areaID, counter, attribute1) == attributeStatus1) &
        (getAttribute(areaID, counter, attribute2) == attributeStatus2) &
        (getAttribute(areaID, counter, attribute3) == attributeStatus3) &
        (getAttribute(areaID, counter, attribute4) == attributeStatus4) )
      {
        returnVector.add(0, new Long(counter));
        total--;
      }
      counter--;
    }
    return returnVector;
  }

  private void areaRemoveMessage( String areaID, long index ) throws JEdPointException
  {
    try
    {
      RandomAccessFile source, destination;
      long messageStarts, messageEnds;
      long fileSize;
      long counter;
      byte buffer[] = new byte[bufferSize];

      areaOpen(areaID);

      fileSize = areaGetFileSize(areaID);

      messageStarts = areaSeekMessage(areaID, index);

      if (index == statGet(areaID, statTotalMessages))
        messageEnds = fileSize;
      else
        messageEnds = areaSeekMessage(areaID, index+1);

      areaClose(areaID);

      source = new RandomAccessFile( this.getCorrectDirectory(areaID) + File.separator + areaID + fileExtensions[extData], "r");
      destination = new RandomAccessFile( this.getCorrectDirectory(areaID) + File.separator + areaID + ".temp", "rw");

      source.seek(0);
      destination.seek(0);

      for (counter=0; counter<messageStarts/bufferSize; counter++)
      {
        source.read(buffer, 0, buffer.length);
        destination.write(buffer, 0, buffer.length);
      }
      source.read(buffer, 0, (int)messageStarts%bufferSize);
      destination.write(buffer, 0, (int)messageStarts%bufferSize);

      for (counter=messageEnds/bufferSize; counter<(fileSize-messageEnds)/bufferSize; counter++)
      {
        source.read(buffer, 0, buffer.length);
        destination.write(buffer, 0, buffer.length);
      }
      source.read(buffer, 0, (int)(fileSize-messageEnds)%bufferSize);
      destination.write(buffer, 0, (int)(fileSize-messageEnds)%bufferSize);

      source.close();
      destination.close();
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal,
        "Could not delete message #" + index + " from area " + areaID,
        "No read permission? Disk full?",
        "Check read permissions and disk space.");
    }
  }

  /**
   * Opens a msgbase (data + attributes) for editing / seeking / whatever.
   * <br><br>
   * Creates a file if one does not exist.
   */
  private void areaOpen( String areaID ) throws JEdPointException
  {
    String tempString = "";

    // Check to see if this area isn't already open
    if (openArea.compareToIgnoreCase(areaID) == 0)
      // If it is, ignore the request
      return;

    try
    {
        tempString = this.getCorrectDirectory(openArea) + File.separator + openArea + fileExtensions[extData];
        if (fileData != null) fileData.close();
        tempString = this.getCorrectDirectory(openArea) + File.separator + openArea + fileExtensions[extAttr];
        if (fileAttr != null) fileAttr.close();
    }
    catch (IOException ioe)
    {
      throw new JEdPointException(ioe, JEdPointException.severityFatal,
        "Could not close the file: " + tempString,
        "Unknown.",
        "This shouldn't happen.");
    }

    openArea = areaID;

    try
    {
      tempString = this.getCorrectDirectory(openArea) + File.separator + openArea + fileExtensions[extData];
      fileData = new RandomAccessFile( tempString, "rw" );
      tempString = this.getCorrectDirectory(openArea) + File.separator + openArea + fileExtensions[extAttr];
      fileAttr = new RandomAccessFile( tempString, "rw" );
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal,
        "Could not open the file: " + tempString,
        "Sharing violation? No write permission?",
        "Check write permissions.");
    }

    // We set these to -2billion because we've just opened a new area and haven't
    // searched for anything.
    lastMessage = Long.MIN_VALUE;
    lastMessagePosition = Long.MIN_VALUE;
    lastHeaderSize = Integer.MIN_VALUE;
    lastBodySize = Integer.MIN_VALUE;
    lastAttribute = Long.MIN_VALUE;
    lastAttributeRead = Integer.MIN_VALUE;
  }

  /**
   * Closes an area.
   *
   * If the area isn't open (there can only be one area open at a time, the request
   * will be ignored.
   */
  private void areaClose( String areaID ) throws JEdPointException
  {
    String tempString = "";

    if (openArea.compareToIgnoreCase(areaID) != 0)
      return;

    try
    {
      tempString = this.getCorrectDirectory(openArea) + File.separator + openArea + fileExtensions[extData];
      if (fileData != null) fileData.close();
      tempString = this.getCorrectDirectory(openArea) + File.separator + openArea + fileExtensions[extAttr];
      if (fileAttr != null) fileAttr.close();
    }
    catch (IOException ioe)
    {
      throw new JEdPointException(ioe, JEdPointException.severityFatal,
        "Could not close the file: " + tempString,
        "Unknown.",
        "This shouldn't happen.");
    }

    openArea = "";
    lastMADArea = "";
  }

  /**
   * Deletes an area (data + attr) from disk.
   */
  private void areaDelete(String areaID) throws JEdPointException
  {
    String tempString;

    if (this.openArea.compareToIgnoreCase(areaID) == 0)
      this.areaClose(areaID);

    tempString = this.getCorrectDirectory(areaID) + File.separator + areaID + fileExtensions[extData];
    deleteFile(tempString);
    tempString = this.getCorrectDirectory(areaID) + File.separator + areaID + fileExtensions[extAttr];
    deleteFile(tempString);
  }

  /**
   * Returns a position in the specified area where we will find the message.
   */
  private long areaSeekMessage( String areaID, long msg ) throws JEdPointException
  {
    String directory = this.getCorrectDirectory(areaID);
    long fileposition = lastMessagePosition;
    long minSeek = 0;
    long maxSeek = this.areaGetFileSize( areaID );
    long minMsg = 0;
    long maxMsg = this.areaGetCount(areaID );
    long msgFound = lastMessage;
    long filePointer;
    long filesize = maxSeek;
    boolean seekForwards;
    int counter;
    byte buffer[] = new byte[bufferSize];
    boolean close;
    boolean found;

    int sequentialSearches = 0;
    int randomSearches = 0;

    int bufferValue = 0;    // How much of the buffer is actually valid?

    try
    {
      areaOpen(areaID);

      while (msgFound != msg)
      {
        seekForwards = (msgFound <= msg);

        close = ((msgFound - msg > -closeMsg) & (msgFound - msg < closeMsg));
        if ( close )
        {
          if (seekForwards)
          {
            // Sequential seek forwards
            //               ID  #   H   B   what it says     what it says  rID  #   H   B
            fileposition += (4 + 8 + 4 + 4 + lastHeaderSize + lastBodySize + 4 + 8 + 4 + 4);
            fileData.seek(fileposition + 4);  // We'll skip the next msg's ID
            msgFound = fileData.readLong();
            lastMessage = msgFound;
            lastMessagePosition = fileposition;
            lastHeaderSize = fileData.readInt();
            lastBodySize = fileData.readInt();
          }
          else
          {
            // Seek backwards
            // Seek backwards into the end separator.
            fileposition -= (8 + 4 + 4);
            fileData.seek(fileposition);
            msgFound = fileData.readLong();
            lastHeaderSize = fileData.readInt();
            lastBodySize = fileData.readInt();
            fileposition -= (4 + 8 + 4 + 4 + lastHeaderSize + lastBodySize + 4 );
            lastMessage = msgFound;
            lastMessagePosition = fileposition;
          }
          sequentialSearches++;
        }
        else
        {
          // We'll need to do some interpolating.
          fileposition = calculateSeek(minMsg, minSeek, maxMsg, maxSeek, msg);

          randomSearches++;

          found = false;

          while ( fileposition < fileData.length() )
          {
            fileData.seek(fileposition);
            bufferValue = (int)Math.min( (long)bufferSize, filesize - fileposition );
            fileData.read(buffer, 0, bufferValue);
            for (counter=0; counter<bufferValue-4; counter++)
            {
                if (buffer[counter+0] == headerIdentifier[0])
                  if (buffer[counter+1] == headerIdentifier[1])
                    if (buffer[counter+2] == headerIdentifier[2])
                      if (buffer[counter+3] == headerIdentifier[3])
                      {

                        // We've found a message header at this position!
                        filePointer = fileposition + ( bufferValue - (bufferValue-counter) );
                        fileData.seek( filePointer + 4 );
                        // Read which message it is
                        msgFound = fileData.readLong();

                        // Read the sizes
                        lastHeaderSize = fileData.readInt();
                        lastBodySize = fileData.readInt();

                        // If this is the message we're looking for...
                        if (msgFound == msg)  // Messages start at one
                        {
                          lastMessage = msgFound;
                          lastMessagePosition = filePointer;
                          found = true;
                          fileposition = filePointer;
                          break;
                        }
                        else
                        {
                          // Update our values and then seek again...
                          if (msgFound > msg)
                          {
                            maxMsg = msgFound;
                            maxSeek = filePointer;
                          }
                          else
                          {
                            minMsg = msgFound;
                            minSeek = filePointer;
                          }
                          found = true;
                          fileposition = filePointer;
                          break;
                        }
                      }
            } // FOR
            if (found) break;
            fileposition += bufferValue;
          } // WHILE
          if ( fileposition >= fileData.length() )
          {
            // We've reached the end of the file. So let's go ahead and assume
            // that there's some separator information here.
            fileposition -= (8 + 4 + 4);
            fileData.seek(fileposition);
            msgFound = fileData.readLong();
            lastHeaderSize = fileData.readInt();
            lastBodySize = fileData.readInt();
            fileposition -= (4 + 8 + 4 + 4 + lastHeaderSize + lastBodySize + 4 );
            lastMessage = msgFound;
            lastMessagePosition = fileposition;
          }
        }
      }
    }
    catch (Exception e)
    {
      throw new JEdPointException(
        e,
        JEdPointException.severityError,
        "Error trying to seek to message #" + msg + " in area " + areaID,
        "See stack trace.",
        "None" );
    }

    // We're not interesting in logging those single seq searches (searching one
    // message forwards or backwards. We're only interested in those really big searches.
    if (sequentialSearches > 1)
      sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logDebug, "message",
        "Messagebase: Searching to message #" + msg + " in area " + areaID + " took " +
          randomSearches + " random searches and "  + sequentialSearches + " sequential searches.");

    return fileposition;
  }

  /**
   * Returns the file size of the area.
   */
  private long areaGetFileSize( String areaID ) throws JEdPointException
  {
    areaOpen(areaID);
    try
    {
      return fileData.length();
    }
    catch (IOException ioe)
    {
      throw new JEdPointException(ioe, JEdPointException.severityFatal,
        "Could not read the file size for the data file of area " + areaID,
        "Read error.",
        "Check that the file can be read (and that it exists...)");
    }
  }

  /**
   * Returns the message count in this area.
   */
  private long areaGetCount( String areaID )
  {
    String directory;
    byte buffer[] = new byte[this.bufferSize];
    int counter;

    long fileposition;

    long returnvalue = 0;
    int bufferToSearch = buffer.length;

    directory = getCorrectDirectory(areaID);

    try
    {
      areaOpen(areaID);
      fileposition = fileData.length();

      while (fileposition>0)
      {
        fileposition = Math.max( 0, fileposition - bufferToSearch );
        fileData.seek( fileposition );
        bufferToSearch = (int)Math.min( buffer.length, fileData.length() );
        fileData.read( buffer, 0, bufferToSearch );

        // Search for the reverse headerIdentifier
        for (counter=bufferToSearch-4; counter>-1; counter--)
        {
          if (buffer[counter+0] == reverseIdentifier[0])
            if (buffer[counter+1] == reverseIdentifier[1])
              if (buffer[counter+2] == reverseIdentifier[2])
                if (buffer[counter+3] == reverseIdentifier[3])
                {
                  // Read the areaGetCount
                  fileData.seek(fileposition + ( bufferToSearch - (bufferToSearch-counter) ) + 4 );
                  returnvalue = fileData.readLong();
                  return returnvalue;
                }
        }
      }
    }
    catch (Exception e)
    {
    }

    return returnvalue;
  }

  private void areaAddMessage( String areaID, FidonetMessage fm,
    boolean newlyImported,
    boolean read,
    boolean export ) throws JEdPointException
  {
    long msg;

    boolean personal = (fm.getMessageDataString("usernameto").compareToIgnoreCase(this.settingCurrentUser) == 0);

    // Check that the area exists.
    // If it doesn't, create one.
    if (MADFind(areaID) == -1)
      createArea(areaID);

    areaAddMessage(areaID, fm);

    msg = statGet(areaID, statTotalMessages) + 1;

    setAttribute(areaID, msg, attrNewlyImported, newlyImported);
    setAttribute(areaID, msg, attrPersonal, personal);
    setAttribute(areaID, msg, attrRead, read);
    setAttribute(areaID, msg, attrExport, export);

    // delete will always be false
    statAdding(areaID, export, personal, newlyImported, read, false);
  }

  /**
   * Add a message at the end of the msgbase.
   */
  private void areaAddMessage( String areaID, FidonetMessage fm )
  {
    String directory = this.getCorrectDirectory(areaID);
    int counter;
    long length;

    byte headerBuffer[];
    byte bodyBuffer[];
    File tempFile;

    FidonetMessage header = new FidonetMessage();
    FidonetMessage body = new FidonetMessage();

    // Move all of the header data from fm -> header
    for (counter=0; counter<fm.fieldsHeader[fm.version].length; counter++)
      header.setMessageData( fm.fieldsHeader[fm.version][counter], fm.getMessageData(fm.fieldsHeader[fm.version][counter]) );

    // Move all of the body data from fm -> body
    for (counter=0; counter<fm.fieldsBody[fm.version].length; counter++)
      body.setMessageData( fm.fieldsBody[fm.version][counter], fm.getMessageData(fm.fieldsBody[fm.version][counter]) );

    try
    {
      length = areaGetCount(areaID);

      headerBuffer = JEdPointUtilities.objectToByteArray(header);
      if (settingCompressHeader) headerBuffer = JEdPointUtilities.compressByteArray(headerBuffer, true, settingHeaderCompressionLevel);

      bodyBuffer = JEdPointUtilities.objectToByteArray(body);
      if (settingCompressBody) bodyBuffer = JEdPointUtilities.compressByteArray(bodyBuffer, true, settingBodyCompressionLevel);

      areaOpen(areaID);

      fileData.seek(fileData.length());

      // Write the ID at the beginning
      fileData.write(headerIdentifier);       // 4 bytes
      fileData.writeLong(length+1);           // 8 bytes
      fileData.writeInt(headerBuffer.length); // 4 bytes
      fileData.writeInt(bodyBuffer.length);   // 4 bytes  = 20 bytes total.

      // Write the header and body
      fileData.write(headerBuffer);
      fileData.write(bodyBuffer);

      // Write the ID at the end
      fileData.write(reverseIdentifier);
      fileData.writeLong(length+1);
      fileData.writeInt(headerBuffer.length);
      fileData.writeInt(bodyBuffer.length);

      // Add an attribute space to the end of the attribute file
      fileAttr.seek( fileAttr.length() );
      fileAttr.writeByte(0);

      // Close it (flush it to disk and everything)
      areaClose(areaID);
    }
    catch (Exception e)
    {
    }
  }

  /**
   * Creates an empty msgbase file.
   *
   * Creates a .data file and an .attributes file and add the MAD data to the MAD
   *
   * This method creates a small MessageAreaData which is then sends off to the
   * other createArea, which is the one which does all the work, really.
   */
  private void createArea( String areaID ) throws JEdPointException
  {
    MessageAreaData tempMAD = new MessageAreaData();
    tempMAD.setMessageAreaData("id", areaID.toUpperCase());
    tempMAD.setMessageAreaData("origin", settingDefaultAreaOrigin);
    tempMAD.setMessageAreaData("tearline", settingDefaultAreaTearline);
    createArea(tempMAD);
  }

  /**
   * Creates an empty msgbase file.
   *
   * Creates a .data file and an .attributes file and add the MAD data to the MAD
   */
  private void createArea( MessageAreaData MADToUse ) throws JEdPointException
  {
    MADToUse.setMessageAreaData("id", MADToUse.getMessageAreaDataString("id").toUpperCase() );
    areaOpen( MADToUse.getMessageAreaDataString("id") );

    //gatherStatistics( MADToUse.getMessageAreaDataString("id") , MADToUse );

    MADAdd( MADToUse );
  }

  /**
   * Packs an area by completely reading and writing it.
   */
  private void packAreaRepair( String areaID ) throws JEdPointException
  {
    MessageAreaData tempMAD;
    String tempArea = areaID + ".tmp";
    String dataFilename = getCorrectDirectory(areaID) + File.separator + areaID + fileExtensions[extData];
    String attrFilename = getCorrectDirectory(areaID) + File.separator + areaID + fileExtensions[extAttr];
    RandomAccessFile rafData;
    RandomAccessFile rafAttr;
    FidonetMessage fm;
    byte buffer[] = new byte[bufferSize];
    int counter;
    int messagecounter = 0;
    int bufferValid;    // How much of the buffer is valid?
    int maxValid;       // How much can we read out of the file?
    long filePosition = 0;
    long tempFilePosition;    // Used when searching for data after the header identifier
    long dataFileSize;

    int headerSize;
    int bodySize;
    long messageNumber;
    byte header[];
    byte body[];

    boolean newlyImported, read, export;
    int attribute;

    try
    {
      // Close the area if it open, else we won't be able to open the files.
      areaClose( areaID );

      // Open the input files
      // Open the data file
      try
      {
        rafData = new RandomAccessFile( dataFilename, "r" );
      }
      catch (FileNotFoundException fnfe)
      {
        throw new JEdPointException( fnfe, JEdPointException.severityError, "File not found opening the file " + dataFilename,
          "File could not be found.",
          "Delete the area completely?");
      }
      // Open the attribute file
      try
      {
        rafAttr = new RandomAccessFile( attrFilename, "r" );
      }
      catch (FileNotFoundException fnfe)
      {
        throw new JEdPointException( fnfe, JEdPointException.severityError, "File not found opening the file " + attrFilename,
          "File could not be found.",
          "Delete the area completely?");
      }

      // Both the data and attribute files are now open and ready for action.
      // Let's get it on!

      // Create and open the temparea
      createArea(tempArea);

      // Copy the RW attributes of areaID to the tempArea
      copyRWMAD( areaID, tempArea );

      areaOpen(tempArea);

      dataFileSize = rafData.length();
      while (filePosition < dataFileSize-4)
      {
        rafData.seek( filePosition );
        // Read as much of the file into the buffer
        maxValid = (int)Math.min((long)bufferSize, rafData.length()-filePosition);
        bufferValid = rafData.read(buffer, 0, maxValid);

        // Go through the buffer, looking for the header identifier
        for (counter=0; counter<bufferValid-4; counter++)
        {
          if (
            (buffer[counter+0] == headerIdentifier[0]) &
            (buffer[counter+1] == headerIdentifier[1]) &
            (buffer[counter+2] == headerIdentifier[2]) &
            (buffer[counter+3] == headerIdentifier[3]) )
          {
            // This try is here because if we can't do everything right while reading
            // a message, just skip it and go the next one...
            try
            {
              // We've found our identifier match, read the data.
              tempFilePosition = filePosition + counter + 4;
              rafData.seek( tempFilePosition );

              // Read whatever is in the header...
              messageNumber = rafData.readLong();
              headerSize = rafData.readInt();
              bodySize = rafData.readInt();

              header = new byte[headerSize];
              rafData.read(header, 0, headerSize);
              header = JEdPointUtilities.compressByteArray(header, false, 0);
              body = new byte[bodySize];
              rafData.read(body, 0, bodySize);
              body = JEdPointUtilities.compressByteArray(body, false, 0);

              // Put the body and the header into one FidonetMessage
              fm = assembleHeaderAndBody( (FidonetMessage)JEdPointUtilities.byteArrayToObject(header), (FidonetMessage)JEdPointUtilities.byteArrayToObject(body) );

              // Try to extract whatever attributes possible
              try
              {
                rafAttr.seek( attributeCount*(messageNumber-1) );
                attribute = (int)rafAttr.readByte();
              }
              catch (Exception e)
              {
                attribute = attributedefault;
              }
              newlyImported = ((attribute & attrNewlyImported) == attrNewlyImported);
              read = ((attribute & attrRead) == attrRead);
              export = ((attribute & attrExport) == attrExport);

              // If this message isn't marked for deletion, copy it over
              if ( ((attribute & attrDelete) != attrDelete) )
                areaAddMessage(tempArea, fm, newlyImported, read, export);
            }
            catch (Exception e)
            {
            }
          } // if headeridentifier
        } // for (counter=0; counter<bufferValid-4; counter++)
        filePosition += bufferValid-4;
      } // while

      // Close all our files
      rafData.close();
      rafAttr.close();

      copyLastRead( areaID, tempArea );
      tempMAD = new MessageAreaData( (MessageAreaData)MAD.elementAt(MADFind(tempArea)) );
      removeArea(areaID);
      tempMAD.setMessageAreaData("id", areaID);
      changeArea(tempArea, tempMAD);
    }
    catch (JEdPointException jpe)
    {
      throw jpe;
    }
    catch (Exception e)
    {
      throw new JEdPointException( e, JEdPointException.severityError, "Could not pack the area.",
        "Could not read the data or attribute files for the area " + areaID,
        "Check that the data/attribute files for the area have read permission.");
    }
  }

 /**
   * Packs an area by completely reading and writing it.
   * Is not capable of repairing faulty messagebases.
   */
  private void packAreaQuick( String areaID ) throws JEdPointException
  {
    long counter;
    long max;
    String tempArea = areaID + ".tmp";
    FidonetMessage fm;
    boolean newlyImported, read, export;
    MessageAreaData tempMAD;

    max = statGet(areaID, statTotalMessages);
    createArea(tempArea);

    // Copy the RW attributes of areaID to the tempArea
    copyRWMAD( areaID, tempArea );

    areaOpen(tempArea);

    for (counter=1; counter<=max; counter++)
    {
      if (getAttribute(areaID, counter, attrDelete) == false)
      {
        newlyImported = getAttribute(areaID, counter, attrNewlyImported);
        read = getAttribute(areaID, counter, attrRead);
        export = getAttribute(areaID, counter, attrExport);

        fm = assembleHeaderAndBody( areaGetHeader( areaID, counter ), areaGetBody( areaID, counter ) );
        areaAddMessage(tempArea, fm, newlyImported, read, export);
      }
    }

    copyLastRead( areaID, tempArea );
    tempMAD = new MessageAreaData( (MessageAreaData)MAD.elementAt(MADFind(tempArea)) );
    removeArea(areaID);
    tempMAD.setMessageAreaData("id", areaID);
    changeArea(tempArea, tempMAD);
  }

  /**
   * Deletes an area and removes it from the MAD
   */
  private void removeArea( String areaID ) throws JEdPointException
  {
    if (this.MADFind(areaID) != -1)
    {
      areaClose(areaID);
      areaDelete(areaID);
      MADRemove(areaID);
    }
  }

  private void copyRWMAD( String oldID, String newID )
  {
    int counter;
    MessageAreaData oldMAD, newMAD;

    oldMAD = (MessageAreaData)MAD.elementAt( MADFind(oldID) );
    newMAD = (MessageAreaData)MAD.elementAt( MADFind(newID) );

    for (counter=0; counter<oldMAD.fieldsrw[oldMAD.version].length; counter++)
      newMAD.setMessageAreaData( oldMAD.fieldsrw[oldMAD.version][counter], oldMAD.getMessageAreaData(oldMAD.fieldsrw[oldMAD.version][counter]) );

    newMAD.setMessageAreaData("id", newID);
  }

  /**
   * Copies the "lastread" attribute from the old area to the new one.
   *
   * Also: it checks that this value is not illegal.
   */
  private void copyLastRead( String oldID, String newID )
  {
    int counter;
    MessageAreaData oldMAD, newMAD;
    long lastread;

    oldMAD = (MessageAreaData)MAD.elementAt( MADFind(oldID) );
    newMAD = (MessageAreaData)MAD.elementAt( MADFind(newID) );

    lastread = oldMAD.getMessageAreaDataLong("lastread");

    if (newMAD.getMessageAreaDataLong("totalmessages") >= lastread)
      // Save the last read ID
      newMAD.setMessageAreaData( "lastread", lastread );
  }

  // Retrieves a FidonetMessage header
  private FidonetMessage areaGetHeader( String areaID, long msg ) throws JEdPointException
  {
    byte buffer[];
    int headerSize;
    FidonetMessage fm;
    FidonetMessage returnFM = new FidonetMessage();
    int counter;
    RandomAccessFile raf;

    try
    {
      areaOpen(areaID);

      fileData.seek( areaSeekMessage(areaID, msg) + 4 + 8 + 4 + 4);

      headerSize = lastHeaderSize;
      buffer = new byte[headerSize];

      fileData.read(buffer, 0, headerSize);

      // Uncompress the data
      buffer = JEdPointUtilities.compressByteArray(buffer, false, 0);

      fm = (FidonetMessage)JEdPointUtilities.byteArrayToObject(buffer);

      if (fm == null)
      {
        throw new JEdPointException( new Exception("fm = null"), JEdPointException.severityError,
          "Could convert the read data from message #" + msg + " in area " + areaID + " to a FidonetMessage header.",
          "Data is corrupt",
          "Repack the data file.");
      }

      // Here is where our handling of the attributes comes into play.
      // It was specced in the FidonetMessage.java that these attributes are to be
      // handled exclusively by the msgbase, and that's what we're doing.
      fm.setMessageData("attributenewlyimported", getAttribute(areaID, msg, attrNewlyImported) );
      fm.setMessageData("attributesent", getAttribute(areaID, msg, attrExport) );
      fm.setMessageData("attributeread", getAttribute(areaID, msg, attrRead) );
      fm.setMessageData("attributepersonal", getAttribute(areaID, msg, attrPersonal) );
      fm.setMessageData("attributedelete", getAttribute(areaID, msg, attrDelete) );

      // Copy all of the fields in fm to returnFM;
      for (counter=0; counter<fm.fieldsHeader[ fm.version ].length; counter++)
        returnFM.setMessageData( fm.fieldsHeader[fm.version][counter], fm.getMessageData(fm.fieldsHeader[fm.version][counter]) );
    }
    catch (JEdPointException jpe)
    {
      throw jpe;
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal, "Could not read the header for message " + msg + " in area " + areaID,
        "See exception data.",
        "See exception data." );
    }
    return returnFM;
  }

  // Retrieves a FidonetMessage body
  private FidonetMessage areaGetBody( String areaID, long msg ) throws JEdPointException
  {
    byte buffer[];
    int headerSize;
    int bodySize;
    int counter;
    FidonetMessage fm;
    FidonetMessage returnFM = new FidonetMessage();
    RandomAccessFile raf;

    try
    {
      areaOpen(areaID);

      fileData.seek( areaSeekMessage(areaID, msg) );

      fileData.readByte();
      fileData.readByte();
      fileData.readByte();
      fileData.readByte();
      fileData.readLong();
      headerSize = fileData.readInt();
      bodySize = fileData.readInt();
      buffer = new byte[bodySize];

      fileData.seek( fileData.getFilePointer() + headerSize);

      fileData.read(buffer, 0, bodySize);

      // Uncompress the buffer
      buffer = JEdPointUtilities.compressByteArray(buffer, false, 0);

      fm = (FidonetMessage)JEdPointUtilities.byteArrayToObject(buffer);

      if (fm == null)
      {
        throw new JEdPointException( new Exception("fm = null"), JEdPointException.severityError,
          "Could convert the read data from message #" + msg + " in area " + areaID + " to a FidonetMessage body.",
          "Data is corrupt",
          "Repack the data file.");
      }

      // Copy all of the body fields in fm to returnFM;
      for (counter=0; counter<fm.fieldsBody[ fm.version ].length; counter++)
        returnFM.setMessageData( fm.fieldsBody[fm.version][counter], fm.getMessageData(fm.fieldsBody[fm.version][counter]) );
    }
    catch (JEdPointException jpe)
    {
      throw jpe;
    }
    catch (Exception e)
    {
    }
    return returnFM;
  }

  /**
   * Calculates where in a file one should seek for a given msgToSeek.
   */
  private long calculateSeek( long minMsg, long minSeek, long maxMsg, long maxSeek, long msgToSeek )
  {
    long returnValue;

    msgToSeek--;

    double msgRelative = (double)((msgToSeek-minMsg)) / (double)((maxMsg-minMsg));

    returnValue = maxSeek - minSeek;

    // * because it will most probably be 0.xxx
    returnValue = (long)((double)returnValue * (double)msgRelative);

    returnValue += minSeek;

    return returnValue;
  }

  // ---------------------------------------------------------------------------
  // MAD
  // ---------------------------------------------------------------------------

  /**
   * Add a MessageAreaData to the MAD.
   */
  private void MADAdd( MessageAreaData mad ) throws JEdPointException
  {
    if (MADFind(mad.getMessageAreaDataString("id")) == -1)
    {
      MAD.add(mad);
      MADAutoSaver.MADSave();
      MADSort();
    }
  }

  /**
   * Add a MessageAreaData to the MAD.
   */
  private void MADAdd( String areaID ) throws JEdPointException
  {
    MessageAreaData mad;
    if (MADFind(areaID) == -1)
    {
      mad = new MessageAreaData();
      mad.setMessageAreaData("id", areaID);
      MADAdd(mad);
    }
  }

  /**
   * Sorts the areas in the vector.
   *
   * Currently it uses a bubble sort. Couldn't really care about how slow it is
   * at the present moment.
   */
  private void MADSort()
  {
    MessageAreaData sortedMAD[] = new MessageAreaData[ MAD.size() ];
    MessageAreaData tempMAD;
    int bigcounter;
    int counter;
    boolean changed;

    for (counter=0; counter<MAD.size(); counter++)
    {
      sortedMAD[counter] = (MessageAreaData)MAD.elementAt(counter);
    }

    changed = true;
    while (changed)
    {
      changed = false;
      for (counter=0; counter<sortedMAD.length-1; counter++)
      {
        if ( sortedMAD[counter].getMessageAreaDataString("id").compareToIgnoreCase( sortedMAD[counter+1].getMessageAreaDataString("id") ) > 0)
        {
          tempMAD = sortedMAD[counter];
          sortedMAD[counter] = sortedMAD[counter+1];
          sortedMAD[counter+1] = tempMAD;
          changed = true;
          break;
        }
      } // FOR
    } // WHILE

    MAD.clear();
    for (counter=0; counter<sortedMAD.length; counter++)
      MAD.add(sortedMAD[counter]);

    MADAutoSaver.MADSave();
  }

  /**
   * Finds where in the MAD the areaID is
   */
  private int MADFind( String areaID )
  {
    int counter;
    MessageAreaData tempMAD;

    for (counter=0; counter<MAD.size(); counter++)
    {
      tempMAD = (MessageAreaData)MAD.elementAt(counter);
      if (tempMAD.getMessageAreaDataString("id").compareToIgnoreCase(areaID) == 0)
      {
        // We've found the element
        return counter;
      }
    }
    return -1;
  }

  /**
   * Removes a MessageAreaData from the MAD.
   */
  private void MADRemove( MessageAreaData mad ) throws JEdPointException
  {
    MADRemove( mad.getMessageAreaDataString("id") );
  }

  /**
   * Removes a MessageAreaData from the MAD.
   */
  private void MADRemove( String areaID ) throws JEdPointException
  {
    int index;

    index = MADFind(areaID);
    if ( index != -1 )
      MAD.removeElementAt( index );
    MADAutoSaver.MADSave();
    MADSort();
  }

  /**
   * Saves the MAD to disk.
   */
  private void MADSave() throws JEdPointException
  {
    if (!JEdPointUtilities.saveObject( MADFile, MAD ))
    {
      throw new JEdPointException( new Exception("Could not save message base MessageAreaData file."), JEdPointException.severityFatal,
        "The file: " + MADFile + " could not be written.",
        "No write permission in that direcory? Disk full?",
        "Check to see that we have write permission in that directory" );
    }
  }

  // ---------------------------------------------------------------------------
  // ATTRIBUTES
  // ---------------------------------------------------------------------------
  /**
   * Retrieves an attribute from the attribute file for a specific message.
   */
  private boolean getAttribute( String areaID, long msg, int attributeToGet ) throws JEdPointException
  {
    msg--;
    try
    {
      this.areaOpen(areaID);
      if ( this.lastAttribute != msg )
      {
        fileAttr.seek(msg * this.attributeCount);
        this.lastAttributeRead = (int) fileAttr.readByte();
        this.lastAttribute = msg;
      }
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal,
        "Could not read the attribute for message #" + msg + " in area " + areaID,
        "No read permission? Broken file?",
        "Check for read permission.");
    }

    return ((lastAttributeRead & attributeToGet) == attributeToGet);
  }

  /**
   * Sets an attribute from the attribute file for a specific message.
   */
  private void setAttribute( String areaID, long msg, int attributeToSet, boolean state ) throws JEdPointException
  {
    msg--;
    try
    {
      areaOpen(areaID);
      if ( this.lastAttribute != msg )
      {
        fileAttr.seek(msg * this.attributeCount);
        lastAttributeRead = (int) fileAttr.readByte();
        lastAttribute = msg;
      }

      if (state)
      {
        // Set it to true
        if ((lastAttributeRead & attributeToSet) != attributeToSet)
          lastAttributeRead = lastAttributeRead + attributeToSet;
      }
      else
      {
        // Set it to false
        if ((lastAttributeRead & attributeToSet) == attributeToSet)
          lastAttributeRead = lastAttributeRead - attributeToSet;
      }

      fileAttr.seek(msg * this.attributeCount);
      fileAttr.writeByte(lastAttributeRead);
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal,
        "Could not read the attribute for message #" + msg + " in area " + areaID,
        "No read permission? Broken file?",
        "Check for read permission.");
    }
  }

  private void setMessageAttributes( String areaID, long index,
    Boolean export,
    Boolean personal,
    Boolean newlyImported,
    Boolean read,
    Boolean delete )
  {
    boolean setExport, setPersonal, setNewlyImported, setRead, setDelete;

    try
    {
      statSubtracting(areaID,
        getAttribute(areaID, index, attrExport),
        getAttribute(areaID, index, attrPersonal),
        getAttribute(areaID, index, attrNewlyImported),
        getAttribute(areaID, index, attrRead),
        getAttribute(areaID, index, attrDelete)
      );

      if (export==null)
        setExport = getAttribute(areaID, index, attrExport);
      else
        setExport = export.booleanValue();

      if (personal==null)
        setPersonal = getAttribute(areaID, index, attrPersonal);
      else
        setPersonal = personal.booleanValue();

      if (newlyImported==null)
        setNewlyImported = getAttribute(areaID, index, attrNewlyImported);
      else
        setNewlyImported = newlyImported.booleanValue();

      if (read==null)
        setRead = getAttribute(areaID, index, attrRead);
      else
        setRead = read.booleanValue();

      if (delete==null)
        setDelete = getAttribute(areaID, index, attrDelete);
      else
        setDelete = delete.booleanValue();

      if (export != null)
        setAttribute(areaID, index, attrExport, export.booleanValue());
      if (personal != null)
        setAttribute(areaID, index, attrPersonal, personal.booleanValue());
      if (newlyImported != null)
        setAttribute(areaID, index, attrNewlyImported, newlyImported.booleanValue());
      if (read != null)
        setAttribute(areaID, index, attrRead, read.booleanValue());
      if (delete != null)
        setAttribute(areaID, index, attrDelete, delete.booleanValue());

      statAdding(areaID, setExport, setPersonal, setNewlyImported, setRead, setDelete);
    }
    catch (JEdPointException jpe)
    {
    }
  }

  /**
   * Creates an empty file.
   */
  private void createEmptyFile( String filename ) throws JEdPointException
  {
    try
    {
      File file = new File( filename );
      file.createNewFile();
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityFatal,
        "Could not create the file " + filename,
        "No write permissions?",
        "Check write permissions." );
    }
  }

  /**
   * Deletes a file
   */
  private void deleteFile( String filename ) throws JEdPointException
  {
    try
    {
      File file = new File(filename);
        if (!file.delete()) throw new Exception("File.delete() failed!");
    }
    catch (Exception e)
    {
      throw new JEdPointException(e, JEdPointException.severityError,
        "Could not delete the file " + filename,
        "No write permissions?",
        "Check write permissions." );
    }
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

  /**
   * Checks whether a file exists.
   */
  private boolean fileExists(String fileToCheck)
  {
    File tempFile = new File(fileToCheck);

    return tempFile.exists();
  }

  // AUTOSAVE CLASS
  private class MADAutoSave implements Runnable
  {
    private final int secondsToSaveAt = 2;
    private int secondsSinceLastRequest;

    public void MADSave()
    {
      secondsSinceLastRequest = 0;
    }

    public void run()
    {
      secondsSinceLastRequest = 0;
      while (true)
      {
        try
        {
          Thread.sleep(1000);
        }
        catch (Exception e)
        {
          // What do we do if we can't sleep?
          // God only knows...
        }
        secondsSinceLastRequest ++;

        if (secondsSinceLastRequest == secondsToSaveAt )
        {
          if (!JEdPointUtilities.saveObject( MADFile, MAD ))
          {
            // What do we do if we can't save the MAD?
          }
        } // if (secondsSinceLastRequest == secondsToSaveAt )

      } // while (true)
    }
  } // class

  // A very useful method for profiling actions
  private GregorianCalendar profileGC = new GregorianCalendar();
  private void profile(String actionPerformed)
  {
    long oldValue = profileGC.getTime().getTime();
    long newValue = new GregorianCalendar().getTime().getTime();

    while (actionPerformed.length() != 30)
      actionPerformed = " " + actionPerformed;

    if (newValue < oldValue)
      newValue += 1000;

    actionPerformed = actionPerformed + " took " + (newValue-oldValue) + "ms";

    System.out.println( actionPerformed );

    profileGC = new GregorianCalendar();
  }

}
