package JEdPoint.modules.module_ui_Default;

import JEdPoint.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer
{
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    DefaultTableModel dtm = (DefaultTableModel)table.getModel();
    Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (dtm.getSelected(row)) cell.setBackground(new Color(255,255,0) );
    return cell;
  }
}
