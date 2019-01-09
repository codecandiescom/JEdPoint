package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class PanelAreasEdit extends JPanel
{
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JTextField jTextFieldDescription = new JTextField();
  JTextField jTextFieldTearline = new JTextField();
  JTextField jTextFieldOrigin = new JTextField();
  JLabel jLabel4 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JTextField jTextFieldReplyIn = new JTextField();
  JCheckBox jCheckBoxReadOnly = new JCheckBox();
  JButton jButtonOk = new JButton();
  JButton jButtoncancel = new JButton();

  JDialog parentDialog;
  boolean dataOK = true;
  JCheckBox jCheckBoxDescription = new JCheckBox();
  JCheckBox jCheckBoxChangeReadOnly = new JCheckBox();
  JCheckBox jCheckBoxTearline = new JCheckBox();
  JCheckBox jCheckBoxReplyInArea = new JCheckBox();
  JCheckBox jCheckBoxOrigin = new JCheckBox();
  JLabel jLabel1 = new JLabel();      // Becomes false if cancel is clickedsså

  public PanelAreasEdit(JDialog parentDialog)
  {
    this.parentDialog = parentDialog;
    try
    {
      jbInit();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception
  {
    Boolean tempBoolean;

    this.setSize(new Dimension(507, 224));
    this.setLayout(null);


    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setText("Description");
    jLabel2.setBounds(new Rectangle(5, 30, 82, 17));
    jLabel2.setToolTipText("Area description");

    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setText("Tearline");
    jLabel3.setBounds(new Rectangle(5, 54, 82, 17));
    jLabel3.setToolTipText("Either a line of text or a file name");

    jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel4.setText("Origin");
    jLabel4.setBounds(new Rectangle(5, 78, 82, 17));
    jLabel4.setToolTipText("Either a line of text of a file name");

    jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel5.setText("Reply in area");
    jLabel5.setBounds(new Rectangle(5, 103, 82, 17));
    jLabel5.setToolTipText("Which area which replies will automatically be placed in");

    jTextFieldDescription.setBounds(new Rectangle(91, 30, 377, 21));
    jTextFieldTearline.setBounds(new Rectangle(91, 53, 377, 21));
    jTextFieldOrigin.setBounds(new Rectangle(91, 77, 377, 21));
    jTextFieldReplyIn.setBounds(new Rectangle(91, 101, 377, 21));

    jCheckBoxReadOnly.setText("Read-Only");
    jCheckBoxReadOnly.setBounds(new Rectangle(91, 125, 90, 25));
    jCheckBoxReadOnly.setToolTipText("Can new messages be written in this area?");

    jButtonOk.setToolTipText("");
    jButtonOk.setText("Ok");
    jButtonOk.setBounds(new Rectangle(79, 158, 79, 27));
    jButtonOk.setMnemonic('O');
    jButtonOk.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonOk_actionPerformed(e);
      }
    });

    jButtoncancel.setText("Cancel");
    jButtoncancel.setBounds(new Rectangle(314, 158, 79, 27));
    jButtoncancel.setMnemonic('C');
    jButtoncancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtoncancel_actionPerformed(e);
      }
    });

    jCheckBoxDescription.setMaximumSize(new Dimension(20, 20));
    jCheckBoxDescription.setMinimumSize(new Dimension(20, 20));
    jCheckBoxDescription.setPreferredSize(new Dimension(20, 20));
    jCheckBoxDescription.setBounds(new Rectangle(471, 30, 14, 19));
    jCheckBoxChangeReadOnly.setMaximumSize(new Dimension(20, 20));
    jCheckBoxChangeReadOnly.setMinimumSize(new Dimension(20, 20));
    jCheckBoxChangeReadOnly.setPreferredSize(new Dimension(20, 20));
    jCheckBoxChangeReadOnly.setBounds(new Rectangle(471, 132, 14, 15));
    jCheckBoxTearline.setMaximumSize(new Dimension(20, 20));
    jCheckBoxTearline.setMinimumSize(new Dimension(20, 20));
    jCheckBoxTearline.setPreferredSize(new Dimension(20, 20));
    jCheckBoxTearline.setBounds(new Rectangle(471, 53, 13, 18));
    jCheckBoxReplyInArea.setMaximumSize(new Dimension(20, 20));
    jCheckBoxReplyInArea.setMinimumSize(new Dimension(20, 20));
    jCheckBoxReplyInArea.setPreferredSize(new Dimension(20, 20));
    jCheckBoxReplyInArea.setBounds(new Rectangle(471, 102, 13, 16));
    jCheckBoxOrigin.setMaximumSize(new Dimension(20, 20));
    jCheckBoxOrigin.setMinimumSize(new Dimension(20, 20));
    jCheckBoxOrigin.setPreferredSize(new Dimension(20, 20));
    jCheckBoxOrigin.setBounds(new Rectangle(471, 77, 13, 19));
    jLabel1.setText("Change");
    jLabel1.setBounds(new Rectangle(453, 8, 46, 17));
    this.add(jLabel2, null);
    this.add(jLabel3, null);
    this.add(jLabel4, null);
    this.add(jLabel5, null);
    this.add(jTextFieldDescription, null);
    this.add(jTextFieldTearline, null);
    this.add(jTextFieldOrigin, null);
    this.add(jTextFieldReplyIn, null);
    this.add(jCheckBoxReadOnly, null);
    this.add(jButtoncancel, null);
    this.add(jButtonOk, null);
    this.add(jCheckBoxDescription, null);
    this.add(jCheckBoxTearline, null);
    this.add(jCheckBoxChangeReadOnly, null);
    this.add(jCheckBoxReplyInArea, null);
    this.add(jCheckBoxOrigin, null);
    this.add(jLabel1, null);
  }

  public MessageAreaData retrieveInfo()
  {
    if (this.dataOK)
    {
      MessageAreaData returnMAD = new MessageAreaData();
      if (jCheckBoxDescription.isSelected())
        returnMAD.setMessageAreaData( "description",  jTextFieldDescription.getText() );
      else
        returnMAD.setMessageAreaData( "description",  null );

      if (jCheckBoxTearline.isSelected())
        returnMAD.setMessageAreaData( "tearline",     jTextFieldTearline.getText() );
      else
        returnMAD.setMessageAreaData( "tearline",     null );

      if (jCheckBoxOrigin.isSelected())
        returnMAD.setMessageAreaData( "origin",       jTextFieldOrigin.getText() );
      else
        returnMAD.setMessageAreaData( "origin",       null );

      if (jCheckBoxReplyInArea.isSelected())
        returnMAD.setMessageAreaData( "replyin",      jTextFieldReplyIn.getText() );
      else
        returnMAD.setMessageAreaData( "replyin",      null );

      if (jCheckBoxChangeReadOnly.isSelected())
      {
        if (jCheckBoxReadOnly.isSelected())
          returnMAD.setMessageAreaData( "readonly", new Boolean(true));
        else
          returnMAD.setMessageAreaData( "readonly", new Boolean(false));
      }
      else
        returnMAD.setMessageAreaData( "readonly", null );

      return returnMAD;
    }
    else
      return null;
  }

  private boolean CheckForErrors()
  {
    boolean ok = true;

    return ok;
  }

  void jButtonOk_actionPerformed(ActionEvent e)
  {
    if (this.CheckForErrors())
      parentDialog.dispose();
  }

  void jButtoncancel_actionPerformed(ActionEvent e)
  {
    this.dataOK = false;
    parentDialog.dispose();
  }
}

