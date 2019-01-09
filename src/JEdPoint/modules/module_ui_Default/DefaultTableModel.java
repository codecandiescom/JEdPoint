package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.io.Serializable;
import java.util.*;

public class DefaultTableModel implements TableModel, Serializable
{
  Object moduleData[][] = new Object[1][1];
  String columnNameArray[] = new String[1];
  Object columnTypeArray[] = new Object[1];

  public void initColumns(int newColumns)
  {
    columnNameArray = new String[newColumns];
    columnTypeArray = new Object[newColumns];
  }

  public void initRows(int newRows)
  {
    moduleData = new Object[newRows][ columnNameArray.length ];
  }

  public void setColumnNames(Object newNameArray[])
  {
    int counter;

    for (counter=0; counter<newNameArray.length; counter++)
    {
      columnNameArray[counter] = newNameArray[counter].toString();
    }
  }

  public void setColumnTypes(Object newTypeArray[])
  {
    int counter;

    for (counter=0; counter<newTypeArray.length; counter++)
    {
      columnTypeArray[counter] = newTypeArray[counter];
    }
  }

  public Object getColumnType(int column)
  {
    return columnTypeArray[column];
  }

  public String getColumnName(int column)
  {
    return columnNameArray[column];
  }

  public void removeTableModelListener(TableModelListener tml)
  {
  }

  public void addTableModelListener(TableModelListener tml)
  {
  }

  public void setValueAt(Object newValue, int row, int column)
  {
    String tempString;

    // We handle dates separately because converting it to a normal string
    // would make it far too long.
    if (newValue instanceof GregorianCalendar)
    {
      GregorianCalendar gCal  = (GregorianCalendar)newValue;
      moduleData[row][column] = JEdPointUtilities.GregorianCalendarToString(gCal);
    }
    else
      moduleData[row][column] = newValue;
  }

  public boolean getSelected(int row)
  {
    return true;
  }

  public void setSelected(int row, boolean newValue)
  {
  }

  public Object getValueAt(int row, int column)
  {
    return moduleData[row][column];
  }

  public boolean isCellEditable(int row, int column)
  {
    return false;
  }

  public Class getColumnClass(int column)
  {
    return columnTypeArray[column].getClass();
  }

  public int getColumnCount()
  {
    return columnNameArray.length;
  }

  public int getRowCount()
  {
    return moduleData.length;
  }
}
