package avr_debug_server;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableModel;


public class DevicesTable extends JTable {
	private static final long serialVersionUID = 8112399479804595292L;
	private final TableModel tableModel;
	public DevicesTable(TableModel tm) {
		super(tm);
		tableModel = tm;
		setRowHeight(20);
		setFocusable(false);
		getColumn("del").setPreferredWidth(10);
		getColumn("status").setPreferredWidth(10);
		getColumn("del").setCellEditor(new DefaultCellEditor(new JComboBox<>()){
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				System.out.println("in" + row + " " + column);
				System.out.println(tableModel.getValueAt(row, 0));
				return null;
			}
		});
	}
}
