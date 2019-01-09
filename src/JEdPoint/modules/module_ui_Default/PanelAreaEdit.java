package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class PanelAreaEdit extends JPanel
{
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JTextField jTextFieldAreaID = new JTextField();
  JTextField jTextFieldDescription = new JTextField();
  JTextField jTextFieldTearline = new JTextField();
  JTextField jTextFieldOrigin = new JTextField();
  JLabel jLabel4 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JTextField jTextFieldReplyIn = new JTextField();
  JCheckBox jCheckBoxReadOnly = new JCheckBox();
  JButton jButtonOk = new JButton();
  JButton jButtoncancel = new JButton();

  // MAD
  MessageAreaData mad;
  JDialog parentDialog;
  boolean dataOK = true;      // Becomes false if cancel is clickedsså

  public PanelAreaEdit(JDialog parentDialog, MessageAreaData newMAD)
  {
    this.parentDialog = parentDialog;
    this.mad = newMAD;
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

    this.setSize(new Dimension(487, 224));
    this.setLayout(null);

    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("Area ID");
    jLabel1.setBounds(new Rectangle(5, 10, 82, 17));
    jLabel1.setToolTipText("This area's ID");

    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setText("Description");
    jLabel2.setBounds(new Rectangle(5, 30, 82, 17));
    jLabel2.setToolTipText("This area's description");

    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setText("Tearline");
    jLabel3.setBounds(new Rectangle(5, 54, 82, 17));
    jLabel3.setToolTipText("The tearline (either a text or a file name) to be used in this area");

    jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel4.setText("Origin");
    jLabel4.setBounds(new Rectangle(5, 78, 82, 17));
    jLabel4.setToolTipText("The origin (either a text or a file name) to be used in this area");

    jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel5.setText("Reply in area");
    jLabel5.setBounds(new Rectangle(5, 103, 82, 17));
    jLabel5.setToolTipText("The area which replies will automatically be placed in");

    jTextFieldAreaID.setBounds(new Rectangle(91, 7, 377, 21));
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

    // Set the values of the textfields to whatever is in the mad
    jTextFieldAreaID.setText(         (String)mad.getMessageAreaData("id") );
    jTextFieldDescription.setText(    (String)mad.getMessageAreaData("description"));
    jTextFieldTearline.setText(       (String)mad.getMessageAreaData("tearline"));
    jTextFieldOrigin.setText(         (String)mad.getMessageAreaData("origin"));
    jTextFieldReplyIn.setText(        (String)mad.getMessageAreaData("replyin"));

    tempBoolean = (Boolean)mad.getMessageAreaData("readonly");
    if (tempBoolean.booleanValue() == true)
      jCheckBoxReadOnly.setSelected(true);
    else
      jCheckBoxReadOnly.setSelected(false);

    this.add(jLabel1, null);
    this.add(jLabel2, null);
    this.add(jLabel3, null);
    this.add(jLabel4, null);
    this.add(jLabel5, null);
    this.add(jTextFieldAreaID, null);
    this.add(jTextFieldDescription, null);
    this.add(jTextFieldTearline, null);
    this.add(jTextFieldOrigin, null);
    this.add(jTextFieldReplyIn, null);
    this.add(jCheckBoxReadOnly, null);
    this.add(jButtoncancel, null);
    this.add(jButtonOk, null);
  }

  public MessageAreaData retrieveInfo()
  {
    if (this.dataOK)
    {
      MessageAreaData returnMAD = new MessageAreaData();

      returnMAD.setMessageAreaData( "id",           jTextFieldAreaID.getText() );
      returnMAD.setMessageAreaData( "description",  jTextFieldDescription.getText() );
      returnMAD.setMessageAreaData( "tearline",     jTextFieldTearline.getText() );
      returnMAD.setMessageAreaData( "origin",       jTextFieldOrigin.getText() );
      returnMAD.setMessageAreaData( "replyin",      jTextFieldReplyIn.getText() );
      if (jCheckBoxReadOnly.isSelected())
        returnMAD.setMessageAreaData( "readonly", new Boolean(true));
      else
        returnMAD.setMessageAreaData( "readonly", new Boolean(false));

        return returnMAD;
    }
    else
      return null;
  }

  private boolean CheckForErrors()
  {
    boolean ok = true;

    if (jTextFieldAreaID.getText().length() == 0)
    {
      JOptionPane.showConfirmDialog(this, "Give the area a proper name!", "JEdPoint", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
      jTextFieldAreaID.requestFocus();
      ok = false;
    }

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