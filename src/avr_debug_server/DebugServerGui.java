package avr_debug_server;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DebugServerGui {
	static ConnectionHandler handler;
	public static void main(String[] args) {
		MainFrame frame = new MainFrame("Remote debug server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(550, 200);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		int port=3129;
		try{
			if(args.length>1){
				port = Integer.parseInt(args[1]);
			}
		}catch(NumberFormatException|IndexOutOfBoundsException e){
			port = 3129;
		}
		handler = new ConnectionHandler(frame, port);
		handler.handle();
	}
}

class MainFrame extends JFrame {
	private static final long serialVersionUID = 4984240959690544441L;
	private JComboBox<String> programmerPath;
	private JComboBox<String> targetName;
	private JTextField port;
	private JPanel mainPanel;
	private int fieldSize = 35;
	
	public MainFrame(String s){
		super(s);
		createGui();
	}
	
	private JPanel createTextFieldPair(String s, JComponent field){
		JPanel panel = new JPanel();
		JTextField label = new JTextField(s);
		label.setEditable(false);
		label.setColumns(10);
		label.setFocusable(false);
		panel.add(label);
		panel.add(field);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		return panel;
	}
	
	public String getProgrammerPath() {
		return (String)programmerPath.getSelectedItem();
	}
	public String getTargetName() {
		return (String) targetName.getSelectedItem();
	}
	 public String getPort() {
		return port.getText();
	}
	
	public void clientConnected(){
		programmerPath.setEnabled(false);
		targetName.setEnabled(false);
		port.setEnabled(false);
	}
	
	public void clientDisconnected(){
		programmerPath.setEnabled(true);
		targetName.setEnabled(true);
		port.setEnabled(true);		
	}
	
	private void createGui(){
		programmerPath = new JComboBox<String>(DeviceFinder.printList());
		JPanel programmerPathPanel = createTextFieldPair("Программатор-отладчик: ", programmerPath);
		JButton refreshButton = new JButton(new ImageIcon("refresh.png"));
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				programmerPath.removeAllItems();
				for(String s : DeviceFinder.printList())
					programmerPath.addItem(s);
			}
		});
		refreshButton.setMaximumSize(new Dimension(25, 25));
		programmerPathPanel.add(refreshButton);
		targetName = new JComboBox<String>(loadSupportedDevices("avarice_supported_devices.txt"));
		targetName.setSelectedItem("atmega128");
		JPanel targetNamePanel = createTextFieldPair("Микроконтроллер: ", targetName);
		port = new JTextField("4242");
		port.setColumns(fieldSize);
		JPanel portPanel = createTextFieldPair("Порт AVaRICE: ", port);
		JPanel configPanel = new JPanel();
		configPanel.add(programmerPathPanel);
		configPanel.add(targetNamePanel);
		configPanel.add(portPanel);
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		mainPanel = new JPanel();
		mainPanel.add(configPanel);
		getContentPane().add(mainPanel);
	}
	
	private String[] loadSupportedDevices(String filename){
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(filename));
		String s;
			while((s=in.readLine())!=null){
				list.add(s);
			}
			in.close();
		} catch (IOException e) {
			String result[] = {""};
			return result;
		}
		String result[] = new String[list.size()];
		list.toArray(result);
		return result;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
