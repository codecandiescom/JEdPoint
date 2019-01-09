package JEdPoint;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.jar.*;   // For JarClassLoader

/**
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */
public class JEdPointMicrokernel implements Runnable
{
  // ---------------------------------------------------------------------------
  // MODULE DESCRIPTION FINALS
  // ---------------------------------------------------------------------------
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.moduleMicrokernel );
  private static final String infoAuthor            = "Edward Hevlund";
  private static final String infoModuleName        = "JEdPoint";
  private static final String infoModuleNameVersion = "JEdPoint v" + infoVersionHigh + "." + infoVersionLow;
  private static final String infoShortDescription  = "The microkernel module which controls all other modules.";
  private static final String infoLongDescription   = "The microkernel is the base of JEdPoint. It controls loading of modules and routes the messages that are so important to the microkernel architecture.";

  // ---------------------------------------------------------------------------
  // THE SETTINGS
  // ---------------------------------------------------------------------------
  private String settingsUserName;
  private PointNumber settingsPointNumber;
  private String settingsAreafixName;
  private PointNumber settingsAreafixPointNumber;
  private String settingsAreafixPassword;
  private String settingsCodepage;
  private String settingsSettingsDir = "";
  private String settingsDataDir = "";
  private String settingsImportDir = "";
  private String settingsExportDir = "";
  private String settingsNetmailDir = "";
  private String settingsPacketPassword = "";
  private String settingsDefaultAreaTearline = "";
  private String settingsDefaultAreaOrigin = "";

  // This where the loaded modules are stored
  private Vector modules = new Vector(JEdPointModule.moduleTypes);
  private Vector threads = new Vector(JEdPointModule.moduleTypes);

  // Our locally declared classes
  private DynamicClassLoader dcl = new DynamicClassLoader();
  private MessageFetcher MsgFetcher = new MessageFetcher(this);
  private Thread MsgFetcherThread = new Thread(MsgFetcher);

  // FidonetMessageUtils
  private FidonetMessageUtils FMUtils = new FidonetMessageUtils();
  // This ensures that there always exists at least one instance of FMU and, coincidentally,
  // msgIDLastCalled.

  //----------------------------------------------------------------------------
  // CODE BEGINS HERE
  //----------------------------------------------------------------------------
  public static void main(String[] args)
  {
    new JEdPointMicrokernel(args);
  }

  private JEdPointMicrokernel(String args[])
  {
    Properties jedpointProperties;

    if (args.length<1)
    {
      args = new String[1];
      args[0] = new String("JEdPoint.conf");
    }

    jedpointProperties = new Properties();
    try
    {
      jedpointProperties.load(new java.io.FileInputStream(args[0]));
    }
    catch (java.io.FileNotFoundException fnfe)
    {
      System.out.println("Could not find the file " + args[0] + " in the current directory.");
      System.out.println("This file is necessary for proper funtioning of JEdPoint.");
      System.out.println("Please consult the manual as to how to make one.");
      System.exit(1);
    }
    catch (Exception e)
    {
      System.out.println("Could not read the file " + args[0] + " in the current directory.");
      System.out.println("This file is necessary for proper funtioning of JEdPoint.");
      System.out.println("Please consult the manual as to how to make a proper file.");
      System.exit(1);
    }

    // Set the PID of the FidonetMessageUtils to the text string of this module (the microkernel)
    FMUtils.setPID( infoModuleNameVersion );

    // Increase the size of modules until it is == capacity
    while (modules.size() != modules.capacity()) modules.add(null);

    // Increase the size of threads until it is == capacity
    while (threads.size() != threads.capacity()) threads.add(null);

    // Add this Thread to the thread vector
    Thread t = new Thread(this);
    t.start();
    threads.setElementAt(t, JEdPointModule.moduleMicrokernel);

    // Load the most primitive of log modules.
    // This will output stuff directly to System.out
    // It will be used until the Default module is loaded.
    try
    {
      registerDefaultModule( new JEdPoint.modules.module_log.module_log() );
    }
    catch (JEdPointException JPE)
    {
      // This will never happen. There is no way that the primitive logging module won't be able to load.
    }

    // Send the properties to processSettings
    // It will extract our setttings from the properties file (args[0])
    sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.moduleInit, "properties", jedpointProperties);

    // Start the UI
    sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiStartUI, new JEdPointMessage());

    JEdPointMessage JPM = new JEdPointMessage();
    Boolean bool;
    int counter;
    Thread thread;
    do
    {
      try
      {
        Thread.sleep(500);
      }
      catch (Exception e)
      {
      }
      JPM = sendMessage(JEdPointModule.moduleUI, JEdPointMessage.uiGetStatus, JPM);
      bool = (Boolean)JPM.getResponse("active");
    } while ( bool.booleanValue() );

    shutdown(0);
  }

  private void processSettings(Properties jedpointProperties) throws JEdPointException
  {
    Vector loadVector = new Vector();
    int counter;
    String tempString;
    File tempFile;
    JEdPointModule tempModule;

    // First and foremost, check to see if we can find the "settingsdir" setting.
    // If we can't, then we must abort.
    if ( (String)jedpointProperties.get((String)"settingsdir") == null)
    {
      System.out.println("Couldn't find the \"settingsdir\" setting in the config file. Please edit the config file so that it contains such a variable.");
      System.exit(1);
    }

    tempFile = new File( (String)jedpointProperties.get((String)"settingsdir") );

    if (!tempFile.exists())
    {
      try
      {
        tempFile.mkdirs();
      }
      catch (Exception e)
      {
        System.out.println("The settings directory does not exist and could not be created!");
        System.exit(1);
      }
    }
    if (!tempFile.isDirectory())
    {
        System.out.println("The settings directory is actually a file. Please remove the file.");
        System.exit(1);
    }

    settingsSettingsDir = tempFile.toString();
    // We now have a guaranteed settingsdir, which the other modules can use.

    // Get the username
    settingsUserName = (String)jedpointProperties.get((String)"username");
    if (settingsUserName == null)
      throw new JEdPointException(new Exception("Unable to find the \"username\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"username\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a username settings.");

    // Get the user's point number
    try
    {
      settingsPointNumber = PointNumber.parsePointNumber( (String)jedpointProperties.get((String)"pointnumber") );
    }
    catch (Exception e)
    {
      throw new JEdPointException(new Exception("Unable to parse the \"pointnumber\" setting in the config file."),
        JEdPointException.severityFatal,
        "Unable to parse the \"pointnumber\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a valid point number.");
    }

    // Get Areafix's name
    settingsAreafixName = (String)jedpointProperties.get((String)"areafixname");
    if (settingsAreafixName == null)
      throw new JEdPointException(new Exception("Unable to find the \"areafixname\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"areafixname\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a username settings.");

    // Get Areafix's point number
    try
    {
      settingsAreafixPointNumber = PointNumber.parsePointNumber( (String)jedpointProperties.get((String)"areafixpointnumber") );
    }
    catch (Exception e)
    {
      throw new JEdPointException(new Exception("Unable to parse the \"areafixpointnumber\" setting in the config file."),
        JEdPointException.severityFatal,
        "Unable to parse the \"areafixpointnumber\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a valid point number.");
    }

    // Get the areafix password
    settingsAreafixPassword = (String)jedpointProperties.get((String)"areafixpassword");
    if (settingsPacketPassword == null)
      throw new JEdPointException(new Exception("Unable to find the \"areafixpassword\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"areafixpassword\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a packetpassword setting.");

    // Get the netmail directory
    settingsPacketPassword = (String)jedpointProperties.get((String)"packetpassword");
    if (settingsPacketPassword == null)
      throw new JEdPointException(new Exception("Unable to find the \"packetpassword\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"packetpassword\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a packetpassword setting.");

    // Get the codepage
    settingsCodepage = (String)jedpointProperties.get((String)"codepage");
    if (settingsCodepage == null)
      throw new JEdPointException(new Exception("Unable to find the \"codepage\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"codepage\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a codepage setting.");

    // Get the data directory
    settingsDataDir = (String)jedpointProperties.get((String)"datadir");
    if (settingsDataDir == null)
      throw new JEdPointException(new Exception("Unable to find the \"datadir\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"datadir\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains an importdir setting.");
    settingsDataDir = parseSetting( settingsDataDir );
    checkDirectory( settingsDataDir );

    // Get the import directory
    settingsImportDir = (String)jedpointProperties.get((String)"importdir");
    if (settingsImportDir == null)
      throw new JEdPointException(new Exception("Unable to find the \"importdir\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"importdir\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains an importdir setting.");
    settingsImportDir = parseSetting( settingsImportDir );
    checkDirectory( settingsImportDir );

    // Get the export directory
    settingsExportDir = (String)jedpointProperties.get((String)"exportdir");
    if (settingsExportDir == null)
      throw new JEdPointException(new Exception("Unable to find the \"exportdir\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"exportdir\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains an exportdir setting.");
    settingsExportDir = parseSetting( settingsExportDir );
    checkDirectory( settingsExportDir );

    // Get the netmail directory
    settingsNetmailDir = (String)jedpointProperties.get((String)"netmaildir");
    if (settingsNetmailDir == null)
      throw new JEdPointException(new Exception("Unable to find the \"netmaildir\" setting in the config file."),
        JEdPointException.severityFatal,
        "Could not find the  \"netmaildir\" setting in the config file.",
        "The config file is not properly configured.",
        "Edit the config file so that is contains a netmaildir setting.");
    settingsNetmailDir = parseSetting( settingsNetmailDir );
    checkDirectory( settingsNetmailDir );

    // Default area tearline
    if (jedpointProperties.getProperty("defaultareatearline", "").length() == 0)
      settingsDefaultAreaTearline = "JEdPoint (www.sourceforge.net/projects/jedpoint)";
    else
      settingsDefaultAreaTearline = jedpointProperties.getProperty("defaultareatearline");

    // Default area origin
    if (jedpointProperties.getProperty("defaultareaorigin", "").length() == 0)
      settingsDefaultAreaOrigin = "A JEdPoint User";
    else
      settingsDefaultAreaOrigin = jedpointProperties.getProperty("defaultareaorigin");

    // Add a default logging module
    registerDefaultModule( new JEdPoint.modules.module_log_Default.module_log_Default() );
    // We now have a live log file (of some sort). Now extract and process the other settings, like the username, for example.
    // If something fails, log it and then give it up (terminate).

    // Put all the load tokens in a vector
    if ( (String)jedpointProperties.get((String)"load") != null)
    {
      java.util.StringTokenizer st = new StringTokenizer( (String)jedpointProperties.get((String)"load") );
      while (st.hasMoreTokens())
      {
        tempString = st.nextToken();

        parseSetting( tempString );

        // What does this line do? I forget...
//        tempString = replaceString(tempString, "\"", "");

        loadVector.add(tempString);
      }
    }

    // Start adding our default modules here
    // -------------------------------------
    registerDefaultModule( new JEdPoint.modules.module_messagebase_Default.module_messagebase_Default() );
    registerDefaultModule( new JEdPoint.modules.module_origin_Default.module_origin_Default() );
    registerDefaultModule( new JEdPoint.modules.module_tearline_Default.module_tearline_Default() );
    registerDefaultModule( new JEdPoint.modules.module_poll_Default.module_poll_Default() );
    registerDefaultModule( new JEdPoint.modules.module_messageeditor_Default. module_messageeditor_Default() );
    registerDefaultModule( new JEdPoint.modules.module_import_Default.module_import_Default() );
    registerDefaultModule( new JEdPoint.modules.module_export_Default.module_export_Default() );
    registerDefaultModule( new JEdPoint.modules.module_ui_Default.module_ui_Default() );

    // Load the user's modules
    loadModulesFromVector(loadVector);
  }

  /**
   * Replaces all known setting variables in a string with their values.
   * <br><br>
   * The following settings variables are replaced:
   * <br>
   * %username%     "Edward Hevlund"
   * %pointnumber%  "2:200/486.4"
   * %settingsdir%  "c:\fidonet\jedpoint\settings"
   * %datadir%      "c:\fidonet\jedpoint\data"
   * %importdir%    "c:\fidonet\jedpoint\import"
   * %exportdir%    "c:\fidonet\jedpoint\export"
   * <br>
   */
  private String parseSetting( String settingToParse )
  {
    settingToParse = JEdPointUtilities.replaceString(settingToParse, "%username%", settingsUserName);
    settingToParse = JEdPointUtilities.replaceString(settingToParse, "%pointnumber%", settingsPointNumber.toString());

    settingToParse = JEdPointUtilities.replaceString(settingToParse, "%settingsdir%", settingsSettingsDir);
    settingToParse = JEdPointUtilities.replaceString(settingToParse, "%datadir%", settingsDataDir);

    settingToParse = JEdPointUtilities.replaceString(settingToParse, "%importdir%", settingsImportDir);
    settingToParse = JEdPointUtilities.replaceString(settingToParse, "%exportdir%", settingsExportDir);
    return settingToParse;
  }

  /**
   * Loads all the modules from a vector of filenames.
   *
   * Routes a fatal if a module couldn't be loaded (be it due to the fact that it isn't
   * a module or whatever.
   */
  private void loadModulesFromVector(Vector loadVector)
  {
    int counter;
    JEdPointModule tempMod;
    String tempString;

    for (counter=0; counter<loadVector.size(); counter++)
    {
      tempString = (String)loadVector.elementAt(counter);
      if (this.getModuleType(tempString) != -1)
      {
        this.loadModule(tempString);
      }
      else
      {
        this.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception",
          new JEdPointException(new Exception("Could not load a module."),
          JEdPointException.severityFatal,
          "Could not load the module " + tempString,
          "File not found.\nNot a module.",
          "Check to see that the file exists and that it is a module."));
      }
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
   * Shuts JEdPoint down
   */
  private void shutdown(int errorNumber)
  {
    shutdownAllModules();
    System.exit(errorNumber);
  }

  /**
   * Will call all the modules in reverse startup order and shut them all down.
   */
  private void shutdownAllModules()
  {
    int counter;
    JEdPointModule JPM;

    for (counter=JEdPointModule.moduleTypes-1; counter>0; counter--)
    {
      if (modules.elementAt(counter) != null)
      {
        JPM = (JEdPointModule)modules.elementAt(counter);

        try
        {
          JPM.processMessage( JEdPointMessage.moduleDeInit, new JEdPointMessage() );
        }
        catch (JEdPointException JPE)
        {
          sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exeception", JPE);
        }
      } // If != null
    }
  }

 /**
  * Loads a module from disk and then returns with the module type.
  * <br><br>
  * If it is not a module then -1 will be returned.
  */
  private int getModuleType(String FilenameToLoad)
  {
    JEdPointModule loadModule;
    JEdPointMessage tempMessage = new JEdPointMessage();
    Integer tempInteger;
    try
    {
      loadModule = (JEdPointModule)dcl.LoadClass(FilenameToLoad).newInstance();

      // Give this instance to the module
      loadModule.setJEdPointClass(this);

      tempMessage = loadModule.processMessage(JEdPointMessage.moduleGetInformation, tempMessage );
      tempInteger = (Integer)tempMessage.getResponse("type");
    }
    catch (Exception e)
    {
      return -1;
    }
    return tempInteger.intValue();
  }

  /**
   * The difference between registerModule and registerDefaultModule is that
   * registerDefaultModule will halt the program in the case that a default module
   * can't be loaded.
   *
   * registerModule will simply report the error and revert back to a default module.
   */
  private void registerModule(JEdPointModule newjpm)
  {
    Thread moduleThread;
    Integer tempInteger;
    JEdPointMessage tempMessage = new JEdPointMessage();

    // Give this instance to the module
    newjpm.setJEdPointClass(this);

    try
    {
      tempMessage = newjpm.processMessage(JEdPointMessage.moduleGetInformation, tempMessage );
    }
    catch (JEdPointException jpe)
    {
      // Why couldn't we ask it to process that message?
    }

    try
    {
      moduleThread = new Thread(newjpm);
      moduleThread.start();
      newjpm.processMessage(JEdPointMessage.moduleInit, new JEdPointMessage());

      tempInteger = (Integer)tempMessage.getResponse("type");
      modules.setElementAt(newjpm, tempInteger.intValue());
      threads.setElementAt(moduleThread, tempInteger.intValue());
    }
    catch (JEdPointException JPE)
    {
      // Log the FATAL (how else would we have gotten here? The module couldn't
      // init itself. That must mean that something terrible happened.
      this.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception", JPE);
    }

  }

  /**
   * The difference between registerModule and registerDefaultModule is that
   * registerDefaultModule will halt the program in the case that a default module
   * can't be loaded.
   *
   * registerModule will simply report the error and revert back to a default module.
   */
  private void registerDefaultModule(JEdPointModule newjpm) throws JEdPointException
  {
    Thread moduleThread;
    Integer tempInteger;
    JEdPointMessage tempMessage = new JEdPointMessage();

    // Give this instance to the module
    newjpm.setJEdPointClass(this);

    try
    {
      tempMessage = newjpm.processMessage(JEdPointMessage.moduleGetInformation, tempMessage );
    }
    catch (JEdPointException jpe)
    {
      // Why couldn't we ask it to process that message?
    }

    moduleThread = new Thread(newjpm);
    moduleThread.start();
    newjpm.processMessage(JEdPointMessage.moduleInit, new JEdPointMessage());

    tempInteger = (Integer)tempMessage.getResponse("type");
    modules.setElementAt(newjpm, tempInteger.intValue());
    threads.setElementAt(moduleThread, tempInteger.intValue());
  }

  /**
   * Loads a module from disk and then installs it at the end of the module chain for its type.
   *
   * @param FilenameToLoad Can be either a filename "C:\module.class", a URL "http://jedpoint.com/modules/module.jar" or a normal jar file "c:\module.jar"
   *
   * @return true Module loaded ok
   * @return false Module did not load ok, for whatever reason
   */
  private boolean loadModule(String FilenameToLoad)
  {
    JEdPointModule loadModule;
    try
    {
      loadModule = (JEdPointModule)dcl.LoadClass(FilenameToLoad).newInstance();

      // Load the module into the module vector, init, etc...
      this.registerModule( loadModule );
    }
    catch (Exception e)
    {
      return false;
    }
    // So far, so good.

    return true;
  }

  /**
   * Informs JEdPoint that jedpointModule has messages queued.
   *
   * JEdPoint will then pick the messages up, route them and then return them via
   * the source module's processMessage.
   */
  public void messageReadyForPickup(JEdPointModule jedpointModule)
  {
    this.MsgFetcher.addToQueue(jedpointModule);
    this.MsgFetcherThread.run();
    return;
  }

  private final JEdPointMessage sendMessage(int moduleToSendTo, int messageType, JEdPointMessage JPM)
  {
    return this.routeMessage(moduleToSendTo, messageType, JPM);
  }

  private final JEdPointMessage sendMessage(int moduleToSendTo, int messageType, String newKey, Object newValue)
  {
    return this.sendMessage(moduleToSendTo, messageType, new JEdPointMessage(newKey, newValue));
  }

  /**
   * Routes the message to the correct module(s).
   * <br><br>
   * Will return only when the destination module returns.
   */
  public JEdPointMessage routeMessage(int moduleToSendTo, int messageType, JEdPointMessage JPM)
  {
    int counter;
    Vector moduleVector;
    JEdPointModule tempModule;

    // This check is to see if the message is mean for the microkernel
    if ( (moduleToSendTo!=JEdPointModule.moduleMicrokernel) )
    {
      // This is NOT a microkernel message

      tempModule = (JEdPointModule)modules.elementAt(moduleToSendTo);

      try
      {
        JPM = tempModule.processMessage(messageType, JPM);
      }
      catch (JEdPointException JPE)
      {
        // Handle the exception.
        switch (JPE.getSeverity())
        {
          case (JEdPointException.severityFatal):
            // Log the exception
            this.sendMessage( JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception", JPE);
            // terminate JEdPoint
            shutdown(1);
          break;
          case (JEdPointException.severityError):
            // Log the exception
            this.sendMessage( JEdPointModule.moduleLog, JEdPointMessage.logError, "exception", JPE);
          return null;
        }
      }
      catch (Exception e)
      {
        this.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception",
          new JEdPointException(e, JEdPointException.severityFatal,
          "JEdPoint encountered an unhandled exception.",
          "",
          ""));
      }
    }
    else
    {
      // This is a microkernel message
      try
      {
        JPM = this.processMessage(messageType, JPM);
      }
      catch (JEdPointException JPE)
      {
        // Handle the exception.
        switch (JPE.getSeverity())
        {
          case (JEdPointException.severityFatal):
            // Log the exception
            this.sendMessage( JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception", JPE);
            // terminate JEdPoint
            shutdown(1);
          break;
          case (JEdPointException.severityError):
            // Log the exception
            this.sendMessage( JEdPointModule.moduleLog, JEdPointMessage.logError, "exception", JPE);
          return null;
        }
      }
    }
    return JPM;
  }

  // processes messages specifically for JEdPoint
  private JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
  {
    switch (messageType)
    {
      case JEdPointMessage.moduleInit:
        this.processSettings( (Properties)JPM.getRequest("properties") );
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
      case JEdPointMessage.mkGetSettings:
        JPM.setResponse("username", settingsUserName);
        JPM.setResponse("pointnumber", settingsPointNumber);
        JPM.setResponse("packetpassword", settingsPacketPassword);
        JPM.setResponse("areafixname", settingsAreafixName);
        JPM.setResponse("areafixpointnumber", settingsAreafixPointNumber);
        JPM.setResponse("areafixpassword", settingsAreafixPassword);
        JPM.setResponse("codepage", settingsCodepage);
        JPM.setResponse("settingsdir", settingsSettingsDir);
        JPM.setResponse("datadir", settingsDataDir);
        JPM.setResponse("jedpointdir", System.getProperty("user.dir"));
        JPM.setResponse("netmaildir", settingsNetmailDir);
        JPM.setResponse("importdir", settingsImportDir);
        JPM.setResponse("exportdir", settingsExportDir);
        JPM.setResponse("defaultareatearline", settingsDefaultAreaTearline);
        JPM.setResponse("defaultareaorigin", settingsDefaultAreaOrigin);
      break;
      case JEdPointMessage.mkParseSetting:
        JPM.setResponse( "parsed",  parseSetting(JPM.getRequestString("parse")) );
      break;
    }
    return JPM;
  }

  public void run()
  {
  }

  // ---------------------------------------------------------------------------
  // OUR INTERNAL CLASSES USED FOR CLASS LOADING AND MESSAGE HANDLING
  // ---------------------------------------------------------------------------
  private class DynamicClassLoader extends ClassLoader
  {
    private HashMap ClassCache = new HashMap();

    public String getFilename(Class ClassToFind)
    {
      Class tempclass;
      String key;
      Iterator it = ClassCache.values().iterator();
      while (it.hasNext())
      {
        key = it.next().toString();
        if (ClassCache.get(key).equals(ClassToFind))
        return key;
      }
      return "";
    }

    public Class LoadClass(String FileName)
    {
      Class ReturnClass = (Class)ClassCache.get(FileName);
      if (ReturnClass==null)
      {
        // Is the file a jar? In that case, make it into a URL...
        if (FileName.toLowerCase().indexOf(".jar") != -1)
          FileName = "file:///" + FileName;

        // Check to see if the filename is a URL
        try
        {
          new URL(FileName);

          // If we've gotten this far then it's a URL. Try to load the class
          ReturnClass = new JARClassLoader(new URL(FileName)).LoadClass();
          if (ReturnClass!=null) ClassCache.put(FileName, ReturnClass);
        }
        catch (java.net.MalformedURLException mue)
        {
          try
          {
            FileInputStream fis = new FileInputStream(FileName);
            byte Buffer[] = new byte[fis.available()];
            fis.read(Buffer);

            // To catch those naughty LinkageErrors (class redefine errors)
            try
            {
              ReturnClass = defineClass(null, Buffer, 0, Buffer.length);
            }
            catch (java.lang.LinkageError linkageerror)
            {
            }

            if (ReturnClass!=null) ClassCache.put(FileName, ReturnClass);
          }
          catch (Exception e)
          {
            return null;
          }
        }
      }
      return ReturnClass;
    }

    class JARClassLoader extends URLClassLoader
    {
      private HashMap ClassCache = new HashMap();
      URL url;

      public JARClassLoader(URL url)
      {
        super(new URL[] { url });
        this.url = url;
      }

      private String getMainClassName(URL url) throws IOException
      {
        URL u = new URL("jar", "", url + "!/");
        JarURLConnection uc = (JarURLConnection)u.openConnection();
        Attributes attr = uc.getMainAttributes();
        return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
      }

      public Class LoadClass()
      {
        Class ReturnClass = (Class)ClassCache.get(url.toString());

        if (ReturnClass==null)
          try
          {
            ReturnClass = loadClass( this.getMainClassName(url) );

            if (ReturnClass!=null) ClassCache.put(url.toString(), ReturnClass);
          }
          catch (Exception e)
          {
            System.out.println( e.toString() );
            return null;
          }
        return ReturnClass;
      }
    }
  }

  private class MessageFetcher implements Runnable
  {
    private JEdPointMicrokernel jp;

    // The fetchQueue is where we store all the modules that have messages queued.
    private JEdPointModule fetchQueue[] = new JEdPointModule[0];

    public MessageFetcher(JEdPointMicrokernel newJP)
    {
      this.jp = newJP;
    }

    /**
     * Add a module to the queue.
     *
     * The module's message will be fetched whenever MessageFetcher has the time.
     */
    public void addToQueue(JEdPointModule jpmToAdd)
    {
      this.expandQueue();
      fetchQueue[fetchQueue.length-1] = jpmToAdd;
    }

    /**
     * Increases the fetchQueue by one.
     */
    protected void expandQueue()
    {
      int counter;
      JEdPointModule tempQueue[] = new JEdPointModule[fetchQueue.length+1];

      for (counter=0; counter<fetchQueue.length; counter++)
      {
        tempQueue[counter] = fetchQueue[counter];
      }
      this.fetchQueue = tempQueue;
    }

    /**
     * Decreases the fetchQueue by one.
     *
     * Removes the first object in the queue
     */
    protected void shrinkQueue()
    {
      int counter;
      JEdPointModule tempQueue[] = new JEdPointModule[fetchQueue.length-1];

      for (counter=1; counter<fetchQueue.length; counter++)
      {
        tempQueue[counter-1] = fetchQueue[counter];
      }
      this.fetchQueue = tempQueue;
    }

    public void run()
    {
      Integer moduleToSendTo;
      Integer messageType;

      JEdPointMessage tempJPM;
      JEdPointModule moduleSentFrom;

      while (fetchQueue.length != 0)
      {
        // Check to see that the message hasn't somehow disappeared
        if (fetchQueue[0].messagesAvailable() !=0)
        {
          // Fetch the message.
          tempJPM = fetchQueue[0].fetchMessage();
          // Extract the moduletosendto and messagetype
          moduleToSendTo = (Integer)tempJPM.getRequest("moduleToSendTo");
          messageType = (Integer)tempJPM.getRequest("messageType");
          moduleSentFrom = (JEdPointModule) tempJPM.getRequest("moduleSentFrom");
          // Remove those two extra keys
          tempJPM.removeRequest("moduleToSendTo");
          tempJPM.removeRequest("messageType");

          // Route the message...
          tempJPM = jp.routeMessage(moduleToSendTo.intValue(), messageType.intValue(), tempJPM);
          // Return the message to the module that sent it
          try
          {
            moduleSentFrom.processMessage(messageType.intValue(), tempJPM);
          }
          catch (Exception e)
          {
          }
          // Remove the module from the queue
          shrinkQueue();
        }
      }
    }
  }

}