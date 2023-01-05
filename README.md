# Port-Scanner
Implemented port scanner using JAVA

- PortScanner.java: scanning port of target IP using thread
- Window.java: GUI of this program  

<br><br>

# UI Description
### before execute
<img src="https://user-images.githubusercontent.com/67621291/210791160-8031db77-6d8f-4dc3-85eb-12a56e4910ba.png" width="550">

- Select protocol with combo box
- Press START button to start scanning
- Select renewal cycle of scanning

<br>

### during execute
<img src="https://user-images.githubusercontent.com/67621291/210791428-e5fe2ad4-f4eb-4868-82cd-3a326020c8c9.png" width="550">

- If the port is open, the current state is OPEN, and if it is closed, CLOSE is displayed in the table
- When the START button is pressed, the button changes to STOP and the port scan starts. Combo box and IP input window disabled

<br>

### after finished 
<img src="https://user-images.githubusercontent.com/67621291/210791401-b13cdcc5-8826-481a-acb2-fb18bf078812.png" width="550">

- When the port scan is complete, the button name changes from STOP to START and the combo box and IP input window are activated again. When the refresh cycle is set, the button does not change from STOP to START. It can be activated again as START by forcibly pressing the STOP button
- When the scan is complete, the last update time is printed
- When you press the button, a message window appears stating that the file has been saved (shown above) and saves information as a .csv file
