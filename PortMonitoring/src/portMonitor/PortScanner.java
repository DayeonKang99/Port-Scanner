package portMonitor;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.swing.table.*;

public class PortScanner implements Runnable{

	private int startPort;
	private int endPort;

	// 생성자 
	public PortScanner(int start, int end) {
		
		startPort = start;
		endPort = end;
	}

	int time[] = {0, 0, 5, 10, 15, 20, 25, 30};

	
	// 테이블에 포트 모니터링한 정보를 쓰는 메소드
	private boolean addOrUpdateRow(Vector<String> data){
		
		String port = data.get(0);

		synchronized (Window.Instance.model)
		{
			// 포트 정보가 이미 테이블에 있으면 update
			for (int i = 0; i < Window.Instance.model.getRowCount(); i++)
			{
				if (Window.Instance.model.getValueAt(i, 0).equals(port) == false)
					continue;

				System.out.println("UPDATE ROW: " + port);
				for (int j = 1; j < data.size(); j++)
					Window.Instance.model.setValueAt(data.get(j), i, j);
				return false;
			}

			// 포트 정보가 테이블에 없으면 add 
			System.out.println("ADD ROW: " + port);
			Window.Instance.model.addRow(data);
		}
		return true;
	}

	@Override
	// 스레드 실행을 위한 메소드
	public void run()
	{
		// TODO Auto-generated method stub
		String ip = Window.Instance.inputIP.getText();				// Window에서 입력받은 ip를 가져옴 

		Vector<String> info = null;									// 포트 스캔한 정보를 Vector에 저장  
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");	// 시간 저장을 위한 format

		try {
			do {
				for(int i = startPort; i < endPort; i++)
				{
					Date now = new Date();

					synchronized (Window.Instance.updateTime)
					{
						Window.Instance.updateTime.setText("SCANNING..." + i);	// 스캐닝 중일 떄 출력하는 텍스트 
					}

					// 해당되는 포트에 접속 가능한지 확인 
					try {
						Socket socket = new Socket();
						socket.connect(new InetSocketAddress(ip, i), 200);
						socket.close();
						if (Window.Instance.dataSet.containsKey(i))
							info = Window.Instance.dataSet.get(i);
						else
						{
							// info에 포트 번호, 현재 상태, 활성화 시간, 마지막 확인 시간에 대한 정보 저장 
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
						// 포트가 닫혀있을 때 정보 저장 
						else
						{
							info = Window.Instance.dataSet.get(i);
							info.set(1, "CLOSE");
							info.set(3, format.format(now));
						}
					}
					addOrUpdateRow(info);
				}

				Window.Instance.updateTime.setText(format.format(new Date()));	// 스캔을 완료하고 마지막 업데이트 시간 텍스트로 출력 
				// 갱신 주기 저장 
				int retryTime = time[Window.Instance.time_combo.getSelectedIndex()];

				if (retryTime == 0)
					throw new InterruptedException();

				// 갱신 주기가 설정되어 있으면 스레드 sleep 후 다시 스캔 
				try {
					Thread.sleep(retryTime * 1000);
				} catch (InterruptedException e) {
					return;
				}
			} while (true);
		} catch (Exception e)	// 스레드 종료 
		{
			System.out.println("STOP: " + e);
			synchronized (Window.Instance)
			{
				// 스레드 종료하면 자동으로 START 버튼으로 바뀌고 텍스트 필드와 콤보박스 활성화 
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
				Window.Instance.updateTime.setText(format.format(new Date()));	// 스레드 종료 시 마지막 업데이트 시간 텍스트로 출력 
			}
		}
	}

}
