import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;
import java.io.IOException;
import java.net.URL;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Graphics;
import javax.swing.Timer;

public class TempFan extends JApplet implements SerialPortEventListener
{
	private float temp;
	private int limit = 22;
	
	public void actionPerformed(ActionEvent e) {}
	
	SerialPort serialPort;
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = {"COM3"};
	/**
	 * A BufferedReader which will be fed by a InputStreamReader 
	 * converting the bytes into characters 
	 * making the displayed results codepage independent
	 */
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public void initialize() {
		// the next line is for Raspberry Pi and 
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		//       System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */

	
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */

	FanControl fancontrol = new FanControl();
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				
				String inputLine = input.readLine();
				temp = Float.parseFloat(inputLine);
				fancontrol.settemp(temp,limit);
				
				if(temp >= limit) fancontrol.startfan();
				
				else fancontrol.stopfan();
				
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}
		
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	
	public static void main(String[] args) throws Exception {
		
		JFrame frame = new JFrame("Fan");
		TempFan applet = new TempFan(); 
		applet.initialize();
		frame.add(applet, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1370,780);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true); 
		Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();

	}
	public TempFan() throws IOException { add(fancontrol); }
}

class FanControl extends JPanel
{ 
	private JLabel jlbl = new JLabel();
	private JPanel panel = new JPanel();
	private FanPanel fanpanel = new FanPanel();
	private AudioClip audioClip1;
	private AudioClip audioClip2;
	private AudioClip audioClip3;
	private AudioClip audioClip4;
	private boolean flag1 = true;
	private boolean flag2 = true;
	private boolean flag3 = true;
	private boolean flag4 = true;
	
	public FanControl() 
		{ 
			GUIsettings(); 
			URL audio1 = getClass().getResource("fan1.au"); 
			audioClip1 = Applet.newAudioClip(audio1);
			URL audio2 = getClass().getResource("fan2.au"); 
			audioClip2 = Applet.newAudioClip(audio2);
			URL audio3 = getClass().getResource("fan3.au"); 
			audioClip3 = Applet.newAudioClip(audio3);
			URL audio4 = getClass().getResource("fan4.au"); 
			audioClip4 = Applet.newAudioClip(audio4);
		}
	
	public void GUIsettings(){
		

		fanpanel.setBorder(new LineBorder(Color.black));
		setLayout(new BorderLayout());
		jlbl.setPreferredSize(new Dimension(0,50));
		jlbl.setBorder(new LineBorder(Color.black,3));
		jlbl.setHorizontalAlignment(JLabel.CENTER);
		jlbl.setFont(new Font("Courie",Font.BOLD,36));
		add(jlbl,BorderLayout.NORTH);
		add(fanpanel,BorderLayout.CENTER);
		add(panel, BorderLayout.SOUTH);
	}
	public void startfan(){
		fanpanel.start();
	}
	public void stopfan(){
		fanpanel.stop();
	}
	public void startsound(int number){
		
		if (audioClip1 != null && flag1 && number == 1) { audioClip1.loop(); flag1 = false; flag2 = true; flag3 = true; flag4 = true; audioClip2.stop(); audioClip3.stop(); audioClip4.stop(); }
		if (audioClip2 != null && flag2 && number == 2) { audioClip2.loop(); flag2 = false; flag1 = true; flag3 = true; flag4 = true; audioClip1.stop(); audioClip3.stop(); audioClip4.stop();}
		if (audioClip3 != null && flag3 && number == 3) { audioClip3.loop(); flag3 = false; flag1 = true; flag2 = true; flag4 = true; audioClip1.stop(); audioClip2.stop(); audioClip4.stop();}
		if (audioClip4 != null && flag4 && number == 4) { audioClip4.loop(); flag4 = false; flag1 = true; flag2 = true; flag3 = true; audioClip1.stop(); audioClip2.stop(); audioClip3.stop();}
	}
	public void stopsound(){
		if (audioClip1 != null) { audioClip1.stop(); audioClip2.stop(); audioClip3.stop(); audioClip4.stop(); flag1 = true; flag2 = true; flag3 = true; flag4 = true; }     
	}
	
	public void settemp(float temp,int limit){
		
		if(temp >= limit && temp < limit+2) { jlbl.setText("Tempeture: " + temp + "    ----->    Working"); fanpanel.change(1); startsound(1); }  
		else if (temp >= limit+2 && temp < limit+4) { jlbl.setText("Tempeture: " + temp + "    ----->    Working"); fanpanel.change(3); startsound(2); }
		else if (temp >= limit+4 && temp < limit+6) { jlbl.setText("Tempeture: " + temp + "    ----->    Working"); fanpanel.change(5); startsound(3); }
		else if (temp >= limit+6 && temp < limit+8) { jlbl.setText("Tempeture: " + temp + "    ----->    Working"); fanpanel.change(7); startsound(4); }
		else { jlbl.setText("Tempeture: " + temp + "    ----->    Not Working"); stopsound(); }
	}
}

class FanPanel extends JPanel implements ActionListener  {
	
	  private int delay = 10;
	  private int start = 0;
	  private int end = 40;
	  private double direction;
	  protected Timer timer = new Timer(delay,this);
	  
	  public FanPanel() { }
	  
	  public void change(int multiply) {
		
		  direction = 1.5;
		  direction = multiply*direction;
	}

	public void actionPerformed(ActionEvent e) { repaint(); } // timer
	  
	  protected void paintComponent(Graphics g) {
	    super.paintComponent(g);

		  g.setColor(Color.gray);
		  g.fillRect(0, getHeight()-10, getWidth(), getHeight());
		  g.fillRect(getWidth()/2-5, getHeight()/2, 10, getHeight()/2);
		  g.setColor(Color.black);
		  g.fillArc(0, 0, getWidth(), getHeight(),start,end);
		  g.fillArc(0, 0, getWidth(), getHeight(),start + 90,end);
		  g.fillArc(0, 0, getWidth(), getHeight(),start + 180,end);
		  g.fillArc(0, 0, getWidth(), getHeight(),start + 270,end);
		  start += direction;

	  }
	  
	  public void stop() { timer.stop(); }
	  
	  public void start() {  timer.start();}

	  public void setDelay(int delay) {
	      this.delay = delay;
		  timer.setDelay(delay);    
	  								  }
}