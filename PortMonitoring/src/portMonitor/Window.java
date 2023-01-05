package portMonitor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import portMonitor.Window;

import java.io.*;

public class Window extends JFrame{

	JComboBox<String> proto_combo, time_combo;
	Vector<String> column;
	JTextField inputIP, updateTime;
	JButton btn_start, btn_csv;
	DefaultTableModel model;
	JTable tableview;
	JScrollPane scroll;
	JLabel text;
	int count=0;

	// HashMap for saving port scanning information
	HashMap<Integer,Vector<String>> dataSet = new HashMap<Integer,Vector<String>>();

	public static Window Instance;

	Window(){
		
		Instance = this;
		
		setTitle("Port Monitoring System");
		// create panel to attach
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();


		// create component for panel1
		String protocol[] = {"Select Protocol", "TCP"};
		String time[] = {"Renewal Cycle (s)", "Do not renew", "5", "10", "15", "20", "25", "30"};
		proto_combo = new JComboBox<String>(protocol);  
		time_combo = new JComboBox<String>(time);		
		inputIP = new JTextField(16);					
		inputIP.setText("Enter the IP you want to scan");
		btn_start = new JButton("START");				
		btn_start.addActionListener(new StartAction());

		// attach component to panel1
		panel1.add(proto_combo);
		panel1.add(inputIP);
		panel1.add(btn_start);
		panel1.add(time_combo);


		// create component for panel2 
		column = new Vector<String>();	// Vector for saving titles to be placed in the top column of table
		column.add("Port number");
		column.add("Current Status");
		column.add("Activation Time");
		column.add("Last Confirmation Time");

		// table for outputting scan information
		tableview = new JTable();				
		model = new DefaultTableModel(0, 0);	
		model.setColumnIdentifiers(column);
		tableview.setModel(model);
		// connecting scrolls to a table 
		scroll = new JScrollPane(tableview);
		
		// attach component to panel2
		panel2.add(scroll, BorderLayout.CENTER);
		

		// create component for panel3 
		btn_csv = new JButton("Save as .csv File");	
		text = new JLabel("Last Update Time");	 
		updateTime = new JTextField(20);
		updateTime.setText("HH:mm:ss");
		updateTime.setEnabled(false);
		btn_csv.addActionListener(new WriteCSV());

		// attach component to panel3
		panel3.add(btn_csv);
		panel3.add(text);
		panel3.add(updateTime);


		// attach panels to JFrame
		add(panel1, BorderLayout.NORTH);
		add(panel2, BorderLayout.CENTER);
		add(panel3, BorderLayout.SOUTH);
		
		setSize(600, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}


	// Listener when START button is pressed
	class StartAction implements ActionListener {

		private ArrayList<Thread> activeThread;

		public StartAction()
		{
			activeThread = new ArrayList<Thread>();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			// press START button to switch to STOP, not receiving ip input, all combo boxes disabled
			if (btn_start.getText().equals("START"))
			{
				btn_start.setText("STOP");
				inputIP.setEnabled(false);
				proto_combo.setEnabled(false);
				time_combo.setEnabled(false);

				activeThread.clear();

				// press START button to create thread and scan port
				Thread t = new Thread(new PortScanner(1, 9999));
				t.start();

				activeThread.add(t);
			}
			else {
				// STOP button for stoping thread
				for (Thread t : activeThread)
				{
					t.interrupt();
				}
				activeThread.clear();

				// press STOP button to switch to START button, and enable text field and combo box
				btn_start.setText("START");
				inputIP.setEnabled(true);
				proto_combo.setEnabled(true);
				time_combo.setEnabled(true);
			}
		}
	}

	// Listener when the Save as .csv File button is pressed
	class WriteCSV implements ActionListener {
		Writer writer = null;

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			// saving port scan information as .csv file
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Port Monitor.csv"), "EUC-KR"));
				StringBuffer bufferHeader = new StringBuffer();
				for(int i=0; i<tableview.getColumnCount(); i++){
					bufferHeader.append(tableview.getColumnName(i));
					if(i != tableview.getColumnCount())
						bufferHeader.append(",");
				}
				writer.write(bufferHeader.toString() + "\r\n");
				for(int j=0; j<tableview.getRowCount(); j++){
					StringBuffer buffer = new StringBuffer();
					for(int k=0; k<tableview.getColumnCount(); k++){
						buffer.append(tableview.getValueAt(j, k));
						if(k != tableview.getColumnCount())
							buffer.append(",");
					}
					writer.write(buffer.toString() +"\r\n");
				}
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Pop-up window that says saved
			JOptionPane.showMessageDialog(null, "Port scan information has been saved as a .csv file");
			}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Window();

	}

}
