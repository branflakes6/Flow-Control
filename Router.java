/**
 *
 */
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileInputStream;
/**
 *
 * 
 *
 */
public class Router extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "controller";
	InetSocketAddress dstAddress;
	InetSocketAddress[] addresses = new InetSocketAddress[2];
	boolean haveAddress = false;
	InetSocketAddress forward;
	String destination = "";
	DatagramPacket toForward = null;
	Router(String dstHost, int dstPort, int srcPort) {
		try {
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	public synchronized void onReceipt(DatagramPacket packet) {
		PacketContent content= PacketContent.fromDatagramPacket(packet);
		if (content.getType()==PacketContent.STARTPACKET) {
		    //Recieved a "Hello" Message back from the Controller
			System.out.println("Controller has recieved me \n");
		}
		if (content.getType()==PacketContent.WORKPACKET) {
		    //Recieved a packet from an end user to send to another end user
			System.out.println("Have something to pass on");
			if(destination.equals(((workPacket)content).sendTo())) //Firstly we check if we already have the flowTable for this path
            {
                try {
                    toForward.setSocketAddress(forward); // If we already know where to send the packet we can just send it
                    socket.send(toForward);
                    System.out.println("Passed on packet");
                }
                catch(java.lang.Exception e) {e.printStackTrace();}
            }
			try {
			    // We do not know where to send the packet so we must request the information from the Controller
				System.out.println("requesting routing info");
				DatagramPacket toSend = null;
				destination = ((workPacket)content).sendTo();
				routingPacket request = new routingPacket(destination, ((workPacket)content).from());
				toSend = request.toDatagramPacket();
				toSend.setSocketAddress(dstAddress);
				socket.send(toSend);
				toForward = packet;
			}
			catch(java.lang.Exception e) {e.printStackTrace();}
		}
        if(content.getType()==PacketContent.CONTROLLPACKET)
        {
            //The Controller has responded with the flowTable for this path and we can now forward the packet to the next Router or the end user
            forward = (((controllerPacket)content).getTo());
            try {
                toForward.setSocketAddress(forward);
                socket.send(toForward);
                System.out.println("Passed on packet");
            }
            catch(java.lang.Exception e) {e.printStackTrace();}
        }
	}

	public synchronized void start() throws Exception {
		DatagramPacket packet = null;
		//When a Router starts it sends a "Hello" message to the Controller
		startPacket hello = new startPacket();
		packet = hello.toDatagramPacket();
		packet.setSocketAddress(dstAddress);
		socket.send(packet);
		System.out.println("Packet sent \n");
		this.wait();
	}

    //Main
	public static void main(String[] args) {
		try {
			(new Router(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
