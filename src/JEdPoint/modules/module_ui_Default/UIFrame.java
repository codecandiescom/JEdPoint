package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import JEdPoint.modules.module_ui_Default.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;

public class UIFrame extends JFrame
{
  JEdPointModule jpm;

  Frame MainFrame = this;

  JPanel contentPane;
  JMenuBar jMenuBarMain = new JMenuBar();
  JLabel statusBar = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  JTabbedPane jTabbedPaneMain = new JTabbedPane();
  JPanel jPanelAreas = new JPanel();
  JTable jTableAreas = new JTable();
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel jPanelMessages = new JPanel();
  JTable jTableMessages = new JTable();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel jPanelViewer = new JPanel();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  JLabel jLabelSubject = new JLabel();
  JLabel jLabelToName = new JLabel();
  JLabel jLabelFromName = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel1 = new JLabel();
  BorderLayout borderLayout4 = new BorderLayout();
  JTextArea jTextAreaMessage = new JTextArea();
  TitledBorder titledBorder1;
  JLabel jLabelPointFrom = new JLabel();
  JLabel jLabelPointTo = new JLabel();
  JLabel jLabelDateTime = new JLabel();
  JMenu jMenuJEdPoint = new JMenu();
  JMenuItem jMenuItemJEdPointExit = new JMenuItem();
  JMenu jMenuAreas = new JMenu();
  JMenu jMenuMessages = new JMenu();
  JMenuItem jMenuItemMessagesReply = new JMenuItem();
  JMenuItem jMenuItemMessagesDelete = new JMenuItem();
  JMenuItem jMenuItemMessagesNew = new JMenuItem();
  JMenuItem jMenuItemAreasAdd = new JMenuItem();
  JMenuItem jMenuItemAreasDelete = new JMenuItem();
  JMenuItem jMenuItemAreasEdit = new JMenuItem();
  JMenuItem jMenuItemAreasRefresh = new JMenuItem();
  JMenu jMenu1 = new JMenu();
  JMenuItem jMenuItemOptionsSaveColumnWidths = new JMenuItem();
  JMenuItem jMenuItemOptionSaveWindowPosition = new JMenuItem();
  JMenuItem jMenuItemMessagesReplyInOtherArea = new JMenuItem();
  JMenu jMenuImExport = new JMenu();
  JMenuItem jMenuItemToolsImport = new JMenuItem();
  JScrollPane jScrollPaneAreas = new JScrollPane(jTableAreas);
  JScrollBar jScrollBarMessagesVertical = new JScrollBar(JScrollBar.VERTICAL, 0, 0, 0, 100);
  JScrollPane jScrollPaneViewer = new JScrollPane(jTextAreaMessage);
  JLabel jLabelDateTimeImported = new JLabel();
  JCheckBoxMenuItem jCheckBoxMenuItemOptionsViewKludges = new JCheckBoxMenuItem();
  JMenuItem jMenuItemToolsExport = new JMenuItem();
  JMenuItem jMenuItemAreasPack = new JMenuItem();
  JMenuItem jMenuItemToolsPoll = new JMenuItem();
  JMenuItem jMenuItemMessagesEdit = new JMenuItem();
  JMenu jMenuHelp = new JMenu();
  JMenuItem jMenuItemHelpAbout = new JMenuItem();
  JMenuItem jMenuItemMessagesGo = new JMenuItem();
  JMenuItem jMenuItemAreasCatchUp = new JMenuItem();
  JMenuItem jMenuItemAreasMoveUp = new JMenuItem();
  JMenuItem jMenuItemAreasMoveDown = new JMenuItem();
  JMenuItem jMenuItemMessagesGoToLast = new JMenuItem();
  JMenuItem jMenuItemMessagesGoToFirst = new JMenuItem();
//  CustomTableCellRenderer cellRenderer = new CustomTableCellRenderer();

  // Our two models
  DefaultTableModel areaModel = new DefaultTableModel();
  DefaultTableModel messageModel = new DefaultTableModel();

  // Our private settings
  private module_ui_default_settings privateSettings;

  private Vector areaColumnTypes = new Vector();
  private Vector areaColumnNames = new Vector();
  private Vector areaColumnKeys = new Vector();
  private Vector messageColumnTypes = new Vector();
  private Vector messageColumnNames = new Vector();
  private Vector messageColumnKeys = new Vector();
  private String currentArea = "";

  // For the sake of our scrollbar
  private long currentMessage;// The message that is currently marked (or SHOULD be marked, as a lastread)
  private long maxMessages;   // Max # of msgs in the area
  long pageMinimum; // The lowest msg # on this "page"
  long pageMaximum; // The highest msg # on this "page"

  private Areas areaNames;
//  private Vector areaNames = new Vector();
  private boolean inViewer = false;
  private String settingsDir;
  private String dataDir;
  private static final String configFilename            = "module_ui_default.conf";
  private static final String directoryPrivateSettings  = "module_ui_Default";
  private static final String pathPrivateSettings       = directoryPrivateSettings + File.separator + "settings.private";
  private static final String pathAreas                 = directoryPrivateSettings + File.separator + "areas.private";
  private String settingUserName;
  private PointNumber settingUserPointnumber;
  private String settingAreafixName;
  private PointNumber settingAreafixPointnumber;
  private String settingAreafixPassword;

  // The accelerator keys for the menu
  private final int messagesEdit             = 0;
  private final int messagesNew              = 1;
  private final int messagesReply            = 2;
  private final int messagesReplyInOtherArea = 3;
  private final int messagesDelete           = 4;
  private final int messagesGo               = 5;
  private final int messagesGoToFirst        = 6;
  private final int messagesGoToLast         = 7;
  private int messagesKeys[] = {
    // Key                Modifier(s)
    KeyEvent.VK_E,        0,
    KeyEvent.VK_N,        0,
    KeyEvent.VK_R,        0,
    KeyEvent.VK_R,        KeyEvent.ALT_MASK,
    KeyEvent.VK_DELETE,   0,
    KeyEvent.VK_G,        0,
    KeyEvent.VK_LEFT,     KeyEvent.CTRL_MASK,
    KeyEvent.VK_RIGHT,    KeyEvent.CTRL_MASK,
    0,                    0
  };

  // User defined settings
  // ---------------------
  private boolean confirmQuit;
  private String defaultToUserName;
  private PointNumber defaultToPointNumber;
  private String defaultSubject;
  private String defaultAreaTearline;
  private String defaultAreaOrigin;
  private boolean importAutoStart;
  private boolean importAutoClose;
  private boolean exportAutoStart;
  private boolean exportAutoClose;

  private int autoNextUnreadOrder[] = {
    JEdPointMessage.mbListMessagesPersonalNewUnread,
    JEdPointMessage.mbListMessagesNewUnread,
    JEdPointMessage.mbListMessagesPersonalUnread,
    JEdPointMessage.mbListMessagesUnread
  };

  // Viewer Font
  private String viewerFontName;
  private int viewerFontSize = 15;
  private int viewerFontBold;
  private int viewerFontItalics;
  private Color viewerForeground;
  private Color viewerBackground;

  // AreaList Font & Colors
  private String areaListFontName;
  private int areaListFontSize = 15;
  private int areaListFontBold;
  private int areaListFontItalics;
  private Color areaListGridColor;
  private Color areaListSelectionForeground;
  private Color areaListSelectionBackground;
  private Color areaListForeground;
  private Color areaListBackground;

  // MessageList Font & Colors
  private String messageListFontName;
  private int messageListFontSize = 15;
  private int messageListFontBold;
  private int messageListFontItalics;
  private Color messageListGridColor;
  private Color messageListSelectionForeground;
  private Color messageListSelectionBackground;
  private Color messageListForeground;
  private Color messageListBackground;

  private boolean autoNextUnread;
  private String MainWindowTitle;
  JMenuItem jMenuItemAreasDisconnect = new JMenuItem();
  JMenuItem jMenuItemAreasSort = new JMenuItem();
  // ---------------------
  // User defined settings

  public UIFrame()
  {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    jbInit();
  }

  private void jbInit()
  {
    //setIconImage(Toolkit.getDefaultToolkit().createImage(UIFrame.class.getResource("[Your Icon]")));
    contentPane = (JPanel) this.getContentPane();
    titledBorder1 = new TitledBorder("");
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(640, 489));
    this.setTitle("JEdPoint");
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

    statusBar.setText("Status Bar");

    jPanelAreas.setLayout(borderLayout2);
    jPanelMessages.setLayout(borderLayout3);
    jPanelViewer.setLayout(null);

    jTableAreas.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        jTableAreas_keyPressed(e);
      }
    });
    jTableMessages.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        jTableMessages_keyPressed(e);
      }
    });
    jTableMessages.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        jTableMessages_mouseClicked(e);
      }
    });

    jTableMessages.enableInputMethods(false);
    jPanel1.setLayout(null);
    jLabelSubject.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelSubject.setText("000000000099999999990000000000999999999900000000009999999999000000000099");
    jLabelSubject.setBounds(new Rectangle(56, 48, 510, 17));
    jLabelToName.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelToName.setText("000000000099999999990000000000999999");
    jLabelToName.setBounds(new Rectangle(56, 28, 257, 17));
    jLabelFromName.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelFromName.setText("000000000099999999990000000000999999");
    jLabelFromName.setBounds(new Rectangle(56, 9, 257, 17));
    jLabel3.setText("Subject");
    jLabel3.setBounds(new Rectangle(5, 49, 47, 17));
    jLabel2.setText("To");
    jLabel2.setBounds(new Rectangle(5, 28, 47, 17));
    jLabel1.setText("From");
    jLabel1.setBounds(new Rectangle(5, 9, 47, 17));
    jPanel2.setLayout(borderLayout4);
    jTextAreaMessage.setLineWrap(true);
    jTextAreaMessage.setWrapStyleWord(true);
    jTextAreaMessage.setFont(new java.awt.Font("Monospaced", 0, 15));
    jTextAreaMessage.setBorder(BorderFactory.createLineBorder(Color.black));
    jTextAreaMessage.setEditable(false);
    jTextAreaMessage.setText("jEditorPane1");
//    jTextAreaMessage.setContentType("text/html");     // Later, my pretties. Later.
    jTextAreaMessage.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        jTextAreaMessage_keyPressed(e);
      }
    });
    jLabelPointFrom.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelPointFrom.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelPointFrom.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabelPointFrom.setText("666:666/666.666");
    jLabelPointFrom.setBounds(new Rectangle(319, 9, 97, 17));
    jLabelPointTo.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelPointTo.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelPointTo.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabelPointTo.setText("jLabel5");
    jLabelPointTo.setBounds(new Rectangle(319, 28, 97, 17));
    jLabelDateTime.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelDateTime.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelDateTime.setText("jLabel4");
    jLabelDateTime.setBounds(new Rectangle(422, 9, 144, 17));
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.setJMenuBar(jMenuBarMain);

    jMenuJEdPoint.setText("JEdPoint");
    jMenuJEdPoint.setMnemonic('J');
    jMenuItemJEdPointExit.setText("Exit");
    jMenuItemJEdPointExit.setMnemonic('X');
    jMenuItemJEdPointExit.setAccelerator( KeyStroke.getKeyStroke('X', KeyEvent.CTRL_MASK) );
    jMenuItemJEdPointExit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemJEdPointExit_actionPerformed(e);
      }
    });
    jMenuAreas.setText("Areas");
    jMenuAreas.setMnemonic(KeyEvent.VK_A);
    jMenuMessages.setText("Messages");
    jMenuMessages.setMnemonic(KeyEvent.VK_M);
    jMenuItemMessagesReply.setMnemonic('R');
    jMenuItemMessagesReply.setText("Reply");
    jMenuItemMessagesReply.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesReply_actionPerformed(e);
      }
    });
    jMenuItemMessagesDelete.setMnemonic('D');
    jMenuItemMessagesDelete.setText("Delete");
    jMenuItemMessagesDelete.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesDelete_actionPerformed(e);
      }
    });
    jMenuItemMessagesNew.setMnemonic('N');
    jMenuItemMessagesNew.setText("New");
    jMenuItemMessagesNew.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesNew_actionPerformed(e);
      }
    });
    jMenuItemAreasAdd.setMnemonic('A');
    jMenuItemAreasAdd.setText("Add");
    jMenuItemAreasAdd.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasAdd_actionPerformed(e);
      }
    });
    jMenuItemAreasDelete.setMnemonic('D');
    jMenuItemAreasDelete.setText("Delete");
    jMenuItemAreasDelete.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasDelete_actionPerformed(e);
      }
    });
    jMenuItemAreasEdit.setMnemonic('E');
    jMenuItemAreasEdit.setText("Edit");
    jMenuItemAreasEdit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasEdit_actionPerformed(e);
      }
    });

    jPanel1.setBounds(new Rectangle(0, 0, 574, 75));
    jPanel2.setBounds(new Rectangle(0, 75, 100, 100));

    jPanelViewer.addComponentListener(new java.awt.event.ComponentAdapter()
    {
      public void componentResized(ComponentEvent e)
      {
        jPanelViewer_componentResized(e);
      }
    });
    jMenuItemAreasRefresh.setMnemonic('R');
    jMenuItemAreasRefresh.setText("Refresh");
    jMenuItemAreasRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, true));
    jMenuItemAreasRefresh.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasRefresh_actionPerformed(e);
      }
    });
    jMenu1.setMnemonic('O');
    jMenu1.setText("Options");
    jMenuItemOptionsSaveColumnWidths.setMnemonic('S');
    jMenuItemOptionsSaveColumnWidths.setText("Save Column Widths");
    jMenuItemOptionsSaveColumnWidths.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemOptionsSaveColumnWidths_actionPerformed(e);
      }
    });
    jMenuItemOptionSaveWindowPosition.setText("Save Window Position");
    jMenuItemOptionSaveWindowPosition.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemOptionSaveWindowPosition_actionPerformed(e);
      }
    });
    jTableAreas.addComponentListener(new java.awt.event.ComponentAdapter()
    {
      public void componentResized(ComponentEvent e)
      {
        jTableAreas_componentResized(e);
      }
    });
    jTableMessages.addComponentListener(new java.awt.event.ComponentAdapter()
    {
      public void componentResized(ComponentEvent e)
      {
        jTableMessages_componentResized(e);
      }
    });
    jMenuItemMessagesReplyInOtherArea.setMnemonic('O');
    jMenuItemMessagesReplyInOtherArea.setText("Reply in other area");
    jMenuItemMessagesReplyInOtherArea.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesReplyInOtherArea_actionPerformed(e);
      }
    });
    jMenuImExport.setMnemonic('T');
    jMenuImExport.setText("Tools");
    jMenuItemToolsImport.setMnemonic('I');
    jMenuItemToolsImport.setText("Import");
    jMenuItemToolsImport.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK) );
    jMenuItemToolsImport.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemToolsImport_actionPerformed(e);
      }
    });
    jLabelDateTimeImported.setBorder(BorderFactory.createLineBorder(Color.black));
    jLabelDateTimeImported.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelDateTimeImported.setText("jLabel4");
    jLabelDateTimeImported.setBounds(new Rectangle(422, 28, 144, 17));
    jCheckBoxMenuItemOptionsViewKludges.setMnemonic('K');
    jCheckBoxMenuItemOptionsViewKludges.setSelected(false);
    jCheckBoxMenuItemOptionsViewKludges.setText("View Kludges");
    jCheckBoxMenuItemOptionsViewKludges.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jCheckBoxMenuItemOptionsViewKludges_actionPerformed(e);
      }
    });
    jMenuItemToolsExport.setMnemonic('E');
    jMenuItemToolsExport.setText("Export");
    jMenuItemToolsExport.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK) );
    jMenuItemToolsExport.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemToolsExport_actionPerformed(e);
      }
    });
    jMenuItemAreasPack.setMnemonic('P');
    jMenuItemAreasPack.setText("Pack");
    jMenuItemAreasPack.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasPack_actionPerformed(e);
      }
    });
    jMenuItemToolsPoll.setMnemonic('P');
    jMenuItemToolsPoll.setText("Poll");
    jMenuItemToolsPoll.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_P, KeyEvent.CTRL_MASK ));
    jMenuItemToolsPoll.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemToolsPoll_actionPerformed(e);
      }
    });
    jMenuItemMessagesEdit.setMnemonic('E');
    jMenuItemMessagesEdit.setText("Edit");
    jMenuItemMessagesEdit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesEdit_actionPerformed(e);
      }
    });
    jMenuHelp.setMnemonic('H');
    jMenuHelp.setText("Help");
    jMenuItemHelpAbout.setMnemonic('A');
    jMenuItemHelpAbout.setText("About");
    jMenuItemHelpAbout.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemHelpAbout_actionPerformed(e);
      }
    });
    jTableAreas.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        jTableAreas_mouseClicked(e);
      }
    });
    jMenuItemMessagesGo.setMnemonic('G');
    jMenuItemMessagesGo.setText("Go to message #");
    jMenuItemMessagesGo.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesGo_actionPerformed(e);
      }
    });
    jMenuItemAreasCatchUp.setMnemonic('C');
    jMenuItemAreasCatchUp.setText("Catch Up");
    jMenuItemAreasCatchUp.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasCatchUp_actionPerformed(e);
      }
    });
    jMenuItemAreasMoveUp.setText("Move Up");
    jMenuItemAreasMoveUp.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_MASK));
    jMenuItemAreasMoveUp.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasMoveUp_actionPerformed(e);
      }
    });
    jMenuItemAreasMoveDown.setText("Move Down");
    jMenuItemAreasMoveDown.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_MASK));
    jMenuItemAreasMoveDown.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasMoveDown_actionPerformed(e);
      }
    });
    jMenuItemMessagesGoToLast.setText("Go to last");
    jMenuItemMessagesGoToLast.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesGoToLast_actionPerformed(e);
      }
    });
    jMenuItemMessagesGoToFirst.setText("Go to first");
    jMenuItemMessagesGoToFirst.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemMessagesGoToFirst_actionPerformed(e);
      }
    });
    jMenuItemAreasDisconnect.setToolTipText("Disconnect via AreaFix");
    jMenuItemAreasDisconnect.setText("Disconnect");
    jMenuItemAreasDisconnect.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasDisconnect_actionPerformed(e);
      }
    });
    jMenuItemAreasSort.setMnemonic('S');
    jMenuItemAreasSort.setText("Sort");
    jMenuItemAreasSort.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItemAreasSort_actionPerformed(e);
      }
    });
    contentPane.add(statusBar, BorderLayout.SOUTH);
    contentPane.add(jTabbedPaneMain, BorderLayout.CENTER);

    jTabbedPaneMain.add(jPanelAreas,  "Areas");

    // Add the ScrollPane (and together with it, the JList)
    jPanelAreas.add(jScrollPaneAreas);
    jPanel1.add(jLabelSubject, null);
    jPanel1.add(jLabelToName, null);
    jPanel1.add(jLabelFromName, null);
    jPanel1.add(jLabel3, null);
    jPanel1.add(jLabel2, null);
    jPanel1.add(jLabel1, null);
    jPanel1.add(jLabelPointFrom, null);
    jPanel1.add(jLabelPointTo, null);
    jPanel1.add(jLabelDateTime, null);
    jPanel1.add(jLabelDateTimeImported, null);
    jPanelViewer.add(jPanel1);
    jPanelViewer.add(jPanel2);
    jPanel2.add(jScrollPaneViewer, BorderLayout.CENTER);
    jPanelMessages.add( jTableMessages.getTableHeader(), BorderLayout.NORTH );
    jPanelMessages.add( jTableMessages, BorderLayout.CENTER );
    jPanelMessages.add( jScrollBarMessagesVertical, BorderLayout.EAST );
    jMenuBarMain.add(jMenuJEdPoint);
    jMenuBarMain.add(jMenuAreas);
    jMenuBarMain.add(jMenuMessages);
    jMenuBarMain.add(jMenuImExport);
    jMenuBarMain.add(jMenu1);
    jMenuBarMain.add(jMenuHelp);
    jMenuJEdPoint.add(jMenuItemJEdPointExit);

    // Are those dimensions too big?
    jTableAreas.setPreferredScrollableViewportSize( new Dimension(jPanelAreas.getWidth(), jPanelAreas.getHeight()) );
    jTableMessages.setPreferredScrollableViewportSize( new Dimension(jPanelMessages.getWidth(), jPanelMessages.getHeight()) );

    jTableAreas.setOpaque(false);
    jTableMessages.setOpaque(false);

    jTableAreas.setModel(areaModel);
    jTableMessages.setModel(messageModel);

    jMenuMessages.add(jMenuItemMessagesEdit);
    jMenuMessages.add(jMenuItemMessagesNew);
    jMenuMessages.add(jMenuItemMessagesReply);
    jMenuMessages.add(jMenuItemMessagesReplyInOtherArea);
    jMenuMessages.addSeparator();
    jMenuMessages.add(jMenuItemMessagesGo);
    jMenuMessages.add(jMenuItemMessagesGoToFirst);
    jMenuMessages.add(jMenuItemMessagesGoToLast);
    jMenuMessages.add(jMenuItemMessagesDelete);
    jMenuAreas.add(jMenuItemAreasAdd);
    jMenuAreas.add(jMenuItemAreasDelete);
    jMenuAreas.add(jMenuItemAreasEdit);
    jMenuAreas.add(jMenuItemAreasPack);
    jMenuAreas.addSeparator();
    jMenuAreas.add(jMenuItemAreasDisconnect);
    jMenuAreas.add(jMenuItemAreasCatchUp);
    jMenuAreas.addSeparator();
    jMenuAreas.add(jMenuItemAreasMoveUp);
    jMenuAreas.add(jMenuItemAreasMoveDown);
    jMenuAreas.add(jMenuItemAreasSort);
    jMenuAreas.add(jMenuItemAreasRefresh);
    jMenu1.add(jCheckBoxMenuItemOptionsViewKludges);
    jMenu1.addSeparator();
    jMenu1.add(jMenuItemOptionsSaveColumnWidths);
    jMenu1.add(jMenuItemOptionSaveWindowPosition);
    jMenuImExport.add(jMenuItemToolsImport);
    jMenuImExport.add(jMenuItemToolsExport);
    jMenuImExport.add(jMenuItemToolsPoll);
    jMenuHelp.add(jMenuItemHelpAbout);

    // Remove the border around the selected cell
    UIManager.put( "Table.focusCellHighlightBorder", new javax.swing.plaf.BorderUIResource.BevelBorderUIResource(2) );

    setMenuEnabled();
  }

  private void setMenuMessageAccelerators(boolean active)
  {
    int counter;
    KeyStroke keystrokes[] = new KeyStroke[ messagesKeys.length/2 ];

    if (active)
      for (counter=0; counter<keystrokes.length; counter++)
        keystrokes[counter] = KeyStroke.getKeyStroke(messagesKeys[counter*2], messagesKeys[counter*2+1] | 0, true);
    else
      for (counter=0; counter<keystrokes.length; counter++)
        keystrokes[counter] = null;

    jMenuItemMessagesEdit.setAccelerator( keystrokes[messagesEdit] );
    jMenuItemMessagesNew.setAccelerator( keystrokes[messagesNew] );
    jMenuItemMessagesReply.setAccelerator( keystrokes[messagesReply] );
    jMenuItemMessagesReplyInOtherArea.setAccelerator( keystrokes[messagesReplyInOtherArea] );
    jMenuItemMessagesDelete.setAccelerator( keystrokes[messagesDelete] );
    jMenuItemMessagesGo.setAccelerator( keystrokes[messagesGo] );
    jMenuItemMessagesGoToFirst.setAccelerator( keystrokes[messagesGoToFirst] );
    jMenuItemMessagesGoToLast.setAccelerator( keystrokes[messagesGoToLast] );
  }

  private void setMenuEnabled()
  {
    boolean menuJEdPoint = true;
    boolean menuAreas = true;
    boolean menuMessages = true;

    switch (jTabbedPaneMain.getTabCount())
    {
      case 0:
      break;
      case 1:
        // Only areas tab is visible
        // Messages should be disabled
        menuMessages = false;
      break;
      case 2:
        // Messages is now visible
        menuAreas = false;
      break;
      case 3:
        // We're in the message viewer
        menuAreas = false;
      break;
    }

    jMenuJEdPoint.setEnabled(menuJEdPoint);
    jMenuAreas.setEnabled(menuAreas);
    jMenuMessages.setEnabled(menuMessages);

    // Set the key accelerators
    setMenuMessageAccelerators(menuMessages);
  }

  private void loadSettings() throws JEdPointException
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Properties props = new Properties();
    String tempString;
    StringTokenizer st;
    PointNumber tempPointNumber;
    int counter;
    HashMap tempHashMap;
    String fullConfigFilename;    // The full (reltive) path  + filename of the config file

    JPM = jpm.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, new JEdPointMessage());
    settingsDir = (String)JPM.getResponse("settingsdir");
    dataDir = (String)JPM.getResponse("datadir");

    fullConfigFilename = settingsDir + File.separator + configFilename;

    defaultAreaTearline = JPM.getResponseString("defaultareatearline");
    defaultAreaOrigin = JPM.getResponseString("defaultareaorigin");
    settingUserName = JPM.getResponseString("username");
    settingUserPointnumber = (PointNumber)JPM.getResponse("pointnumber");
    settingAreafixName = JPM.getResponseString("areafixname");
    settingAreafixPointnumber = (PointNumber)JPM.getResponse("areafixpointnumber");
    settingAreafixPassword = JPM.getResponseString("areafixpassword");

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

    // Create our special directory in %datadir%, if necessary.
    JEdPointUtilities.checkDirectory( dataDir + File.separator + pathPrivateSettings.substring(0, pathPrivateSettings.indexOf(File.separator)) );

    // Load the areaNames from disk
    areaNames = (Areas)JEdPointUtilities.loadObject( dataDir + File.separator + pathAreas );
    if (areaNames == null)
      areaNames = new Areas();

    // Try to load our saved settings from disk
    tempHashMap = (HashMap)JEdPointUtilities.loadObject( dataDir + File.separator + pathPrivateSettings);
    if (tempHashMap == null)
      privateSettings = new module_ui_default_settings();
    else
    {
      privateSettings = new module_ui_default_settings();
      privateSettings.loadSettings(tempHashMap);
    }

    // Set the main window to the privateSettings size, unless the saved size is brand new
    if ( new Dimension().hashCode() != privateSettings.MainWindowDimension.hashCode() )
      this.setSize( privateSettings.MainWindowDimension );

    // Load the column types for the area browser
    st = new StringTokenizer( props.getProperty("areabrowsercolumntypes") );
    while (st.hasMoreTokens())
    {
      areaColumnTypes.add(st.nextToken());
    }

    // Load the column names for the area browser
    st = new StringTokenizer( props.getProperty("areabrowsercolumnnames"), "\"", false );
    while (st.hasMoreTokens())
    {
      areaColumnNames.add( st.nextToken() );
    }

    areaModel.initColumns( areaColumnTypes.size() );
    areaModel.setColumnNames( areaColumnNames.toArray() );
    areaModel.setColumnTypes( areaColumnTypes.toArray() );
    jTableAreas.createDefaultColumnsFromModel();
    setAreaModelData();

    // Load the column types for the message browser
    FidonetMessage fm = new FidonetMessage();
    fm.setMessageData( "index", new Long(0) );    // Index isn't included in FidonetMessage
                                                  // It's something we handle ourselves.
    st = new StringTokenizer( props.getProperty("messagebrowsercolumntypes") );
    while (st.hasMoreTokens())
    {
      messageColumnKeys.add( st.nextToken() );
      messageColumnTypes.add( fm.getMessageData( messageColumnKeys.lastElement().toString() ) );
    }

    // Load the column names for the message browser
    st = new StringTokenizer( props.getProperty("messagebrowsercolumnnames"), "\"", false );
    while (st.hasMoreTokens())
    {
      messageColumnNames.add( st.nextToken() );
    }

    // We have the column data. Put it in the message model
    messageModel.initColumns( messageColumnNames.size() );
    messageModel.setColumnNames( messageColumnNames.toArray() );
    messageModel.setColumnTypes( messageColumnTypes.toArray() );

    jTableMessages.createDefaultColumnsFromModel();
/*
    // Set the cell renderer for the columns
    for (counter=0; counter<jTableMessages.getColumnCount(); counter++)
      if ((messageModel.getColumnClass(counter)) != new Boolean(true).getClass())
        jTableMessages.getColumn( messageModel.getColumnName(counter) ).setCellRenderer( cellRenderer );
*/
    // Confirmations?
    if (props.getProperty("confirmquit", "true").toLowerCase().compareTo("false") == 0)
      confirmQuit = false;
    else
      confirmQuit = true;

    // Go to the next unread message when the end of the area is reached
    if (props.getProperty("autonextunread", "false").toLowerCase().compareTo("false") == 0)
      autoNextUnread = false;
    else
      autoNextUnread = true;

    // Autostart the importer when it is invoked?
    if (props.getProperty("importautostart", "false").toLowerCase().compareTo("false") == 0)
      importAutoStart = false;
    else
      importAutoStart = true;

    // Autoclose the importer when it is done importing all the messages?
    if (props.getProperty("importautoclose", "false").toLowerCase().compareTo("false") == 0)
      importAutoClose = false;
    else
      importAutoClose = true;

    // Autostart the exporter when it is invoked?
    if (props.getProperty("exportautostart", "false").toLowerCase().compareTo("false") == 0)
      exportAutoStart = false;
    else
      exportAutoStart = true;

    // Autoclose the exporter when it is done exporting all the messages?
    if (props.getProperty("exportautoclose", "false").toLowerCase().compareTo("false") == 0)
      exportAutoClose = false;
    else
      exportAutoClose = true;

    // Default user
    if (props.getProperty("defaultuser", "").length() == 0)
      defaultToUserName = "All";
    else
      defaultToUserName = props.getProperty("defaultuser");

    // Default Point
    tempPointNumber = new PointNumber(0,0,0,0);
    if (props.getProperty("defaultpoint", "").length() != 0)
      tempPointNumber = PointNumber.parsePointNumber( props.getProperty("defaultpoint") );
    defaultToPointNumber = tempPointNumber;

    // Default Subject
    if (props.getProperty("defaultsubject", "").length() == 0)
      defaultSubject = "";
    else
      defaultSubject = props.getProperty("defaultsubject");

    // Load the colors for the different components
    loadFontAndColors(props, jTableAreas, "arealist", areaListFontName, areaListFontSize, areaListFontBold, areaListFontItalics, areaListForeground, areaListBackground);
    loadFontAndColors(props, jTableMessages, "messagelist", messageListFontName, messageListFontSize, messageListFontBold, messageListFontItalics, messageListForeground, messageListBackground);
    loadFontAndColors(props, jTextAreaMessage, "viewer", viewerFontName, viewerFontSize, viewerFontBold, viewerFontItalics, viewerForeground, viewerBackground);

    // Load the grid colors
    // First of the area list
    areaListGridColor = JEdPointUtilities.parseColor( props.getProperty("arealistgridcolor", "0 0 0") );
    if (areaListGridColor != null)
      jTableAreas.setGridColor( areaListGridColor );
    // And now the message list
    messageListGridColor = JEdPointUtilities.parseColor( props.getProperty("messagelistgridcolor", "0 0 0") );
    if (messageListGridColor != null)
      jTableMessages.setGridColor( messageListGridColor );

    // Load the selection foreground and background colors
    // First of the area list
    // Background
    areaListSelectionForeground = JEdPointUtilities.parseColor( props.getProperty("arealistselectiontextcolor", "255 255 255") );
    if (areaListSelectionForeground != null)
      jTableAreas.setSelectionForeground( areaListSelectionForeground );
    // Background
    areaListSelectionBackground = JEdPointUtilities.parseColor( props.getProperty("arealistselectionbackgroundcolor", "0 100 200") );
    if (areaListSelectionBackground != null)
      jTableAreas.setSelectionBackground( areaListSelectionBackground );

    // And now the message list
    // Foreground
    messageListSelectionForeground = JEdPointUtilities.parseColor( props.getProperty("messagelistselectiontextcolor", "255 255 255") );
    if (messageListSelectionForeground != null)
      jTableMessages.setSelectionForeground( messageListSelectionForeground );
    // Background
    messageListSelectionBackground = JEdPointUtilities.parseColor( props.getProperty("messagelistselectionbackgroundcolor", "0 100 200") );
    if (messageListSelectionBackground != null)
      jTableMessages.setSelectionBackground( messageListSelectionBackground );

    // Main Window Title
    setMainWindowTitle( props.getProperty("mainwindowtitle", "JEdPoint") );

    // Set the "View Kludges" checkbox in the menu
    jCheckBoxMenuItemOptionsViewKludges.setState( privateSettings.viewKludges );
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

  private void setMainWindowTitle(String mainWindowTitle)
  {
    this.MainWindowTitle = mainWindowTitle;
    MainFrame.setTitle( MainWindowTitle );
  }

  private void setAreaTableModelData()
  {
    int counter;
    MessageAreaData mad;
    JEdPointMessage JPM = new JEdPointMessage();
    int columnCounter;

    for (counter=0; counter<areaNames.size(); counter++)
    {
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", (String)areaNames.elementAt(counter) );
      mad = (MessageAreaData)JPM.getResponse("messageareadata");
      for (columnCounter=0; columnCounter<areaColumnTypes.size(); columnCounter++)
      {
        areaModel.setValueAt( mad.getMessageAreaData( areaColumnTypes.elementAt(columnCounter).toString() ), counter, columnCounter);
      }
    }
  }

  // Fills the model with Area information
  private void setAreaModelData()
  {
    JEdPointMessage JPM = new JEdPointMessage();
    int lastSelected = Math.max(jTableAreas.getSelectedRow(), 0);

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbListAreas, new JEdPointMessage());
    areaNames.setVector( (Vector)JPM.getResponse("areas") );

    areaModel.initRows(areaNames.size());

    setAreaTableModelData();

    if ( jTableAreas.getRowCount() != 0)
      if (lastSelected < jTableAreas.getRowCount())
        jTableAreas.setRowSelectionInterval(lastSelected, lastSelected);
      else
        jTableAreas.setRowSelectionInterval( jTableAreas.getRowCount()-1, jTableAreas.getRowCount()-1);

    jTableAreas.updateUI();

    privateSettings.restoreWidths( jTableAreas.getColumnModel(), privateSettings.AreaColumnWidths );

    jTableAreas.requestFocus();
  }

  // MESSAGE MODEL -------------------------------------------------------------
  // Fills the model with Message information from the specified message area
  private void setMessageModelData(String areaID)
  {
    JEdPointMessage JPM = new JEdPointMessage();
    MessageAreaData mad;
    Long tempLong;

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", areaID );
    mad = (MessageAreaData)JPM.getResponse("messageareadata");

    tempLong = (Long)mad.getMessageAreaData("totalmessages");

    maxMessages = tempLong.longValue();

    // Get the last read
    tempLong = (Long)mad.getMessageAreaData("lastread");
    currentMessage = tempLong.longValue();

    scrollMessageScrollBar();
  }

  private void scrollMessageScrollBar()
  {
    int lastSelected = Math.max(jTableMessages.getSelectedRow(), 0);
    long counter;
    int screenLines;
    int columnCounter;
    FidonetMessage fm;
    JEdPointMessage JPM = new JEdPointMessage();
    Integer tempInteger;
    Vector tempVector;

    // If the amount of messages == 0, don't bother...
    if (maxMessages != 0)
    {
      if (currentMessage == -1)
        currentMessage = 1;

      messageModel.initRows(1);

      // Get the screenLines (table rows visible)
      screenLines = (int) (jTableMessages.getHeight() / jTableMessages.getCellRect(0, 0, true).getHeight());

      // Either the table isn't visible or ... umm... it isn't visible.
      if (screenLines == 0) return;

      currentMessage--;
      pageMinimum = (currentMessage / screenLines) * screenLines +1 ;
      pageMaximum = ((currentMessage / screenLines)+1) * screenLines;
      currentMessage++;

      // Check that pageMax isn't out of range
      pageMaximum = Math.min(maxMessages, pageMaximum);

      messageModel.initRows( (int)((pageMaximum+1)-pageMinimum) );

      // Get all the headers we need from the message base
      JPM.setRequest("id", currentArea);
      JPM.setRequest("rangestart", pageMinimum );
      JPM.setRequest("rangestop", pageMaximum );
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetMessageHeaders, JPM);
      tempVector = (Vector)JPM.getResponse("messagevector");

      for (counter=pageMinimum; counter<=pageMaximum; counter++)
      {
        fm = (FidonetMessage)tempVector.elementAt((int)(counter-pageMinimum));
        fm.setMessageData("index", (long)(counter) );

        for (columnCounter=0; columnCounter<messageColumnTypes.size(); columnCounter++)
          messageModel.setValueAt( fm.getMessageData( messageColumnKeys.elementAt(columnCounter).toString() ), (int)(counter-pageMinimum), columnCounter);
      } // FOR

      // Show the data
      jTableMessages.requestFocus();
      jTableMessages.updateUI();

      // And resize the columns
      privateSettings.restoreWidths(jTableMessages.getColumnModel(), privateSettings.MessageColumnWidths);

      // Select the current message
      jTableMessages.setRowSelectionInterval( (int)(currentMessage-pageMinimum), (int)(currentMessage-pageMinimum) );

      // Fix the scrollbar's value
      // We need the following values: Number of pages, Page we are on
      jScrollBarMessagesVertical.setValues((int)((currentMessage-1) / screenLines), 1, 0, (int)(maxMessages/screenLines)+1);
    } // IF MaxMessages != 0
    else
    {
      messageModel.initRows(0);
      jTableMessages.updateUI();
    }
  }

  // VIEWER DATA ---------------------------------------------------------------
  private void setViewerData(String areaID, long messageNumber)
  {
    FidonetMessage fm;
    JEdPointMessage JPM = new JEdPointMessage();
    GregorianCalendar tempCalendar;
    String tearline, origin;
    String tempString;
    GregorianCalendar gCal;
    Vector tempVector;
    int counter;

    if (!inViewer) return;

    // Retrieve a FidonetMessage
    JPM.setRequest("id", areaID);
    JPM.setRequest("index", new Long(messageNumber) );
    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbReadMessage, JPM);

    // If the messagebase encountered an error trying to read the message, it SHOULD have informed
    // the user. And, well, since we can't do anything... we'll just forget about it.
    if (JPM == null) return;

    fm = (FidonetMessage)JPM.getResponse("message");

    // Set the text for the tabbedpane
    jTabbedPaneMain.setTitleAt(2, String.valueOf(messageNumber) + " / " + maxMessages);

    // Set the labels and stuff
    jLabelFromName.setText( (String)fm.getMessageData("usernamefrom") );
    jLabelToName.setText( (String)fm.getMessageData("usernameto") );

    jLabelPointFrom.setText( fm.getMessageData("pointfrom").toString() );
    jLabelPointTo.setText( fm.getMessageData("pointto").toString() );

    jLabelSubject.setText( (String)fm.getMessageData("subject") );

    // Set the time + date
    gCal  = (GregorianCalendar)fm.getMessageData("datetime");
    jLabelDateTime.setText( JEdPointUtilities.GregorianCalendarToString(gCal) );

    // Set the time+date imported
    gCal  = (GregorianCalendar)fm.getMessageData("datetimeimported");
    jLabelDateTimeImported.setText( JEdPointUtilities.GregorianCalendarToString(gCal) );

    JPM = new JEdPointMessage();
    JPM.setRequest( "origin", fm.getMessageData("origin") );
    JPM.setRequest( "point", fm.getMessageData("pointfrom") );
    JPM = jpm.sendMessage(JEdPointModule.moduleOrigin, JEdPointMessage.originAssemble, JPM);
    origin = (String)JPM.getResponse("origin");

    JPM = jpm.sendMessage(JEdPointModule.moduleTearline, JEdPointMessage.tlAssemble, "tearline", fm.getMessageData("tearline") );
    tearline = (String)JPM.getResponse("tearline");

    tempString = "";

    // Add the prekludges
    if (privateSettings.viewKludges)
    {
      tempVector = (Vector)fm.getMessageData("prekludges");
      for (counter=0; counter<tempVector.size(); counter++)
        tempString = tempString + (String)tempVector.elementAt(counter) + "\n";
    }

    // Add the message body (yes, this is kinda like the most important part ...)
    // And stuff.
    tempString = tempString + (String)fm.getMessageData("message");

    if (!tempString.endsWith("\n"))
    {
      // The string doesn't end with a \n, add one for the tearline + origin's saké
      tempString = tempString + "\n";
    }

    // If it's the netmail area, we shouldn't have an origin.
    if ( currentArea.compareToIgnoreCase("NETMAIL") == 0)
      tempString = tempString + tearline + "\n";
    else
      tempString = tempString + tearline + "\n" + origin + "\n";

    // Add the postkludges
    if (privateSettings.viewKludges)
    {
      tempVector = (Vector)fm.getMessageData("postkludges");
      for (counter=0; counter<tempVector.size(); counter++)
        tempString = tempString + (String)tempVector.elementAt(counter) + "\n";
    }

    jTextAreaMessage.setText( tempString );

    jTextAreaMessage.setCaretPosition(0);

    jTextAreaMessage.requestFocus();
  }

  // Rolls the current selection in the jtable up one row
  // If wrap=true, then it will go to position max once the beginning of the table is reached
  private void scrollBackward(JTable jtable, boolean wrap)
  {
    int newRow = jtable.getSelectedRow() - 1;

    if ( newRow == -1 )
    {
      if (wrap)
        newRow = jtable.getRowCount()-1;
      else
        newRow++;
    }
    jtable.setRowSelectionInterval( newRow,  newRow );
  }

  // Rolls the current selection in the jtable down one row
  // If wrap=true, then it will go to position 1 once the end of the table is reached
  private void scrollForward(JTable jtable, boolean wrap)
  {
    int newRow = jtable.getSelectedRow() +1;

    if ( newRow == jtable.getRowCount() )
    {
      if (wrap)
        newRow = 0;
      else
        newRow--;
    }
    jtable.setRowSelectionInterval( newRow,  newRow );
  }

  private MessageAreaData editMessageAreaData(String dialogTitle, MessageAreaData mad)
  {
    JDialog jdialog = new JDialog();
    jdialog.setTitle(dialogTitle);
    jdialog.setModal(true);
    PanelAreaEdit jpanel = new PanelAreaEdit(jdialog, mad);

    jdialog.setSize( jpanel.getSize());
    jdialog.getContentPane().add( jpanel );
    jdialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    jdialog.setResizable(false);

    jdialog.show();

    mad = jpanel.retrieveInfo();

    return mad;
  }

  // Pulls up the PanelAreasEdit dialog, to edit several areas at once
  private MessageAreaData editMessageAreasData(String dialogTitle)
  {
    MessageAreaData mad;
    JDialog jdialog = new JDialog();
    jdialog.setTitle(dialogTitle);
    jdialog.setModal(true);
    PanelAreasEdit jpanel = new PanelAreasEdit(jdialog);

    jdialog.setSize( jpanel.getSize());
    jdialog.getContentPane().add( jpanel );
    jdialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    jdialog.setResizable(false);

    jdialog.show();

    mad = jpanel.retrieveInfo();

    return mad;
  }

  private boolean doReadOnlyCheck(MessageAreaData mad)
  {
    if (mad.getMessageAreaDataBoolean("readonly"))
    {
      // The user isn't allowed to write here. Tell him and then return false
      JOptionPane.showConfirmDialog(this, "This area is read only!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
    return true;
  }

  /**
   * Checks to see if an area exists in the message base.
   */
  private boolean doesAreaExist( String areaname )
  {
    JEdPointMessage JPM = new JEdPointMessage();
    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", areaname);
    if (JPM.getResponse("messageareadata") != null)
      return true;
    else
      return false;
  }

  public void shutDown()
  {
    // Save our arealist
    JEdPointUtilities.saveObject(dataDir + File.separator + pathAreas, areaNames);
    dispose();
  }

  private void quitJEdPoint()
  {
    if (this.confirmQuit)
      if (JOptionPane.showConfirmDialog(this, "Really quit?", "JEdPoint", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        shutDown();
      else;
    else
      shutDown();
  }

  private void resizeViewerPanels()
  {
    jPanel2.setBounds(new Rectangle(0, 75, jPanelAreas.getWidth(), jPanelAreas.getHeight()-75));
  }

  public void initialize(JEdPointModule newJPM) throws JEdPointException
  {
    jpm = newJPM;

    loadSettings();
  }

  // Just a simple method that remaps doubleclicks to VK_ENTER at the keyPressed handler
  void jTableAreas_mouseClicked(MouseEvent e)
  {
    if ( e.getClickCount() == 2)
    {
      // Mouse doubleclicked
      jTableAreas_keyPressed( new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), 0, KeyEvent.VK_ENTER) );
    }
  }

  void jTableAreas_keyPressed(KeyEvent e)
  {
    int keyCode = e.getKeyCode();
    JEdPointMessage JPM;
    MessageAreaData mad;

    switch (keyCode)
    {
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_RIGHT:
        // We don't have a border around the selected cell, so ignore the
        // keypress.
        e.consume();
      break;

      // Navigation
      case KeyEvent.VK_HOME:
        if ( jTableAreas.getRowCount() != 0 )
          jTableAreas.setRowSelectionInterval( 0, 0 );
      break;
      case KeyEvent.VK_END:
        if ( jTableAreas.getRowCount() != 0 )
          jTableAreas.setRowSelectionInterval( jTableAreas.getRowCount()-1, jTableAreas.getRowCount()-1 );
      break;
      case KeyEvent.VK_ENTER:
        e.consume();
        jTabbedPaneMain.remove(jPanelMessages);
        jTabbedPaneMain.remove(jPanelViewer);
        currentArea = (String)areaNames.elementAt( jTableAreas.getSelectedRow() );
        jTabbedPaneMain.add(jPanelMessages, currentArea);
        jTabbedPaneMain.setSelectedIndex(1);
        setMessageModelData( (String)areaNames.elementAt( jTableAreas.getSelectedRow() ) );
        scrollMessageScrollBar();
        setMenuEnabled();
      break;
      case KeyEvent.VK_ESCAPE:
        quitJEdPoint();
      break;
    }
  }

  // Just a simple method that remaps doubleclicks to VK_ENTER at the keyPressed handler
  void jTableMessages_mouseClicked(MouseEvent e)
  {
    if ( e.getClickCount() == 2)
    {
      // Mouse doubleclicked
      jTableMessages_keyPressed( new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), 0, KeyEvent.VK_ENTER) );
    }
  }

  void jTableMessages_keyPressed(KeyEvent e)
  {
    int keyCode = e.getKeyCode();

    switch (keyCode)
    {
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_RIGHT:
        // We don't have a border around the selected cell, so ignore the
        // keypress.
        e.consume();
      break;
      case KeyEvent.VK_UP:
        if (currentMessage > 1)
        {
          currentMessage--;
          if (currentMessage<pageMinimum)
          {
            scrollMessageScrollBar();
            e.consume();
          }
        }
      break;
      case KeyEvent.VK_DOWN:
        if ( (currentMessage < maxMessages) & (maxMessages != 0) )
        {
          currentMessage++;
          if (currentMessage>pageMaximum)
          {
            scrollMessageScrollBar();
            e.consume();
          }
        }
      break;
      case KeyEvent.VK_HOME:
        if (currentMessage > 1)
        {
          currentMessage = 1;
          scrollMessageScrollBar();
        }
        e.consume();
      break;
      case KeyEvent.VK_END:
        if ( (currentMessage < maxMessages) & (maxMessages != 0) )
        {
          currentMessage = maxMessages;
          scrollMessageScrollBar();
        }
        e.consume();
      break;
      case KeyEvent.VK_PAGE_DOWN:
        // Increase the current message by the screen size
        currentMessage = Math.min(currentMessage+(pageMaximum+1-pageMinimum), maxMessages);
        scrollMessageScrollBar();
        e.consume();
      break;
      case KeyEvent.VK_PAGE_UP:
        // Decrease the current message by the screen size
        currentMessage = Math.max(currentMessage-(pageMaximum+1-pageMinimum), 0);
        scrollMessageScrollBar();
        e.consume();
      break;
      case KeyEvent.VK_ENTER:
        e.consume();
        if (maxMessages != 0)
        {
          inViewer = true;
          if (jTabbedPaneMain.indexOfComponent(jPanelViewer) == -1)
            jTabbedPaneMain.add(jPanelViewer);
          setViewerData( currentArea, currentMessage );
          jTabbedPaneMain.setSelectedComponent( jPanelViewer );
          setMenuEnabled();
          resizeViewerPanels();
        }
      break;
      case KeyEvent.VK_ESCAPE:
        jTabbedPaneMain.setSelectedIndex( 0 );
        jTabbedPaneMain.remove(jPanelViewer);
        jTabbedPaneMain.remove(jPanelMessages);
        jTableMessages.removeAll();
        setMenuEnabled();
        setAreaModelData();
        // Select the area that we've just been in
        jTableAreas.setRowSelectionInterval(areaNames.indexOf(currentArea), areaNames.indexOf(currentArea));
        // TODO: Get the scrollpane to scroll to the correct place in the list.
        e.consume();
      break;
    }
  }

  void jPanelViewer_componentResized(ComponentEvent e)
  {
    resizeViewerPanels();
  }

  void jTextAreaMessage_keyPressed(KeyEvent e)
  {
    int keyCode = e.getKeyCode();
    JEdPointMessage JPM;
    Vector tempVector;
    MessageAreaData tempMAD;
    Long tempLong;
    int counter;
    JScrollBar jscrollbar;

    // If the keys pressed are in our shortcuts, return.
    for (counter=0; counter<messagesKeys.length/2; counter++)
    {
      if ( (e.getKeyCode()  == messagesKeys[counter*2])
        && (e.getModifiers() == messagesKeys[counter*2+1]) )
          return;
    }

    switch (keyCode)
    {
      case KeyEvent.VK_UP:
      {
        jscrollbar = jScrollPaneViewer.getVerticalScrollBar();
        jscrollbar.setValue( jscrollbar.getValue() - jscrollbar.getUnitIncrement(-1) );
      }
      e.consume();
      break;
      case KeyEvent.VK_DOWN:
      {
        jscrollbar = jScrollPaneViewer.getVerticalScrollBar();
        jscrollbar.setValue( jscrollbar.getValue() + jscrollbar.getUnitIncrement(1) );
      }
      e.consume();
      break;
      case KeyEvent.VK_LEFT:
        if (currentMessage>1)
        {
          // Get the previous message
          scrollBackward(jTableMessages, false);
          currentMessage--;
          setViewerData( currentArea, currentMessage );
        }
        e.consume();
      break;
      case KeyEvent.VK_RIGHT:
        e.consume();
        if (currentMessage < maxMessages)
        {
          // Get the next message
          scrollForward(jTableMessages, false);
          currentMessage++;
          setViewerData( currentArea, currentMessage );
        }
        else
        {
          int orderCounter;

          // Find the next unread message in any area
          if ( areaNames.contains(currentArea) )
          {
            for (orderCounter=0; orderCounter<autoNextUnreadOrder.length; orderCounter++)
            {
              // Find the next message in this area
              for (counter=areaNames.indexOf(currentArea)+1; counter<areaNames.size(); counter++)
              {
                JPM = jpm.sendMessage( JEdPointModule.moduleMessageBase, autoNextUnreadOrder[orderCounter], "id", (String)areaNames.elementAt(counter) );
                tempVector = (Vector)JPM.getResponse("vector");
                if (tempVector.size()!=0)
                {
                  tempLong = (Long)tempVector.elementAt(0);
                  currentArea = (String)areaNames.elementAt(counter);
                  jTabbedPaneMain.setTitleAt(1, currentArea);
                  setMessageModelData(currentArea);
                  currentMessage = tempLong.longValue();
                  setViewerData( currentArea, currentMessage );
                  return;
                }
              } // for (counter=tempVector.indexOf(currentArea); counter<tempVector.size(); counter++)
              // Find the next message in the next area
              for (counter=areaNames.indexOf(currentArea)+1; counter<areaNames.size(); counter++)
              {
                JPM = jpm.sendMessage( JEdPointModule.moduleMessageBase, autoNextUnreadOrder[orderCounter], "id", (String)areaNames.elementAt(counter) );
                tempVector = (Vector)JPM.getResponse("vector");
                if (tempVector.size()!=0)
                {
                  tempLong = (Long)tempVector.elementAt(0);
                  currentArea = (String)areaNames.elementAt(counter);
                  jTabbedPaneMain.setTitleAt(1, currentArea);
                  setMessageModelData(currentArea);
                  currentMessage = tempLong.longValue();
                  setViewerData( currentArea, currentMessage );
                  return;
                }
              } // for (counter=tempVector.indexOf(currentArea); counter<tempVector.size(); counter++)
              // Find any unread message anywhere.
              for (counter=0; counter<areaNames.size(); counter++)
              {
                JPM = jpm.sendMessage( JEdPointModule.moduleMessageBase, autoNextUnreadOrder[orderCounter], "id", (String)areaNames.elementAt(counter) );
                tempVector = (Vector)JPM.getResponse("vector");
                if (tempVector.size()!=0)
                {
                  tempLong = (Long)tempVector.elementAt(0);
                  currentArea = (String)areaNames.elementAt(counter);
                  jTabbedPaneMain.setTitleAt(1, currentArea);
                  setMessageModelData(currentArea);
                  currentMessage = tempLong.longValue();
                  setViewerData( currentArea, currentMessage );
                  return;
                }
              } // for (counter=tempVector.indexOf(currentArea); counter<tempVector.size(); counter++)
            } // for (orderCounter=0; orderCounter<autoNextUnreadOrder.length; orderCounter++)
            // Ok, so we haven't found any unread messages.
            // Go out to the area list

            // Simulate two escapes
            jTextAreaMessage_keyPressed( new KeyEvent((Component)e.getSource(), e.getID(), 0, 0, KeyEvent.VK_ESCAPE) );
            jTableMessages_keyPressed( new KeyEvent((Component)e.getSource(), e.getID(), 0, 0, KeyEvent.VK_ESCAPE) );
          } // if ( areaNames.contains(currentArea) )
        } // else
        break;
      case KeyEvent.VK_ESCAPE:
        scrollMessageScrollBar();
        jTabbedPaneMain.remove( jPanelViewer );
        inViewer = false;
        e.consume();
      break;
    }
  }

  void jMenuItemJEdPointExit_actionPerformed(ActionEvent e)
  {
    quitJEdPoint();
  }

  void jMenuItemAreasAdd_actionPerformed(ActionEvent e)
  {
    MessageAreaData mad = new MessageAreaData();
    mad.setMessageAreaData("tearline", defaultAreaTearline);
    mad.setMessageAreaData("origin", defaultAreaOrigin);
    mad = editMessageAreaData("Add a new area", mad);
    if (mad!=null)
    {
      if (jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbAddArea, "messageareadata", mad) == null)
      {
        // The area could not be created
        // Warn the user
        JOptionPane.showConfirmDialog(this, "Could not add the area!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        // And log it
        jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not add an area.");
      }
      else
      {
        // Add was sucessfull
        JOptionPane.showConfirmDialog(this, "Added the area!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        this.setAreaModelData();
      }
    }
  }

  void jMenuItemAreasMoveUp_actionPerformed(ActionEvent e)
  {
    int counter;

    // Move the selected areas one up
    for (counter=0; counter<jTableAreas.getRowCount(); counter++)
    {
      if (jTableAreas.isRowSelected(counter))
        areaNames.moveUp(counter);
    }

    setAreaTableModelData();


    jTableAreas.removeRowSelectionInterval(0, 0);
    for (counter=0; counter<jTableAreas.getRowCount()-1; counter++)
    {
      if (jTableAreas.isRowSelected(counter+1))
      {
        jTableAreas.addRowSelectionInterval(counter, counter);
        jTableAreas.removeRowSelectionInterval(counter+1, counter+1);
      }
    }
  }

  void jMenuItemAreasMoveDown_actionPerformed(ActionEvent e)
  {
    int counter;
    for (counter=0; counter<jTableAreas.getRowCount(); counter++)
    {
      if (jTableAreas.isRowSelected(counter))
        areaNames.moveDown(counter);
    }

    setAreaTableModelData();

    for (counter=jTableAreas.getRowCount(); counter>0; counter--)
    {
      if (jTableAreas.isRowSelected(counter-1))
      {
        jTableAreas.removeRowSelectionInterval(counter-1, counter-1);
        jTableAreas.addRowSelectionInterval(counter, counter);
      }
    }
  }

  /**
   * Sort ALL of the areas.
   */
  void jMenuItemAreasSort_actionPerformed(ActionEvent e)
  {
    areaNames.sort();
    setAreaTableModelData();
  }

  void jMenuItemAreasEdit_actionPerformed(ActionEvent e)
  {
    MessageAreaData oldMAD, newMAD;
    JEdPointMessage JPM = new JEdPointMessage();
    int counter;
    String tempString;
    Vector tempVector = new Vector();

    if (jTableAreas.getSelectedRowCount() == 1)
    {
      JPM.setRequest("id", (String)areaNames.elementAt( jTableAreas.getSelectedRow() ) );
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, JPM);
      oldMAD = (MessageAreaData) JPM.getResponse("messageareadata");

      newMAD = editMessageAreaData("Edit an area", oldMAD);

      if (newMAD!=null)
      {
        JPM = new JEdPointMessage();
        JPM.setRequest("id", (String)oldMAD.getMessageAreaData("id"));
        JPM.setRequest("messageareadata", newMAD);

        if (jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbChangeArea, JPM) == null)
        {
          // The area could not be changed
          // Warn the user
          JOptionPane.showConfirmDialog(this, "Could not change area: " + (String)newMAD.getMessageAreaData("id"), "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
          // And log it
          jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not change area: " + (String)newMAD.getMessageAreaData("id"));
        }
        else
        {
          JOptionPane.showConfirmDialog(this, "Changed the area " + (String)newMAD.getMessageAreaData("id"), "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
          this.setAreaModelData();
        }
      } // IF newMAD != null
    }
    else
    {
      // Edit several areas
      newMAD = editMessageAreasData("Edit areas");

      if (newMAD!=null)
      {
        for (counter=0; counter<jTableAreas.getRowCount(); counter++)
        {
          if (jTableAreas.isRowSelected(counter))
          {
            // Get the area's information
            JPM.setRequest("id", (String)areaNames.elementAt( counter ) );
            JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, JPM);
            oldMAD = (MessageAreaData) JPM.getResponse("messageareadata");

            if (newMAD.getMessageAreaData("description") != null)
              oldMAD.setMessageAreaData( "description", newMAD.getMessageAreaData("description") );

            if (newMAD.getMessageAreaData("tearline") != null)
              oldMAD.setMessageAreaData( "tearline", newMAD.getMessageAreaData("tearline") );

            if (newMAD.getMessageAreaData("origin") != null)
              oldMAD.setMessageAreaData( "origin", newMAD.getMessageAreaData("origin") );

            if (newMAD.getMessageAreaData("replyin") != null)
              oldMAD.setMessageAreaData( "replyin", newMAD.getMessageAreaData("replyin") );

            if (newMAD.getMessageAreaData("readonly") != null)
              oldMAD.setMessageAreaData( "readonly", newMAD.getMessageAreaData("readonly") );

            JPM = new JEdPointMessage();
            JPM.setRequest("id", (String)oldMAD.getMessageAreaData("id"));
            JPM.setRequest("messageareadata", oldMAD);
            if (jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbChangeArea, JPM) == null)
            {
              tempVector.add( (String)newMAD.getMessageAreaData("id") );
              jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not change area: " + (String)newMAD.getMessageAreaData("id"));
            } // ChangeArea
          } // If row selected
        }
        // We're done changing all the areas.
        // If the vector is bigger than 0, that means there was a problem.
        // Compile a tempString of the area names;
        if (tempVector.size() != 0)
        {
          if (tempVector.size() == 1)
            tempString = "The following area could not be changed:";
          else
            tempString = "The following areas could not be changed:";
          while (!tempVector.isEmpty())
          {
            tempString = "\n" + tempString + (String)tempVector.elementAt(0);
            tempVector.remove(0);
          }
          // Show the error message to the user
          JOptionPane.showConfirmDialog(this, tempString, "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
          // And log it
          jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", tempString);
        }
        else
          // Inform the user that the areas were changed
          JOptionPane.showConfirmDialog(this, "The areas have been changed!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

        setAreaModelData();
      } // if newmad != null
    } // Edit several areas
  }

  void jMenuItemAreasDelete_actionPerformed(ActionEvent e)
  {
    int counter;
    Vector areaVector = new Vector(jTableAreas.getSelectedRowCount());

    // Delete all marked messages
    if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + jTableAreas.getSelectedRowCount() + " areas?", "JEdPoint", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
    {
      for (counter=jTableAreas.getRowCount(); counter>=0; counter--)
      {
        if (jTableAreas.isRowSelected(counter))
        {
          areaVector.add((String)areaNames.elementAt(counter));
          // Remove the row
          areaNames.remove(counter);
        }
      }

      // Send the vector to the messagebase module
      if (jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbRemoveArea, "ids", areaVector) == null)
      {
        // The areas could not be removed
        // Warn the user
        JOptionPane.showConfirmDialog(this, "Could not remove the area(s)!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        // And log it
        jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not remove the area(s)!");
      }
      this.setAreaModelData();
    }
  }

  private JEdPointMessage disconnectArea( String areaName )
  {
    JEdPointMessage JPM = new JEdPointMessage();
    FidonetMessage fm;

    fm = FidonetMessageUtils.newMessage(settingUserName, settingUserPointnumber,
      settingAreafixName, settingAreafixPointnumber, settingAreafixPassword, "JEdPoint Disconnect", "" );
    fm.setMessageData("message", "-" + areaName);

    return jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbWriteMessage, "id", "netmail", "message", fm);
  }

  /**
   * Disconnects all the selected areas by sending a netmail to Areafix.
   */
  void jMenuItemAreasDisconnect_actionPerformed(ActionEvent e)
  {
    int counter;
    Vector areaVector = new Vector(jTableAreas.getSelectedRowCount());
    Vector unreadMessages;
    JEdPointMessage JPM;
    String areaID;

    // Mark all the messages in the marked areas as read
    if (JOptionPane.showConfirmDialog(this, "Are you sure you want to disconnect " + jTableAreas.getSelectedRowCount() + " areas?", "JEdPoint", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
    {
      for (counter=jTableAreas.getRowCount(); counter>=0; counter--)
        if (jTableAreas.isRowSelected(counter))
          areaVector.add((String)areaNames.elementAt(counter));

      while (!areaVector.isEmpty())
      {
        areaID = (String)areaVector.elementAt(0);
        if (disconnectArea(areaID) == null)
          JOptionPane.showConfirmDialog(this, "Could not disconnect " + areaID, "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        areaVector.remove(0);
      }
      setAreaTableModelData();
      JOptionPane.showConfirmDialog(this, "Areas disconnected!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }
  }

  void jMenuItemAreasCatchUp_actionPerformed(ActionEvent e)
  {
    int counter;
    Vector areaVector = new Vector(jTableAreas.getSelectedRowCount());
    Vector unreadMessages;
    JEdPointMessage JPM;
    String areaID;

    // Mark all the messages in the marked areas as read
    if (JOptionPane.showConfirmDialog(this, "Are you sure you want to catch up " + jTableAreas.getSelectedRowCount() + " areas?", "JEdPoint", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
    {
      for (counter=jTableAreas.getRowCount(); counter>=0; counter--)
        if (jTableAreas.isRowSelected(counter))
          areaVector.add((String)areaNames.elementAt(counter));

      while (!areaVector.isEmpty())
      {
        areaID = (String)areaVector.elementAt(0);
        JPM = jpm.sendMessage( JEdPointModule.moduleMessageBase, JEdPointMessage.mbCatchUpArea, "id", areaID );
        if (JPM == null)
          JOptionPane.showConfirmDialog(this, "Could not catch up " + areaID, "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        areaVector.remove(0);
      }
      setAreaTableModelData();
      JOptionPane.showConfirmDialog(this, "Done catching up!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }
  }

  /**
   * Edits the current message and replaces it in the messagebase
   */
  void jMenuItemMessagesEdit_actionPerformed(ActionEvent e)
  {
    messagesEdit();
  }

  void jMenuItemMessagesDelete_actionPerformed(ActionEvent e)
  {
    messagesDelete();
  }

  void jMenuItemMessagesNew_actionPerformed(ActionEvent e)
  {
    messagesNew();
  }

  private void messagesEdit()
  {
    Object tempObj;
    FidonetMessage fm;
    JEdPointMessage JPM = new JEdPointMessage();

    if (currentMessage < 1 )
    {
      JOptionPane.showConfirmDialog(this, "Select a message first!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
      return;
    }

    JPM = new JEdPointMessage();
    JPM.setRequest("id", currentArea);
    JPM.setRequest("index", currentMessage);
    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetMessage, JPM);
    fm = (FidonetMessage)JPM.getResponse("message");

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageEditor, JEdPointMessage.meEditMessage, "message", fm, "jframe", MainFrame);

    show();

    fm = (FidonetMessage)JPM.getResponse("message");

    if (!JPM.getResponseBoolean("cancel"))
    {
      // The editing was not canceled, change the message

      JPM.clear();
      JPM.setRequest("id", currentArea);
      JPM.setRequest("index", currentMessage);
      JPM.setRequest("message", fm);
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbChangeMessage, JPM);
      if (JPM == null)
      {
        // The message could not be changed
        // Warn the user
        JOptionPane.showConfirmDialog(this, "Could not change the message!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        // And log it
        jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "UI: Could not change a message.");
      }
      else
      {
        // Tell the user
        JOptionPane.showConfirmDialog(this, "Message changed!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        // Update the viewer data
        setViewerData(currentArea, currentMessage);
      }
    }
  }
  private void messagesDelete()
  {
    int counter;
    Vector messageVector = new Vector(jTableMessages.getSelectedRowCount());
    JEdPointMessage JPM = new JEdPointMessage();

    // Delete/Undelete all marked messages
    if (JOptionPane.showConfirmDialog(this, "Are you sure you want to un/delete " + jTableMessages.getSelectedRowCount() + " messages?", "JEdPoint", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
    {
      for (counter=jTableMessages.getRowCount(); counter>=0; counter--)
      {
        if (jTableMessages.isRowSelected(counter))
        {
          messageVector.add(0, new Long(pageMinimum + counter));
        }
      }

      // Send the vector to the messagebase module
      JPM.setRequest("id", this.currentArea);
      JPM.setRequest("indexes", messageVector);
      if (jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbDeleteMessage, JPM ) == null)
      {
        // The messages could not be removed
        // Warn the user
        JOptionPane.showConfirmDialog(this, "Could not delete/undelete the message(s)!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        // And log it
        jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not remove the messages(s)!");
      }
      else
      {
        JOptionPane.showConfirmDialog(this, jTableMessages.getSelectedRowCount() + " messages have been marked/unmarked for deletion!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
      }
      setMessageModelData(currentArea);
    }
  }

  private void messagesNew()
  {
    Object tempObj;
    FidonetMessage fm;
    JEdPointMessage JPM = new JEdPointMessage();
    MessageAreaData mad;            // Used to get the origin and tearline data
    // Name + Point of current user
    String userName;
    PointNumber pointNumber;
    // Origin and Tearline to use
    String origin;
    String tearline;

    // Get the information for this area
    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", currentArea);
    mad = (MessageAreaData)JPM.getResponse("messageareadata");

    if (!doReadOnlyCheck(mad))
      return;

    // Get the user's info
    JPM = jpm.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, JPM );
    userName = (String)JPM.getResponse("username");
    pointNumber = (PointNumber)JPM.getResponse("pointnumber");

    origin = (String)mad.getMessageAreaData("origin");
    JPM = jpm.sendMessage(JEdPointModule.moduleOrigin, JEdPointMessage.originGetOrigin, "file", origin);
    origin = JPM.getResponseString("origin");
    tearline = (String)mad.getMessageAreaData("tearline");
    JPM = jpm.sendMessage(JEdPointModule.moduleTearline, JEdPointMessage.tlGetTearline, "file", tearline);
    tearline = JPM.getResponseString("tearline");

    // Make the completely new message
    fm = FidonetMessageUtils.newMessage( userName, pointNumber, this.defaultToUserName, this.defaultToPointNumber, this.defaultSubject, tearline, origin );

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageEditor, JEdPointMessage.meEditMessage, "message", fm, "jframe", MainFrame);

    this.show();

    fm = (FidonetMessage)JPM.getResponse("message");

    if (!JPM.getResponseBoolean("cancel"))
    {
      // The editing was not canceled, add the message

      // Set the proper flags
      fm.setMessageData("attributesent", false);
      fm.setMessageData("attributeread", false);

      JPM.clear();
      JPM.setRequest("id", this.currentArea);
      JPM.setRequest("message", fm);
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbWriteMessage, JPM);
      if (JPM == null)
      {
        // The message could not be added
        // Warn the user
        JOptionPane.showConfirmDialog(this, "Could not add the message!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        // And log it
        jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not add a message.");
      }
      else
      {
        // Update the table of messages
        setMessageModelData(currentArea);
        // Update the viewer data
        setViewerData(currentArea, currentMessage);
      }
    }
  }

  private void messagesReply()
  {
    JEdPointMessage JPM = new JEdPointMessage();
    MessageAreaData mad = new MessageAreaData();
    String areaToReplyIn;

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", this.currentArea);
    mad = (MessageAreaData)JPM.getResponse("messageareadata");

    // If the area has a replyin thingy specified, use that area, rather than this one.
    areaToReplyIn = mad.getMessageAreaDataString("replyin");
    if ( areaToReplyIn.compareTo("") != 0)
    {
      // Check that the area exists
      if (!doesAreaExist(areaToReplyIn))
      {
        // Create the area
        mad = new MessageAreaData();
        mad.setMessageAreaData("id", areaToReplyIn);
        jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbAddArea, "messageareadata", mad);
      }
      reply( areaToReplyIn );
    }
    else
      reply(currentArea);
  }

  private void messagesReplyInOtherArea()
  {
    // Popup a "Reply In Other Area" dialog
    JDialog jdialog = new JDialog();
    jdialog.setTitle("Select the area in which to reply:");
    jdialog.setModal(true);
    PanelReplyInOtherArea jpanel = new PanelReplyInOtherArea(jdialog, jpm);

    jdialog.setSize( jpanel.getSize());
    jdialog.getContentPane().add( jpanel );
    jdialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    jdialog.setResizable(true);

    jdialog.show();

    if (jpanel.isDataOK())
    {
      // The user didn't cancel.
      reply( jpanel.returnSelectedArea() );
    }
  }

  private void reply(String areaToReplyIn)
  {
    Object tempObj;
    FidonetMessage fm;
    JEdPointMessage JPM = new JEdPointMessage();
    MessageAreaData mad;            // Used to get the origin and tearline data
    // Name + Point of current user
    String userName;
    PointNumber pointNumber;
    // Origin and Tearline to use
    String origin;
    String tearline;

    // Retrieve the area's information
    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetAreaInformation, "id", areaToReplyIn);
    mad = (MessageAreaData)JPM.getResponse("messageareadata");

    if (!doReadOnlyCheck(mad))
      return;

    // Get the user's info
    JPM = jpm.sendMessage(JEdPointModule.moduleMicrokernel, JEdPointMessage.mkGetSettings, JPM );
    userName = (String)JPM.getResponse("username");
    pointNumber = (PointNumber)JPM.getResponse("pointnumber");

    // Get an origin + tearline
    origin = (String)mad.getMessageAreaData("origin");
    JPM = jpm.sendMessage(JEdPointModule.moduleOrigin, JEdPointMessage.originGetOrigin, "file", origin);
    origin = JPM.getResponseString("origin");
    tearline = (String)mad.getMessageAreaData("tearline");
    JPM = jpm.sendMessage(JEdPointModule.moduleTearline, JEdPointMessage.tlGetTearline, "file", tearline);
    tearline = JPM.getResponseString("tearline");

    // get the current message
    JPM.setRequest("id", this.currentArea);
    JPM.setRequest("index", currentMessage );
    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbGetMessage, JPM);
    fm = (FidonetMessage)JPM.getResponse("message");

    JPM = new JEdPointMessage();
    JPM.setRequest("message",
      FidonetMessageUtils.reply(fm,
        userName,
        pointNumber,
        (String)fm.getMessageData("usernamefrom"),
        (PointNumber)fm.getMessageData("pointfrom"),
        tearline,
        origin));
    JPM.setRequest("jframe", MainFrame);

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageEditor, JEdPointMessage.meEditMessage, JPM);
    this.show();

    fm = (FidonetMessage)JPM.getResponse("message");

    if (!JPM.getResponseBoolean("cancel"))
    {
      // The editing was not canceled, add the message
      JPM.clear();

      JPM.setRequest("id", areaToReplyIn);
      JPM.setRequest("message", fm);
      JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbWriteMessage, JPM);
      if (JPM == null)
      {
        // The message could not be added
        // Warn the user
        JOptionPane.showConfirmDialog(this, "Could not add the message!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        // And log it
        jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not add a message.");
      }
      else
      {
        // Update the table of messages
        setMessageModelData(currentArea);
        // Update the viewer data
        setViewerData(currentArea, currentMessage);
      }
    }
  }

  void jMenuItemMessagesReply_actionPerformed(ActionEvent e)
  {
    messagesReply();
  }

  void jMenuItemMessagesReplyInOtherArea_actionPerformed(ActionEvent e)
  {
    messagesReplyInOtherArea();
  }

  void jMenuItemMessagesGo_actionPerformed(ActionEvent e)
  {
    final int jump          =0;
    final int jumpForwards  =1;
    final int jumpBackwards =2;
    int jumptype;

    long messageToGoTo;
    String response;

    try
    {
      response = JOptionPane.showInputDialog(this, "Please input message number to go to.\nYou can use + and - in front of the number to jump a relative distance.", "Go to message", JOptionPane.QUESTION_MESSAGE, null, null, null).toString();
    }
    catch (NullPointerException npe)
    {
      return;
    }

    if (response.startsWith("+"))
    {
      jumptype = jumpForwards;
      // remove the plus from the string
      response = response.substring(1);
    }
    else
    {
      if (response.startsWith("-"))
      {
        jumptype = jumpBackwards;
        response = response.substring(1);
      }
      else
        jumptype = jump;
    }

    try
    {
      messageToGoTo = Math.abs( Long.parseLong(response.toString()) );

    }
    catch (NumberFormatException nfe)
    {
      JOptionPane.showConfirmDialog(this, "Not a number!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
      // The user did not input a number
      return;
    }

    switch (jumptype)
    {
      case jumpForwards:
        currentMessage = Math.min( maxMessages, currentMessage+messageToGoTo );
      break;
      case jumpBackwards:
        currentMessage = Math.max( 1, currentMessage-messageToGoTo );
      break;
      case jump:
        currentMessage = Math.max( 1, messageToGoTo );
        currentMessage = Math.min( currentMessage, maxMessages );
      break;
    }
    scrollMessageScrollBar();
  }

  /**
   * Page to the last message in the area
   */
  void jMenuItemMessagesGoToFirst_actionPerformed(ActionEvent e)
  {
    if (maxMessages>0)
    {
      currentMessage = 1;
      setViewerData(currentArea, currentMessage);
    }
  }

  void jMenuItemMessagesGoToLast_actionPerformed(ActionEvent e)
  {
    currentMessage = maxMessages;
    setViewerData(currentArea, currentMessage);
  }

  void jMenuItemOptionsSaveColumnWidths_actionPerformed(ActionEvent e)
  {
    privateSettings.AreaColumnWidths = privateSettings.saveWidths( jTableAreas.getColumnModel() );
    privateSettings.MessageColumnWidths = privateSettings.saveWidths( jTableMessages.getColumnModel() );

    if (JEdPointUtilities.saveObject(dataDir + File.separator + pathPrivateSettings, privateSettings.saveSettings()))
      JOptionPane.showConfirmDialog(this, "Column width settings saved!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    else
      JOptionPane.showConfirmDialog(this, "Column width settings could not be saved!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
  }

  void jMenuItemOptionSaveWindowPosition_actionPerformed(ActionEvent e)
  {
    privateSettings.MainWindowDimension = this.getSize();

    if ( JEdPointUtilities.saveObject(dataDir + File.separator + pathPrivateSettings, privateSettings.saveSettings()) )
      JOptionPane.showConfirmDialog(this, "Window size settings saved!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    else
      JOptionPane.showConfirmDialog(this, "Window size settings could not be saved!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
  }

  void jTableAreas_componentResized(ComponentEvent e)
  {
    privateSettings.restoreWidths( jTableAreas.getColumnModel(), privateSettings.AreaColumnWidths );
  }

  void jTableMessages_componentResized(ComponentEvent e)
  {
    scrollMessageScrollBar();
    privateSettings.restoreWidths( jTableMessages.getColumnModel(), privateSettings.MessageColumnWidths );
  }

  void jMenuItemAreasRefresh_actionPerformed(ActionEvent e)
  {
    this.setAreaModelData();
  }

  void this_windowOpened(WindowEvent e)
  {
    jTableAreas.requestFocus();
  }

  void this_windowClosing(WindowEvent e)
  {
    this.quitJEdPoint();
  }

  void jCheckBoxMenuItemOptionsViewKludges_actionPerformed(ActionEvent e)
  {
    if (privateSettings.viewKludges)
      privateSettings.viewKludges = false;
    else
      privateSettings.viewKludges = true;

    setViewerData(currentArea, currentMessage );

    jCheckBoxMenuItemOptionsViewKludges.setState( privateSettings.viewKludges );
  }

  void jMenuItemToolsImport_actionPerformed(ActionEvent e)
  {
    DialogImportExport die = new DialogImportExport(this,
                                  jpm,
                                  "Importer",
                                  JEdPointModule.moduleImport,
                                  JEdPointMessage.importStart,
                                  JEdPointMessage.importCancel,
                                  JEdPointMessage.importGetStatus );
    die.setModal(true);
    die.setAutoStart(importAutoStart);
    die.setAutoClose(importAutoClose);
    die.setVisible(true);
    setAreaModelData();
  }

  void jMenuItemToolsExport_actionPerformed(ActionEvent e)
  {
    DialogImportExport die = new DialogImportExport(this,
                                  jpm,
                                  "Exporter",
                                  JEdPointModule.moduleExport,
                                  JEdPointMessage.exportStart,
                                  JEdPointMessage.exportCancel,
                                  JEdPointMessage.exportGetStatus );
    die.setAutoStart(exportAutoStart);
    die.setAutoClose(exportAutoClose);
    die.setModal(true);
    die.setVisible(true);
    setAreaModelData();
  }

  public void displayJOptionPane( JOptionPane jop, String title )
  {
    JDialog jdialog = jop.createDialog(MainFrame, title);
    jdialog.setModal(true);
    jdialog.setVisible(true);
  }

  void jMenuItemToolsPoll_actionPerformed(ActionEvent e)
  {
    DialogPoll dp = new DialogPoll(this, jpm );
    dp.setModal(true);
    dp.setVisible(true);
  }

  void jMenuItemHelpAbout_actionPerformed(ActionEvent e)
  {
    JEdPointMessage JPM;
    int counter;
    String moduleInfo = "";

    moduleInfo += "Java VM version: " + System.getProperty("java.vm.version") + "\n";
    for (counter=0; counter<JEdPointModule.moduleTypes; counter++)
    {
      JPM = jpm.sendMessage(counter, JEdPointMessage.moduleGetInformation, "", "");
      moduleInfo += JPM.getResponseString("modulenameversion") + "\n";
    }

    JOptionPane.showMessageDialog(this, moduleInfo, "JEdPoint - About", JOptionPane.INFORMATION_MESSAGE);
  }

 // ---------------------------------------------------------------------------
  // AREA PACKING STUFF
  /**
   * This area packing stuff is interesting.
   *
   * There are two threads here. Packer and DialogShower.
   *
   * First you start a packer thread. And then the packer thread pops up a dialog
   * informing the user of the current progress.
   *
   * If you don't use two threads, everything is going to go to hell. Windows will
   * hang and you won't get anywhere.
   */
  void jMenuItemAreasPack_actionPerformed(ActionEvent e)
  {
    Thread tempThread = new Thread( new Packer() );
    tempThread.start();
    tempThread = null;
  }

  public class Packer implements Runnable
  {
    public void run()
    {
      int counter;
      Vector areaVector = new Vector(jTableAreas.getSelectedRowCount());
      String tempString;
      int packsFailed, packsSuccessfull;
      DialogShower ds;
      Object reply;

      reply = JOptionPane.showInputDialog(
        MainFrame,
        "Select how you wish to pack the following " + jTableAreas.getSelectedRowCount() + " areas:",
        "JEdPoint",
        JOptionPane.QUESTION_MESSAGE,
        null,
        new String[] { "Standard", "Repair", },
        "Standard"
        );

      if ( reply != null )
      {
        for (counter=jTableAreas.getRowCount(); counter>=0; counter--)
        {
          if (jTableAreas.isRowSelected(counter))
          {
            areaVector.add((String)areaNames.elementAt(counter));
          }
        }

        packsFailed = 0;
        packsSuccessfull = 0;
        while ( !areaVector.isEmpty() )
        {
          tempString = (String)areaVector.elementAt(0);

          // Inform the user as to what we're doing...
          ds = new DialogShower(MainFrame, "Packing area " + tempString );
          if (jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbPackArea, "id", tempString, "repair", new Boolean(reply=="Repair") ) == null)
          {
            // The area could not be packed
            // Warn the user
            JOptionPane.showConfirmDialog(MainFrame, "Could not pack the area " + tempString, "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            // And log it
            jpm.sendMessage(JEdPointModule.moduleLog, JEdPointMessage.logWarning, "message", "Could not pack the area " + tempString);
            packsFailed++;
          }
          else
          {
            packsSuccessfull++;
          }
          areaVector.remove( 0 );

          ds.close();
        }

        // Report our success to the user
        if (packsFailed!=0)
        {
          tempString = packsFailed + " of " + (packsFailed+packsSuccessfull) + " areas could not be packed!";
          JOptionPane.showConfirmDialog(MainFrame, tempString, "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
        else
        {
          tempString = packsSuccessfull + " areas have been packed!";
          JOptionPane.showConfirmDialog(MainFrame, tempString, "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        }
        setAreaModelData();
      }
    } // run()
  }

  public class DialogShower implements Runnable
  {
    String message;
    Component parent;
    JDialog jdialog;
    JOptionPane jop = new JOptionPane();
    Thread tempThread;

    public DialogShower(Component newParent, String newMessage)
    {
      parent = newParent;
      message = newMessage;
      tempThread = new Thread(this);
      tempThread.start();
    }

    public void close()
    {
      jdialog.setVisible(false);
      jdialog = null;
      tempThread = null;
    }

    public void run()
    {
      jop.setOptions(new String[]{});
      jop.setMessage(message);
      jdialog = jop.createDialog(parent, "JEdPoint");
      jdialog.setModal(false);
      jdialog.setVisible(true);
    }
  }

  // AREA PACKING STUFF
  // ---------------------------------------------------------------------------
}
