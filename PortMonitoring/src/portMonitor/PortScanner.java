package portMonitor;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.swing.table.*;

public class PortScanner implements Runnable{

	private int startPort;
	private int endPort;

	public PortScanner(int start, int end) {
		
		startPort = start;
		endPort = end;
	}

	int time[] = {0, 0, 5, 10, 15, 20, 25, 30};

	
	// method for writing port scan information 
	private boolean addOrUpdateRow(Vector<String> data){
		
		String port = data.get(0);

		synchronized (Window.Instance.model)
		{
			// if port information already exists, then update
			for (int i = 0; i < Window.Instance.model.getRowCount(); i++)
			{
				if (Window.Instance.model.getValueAt(i, 0).equals(port) == false)
					continue;

				System.out.println("UPDATE ROW: " + port);
				for (int j = 1; j < data.size(); j++)
					Window.Instance.model.setValueAt(data.get(j), i, j);
				return false;
			}

			// if port information doesn't exist, then add 
			System.out.println("ADD ROW: " + port);
			Window.Instance.model.addRow(data);
		}
		return true;
	}

	@Override
	// method for thread
	public void run()
	{
		// TODO Auto-generated method stub
		String ip = Window.Instance.inputIP.getText();				 

		Vector<String> info = null;									
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");	

		try {
			do {
				for(int i = startPort; i < endPort; i++)
				{
					Date now = new Date();

					synchronized (Window.Instance.updateTime)
					{
						Window.Instance.updateTime.setText("SCANNING..." + i);	 
					}

					// verify that the appropriate port is reachable
					try {
						Socket socket = new Socket();
						socket.connect(new InetSocketAddress(ip, i), 200);
						socket.close();
						if (Window.Instance.dataSet.containsKey(i))
							info = Window.Instance.dataSet.get(i);
						else
						{
							// store information about port number, current status, activation time, and last verification time in info 
							info = new Vector<String>();

							info.setSize(4);

							info.set(0, Integer.toString(i));
							info.set(2, format.format(now));

							Window.Instance.dataSet.put(i,  info);
						}
						info.set(1, "OPEN");
						info.set(3, format.format(now));
					}
					catch (IOException e)
					{
						if (Thread.currentThread().isInterrupted())
							return;

						if (Window.Instance.dataSet.containsKey(i) == false)
							continue;
						// save information when a port is closed
						else
						{
							info = Window.Instance.dataSet.get(i);
							info.set(1, "CLOSE");
							info.set(3, format.format(now));
						}
					}
					addOrUpdateRow(info);
				}

				Window.Instance.updateTime.setText(format.format(new Date()));	// complete the scan and print to the last update time
				// save Renewal Cycle 
				int retryTime = time[Window.Instance.time_combo.getSelectedIndex()];

				if (retryTime == 0)
					throw new InterruptedException();

				// Rescan after thread sleep if update Renewal Cycle is set
				try {
					Thread.sleep(retryTime * 1000);
				} catch (InterruptedException e) {
					return;
				}
			} while (true);
		} catch (Exception e)	// terminate thread 
		{
			System.out.println("STOP: " + e);
			synchronized (Window.Instance)
			{
				// automatically changes to START button when thread is terminated, and enables text fields and combo boxes
				Window.Instance.btn_start.setText("START");
				Window.Instance.inputIP.setEnabled(true);
				Window.Instance.proto_combo.setEnabled(true);
				Window.Instance.time_combo.setEnabled(true);
			}
		}
		finally
		{
			synchronized (Window.Instance.updateTime)
			{
				Window.Instance.updateTime.setText(format.format(new Date()));	// print last update time to text when thread is terminated 
			}
		}
	}

}
