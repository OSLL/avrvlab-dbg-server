package avr_debug_server;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


public class AddNewDeviceFrame extends JFrame {
	private static final long serialVersionUID = 8358799503638401632L;
	private JComboBox<String> pathComboBox;
	private JComboBox<String> targetComboBox;
	private JTextField idTextField;
	private MainFrame mainFrame;
	
	public AddNewDeviceFrame(MainFrame mainFrame) {
		super("Add new device");
		this.mainFrame = mainFrame;
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		createGui();
	}
	
	@Override
	public void setVisible(boolean b) {
		if(b){
			pathComboBox.removeAllItems();
			String[] devices = DeviceDispatcher.getSystemDeviceList();
			for(String s : devices)
				pathComboBox.addItem(s);
			targetComboBox.setSelectedIndex(0);
			idTextField.setText("");
		}
		super.setVisible(b);
	}
	
	private void createGui(){
		/*Path selecting*/
		Box box1 = Box.createHorizontalBox();
		JLabel label1 = new JLabel("Path:");
		pathComboBox = new JComboBox<String>();
		pathComboBox.setPreferredSize(new Dimension(160, 20));
		box1.add(label1);
		box1.add(Box.createHorizontalStrut(15));
		box1.add(pathComboBox);
		
		/*Target selecting*/
		Box box2 = Box.createHorizontalBox();
		JLabel label2 = new JLabel("Target:");
		targetComboBox = new JComboBox<String>(DeviceDispatcher.getSupportedDevices());
		targetComboBox.setPreferredSize(new Dimension(160, 20));
		box2.add(label2);
		box2.add(Box.createHorizontalStrut(1));
		box2.add(targetComboBox);
		
		/*id field*/
		Box box3 = Box.createHorizontalBox();
		JLabel label3 = new JLabel("id:");
		idTextField = new JTextField(5);
		idTextField.setPreferredSize(new Dimension(50, 20));
		idTextField.setMaximumSize(idTextField.getPreferredSize());
		box3.add(label3);
		box3.add(Box.createHorizontalStrut(35));
		box3.add(idTextField);
		box3.add(Box.createHorizontalGlue());
		
		/*Add button*/
		Box box4 = Box.createHorizontalBox();
		JButton button = new JButton("Add");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					int id = Integer.parseInt(idTextField.getText());
					String selectedPath = (String)pathComboBox.getSelectedItem();
					if(selectedPath == null)
						return;
					if(!mainFrame.addNewDevice(id, (String)targetComboBox.getSelectedItem(),selectedPath))
						return;
				}catch(NumberFormatException e){
					return;
				}
				setVisible(false);
			}
		});
		box4.add(button);
		
		/*Main box*/
		Box mainBox = Box.createVerticalBox();
		mainBox.setBorder(new EmptyBorder(12, 12, 12, 12));
		mainBox.add(box1);
		mainBox.add(Box.createVerticalStrut(12));
		mainBox.add(box2);
		mainBox.add(Box.createVerticalStrut(12));
		mainBox.add(box3);
		mainBox.add(Box.createVerticalStrut(12));
		mainBox.add(button);
		getContentPane().add(mainBox);
		pack();
	}
	
}
