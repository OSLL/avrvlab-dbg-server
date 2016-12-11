package avrdebug.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;

import avrdebug.configs.AppConfigs;
import avrdebug.configs.DeviceConfigsItem;
import avrdebug.configs.DevicesConfigs;

public class Main {
	public static void main(String[] args) {
		try {
			new DevicesConfigs(new File("devicesConfig.ini"));
		} catch (IOException e1) {
			System.err.println("Error loading devices config file (devicesConfig.ini)");
			System.exit(-1);
		}
		try {
			new AppConfigs(new File("appConfig.ini"));
		} catch (IOException e1) {
			System.err.println("Error loading config file (appConfig.ini)");
			System.exit(-2);
		}
		
		int port;
		try{
			port = Integer.parseInt(args[1]);
		}catch(NumberFormatException|IndexOutOfBoundsException e){
			port = 3129;
		}
		new MainFrame("AVR remote debuggind server", port);
		//ConnectionHandler ch = new ConnectionHandler(2424);
		
	}

}

class MainFrame extends JFrame{
	private static final long serialVersionUID = 712987842762414398L;
	private DeviceDispatcher deviceDispatcher;
	private SimulatorDispatcher simularDispatcher;
	private int serverPort;
	private ConnectionHandler connectionHandler;

	public MainFrame(String s, int port){
		super(s);

		//!!!!!!!!!!!!!!!!!
			deviceDispatcher = new DeviceDispatcher();
			simularDispatcher = new SimulatorDispatcher();
			loadDeviceConfiguration();
			serverPort = port; //???
			connectionHandler = new ConnectionHandler(serverPort, deviceDispatcher, simularDispatcher);
			connectionHandler.start();
			
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

	
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	private void loadDeviceConfiguration(){
		ArrayList<DeviceConfigsItem> devices = DevicesConfigs.getDeviceList();
		for(DeviceConfigsItem device : devices){
			if("mcu".equals(device.getProperty("type"))){
				deviceDispatcher.addNewDevice(Integer.parseInt(device.getProperty("id")), device.getProperty("model"), device.getProperty("path"));
			}
			if("simulator".equals(device.getProperty("type"))){
				simularDispatcher.addNewDevice(Integer.parseInt(device.getProperty("id")));
			}
		}
	}	
	
	private void createGui(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(550, 200);
		this.setLocationByPlatform(true);
		this.setVisible(true);

		//simulatorTable = new JTable(simulatorsModel);
		//JScrollPane pane = new JScrollPane(simulatorTable);
		//getContentPane().add(pane,BorderLayout.WEST);
		JButton button = new JButton(new ImageIcon("icons/add.png"));
		Box box = Box.createHorizontalBox();
		button.setPreferredSize(new Dimension(30, 30));
		button.setMaximumSize(button.getPreferredSize());
		box.add(Box.createHorizontalStrut(400));
		box.setBorder(new EmptyBorder(3, 3, 3, 3));
		getContentPane().add(box,BorderLayout.NORTH);
		pack();
	}

	
}