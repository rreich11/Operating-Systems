package module4Assignment;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public class newClientCode {

	public static void main(String[] args) throws IOException{
		
		//hard code the IP address and port number
		args = new String[] {"127.0.0.1", "11158"};
		
		//initialize arrayList for received Packets as objects
		ArrayList<Packet> packetsObj = new ArrayList<>();
		
		//an array to hold IDs of packets
		int[] IDs;
		
		ArrayList<Integer> missingPackets;
		
		ArrayList<Integer> copyMissingPackets = new ArrayList<>();
		
		
		//if IP and port not passed in, end program
		 if (args.length != 2) {
	            System.err.println(
	                "Usage: java EchoClient <host name> <port number>");
	            System.exit(1);
	        }
		 
		 String IPAddress = args[0];
	     int portNumber = Integer.parseInt(args[1]);

	     try (
	    		 Socket clientSocket = new Socket(IPAddress, portNumber);
	    		 // stream to write text requests to server
	    		 PrintWriter requestWriter = new PrintWriter(clientSocket.getOutputStream(), true);
	    		 // stream to read text response from server
	    		 BufferedReader responseReader= new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
	    		 ){

	    	 //read packets from server
	    	 String packetReceived  = responseReader.readLine();
	    	 //loop until server sends the last packet
	    	 while(packetReceived != "%"){

	    		 String finalPacketCode = null;
	    		 if(packetReceived.contains("%")) {
	    			 
	    			 //if the code for final packet was sent over, remove the code 
	    			 //before turning into packet Obj
	    			 packetReceived = packetReceived.substring(1);

	    			 System.out.println("last Packet received: " + packetReceived);

	    			 //as per protocol, the last packet is indicated with a percent sign
	    			 finalPacketCode = "%";
	    		 }

	    		 //create packet into object with id and message
	    		 Packet packetObj = packetToObject(packetReceived);

	    		 //add object to list of received packets
	    		 packetsObj.add(packetObj);

	    		 if(finalPacketCode == "%") {
	    			 //set packet received to % to end loop
	    			 packetReceived = "%";
	    			 
	    		 }
	    		 else {
	    			 System.out.println("Packet received: " + packetReceived);
	    			 //read the next line from server
	    			 packetReceived  = responseReader.readLine();
	    			 
	    		 }


	    	 }

	    	 //put the received packets in order by their id
	    	 sortPackets(packetsObj);

	    	 //get the total number of packets to receive, and initialize array to hold ID
	    	 int ttlPackets = getTotalNumPackets(packetsObj);
	    	 IDs = new int[ttlPackets]; 

	    	 //all Ids of packets received to the array
	    	 addIDsToArray(IDs, packetsObj);

	    	 //get the id of missing packets and put into array
	    	 missingPackets = addMissingIdsToarray(IDs);

	    	 //create a deep copy of missingPackets Array
	    	 createCopyMissingPacketsArray(missingPackets, copyMissingPackets);

	    	 //request the missing packets from server
	    	 requestMissingPacketsFromServer(missingPackets, requestWriter);

	    	 //let server know when done requesting missing packets
	    	 requestWriter.println("done");
	    	 
	    	 //keep requesting and receiving packets from server until the copyMissingPackets array is empty
	    	 while(!copyMissingPackets.isEmpty()) {
	    		 //read from the server
	    		 String packetResent = responseReader.readLine();
	    		 
	    		 //read packets until server sends signal that all sent (as per protocol)
	    		 readResentPacketsFromServer(packetsObj, copyMissingPackets, responseReader, packetResent);

	    		 if(copyMissingPackets.isEmpty()) {
	    			 //let server know when done requesting missing packets
	    			 requestWriter.println("final packet received");
	    			 requestWriter.println("client received all packets!!");

	    		 }
	    		 else {
	    			 requestMissingPacketsFromServer(copyMissingPackets, requestWriter);
	    		 }
	    		 requestWriter.println("done");
	    	 }

	    	 System.out.println("All packets received");
	    	 //sort the packets once more, with all the added packets
	    	 sortPackets(packetsObj);
	    	 
	    	 //display the message of ordered packets
	    	 displayFinalMsg(packetsObj);
	     }

	     catch (UnknownHostException e) {
	    	 System.err.println("Don't know about host " + IPAddress);
	    	 System.exit(1);
	     } catch (IOException e) {
	    	 System.err.println("Couldn't get I/O for the connection to " +
	    			 IPAddress);
	    	 System.exit(1);
	     }

	}
	/**
	 * the packetToObject method gets the Id and message 
	 * of packet and creates a Packet object
	 * @param packet the packet the client receives from server
	 * @return the Packet object
	 */
	private static Packet packetToObject(String packet) {
		//get the id of packet as a String
		//as per protocol, first two characters of packet is the id of packet
		String StringID = packet.substring(0, 2);

		//parse the String id to an int
		int id = Integer.parseInt(StringID);

		//get the message from packet
		//as per protocol the third character till end of string is the message (besides for the last packet)
		String msg = packet.substring(2, packet.length());

		//create a Packet object with the id and msg
		Packet packetObj = new Packet(id, msg);

		return packetObj;

	}

	/**
	 * the sortPackets method sorts the arraylist 
	 * holding the packet objects by packet id
	 * @param packetsObj the arraylist of Packet objects
	 */
	private static void sortPackets(ArrayList<Packet> packetsObj) {

		//sort the packets according to their ids, using selection sort algorithm
		for(int a = 0; a < packetsObj.size() - 1; a++) {
			for (int b = a + 1; b < packetsObj.size(); b++) {
				if(packetsObj.get(a).getPacketID() > packetsObj.get(b).getPacketID()) {
					Packet temp = packetsObj.get(a);
					packetsObj.set(a, packetsObj.get(b));
					packetsObj.set(b, temp);
				}
			}
		}
	}

	/**
	 * the getTotalNumPackets method gets the total
	 * number of packets server is trying to send
	 * @param packetsObj the arraylist of packetsObj the client received
	 * @return the total number of packets
	 */
	private static int getTotalNumPackets(ArrayList<Packet> packetsObj) {
		//get the last packet obj
		Packet lastPacket = packetsObj.get(packetsObj.size() - 1);
		//get the last package msg 
		String lastPacketMsg = lastPacket.getPacketMsg();

		//get the total number of packets from the last packet msg, and parse it to an integer
		//As per protocol, last two digits of last packet is total number of packets
		int ttlPacket = Integer.parseInt(lastPacketMsg.substring(lastPacketMsg.length() - 2, lastPacketMsg.length()));

		//get the last message without the number of packets attacheched
		String lastPacketMsgWithOutNum = lastPacketMsg.substring(0, lastPacketMsg.length() - 2);
		lastPacket.setPacketMsg(lastPacketMsgWithOutNum);

		//display the total number of packets
		System.out.println("total number of packets to receive: " + ttlPacket);

		return ttlPacket;
	}

	/**
	 * the addIDsToArray method gets the ID of each 
	 * packet received and adds it to an array
	 * @param IDs the array to hold IDs
	 * @param packetsObj the arraylist of packetsObj the client received
	 */
	private static void addIDsToArray(int[] IDs, ArrayList<Packet> packetsObj) {
		
		//get the ID of each packet 
		for(int y = 0; y < packetsObj.size(); y++) {
			int packetID = (packetsObj.get(y)).getPacketID();
			//put the ID in correct index
			IDs[packetID - 10] = packetID;
		}
	}

	/**
	 * the addMissingIdsToarray method finds the packets that it 
	 * didn't receive and adds the ids of those packets to an array
	 * @param IDs the array holing IDs of received packets
	 * @return the array of missing packet ids
	 */
	private static ArrayList<Integer> addMissingIdsToarray(int[] IDs) {
	
		//initialize the array to hold number of missing packets
		ArrayList<Integer> missingPackets = new ArrayList<>();

		//get the ids for missing packets and put into array
		for (int a = 0; a < IDs.length; a++) {
			//if the index in IDs array equals 0, 
			if(IDs[a] == 0) {
				//add the ID thats missing(as per protocol, the first index is 10) to the missingPackets array
				missingPackets.add(a + 10);
			}
		}

		//display the missingPackets array
		System.out.print("Missing packets by ID: ");
		for(int n= 0; n < missingPackets.size(); n++) {
			System.out.print(" " + missingPackets.get(n));
		} 
		//skip a line for nicer output in conole
		System.out.println();
		
		return missingPackets;
	}

	/**
	 *the requestMissingPacketsFromServer method requests packets by id, 
	 *that its missing, from server
	 * @param missingPackets the array of missing packets
	 * @param requestWriter the PrintWriter obj that writes to server socket
	 */
	private static void requestMissingPacketsFromServer(ArrayList<Integer> missingPackets, PrintWriter requestWriter) {
		for(int z = 0; z < missingPackets.size(); z++) {
	
			 //request the missing packet from server
			 requestWriter.println(String.valueOf(missingPackets.get(z)));
			 System.out.println("requesting missing packet " + missingPackets.get(z));
	
		 }
	}
	/**
	 * the createCopyMissingPacketsArray method creates a deep copy of the missingPacketsArray
	 * @param missingPackets the array of missing packets
	 * @param copyMissingPackets the array for copy of missingPackets
	 */
	private static void createCopyMissingPacketsArray(ArrayList<Integer> missingPackets, ArrayList<Integer> copyMissingPackets) {
		for(int x = 0; x < missingPackets.size(); x++) {
			int packet = missingPackets.get(x);
			copyMissingPackets.add(packet);
		}
	}
	
	/**
	 * the readResentPacketsFromServer method reads the packets resent from the server,
	 * removes each packet from its missingPacketArray, and adds the new Packet Obj to array of packets 
	 * @param packetsObj the array of Packet objects 
	 * @param copyMissingPackets the deep copy of missingPackets array
	 * @param responseReader the BufferedWriter obj to read from server 
	 * @param packetResent the String read from the server
	 * @throws IOException
	 */
	private static void readResentPacketsFromServer(ArrayList<Packet> packetsObj, ArrayList<Integer> copyMissingPackets,
			BufferedReader responseReader, String packetResent) throws IOException {
		while(!packetResent.equalsIgnoreCase("sent")) {
	
			 System.out.println("packet resent " + packetResent);
			 
			 //turn the received packet into a Packet obj
			 Packet packetObj = packetToObject(packetResent);
			 
			 //for each packet received, remove it from the copyMissingPackets array
			 for(int z = 0; z < copyMissingPackets.size(); z++) {
				 if(packetObj.getPacketID() == copyMissingPackets.get(z)) {
					 copyMissingPackets.remove(z);
				 }
			 }
	
			 //add object to list of received packets
			 packetsObj.add(packetObj);	 
			 //read the next packet
			 packetResent = responseReader.readLine();
		 }
	}
	
	/**
	 * the displayFinalMsg method displays the final message the server sent over
	 * @param packetsObj the arrayList of Packet objects
	 */
	private static void displayFinalMsg(ArrayList<Packet> packetsObj) {
		System.out.println("The servers final message: ");
		 for(int y = 0; y < packetsObj.size(); y++) {
			 System.out.print(packetsObj.get(y).getPacketMsg());
		 }
	}

}
