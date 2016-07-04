package avr_debug_server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.plaf.SliderUI;

public class Main {
	public static void main(String[] args) {
		int port;
		try{
			port = Integer.parseInt(args[1]);
		}catch(NumberFormatException|IndexOutOfBoundsException e){
			port = 3129;
		}
		MainFrame mainFrame = new MainFrame("AVR remote debuggind server", port);
		//ConnectionHandler ch = new ConnectionHandler(2424);
		
	}

}

class MainFrame extends JFrame{
	private static final long serialVersionUID = 712987842762414398L;
	private DeviceDispatcher deviceDispatcher;
	private DevicesTableModel tableModel;
	private DevicesTable table;
	private AddNewDeviceFrame addDeviceWindow;
	private int serverPort;
	private ConnectionHandler connectionHandler;
	public MainFrame(String s, int port){
		super(s);		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(550, 200);
		this.setLocationByPlatform(true);
		this.setVisible(true);
		deviceDispatcher = new DeviceDispatcher();
		tableModel = deviceDispatcher.getModel();
		addDeviceWindow = new AddNewDeviceFrame(this);
		serverPort = port;
		connectionHandler = new ConnectionHandler(port, deviceDispatcher);
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
				connectionHandler.interrupt();
				deviceDispatcher.stopAllService();
			}
			@Override
			public void windowClosed(WindowEvent e) {
			}
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		createGui();
	}
	
	private void createGui(){
		table = new DevicesTable(tableModel, this);
		JScrollPane pane = new JScrollPane(table);
		getContentPane().add(pane,BorderLayout.WEST);
		JButton button = new JButton("Add");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addDeviceWindow.setVisible(true);
			}
		});
		getContentPane().add(button ,BorderLayout.NORTH);
		pack();
	}
	
	public boolean addNewDevice(int number, String target, String path){
		if(deviceDispatcher.addNewDevice(number, target, path)){
			table.revalidate();
			return true;
		}
		return false;
	}
	
	
	public boolean removeDevice(int number){
		if(deviceDispatcher.removeDevice(number)){
			table.revalidate();
			return true;
		}
		return false;
	}

	
	
}