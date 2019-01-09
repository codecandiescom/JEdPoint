package JEdPoint.modules.module_messageeditor_Default;

import JEdPoint.*;
import javax.swing.UIManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;

public class module_messageeditor_default_settings implements Serializable
{
  public Dimension MainWindowDimension;

  public module_messageeditor_default_settings()
  {
    MainWindowDimension = new Dimension();
  }

  public HashMap saveSettings()
  {
    HashMap map = new HashMap();
    map.put("mainwindowdimension", MainWindowDimension);
    return map;
  }

  public void loadSettings(HashMap map)
  {
    MainWindowDimension = (Dimension)map.get("mainwindowdimension");
  }
}
