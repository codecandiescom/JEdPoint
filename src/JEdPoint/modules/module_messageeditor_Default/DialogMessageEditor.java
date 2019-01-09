package JEdPoint.modules.module_messageeditor_Default;

import JEdPoint.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;
import javax.swing.*;

public class DialogMessageEditor extends JDialog
{
  module_messageeditor_Default parentModule;
  JEdPointModule jpm;
  boolean changed = false;
  boolean cancel = false;
  FidonetMessage oldMessage, newMessage;
  String settingsDir;
  String dataDir;

  private static final String configFilename      = "module_messageeditor_default.conf";
  private static final String privateSettingsFile = "module_messageeditor_default.private";

  // A separate class is used for the settings so that we can save it all to disk all at once.
  private module_messageeditor_default_settings privateSettings;

  // User defined settings
  boolean autosave;
  boolean showKludges;

  private String editorFontName = "Dialog";
  private int editorFontSize = 15;
  private int editorFontBold;
  private int editorFontItalics;
  private Color editorForeground;
  private Color editorBackground;

  JPanel jPanelMain = new JPanel();
  JPanel jPanelHeaders = new JPanel();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JTextField jTextFieldNameFrom = new JTextField();
  JTextField jTextFieldNameTo = new JTextField();
  JTextField jTextFieldSubject = new JTextField();
  JTextField jTextFieldPointFrom = new JTextField();
  JTextField jTextFieldPointTo = new JTextField();
  BorderLayout borderLayout1 = new BorderLayout();
  JTextArea jTextAreaMessage = new JTextArea();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenuItem jMenuItemFileSaveAndExit = new JMenuItem();
  JMenuItem jMenuItemFileExit = new JMenuItem();
  BorderLayout borderLayout4 = new BorderLayout();
  JLabel jLabelDateTime = new JLabel();
  JLabel jLabelStatusBar = new JLabel();
  TitledBorder titledBorder1;
  JMenu jMenu1 = new JMenu();
  JMenuItem jMenuItem1 = new JMenuItem();
  JScrollPane jScrollPaneEditor = new JScrollPane ( jTextAreaMessage );
  JLabel jLabel4 = new JLabel();

  // ---------------------------------------------------------------------------
  // MAIN CLASS
  // ---------------------------------------------------------------------------

  public DialogMessageEditor(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public DialogMessageEditor()
  {
    this(null, "", false);
  }

  void jbInit() throws Exception
  {
    titledBorder1 = new TitledBorder("");
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.setTitle("Message Editor");
    this.setSize(500, 500);
    jPanelHeaders.setMinimumSize(new Dimension(1, 90));
    jPanelHeaders.setPreferredSize(new Dimension(1, 100));
    jPanelHeaders.setBounds(new Rectangle(39, 28, 572, 90));
    jPanelHeaders.setLayout(null);
    jLabel1.setDisplayedMnemonic('R');
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("From");
    jLabel1.setBounds(new Rectangle(6, 10, 47, 17));
    jLabel2.setDisplayedMnemonic('T');
    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setText("To");
    jLabel2.setBounds(new Rectangle(6, 33, 47, 17));
    jLabel3.setDisplayedMnemonic('S');
    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setText("Subject");
    jLabel3.setBounds(new Rectangle(6, 56, 47, 17));
    jTextFieldNameFrom.setFocusAccelerator('R');
    jTextFieldNameFrom.setText("0000000000111111111122222222223333333");
    jTextFieldNameFrom.setBounds(new Rectangle(56, 8, 265, 21));
    jTextFieldNameTo.setFocusAccelerator('T');
    jTextFieldNameTo.setText("jTextField2");
    jTextFieldNameTo.setBounds(new Rectangle(56, 31, 265, 21));
    jTextFieldSubject.setFocusAccelerator('S');
    jTextFieldSubject.setText("000000000099999999990000000000999999999900000000009999999999000000000099");
    jTextFieldSubject.setBounds(new Rectangle(55, 54, 511, 21));
    jTextFieldPointFrom.setText("jTextField4");
    jTextFieldPointFrom.setBounds(new Rectangle(328, 9, 101, 21));
    jTextFieldPointTo.setText("666:666/666.666");
    jTextFieldPointTo.setBounds(new Rectangle(328, 32, 101, 21));
    jTextAreaMessage.setFont(new java.awt.Font("Monospaced", 0, 12));
    jTextAreaMessage.setCaretColor(Color.black);
    jTextAreaMessage.setFocusAccelerator('M');
    jTextAreaMessage.setText("Status");
    jTextAreaMessage.setLineWrap(true);
    jTextAreaMessage.setWrapStyleWord(true);
    this.addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        this_windowClosing(e);
      }
      public void windowOpened(WindowEvent e)
      {
        this_windowOpened(e);
      }
    });
    jMenuFile.setMnemonic('F');
    jMenuFile.setText("File");
    jMenuItemFileSaveAndExit.setMnemonic('S');
    jMenuItemFileSaveAndExit.setText("Save and Exit");
    jMenuItemFileSaveAndExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_MASK, true));
    jMenuItemFileSaveAndExit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemFileSaveAndExit_actionPerformed(e);
      }
    });
    jMenuItemFileExit.setMnemonic('X');
    jMenuItemFileExit.setText("Exit");
    jMenuItemFileExit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemFileExit_actionPerformed(e);
      }
    });
    jPanelMain.setLayout(borderLayout4);
    jLabelDateTime.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelDateTime.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelDateTime.setText("jLabel4");
    jLabelDateTime.setBounds(new Rectangle(439, 12, 127, 17));
    jPanelMain.setMinimumSize(new Dimension(600, 500));
    jPanelMain.setPreferredSize(new Dimension(600, 500));
    jLabelStatusBar.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelStatusBar.setText("Status Bar");
    jMenu1.setMnemonic('O');
    jMenu1.setText("Options");
    jMenuItem1.setMnemonic('W');
    jMenuItem1.setText("Save Window Position");
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    jLabel4.setDisplayedMnemonic('M');
    jLabel4.setText("Message");
    jLabel4.setBounds(new Rectangle(6, 79, 54, 17));
    jPanelHeaders.add(jLabel1, null);
    jPanelHeaders.add(jLabel2, null);
    jPanelHeaders.add(jLabel3, null);
    jPanelHeaders.add(jTextFieldNameTo, null);
    jPanelHeaders.add(jTextFieldNameFrom, null);
    jPanelHeaders.add(jTextFieldSubject, null);
    jPanelHeaders.add(jTextFieldPointFrom, null);
    jPanelHeaders.add(jTextFieldPointTo, null);
    jPanelHeaders.add(jLabelDateTime, null);
    jPanelHeaders.add(jLabel4, null);
    jPanelMain.add(jPanelHeaders, BorderLayout.NORTH);
    jPanelMain.add(jScrollPaneEditor, BorderLayout.CENTER);
    jPanelMain.add(jLabelStatusBar, BorderLayout.SOUTH);
    getContentPane().add(jPanelMain, BorderLayout.CENTER);

    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenu1);
    jMenuFile.add(jMenuItemFileSaveAndExit);
    jMenuFile.add(jMenuItemFileExit);
    jMenu1.add(jMenuItem1);
    setJMenuBar(jMenuBar1);
  }

  public void initialize(JEdPointModule newJPM)
  {
    jpm = newJPM;
    loadSettings();
  }

  private void loadSettings()
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Properties props = new Properties();
    String tempString;
    StringTokenizer st;
    PointNumber tempPointNumber;
    Font tempFont;
    Color tempColor;
    HashMap tempHashMap;
    String fullConfigFilename;

    JPM = jpm.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, new JEdPointMessage());
    settingsDir = (String)JPM.getResponse("settingsdir");
    dataDir = (String)JPM.getResponse("datadir");

    fullConfigFilename = settingsDir + File.separator + configFilename;

    try
    {
      props.load( new FileInputStream(fullConfigFilename));
    }
    catch (FileNotFoundException fnfe)
    {
      jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception", new JEdPointException(
        fnfe,
        JEdPointException.severityFatal,
        "Could not find the settings file: " + fullConfigFilename,
        "File does not exist.",
        "Create a new settings file for the UI module."));
    }
    catch (IOException ioe)
    {
      jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logFatal, "exception", new JEdPointException(
        ioe,
        JEdPointException.severityFatal,
        "Could not read the settings file: " + fullConfigFilename,
        "No read permission?",
        "Check to see that the file can be read."));
    }

    // Try to load our saved settings from disk
    tempHashMap = (HashMap)JEdPointUtilities.loadObject( dataDir + "/" + privateSettingsFile);
    if (tempHashMap == null)
      privateSettings = new module_messageeditor_default_settings();
    else
    {
      privateSettings = new module_messageeditor_default_settings();
      privateSettings.loadSettings(tempHashMap);
    }

    // Set the main window to the privateSettings size, unless the saved size is brand new
    if ( new Dimension().hashCode() != privateSettings.MainWindowDimension.hashCode() )
      this.setSize( privateSettings.MainWindowDimension );

    // Save the message automatically?
    if (props.getProperty("autosave", "false").toLowerCase().compareTo("false") == 0)
      autosave = false;
    else
      autosave = true;

    // Show the kludges?
    if (props.getProperty("showkludges", "false").toLowerCase().compareTo("false") == 0)
      showKludges = false;
    else
      showKludges = true;

    loadFontAndColors(props, jTextAreaMessage, "editor", editorFontName, editorFontSize, editorFontBold, editorFontItalics, editorForeground, editorBackground);

    // Set the caret color for the editor
    tempColor = JEdPointUtilities.parseColor( props.getProperty("editorcaretcolor", "0 0 0") );

    if (tempColor != null)
      jTextAreaMessage.setCaretColor( tempColor );

    // End of font stuff
  }

  /**
   * Loads font and color settings for a component from a properties, using the specified
   * key as the base for all ... umm.. keys.
   *
   * The font* and *Color settings are saved from the properties.
   */
  private void loadFontAndColors( Properties properties, Component componentToSet, String keyname,
    String fontName,
    int fontSize,
    int fontBold,
    int fontItalics,
    Color textColor,
    Color backgroundColor)
  {
    Font tempFont;

    // First get the current settings
    tempFont = componentToSet.getFont();

    // Get the font name
    fontName = properties.getProperty(keyname + "fontname", "Monospaced");

    try
    {
      fontSize = Integer.parseInt( properties.getProperty(keyname + "fontsize", "15") );
    }
    catch (NumberFormatException nfe)
    {
    }

    if (properties.getProperty(keyname + "fontbold", "false").toLowerCase().compareTo("false") == 0)
      fontBold = 0;
    else
      fontBold = Font.BOLD;

    if (properties.getProperty(keyname + "fontitalics", "false").toLowerCase().compareTo("false") == 0)
      fontItalics = 0;
    else
      fontItalics = Font.ITALIC;

    try
    {
      componentToSet.setFont( new Font(fontName, (fontBold | fontItalics), fontSize) );
    }
    catch (Exception setFont)
    {
    }

    // Set the text color for the messageList
    textColor = JEdPointUtilities.parseColor( properties.getProperty(keyname + "textcolor", "0 0 0") );
    if (textColor != null)
      componentToSet.setForeground( textColor );

    // Set the background color for the messageList
    backgroundColor = JEdPointUtilities.parseColor( properties.getProperty(keyname + "backgroundcolor", "255 255 255") );
    if (backgroundColor != null)
      componentToSet.setBackground( backgroundColor );
  }

  public void setMessage(FidonetMessage messageToEdit)
  {
    this.oldMessage = new FidonetMessage( messageToEdit );
    this.newMessage = new FidonetMessage( messageToEdit );
    this.setDataFromMessage();
  }

  private void setDataFromMessage()
  {
    StringBuffer wholeMessage = new StringBuffer();
    Vector tempvector;
    JEdPointMessage JPM = new JEdPointMessage();
    int positionAfterMessageText;
    GregorianCalendar gCal;
    String tempstring;
    PointNumber pointnumber;

    jTextFieldNameFrom.setText( (String)newMessage.getMessageData("usernamefrom") );
    jTextFieldNameTo.setText( (String)newMessage.getMessageData("usernameto") );

    pointnumber = (PointNumber) newMessage.getMessageData("pointfrom");
    jTextFieldPointFrom.setText( pointnumber.toString() );
    pointnumber = (PointNumber) newMessage.getMessageData("pointto");
    jTextFieldPointTo.setText( pointnumber.toString() );
//    this.jTextFieldPointFrom.setText( newMessage.getMessageData("pointfrom").toString() );
//    this.jTextFieldPointTo.setText( newMessage.getMessageData("pointto").toString() );

    this.jTextFieldSubject.setText( (String)newMessage.getMessageData("subject") );

    // Set the time + date
    gCal  = (GregorianCalendar)newMessage.getMessageData("datetime");
    this.jLabelDateTime.setText( JEdPointUtilities.GregorianCalendarToString(gCal) );

    if (this.showKludges)
    {
      tempvector = (Vector)newMessage.getMessageData("prekludges");
      while (tempvector.size() != 0)
        wholeMessage.append( (String)tempvector.elementAt(0) );
      wholeMessage.append("\n");
    }

    wholeMessage.append( (String)newMessage.getMessageData("message") +"\n");

    positionAfterMessageText = wholeMessage.toString().length()-1;

    wholeMessage.append("\n");

    JPM = new JEdPointMessage();

    // Add the tearline
    JPM = jpm.sendMessage(JEdPointModule.moduleTearline, JEdPointMessage.tlAssemble, "tearline", newMessage.getMessageData("tearline") );
    wholeMessage.append( (String)JPM.getResponse("tearline") + "\n");

    // Add the origin
    JPM.setRequest( "origin", newMessage.getMessageData("origin") );
    JPM.setRequest( "point", newMessage.getMessageData("pointfrom") );
    JPM = jpm.sendMessage(JEdPointModule.moduleOrigin, JEdPointMessage.originAssemble, JPM);
    wholeMessage.append( (String)JPM.getResponse("origin") + "\n" );

    if (this.showKludges)
    {
      tempvector = (Vector)newMessage.getMessageData("postkludges");
      while (tempvector.size() != 0)
        wholeMessage.append( (String)tempvector.elementAt(0) );
      wholeMessage.append("\n");
    }

    this.jTextAreaMessage.setText( wholeMessage.toString() );
    this.jTextAreaMessage.requestFocus();
    this.jTextAreaMessage.setCaretPosition( positionAfterMessageText );
  }

  /**
   * Returns the string clipped down to maxLength (if necessary).
   */
  private String maxStringLength(String string, int maxLength)
  {
    if (string.length()>maxLength)
      string = string.substring(0, maxLength);
    return string;
  }

  private void getMessageData()
  {
    String uneditableAttributes[] = {
      "attributenewlyimported",
      "attributeread",
      "attributeprivate",
      "attributecrash",
      "attributerecd",
      "attributesent",
      "attributefileattached",
      "attributeintransit",
      "attributeorphan",
      "attributekillsent",
      "attributelocal",
      "attributeholdforpickup",
      "attributefilerequest",
      "attributereturnreceiptrequest",
      "attributeisreturnreceipt",
      "attributeauditrequest",
      "attributefileupdatereq",
      "datetime"
      };

    int counter;
    // Get all the fields that _can_ be edited
    newMessage.setMessageData( "usernamefrom", maxStringLength(jTextFieldNameFrom.getText(), 36) );
    newMessage.setMessageData( "usernameto", maxStringLength(jTextFieldNameTo.getText(), 36) );
    newMessage.setMessageData( "pointfrom", PointNumber.parsePointNumber(jTextFieldPointFrom.getText()) );
    newMessage.setMessageData( "pointto", PointNumber.parsePointNumber(jTextFieldPointTo.getText()) );
    newMessage.setMessageData( "subject", maxStringLength(jTextFieldSubject.getText(), 72) );
    if (showKludges)
      newMessage.setMessageData( "prekludges", FidonetMessageUtils.extractPreKludges(jTextAreaMessage.getText()) );
    else
      newMessage.setMessageData("prekludges", oldMessage.getMessageData("prekludges"));
    newMessage.setMessageData( "message", FidonetMessageUtils.extractMessageBody(jTextAreaMessage.getText()) );
    newMessage.setMessageData( "tearline", FidonetMessageUtils.extractTearline(jTextAreaMessage.getText()) );
    newMessage.setMessageData( "origin", FidonetMessageUtils.extractOrigin(jTextAreaMessage.getText()) );
    if (showKludges)
      newMessage.setMessageData( "postkludges", FidonetMessageUtils.extractPostKludges(jTextAreaMessage.getText()) );
    else
      newMessage.setMessageData("postkludges", oldMessage.getMessageData("postkludges"));

    // The fields which we cannot edit come from the oldMessage
    for (counter=0; counter<uneditableAttributes.length; counter++)
    {
      newMessage.setMessageData(uneditableAttributes[counter], oldMessage.getMessageData(uneditableAttributes[counter]));
    }
  }

  public FidonetMessage getMessage()
  {
    return this.newMessage;
  }

  public void quitEditor()
  {
    int result;

    getMessageData();

    if ( !newMessage.equals(oldMessage) )
    {
      // The message was changed.
      // If autosave is on, save and quit.
      // Which means... just quit (since getMessageData "saves" the data)
      if (this.autosave)
      {
        this.dispose();
        return;
      }

      // Autosave isn't on, so let's ask the user if he really wants to quit without saving
      result = JOptionPane.showConfirmDialog(this, "Do you wish to save this message?", "JEdPoint", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (result == JOptionPane.YES_OPTION)
      {
        // Save the message
        // Which getMessageData does.
        // Then quit.
        this.dispose();
      }
      if (result == JOptionPane.NO_OPTION)
      {
        // Quit without saving.
        // Set newMessage to OldMessage
        this.newMessage = this.oldMessage;
        this.dispose();
      }
    }
    else
      // The message wasn't changed
      // Just quit
      this.dispose();
  }

  public boolean editingCanceled()
  {
    return newMessage.equals(oldMessage);
  }

  void this_windowClosing(WindowEvent e)
  {
    this.quitEditor();
  }

  void jMenuItemFileExit_actionPerformed(ActionEvent e)
  {
    if (this.autosave)
      this.changed = true;
    else
      this.changed = newMessage.equals(oldMessage);
    this.quitEditor();
  }

  void this_windowOpened(WindowEvent e)
  {
    jTextAreaMessage.requestFocus();
  }

  void jMenuItemFileSaveAndExit_actionPerformed(ActionEvent e)
  {
    this.autosave = true;
    this.quitEditor();
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {
    privateSettings.MainWindowDimension = this.getSize();

    if (JEdPointUtilities.saveObject(dataDir + "/" + privateSettingsFile, privateSettings.saveSettings()))
      JOptionPane.showConfirmDialog(this, "Window size settings saved!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    else
      JOptionPane.showConfirmDialog(this, "Window size settings could not be saved!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
  }

}
