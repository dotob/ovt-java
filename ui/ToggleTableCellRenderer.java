package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

//============ cell renderer, for alternating cell color ========================

public class ToggleTableCellRenderer extends DefaultTableCellRenderer {
	public static final long	serialVersionUID	= 0;
	private boolean				anythingbold		= false;

	public ToggleTableCellRenderer() {
		super();
		this.anythingbold = false;
	}

	public ToggleTableCellRenderer(boolean anythingbold) {
		super();
		this.anythingbold = anythingbold;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Color highlight = new Color(235, 240, 255);
		if (!isSelected) {
			int tmp = row % 2;
			if (tmp == 0) {
				cell.setBackground(highlight);
			} else {
				cell.setBackground(Color.white);
			}
			if (anythingbold) {
				cell.setFont(new Font("Tahoma", Font.BOLD, 12));
			}
		}

		return cell;
	}
}