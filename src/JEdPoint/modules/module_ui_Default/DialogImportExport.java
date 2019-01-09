package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class DialogImportExport extends JDialog implements Runnable
{
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel1 = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JButton jButtonStart = new JButton();
  JButton jButtonCancel = new JButton();
  JButton jButtonClose = new JButton();
  java.awt.List listActions = new java.awt.List();

  JEdPointModule jpm;
  Thread checkerThread;

  // Luckily(?) for us, both the import and the export modules have the same start, cancel, status syntax.
  int moduleToSendTo;
  int startMessage;
  int cancelMessage;
  int statusMessage;

  boolean autoStart;
  boolean autoClose;

  public DialogImportExport(Frame ownerFrame, JEdPointModule newjpm, String title, int newModuleToSendTo, int newStartMessage, int newCancelMessage, int newStatusMessage)
  {
    super(ownerFrame, title, true);

    try
    {
      jbInit();
      pack();
      setDialogSize(ownerFrame);
    }
    catch(Exception ex)
    {
      // Error handling here
    }

    jpm = newjpm;

    moduleToSendTo = newModuleToSendTo;
    startMessage = newStartMessage;
    cancelMessage = newCancelMessage;
    statusMessage = newStatusMessage;

    setActiveButtons();
  }

  void jbInit() throws Exception
  {
    panel1.setLayout(borderLayout1);
    jPanel1.setLayout(flowLayout1);
    jButtonStart.setText("Start");
    jButtonStart.setMnemonic('S');
    jButtonStart.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonStart_actionPerformed(e);
      }
    });
    jButtonCancel.setText("Cancel");
    jButtonCancel.setMnemonic('C');
    jButtonCancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonCancel_actionPerformed(e);
      }
    });
    jButtonClose.setText("Close");
    jButtonClose.setMnemonic('L');
    jButtonClose.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonClose_actionPerformed(e);
      }
    });
    panel1.setPreferredSize(new Dimension(500, 400));
    getContentPane().add(panel1);
    panel1.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(jButtonStart, null);
    jPanel1.add(jButtonCancel, null);
    jPanel1.add(jButtonClose, null);
    panel1.add(listActions, BorderLayout.CENTER);
  }

  /**
   * Sets the dialog size to 75% of the owner's size.
   */
  private void setDialogSize( Frame ownerFrame )
  {
    int width  = (int)(ownerFrame.getWidth()*0.75);
    int height = (int)(ownerFrame.getHeight()*0.75);
    int x = (ownerFrame.getWidth()-width) / 2;
    int y = (ownerFrame.getHeight()-height) / 2;

    this.setBounds(x, y, width, height);
  }

  public void setAutoStart(boolean autoStart)
  {
    this.autoStart = autoStart;
  }

  public boolean getAutoStart()
  {
    return autoStart;
  }

  public void setAutoClose(boolean autoClose)
  {
    this.autoClose = autoClose;
  }

  public boolean getAutoClose()
  {
    return autoClose;
  }

  void setActiveButtons()
  {
    JEdPointMessage JPM = new JEdPointMessage();

    jpm.sendMessage(moduleToSendTo, statusMessage, JPM);

    if (JPM.getResponseBoolean("isready"))
    {
      jButtonStart.setEnabled(true);
      jButtonClose.setEnabled(true);
    }
    else
    {
      jButtonStart.setEnabled(false);
      jButtonClose.setEnabled(false);
    }

    if (JPM.getResponseBoolean("cancancel"))
      jButtonCancel.setEnabled(true);
    else
      jButtonCancel.setEnabled(false);
  }

  public void setVisible(boolean visible)
  {
    if (visible)
      if (autoStart)
        start();
    super.setVisible(visible);
  }

  private void close()
  {
    checkerThread = null;
    this.dispose();
  }

  private void start()
  {
    jpm.sendMessage(moduleToSendTo, startMessage, new JEdPointMessage() );
    setActiveButtons();
    checkerThread = new Thread(this);
    checkerThread.start();
  }

  void jButtonClose_actionPerformed(ActionEvent e)
  {
    close();
  }

  void jButtonStart_actionPerformed(ActionEvent e)
  {
    start();
  }

  void jButtonCancel_actionPerformed(ActionEvent e)
  {
    jpm.sendMessage(this.moduleToSendTo, this.cancelMessage, new JEdPointMessage() );
    checkerThread = null;
  }

  public void run()
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Vector tempVector;

    while (true)
    {
      JPM = jpm.sendMessage(this.moduleToSendTo, this.statusMessage, JPM);

      if (JPM.getResponseBoolean("isready"))
      {
        tempVector = (Vector)JPM.getResponse("actions");
        while (!tempVector.isEmpty())
        {
          listActions.add( (String)tempVector.elementAt(0) );
          tempVector.remove(0);
        }

        listActions.select( listActions.getItemCount()-1 );

        jPanel1.requestFocus();

        setActiveButtons();
        break;
      }
      else
      {
        // Set the active buttons here...
        if (JPM.getResponseBoolean("isready"))
        {
          this.jButtonStart.setEnabled(true);
          this.jButtonClose.setEnabled(true);
        }
        else
        {
          this.jButtonStart.setEnabled(false);
          this.jButtonClose.setEnabled(false);
        }

        if (JPM.getResponseBoolean("cancancel"))
          this.jButtonCancel.setEnabled(true);
        else
          this.jButtonCancel.setEnabled(false);

        tempVector = (Vector)JPM.getResponse("actions");
        while (!tempVector.isEmpty())
        {
          this.listActions.add( (String)tempVector.elementAt(0) );
          tempVector.remove(0);
        }
        listActions.select( listActions.getItemCount()-1 );
      }
      try
      {
        Thread.sleep(100);
      }
      catch (Exception e)
      {
      }
    } // while

    // Give the focus to the close button
    jButtonClose.requestFocus();

    // Should the window be autoclosed?
    if (autoClose)
      close();
  }
}
