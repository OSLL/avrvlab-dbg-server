package avr_debug_server;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
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
		frame.setSize(550, 450);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		handler = new ConnectionHandler(frame);
		handler.handle();
	}
	
}



class MainFrame extends JFrame {
	private static final long serialVersionUID = 4984240959690544441L;
	private JComboBox programmerPath;
	private JTextField targetName;
	private JTextField port;
	private JButton buttonTest;
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
		return targetName.getText();
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

		programmerPath = new JComboBox((new DeviceFinder()).printList());
		JPanel programmerPathPanel = createTextFieldPair("Programmer path: ", programmerPath);
		
		//
		targetName = new JTextField("atmega128");
		targetName.setColumns(fieldSize);
		JPanel targetNamePanel = createTextFieldPair("Target name: ", targetName);
		
		//
		port = new JTextField("4242");
		port.setColumns(fieldSize);
		JPanel portPanel = createTextFieldPair("Port: ", port);
		
	
		//
		buttonTest = new JButton("Press to Test");

		JPanel configPanel = new JPanel();
		configPanel.add(programmerPathPanel);
		configPanel.add(targetNamePanel);
		configPanel.add(portPanel);
		//configPanel.add(buttonTest);
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		mainPanel = new JPanel();
		mainPanel.add(configPanel);
		
		getContentPane().add(mainPanel);

	}
	
	@Override
	protected void finalize() throws Throwable {
		
		super.finalize();
	}
}
