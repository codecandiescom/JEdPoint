package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class DialogPoll extends JDialog implements Runnable
{
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel1 = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JButton jButtonSend = new JButton();
  JButton jButtonReceive = new JButton();
  JButton jButtonPoll = new JButton();
  JButton jButtonCancel = new JButton();
  JButton jButtonClose = new JButton();
  java.awt.List listActions = new java.awt.List();

  JEdPointModule jpm;
  Thread checkerThread;

  public DialogPoll(Frame ownerFrame, JEdPointModule newjpm)
  {
    super(ownerFrame, "Poll", true);

    try
    {
      jbInit();
      pack();
      setDialogSize(ownerFrame);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }

    jpm = newjpm;

    setActiveButtons();
  }

  void jbInit() throws Exception
  {
    panel1.setLayout(borderLayout1);
    jPanel1.setLayout(flowLayout1);
    jButtonPoll.setText("Poll (send&receive)");
    jButtonPoll.setMnemonic('P');
    jButtonPoll.setToolTipText("Send outgoing mail and get new mail from your boss");
    jButtonPoll.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonPoll_actionPerformed(e);
      }
    });
    jButtonCancel.setText("Cancel");
    jButtonCancel.setToolTipText("Cancel the current action");
    jButtonCancel.setMnemonic('C');
    jButtonCancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonCancel_actionPerformed(e);
      }
    });
    jButtonClose.setText("Close");
    jButtonClose.setToolTipText("Close this window");
    jButtonClose.setMnemonic('L');
    jButtonClose.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonClose_actionPerformed(e);
      }
    });
    panel1.setPreferredSize(new Dimension(500, 400));
    jButtonSend.setText("Send");
    jButtonSend.setToolTipText("Send outgoing mail");
    jButtonSend.setMnemonic('S');
    jButtonSend.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonSend_actionPerformed(e);
      }
    });
    jButtonReceive.setText("Receive");
    jButtonReceive.setToolTipText("Get new mail from your boss");
    jButtonReceive.setMnemonic('R');
    jButtonReceive.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonReceive_actionPerformed(e);
      }
    });
    getContentPane().add(panel1);
    panel1.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(jButtonPoll, null);
    jPanel1.add(jButtonSend, null);
    jPanel1.add(jButtonReceive, null);
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

  void setActiveButtons()
  {
    JEdPointMessage JPM = new JEdPointMessage();

    jpm.sendMessage(JEdPointModule.modulePoll, JEdPointMessage.pollGetStatus, JPM);

    if (JPM.getResponseBoolean("isready"))
    {
      jButtonPoll.setEnabled(true);
      jButtonReceive.setEnabled(true);
      jButtonSend.setEnabled(true);
      jButtonClose.setEnabled(true);
    }
    else
    {
      jButtonPoll.setEnabled(false);
      jButtonReceive.setEnabled(false);
      jButtonSend.setEnabled(false);
      jButtonClose.setEnabled(false);
    }

    if (JPM.getResponseBoolean("cancancel"))
      jButtonCancel.setEnabled(true);
    else
      jButtonCancel.setEnabled(false);

  }

  void jButtonClose_actionPerformed(ActionEvent e)
  {
    this.dispose();
  }

  void jButtonPoll_actionPerformed(ActionEvent e)
  {
    jpm.sendMessage(JEdPointModule.modulePoll, JEdPointMessage.pollFullPoll, new JEdPointMessage() );
    setActiveButtons();
    checkerThread = new Thread(this);
    checkerThread.start();
  }

  void jButtonCancel_actionPerformed(ActionEvent e)
  {
    jpm.sendMessage(JEdPointModule.modulePoll, JEdPointMessage.pollCancel, new JEdPointMessage() );
    checkerThread = null;
  }

  public void run()
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Vector tempVector;

    while (true)
    {
      JPM = jpm.sendMessage(JEdPointModule.modulePoll, JEdPointMessage.pollGetStatus, JPM);

      if (JPM.getResponseBoolean("isready"))
      {
        tempVector = (Vector)JPM.getResponse("actions");
        while (!tempVector.isEmpty())
        {
          this.listActions.add( (String)tempVector.elementAt(0) );
          tempVector.remove(0);
        }

        listActions.select( listActions.getItemCount()-1 );

        this.setActiveButtons();
        break;
      }
      else
      {
        // Set the active buttons here...
        if (JPM.getResponseBoolean("isready"))
        {
          this.jButtonPoll.setEnabled(true);
          this.jButtonClose.setEnabled(true);
        }
        else
        {
          this.jButtonPoll.setEnabled(false);
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
    }
  }

  void jButtonSend_actionPerformed(ActionEvent e)
  {
    jpm.sendMessage(JEdPointModule.modulePoll, JEdPointMessage.pollSendMail, new JEdPointMessage() );
    setActiveButtons();
    checkerThread = new Thread(this);
    checkerThread.start();
  }

  void jButtonReceive_actionPerformed(ActionEvent e)
  {
    jpm.sendMessage(JEdPointModule.modulePoll, JEdPointMessage.pollGetMail, new JEdPointMessage() );
    setActiveButtons();
    checkerThread = new Thread(this);
    checkerThread.start();
  }
}

