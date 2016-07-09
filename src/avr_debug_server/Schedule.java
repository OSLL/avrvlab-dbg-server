package avr_debug_server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.SortedSet;

import javax.swing.JPanel;

public class Schedule extends JPanel{
	private static final long serialVersionUID = -3103181360421043001L;
	private final int mcuInfoBlockWidth = 120;
	private final int mcuInfoBlockHeight = 30;
	private final int timeBlockWidth = 40;
	private final int timeBlockHeight = 30;
	private final int maxHeight = 24*timeBlockHeight+mcuInfoBlockHeight;;
	SortedSet<ReserveListItem> reserve;
	DevicesTableModel devices;
	public Schedule(SortedSet<ReserveListItem> reserve, DevicesTableModel devices) {
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				x-=timeBlockWidth;
				y-=mcuInfoBlockHeight;
				if(x<=0 || y<=0)
					return;
				x/=mcuInfoBlockWidth;
				int y1 = (y%timeBlockHeight)*2;
				y/=timeBlockHeight;
				System.out.println("MCU "+ x + "time: "+y+":"+y1 );
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		this.reserve = reserve;
		this.devices = devices;
		
		//setPreferredSize(new Dimension(300, maxHeight));
		//setPreferredSize(new Dimension(devices.getRowCount()*mcuInfoBlockWidth+timeBlockWidth, maxHeight));
		//setPreferredSize(new Dimension((devices.getRowCount()+1)*mcuInfoBlockWidth+timeBlockWidth, maxHeight));
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		paintMcuInfo(g2d, timeBlockWidth, 0);
		paintTimeScale(g2d, 0, mcuInfoBlockHeight);
		paintTimeNet(g2d, timeBlockWidth, mcuInfoBlockHeight);
		colorTime(g2d);
		setPreferredSize(new Dimension((devices.getRowCount()+1)*mcuInfoBlockWidth+timeBlockWidth, maxHeight));
		//repaint();
	}
	
	private void colorTime(Graphics2D g2d){
		g2d.setColor(new Color(135, 206, 235));
		for(ReserveListItem cur : reserve){
			//if()
			Calendar start = cur.getStartTime();
			Calendar end = cur.getEndTime();
			int mcuNumber = -1;
			for(int i=0;i<devices.getRowCount();i++){
				if((int)devices.getValueAt(i,0) == cur.getMcuId()){
					mcuNumber = i;
					break;
				}
			}
			if(mcuNumber<0)
				return;
			int x1 = timeBlockWidth + mcuNumber*mcuInfoBlockWidth+1;
			int y1 = mcuInfoBlockHeight + start.get(Calendar.HOUR_OF_DAY)*timeBlockHeight + start.get(Calendar.MINUTE)/2;
			int x2 = mcuInfoBlockWidth-1;
			int y2 = (int)((end.getTimeInMillis() - start.getTimeInMillis())/(1000*60*2));
			g2d.fillRect(x1, y1, x2, y2);
		}
		//g2d.setColor(Color.LIGHT_GRAY);
		
		
	}
	
	private void paintMcuInfo(Graphics2D g2d, int x, int y){
		for(int i=0;i<devices.getRowCount();i++){
			g2d.setColor(Color.darkGray);
			g2d.drawRect(x+i*mcuInfoBlockWidth, y, mcuInfoBlockWidth, mcuInfoBlockHeight);
			g2d.setColor(Color.BLACK);
			g2d.drawString("#"+devices.getValueAt(i, 0), x+i*mcuInfoBlockWidth+2, y+10+2);
			g2d.drawString(""+devices.getValueAt(i, 2), x+i*mcuInfoBlockWidth+2, y+20+5);	
		}
		
		
	}
	
	private void paintTimeScale(Graphics2D g2d, int x, int y){
		for(int i = 0; i<24; i++){
			paintTimeScaleBlock(g2d, x, y+i*timeBlockHeight, i+":00");
		}
	}
	
	private void paintTimeScaleBlock(Graphics2D g2d, int x, int y, String time){
		g2d.setColor(Color.darkGray);
		g2d.drawRect(x, y, timeBlockWidth, timeBlockHeight);
		g2d.setColor(Color.BLACK);
		g2d.drawString(time, x+2, y+10+2);
	}
	
	private void paintTimeNet(Graphics2D g2d, int x, int y){
		g2d.setColor(Color.GRAY);
		for(int j=1;j<=devices.getRowCount();j++){
			for(int i=0; i<24; i++){
				g2d.drawLine(x, y + i*timeBlockHeight, x+devices.getRowCount()*mcuInfoBlockWidth, y + i*timeBlockHeight);
			}
			g2d.drawLine(x+j*mcuInfoBlockWidth, y, x+j*mcuInfoBlockWidth, y+24*timeBlockHeight);
		}
		
		
	}

}
