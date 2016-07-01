package avr_debug_server;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DevicesTableModel implements TableModel {
	private Set<TableModelListener> listeners = new HashSet<>();
	private List<DevicesTableElement> devices;
	
	public DevicesTableModel(List<DevicesTableElement> devices) {
		this.devices = devices;
	}
	
	@Override
	public int getRowCount() {
		return devices.size();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "id";
		case 1:
			return "path";
		case 2:
			return "target";
		case 3:
			return "status";
		case 4:
			return "del";
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
		case 2:
			return String.class;
		case 3:
			return ImageIcon.class;
		case 4:
			return ImageIcon.class;
		default:
			return String.class;
		}
		
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex ==4 )return true;
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DevicesTableElement var = devices.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return var.getNumber();
		case 1:
			return var.getPath();
		case 2:
			return var.getTarget();
		case 3:
			return var.getStatus();
		case 4:
			return var.getTrashBin();
		default:
			return "";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

}
