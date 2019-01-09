package JEdPoint.modules.module_ui_Default;

import java.io.Serializable;
import java.awt.Dimension;
import java.util.*;
import javax.swing.table.*;

public class module_ui_default_settings implements Serializable
{
  public Dimension MainWindowDimension;
  public int AreaColumnWidths[];
  public int MessageColumnWidths[];
  public boolean viewKludges = false;

  public module_ui_default_settings()
  {
    MainWindowDimension = new Dimension();
    AreaColumnWidths = new int[0];
    MessageColumnWidths = new int[0];
  }

  public int[] saveWidths(TableColumnModel tcm)
  {
    int counter;

    int temparray[] = new int[ tcm.getColumnCount() ];

    for (counter=0; counter<tcm.getColumnCount(); counter++)
    {
      temparray[counter] = tcm.getColumn(counter).getWidth();
    }
    return temparray;
  }

  public void restoreWidths(TableColumnModel tcm, int Array[])
  {
    int counter;

    // The column count != array size, foggedaboudid.
    if ( Array.length != tcm.getColumnCount() )
      return;

    for (counter=0; counter<Array.length; counter++)
    {
      tcm.getColumn(counter).setPreferredWidth( Array[counter] );
    }
  }

  public HashMap saveSettings()
  {
    HashMap map = new HashMap();
    map.put("mainwindowdimension", MainWindowDimension);
    map.put("areacolumnwidths", AreaColumnWidths);
    map.put("messagecolumnwidths", MessageColumnWidths);
    map.put("viewkludges", new Boolean(viewKludges));
    return map;
  }

  public void loadSettings(HashMap map)
  {
    Boolean tempBoolean;

    MainWindowDimension = (Dimension)map.get("mainwindowdimension");
    AreaColumnWidths = (int[])map.get("areacolumnwidths");
    MessageColumnWidths = (int[])map.get("messagecolumnwidths");

    tempBoolean = (Boolean)map.get("viewkludges");
    viewKludges = tempBoolean.booleanValue();
  }
}
