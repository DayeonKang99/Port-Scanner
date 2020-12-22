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

	// 포트 모니터링 정보 저장을 위한 HashMap
	HashMap<Integer,Vector<String>> dataSet = new HashMap<Integer,Vector<String>>();

	public static Window Instance;

	Window(){
		
		Instance = this;
		
		setTitle("Port Monitoring System");
		// 상, 중, 하 부분으로 나누고 각 부분에 부착할 패널 생성
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();


		// 패널1 (상 부분)에 들어갈 컴포넌트 생성
		String protocol[] = {"프로토콜 선택", "TCP"};
		String time[] = {"갱신주기 선택 (s)", "갱신 안함", "5", "10", "15", "20", "25", "30"};
		proto_combo = new JComboBox<String>(protocol);  // 프로토콜 선택을 위한 콤보 박스 생성
		time_combo = new JComboBox<String>(time);		// 갱신 주기 선택을 위한 콤보 박스 생성
		inputIP = new JTextField(16);					// 모니터링할 ip를 입력받기 위한 텍스트필드 생성
		inputIP.setText("모니터링 하고자 하는 IP를 입력하세요");
		btn_start = new JButton("START");				// 포트 스캔 시작을 위한 버튼 생성
		btn_start.addActionListener(new StartAction());

		// panel1에 컴포넌트 부착
		panel1.add(proto_combo);
		panel1.add(inputIP);
		panel1.add(btn_start);
		panel1.add(time_combo);


		// panel2 (중 부분)에 들어갈 컴포넌트 생성
		column = new Vector<String>();	// table 맨 위 열에 들어갈 제목 저장을 위한 Vector 
		column.add("Port 번호");
		column.add("현재 상태");
		column.add("활성화 시간");
		column.add("마지막 확인 시간");

		// 스캔 정보 출력을 위한 테이블 생성 
		tableview = new JTable();				
		model = new DefaultTableModel(0, 0);	
		model.setColumnIdentifiers(column);
		tableview.setModel(model);
		// 테이블에 스크롤 연결 
		scroll = new JScrollPane(tableview);
		
		// panel2에 컴포넌트 부착
		panel2.add(scroll, BorderLayout.CENTER);
		

		// panel3 (하 부분)에 들어갈 컴포넌트 생성
		btn_csv = new JButton("CSV 파일로 저장");	// csv 파일에 정보를 저장하기 위한 버튼 생성 
		text = new JLabel("마지막 업데이트 시간");	// 마지막 업데이트 시간 출력을 위한 텍스트 필드 생성 
		updateTime = new JTextField(20);
		updateTime.setText("HH:mm:ss");
		updateTime.setEnabled(false);
		btn_csv.addActionListener(new WriteCSV());

		// panel3에 컴포넌트 부착
		panel3.add(btn_csv);
		panel3.add(text);
		panel3.add(updateTime);


		// JFrame에 패널 부착
		add(panel1, BorderLayout.NORTH);
		add(panel2, BorderLayout.CENTER);
		add(panel3, BorderLayout.SOUTH);
		
		setSize(600, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}


	// START 버튼을 눌렀을 때 리스너
	class StartAction implements ActionListener {

		private ArrayList<Thread> activeThread;

		public StartAction()
		{
			activeThread = new ArrayList<Thread>();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			// START 버튼을 누르면 STOP으로 바뀌고 ip 입력을 못받으며 콤보박스가 모두 비활성화
			if (btn_start.getText().equals("START"))
			{
				btn_start.setText("STOP");
				inputIP.setEnabled(false);
				proto_combo.setEnabled(false);
				time_combo.setEnabled(false);

				activeThread.clear();

				// START 버튼을 누르면 스레드 생성 후 포트 모니터링 
				Thread t = new Thread(new PortScanner(1, 9999));
				t.start();

				activeThread.add(t);
			}
			else {
				// STOP 버튼을 누르면 스레드 중지 
				for (Thread t : activeThread)
				{
					t.interrupt();
				}
				activeThread.clear();

				// STOP 버튼을 누르면 START 버튼으로 바뀌고 텍스트 필드, 콤보박스 활성화 
				btn_start.setText("START");
				inputIP.setEnabled(true);
				proto_combo.setEnabled(true);
				time_combo.setEnabled(true);
			}
		}
	}

	// CSV 파일로 저장 버튼을 눌렀을 때 리스너
	class WriteCSV implements ActionListener {
		Writer writer = null;

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			// csv 파일로 포트 모니터링 정보 저장 
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

			// 저장되었다는 팝업창 띄우기
			JOptionPane.showMessageDialog(null, "포트 모니터링 정보가 CSV 파일로 저장되었습니다.");
			}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Window();

	}

}
