import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
/**
 *
 * 
 *
 */
public class endUser extends Node {
    static final int DEFAULT_SRC_PORT = 50000;
    static final int DEFAULT_DST_PORT = 50001;
    static final String DEFAULT_DST_NODE = "controller";
    InetSocketAddress dstAddress;
    InetSocketAddress srcAddress;
    String sendTo;
    endUser(String dstHost, int dstPort, int srcPort) {
        try {
            dstAddress= new InetSocketAddress(dstHost, dstPort);
            socket= new DatagramSocket(srcPort);
            listener.go();
        }
        catch(java.lang.Exception e) {e.printStackTrace();}
    }

    public synchronized void onReceipt(DatagramPacket packet) {

        PacketContent content= PacketContent.fromDatagramPacket(packet);
        if (content.getType()==PacketContent.USERSTART) { //This is a packet that the controller will send to the user after the user sends a Start Packet
            srcAddress = (((startPacket)content).getAddress());
            System.out.println("Controller has recieved me \n");
            System.out.println("Please enter a user to send a packet to (E1, E2, E3 or E4) or type anything else to quit"); //Ask the user who they want to send a packet to, only accept valid names
            Scanner input = new Scanner(System.in);
            String decision = input.next().toLowerCase();
            if(decision.equals("e1") || decision.equals("e2") || decision.toLowerCase().equals("e3") || decision.toLowerCase().equals("e4"))
            {
                sendTo = decision;
                try {
                    //We need to figure out what Router to send the packet to so we send a request to the Controller for the flowTable
                    routingPacket request = new routingPacket(sendTo, srcAddress);
                    packet = request.toDatagramPacket();
                    packet.setSocketAddress(dstAddress);
                    socket.send(packet);
                }
                catch(java.lang.Exception e) {e.printStackTrace();}
            }
            else
            {
                System.out.println("Invalid selection, no message sent!");
            }
        }
        if(content.getType()==PacketContent.CONTROLLPACKET) {
            //This indicates we have recieved a response from the Controller with the flowTable and can now send a packet to the desired end user
            System.out.println("Recieved Routing Info");
            System.out.println( ((controllerPacket) content).getTo());
            //Send the packet to the Router
            send(((controllerPacket) content).getTo());
        }
        if(content.getType()==PacketContent.WORKPACKET)
        {
            System.out.println("I Have recieved something from another user!");
            //We have recieved a packet from another end user, this protocol simply demonstrates sending and recieving packets through Routers so the packets we recieve do not contain anything.
        }
    }
    public synchronized void send(InetSocketAddress dstAddress) {
        try {
            DatagramPacket packet = null;
            System.out.println(sendTo + "   "  + srcAddress);
            //The User we want to reach and our address so the Routers no where it came from
            workPacket work = new workPacket(sendTo, srcAddress);
            packet = work.toDatagramPacket();
            packet.setSocketAddress(dstAddress);
            socket.send(packet);
            System.out.println("Sent some work");
        }
        catch(java.lang.Exception e) {e.printStackTrace();}
    }

    public synchronized void start() throws Exception {
        //Start by sending a Start Packet telling the Controller we are connected
        DatagramPacket packet = null;
        userPacket start = new userPacket();
        packet = start.toDatagramPacket();
        packet.setSocketAddress(dstAddress);
        socket.send(packet);
        System.out.println("Packet sent \n");
        this.wait();
    }

    public static void main(String[] args) {
        try {
            (new endUser(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
            System.out.println("Program completed");
        } catch(java.lang.Exception e) {e.printStackTrace();}
    }
}
