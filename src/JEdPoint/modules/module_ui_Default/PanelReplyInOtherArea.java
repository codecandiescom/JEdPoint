package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

// ---------------------------------------------------------------------------
// PANEL REPLY IN OTHER AREA
// ---------------------------------------------------------------------------
public class PanelReplyInOtherArea extends JPanel
{
  BorderLayout borderLayout1 = new BorderLayout();
  java.awt.List listAreas = new java.awt.List();

  // Variables
  JEdPointModule jpm;
  JDialog parentDialog;
  boolean dataOK = false;

  public PanelReplyInOtherArea(JDialog newParentDialog, JEdPointModule newJPM)
  {
    this.parentDialog = newParentDialog;
    this.jpm = newJPM;

    try
    {
      jbInit();
      setListData();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public boolean isDataOK()
  {
    return this.dataOK;
  }

  public String returnSelectedArea()
  {
    return listAreas.getSelectedItem();
  }

  private void setListData()
  {
    JEdPointMessage JPM = new JEdPointMessage();
    Vector tempVector;

    JPM = jpm.sendMessage(JEdPointModule.moduleMessageBase, JEdPointMessage.mbListAreas, JPM);

    tempVector = (Vector)JPM.getResponse("areas");

    while (!tempVector.isEmpty())
    {
      listAreas.add( (String)tempVector.elementAt(0) );
      tempVector.remove(0);
    }
  }

  void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    this.setSize(new Dimension(200, 300));
    listAreas.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        listAreas_keyPressed(e);
      }
    });
    this.add(listAreas, BorderLayout.CENTER);
  }

  void listAreas_keyPressed(KeyEvent e)
  {
    switch (e.getKeyCode())
    {
      case KeyEvent.VK_ENTER:
        this.dataOK = true;
        parentDialog.dispose();
      break;
      case KeyEvent.VK_ESCAPE:
        parentDialog.dispose();
      break;
    }
  }
}
