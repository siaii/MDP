package simulator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class PCClient {
	private static PCClient _instance = null;

    InetAddress host;
	Socket socket = null;
	BufferedReader  input = null;;
	PrintStream out     = null;
	String IP_Addr;
	int Port;
	boolean firstflag = false;

	static Boolean connect;


	public static PCClient getInstance() {
		if(_instance==null){
			_instance = new PCClient(CONFIG.SERVER_HOST, CONFIG.SERVER_PORT);
			connect = _instance.connectToDevice();
		}
		if (connect) return _instance;
		else return null;
	}

	private PCClient(String IP_Addr, int Port){
		this.IP_Addr = IP_Addr;
		this.Port = Port;
		_instance = this;
	}


	public boolean connectToDevice() {
		int timeout = 6000;
		try
		{
			if(socket != null) {
				if(socket.isConnected()) {
					return true;
				}
			}
			InetSocketAddress ISA =  new InetSocketAddress(IP_Addr, Port);
			socket = new Socket();
			socket.connect(ISA, timeout);
			System.out.println("Connected");
			// takes input from terminal

			// input = new DataInputStream(socket.getInputStream());
			input = new BufferedReader( 
                new InputStreamReader( 
                    socket.getInputStream())); 
			// sends output to the socket
			out    = new PrintStream(socket.getOutputStream());
			return true;
		}
		catch(UnknownHostException u)
		{
			System.out.println(u);
		}
		catch(IOException i)
		{
			System.out.println(i);
		}
		return false;

	}


	//sends packet data to rpi to be relayed or processed
	public int sendPacket(String packetData){
		try {
			System.out.println("Sending packetData...");
			System.out.println(packetData);
			out.print(packetData);
			System.out.println("Packet sent.");
			out.flush();
			return 0;

		}catch(Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Sending Error: " + e);
			e.printStackTrace();
			while(socket.isClosed()) {
				System.out.println("socket is not connected... reconnecting..");
				connectToDevice();
			}
			System.out.println("resending packet.");
			sendPacket(packetData);

		}
		return 0;
	}

	public String receivePacket(){
		String instruction = null;
		System.out.println("~~~~~Listen to Rpi~~~~~");
		try {
			//need to rethink this.
			do {

				// long timestart = System.currentTimeMillis();
//				while(!input.ready());
				// System.out.println(input.readLine());
				instruction = input.readLine();
			}while(instruction == null ||instruction.equalsIgnoreCase(""));			
		}


		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Receiving Error: " + e);
			e.printStackTrace();
			connectToDevice();
		}
		System.out.println("~~~~~Instruction:" + instruction + "~~~~~");
		return instruction;
	}

	public void closeConnection() {
		try {
			input.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public static void main(String[] args) {
		
	// 	PCClient pcclient = new PCClient("192.168.7.7", 8080);
	// 	Boolean connect = pcclient.connectToDevice();
	// 	if (connect) {
	// 		int send = pcclient.sendPacket("1,mdf");
	// 		// int send2 = pcclient.sendPacket("AND,mdf");
	// 		System.out.println(pcclient.receivePacket());
	// 	} else {
	// 		System.out.println("Error");
	// 	}
	// 	pcclient.closeConnection();
		
	// }
}