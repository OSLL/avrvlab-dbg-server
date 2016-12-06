package avr_debug_server;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

public class SimulatorsTableModel extends AbstractTableModel{
	private CopyOnWriteArrayList<Simulator> devices;

	public SimulatorsTableModel(CopyOnWriteArrayList<Simulator> devices) {
		this.devices = devices;
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return devices.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "id";
		case 1:
			return "status";
		default:
			return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		default:
			return String.class;
		}
	}	
	
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Simulator var = devices.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return var.getNumber();
		case 1:
			return var.getStatus();
		case 2:
		default:
			return "";
		}
	}
}
