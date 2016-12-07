package avrdebug.server;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableModel;


public class DevicesTable extends JTable {
	private static final long serialVersionUID = 8112399479804595292L;
	private final TableModel tableModel;
	private final MainFrame mainFrame;
	public DevicesTable(TableModel tm, MainFrame mf) {
		super(tm);
		tableModel = tm;
		mainFrame = mf;
		setRowHeight(20);
		setFocusable(false);
		getColumn("id").setMaxWidth(30);
		getColumn("del").setMaxWidth(50);
		getColumn("status").setMaxWidth(50);
		getColumn("target").setMaxWidth(150);
		getColumn("path").setMaxWidth(150);
		getColumn("del").setCellEditor(new DefaultCellEditor(new JComboBox<>()){
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				mainFrame.removeDevice((int)tableModel.getValueAt(row, 0));
				return null;
			}
		});
	}
}
