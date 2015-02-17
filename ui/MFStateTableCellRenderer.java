package ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


//============ cell renderer, for alternating cell color ========================

public class MFStateTableCellRenderer extends DefaultTableCellRenderer
{
	public static final long serialVersionUID = 0;
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
													boolean hasFocus, int row, int column)
  {
    Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      			
  	Color highlight = new Color(235,240,255);
  	if (!isSelected){
  		int tmp = row % 6;
  		if (tmp<3)
  			cell.setBackground(highlight);
  		else
  			cell.setBackground(Color.white);
  	}
 	
      return cell;
  }
}