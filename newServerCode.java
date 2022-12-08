package module4Assignment;

import java.io.*;
import java.net.*;
import java.util.*;

public class newServerCode {
	public static void main(String[] args) throws IOException {
		// message to be sent to client
		String message = "Hello client, you have successfully received all packets and arranged them in the right order!";
		
		//display message to be sent to client
		System.out.println("Message to be sent to client: \n" + message);
		
		//list to hold packets
		ArrayList<String> messageInPackets = new ArrayList<>();
		messageIntoPackets(messageInPackets, message);

		//list to hold copy of packets, shuffled
		ArrayList<String> copyMessageInPackets = new ArrayList<>();
		createCopyMessageInPacketsShuffled(messageInPackets, copyMessageInPackets);

		//create Random object
		Random randomNumber = new Random();

		//hard code port Number
		args = new String[] { "11158" };

		//if no port number passed in, end program
		if (args.length != 1) {
			System.err.println("Usage: java EchoServer <port number>");
			System.exit(1);
		}

		//port number
		int portNumber = Integer.parseInt(args[0]);

		try (ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				PrintWriter responseWriter = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				){

			//display msg when client is connected
			System.out.println("Client connected");
			
			//send all packets, except the last one, with 80% probability
			sendPacketsToClient(copyMessageInPackets, randomNumber, responseWriter);
			
			//send the last packet to client with code that it's the last packet
			sendLastPacketToClient(messageInPackets, responseWriter);

			//read the clients request
			String clientRequest = requestReader.readLine();

			//get all the clients requests for packets not received, until client receives last packet
			while(!clientRequest.equalsIgnoreCase("final packet received")) {

				//list to hold packets client requesting again
				ArrayList<String> clientMissing = new ArrayList<>();
				
				//get the clients request for missing packets and add to array
				clientRequest = readClientsRequestForPackets(requestReader, clientRequest, clientMissing);

				//resend the missing packets to client with 80% probability
				resendMissingPacketToClient(messageInPackets, randomNumber, responseWriter, clientMissing);

				//let the client know that all packets have been sent (last packet is always received by client)
				responseWriter.println("sent");

				//read the next clients request
				clientRequest = requestReader.readLine();

				//clear the arraylist holding the missing packets the client requested
				//so that it could hold the new requests
				clientMissing.clear();
			}

			//read the last clients request, and display it
			clientRequest = requestReader.readLine();
			System.out.println(clientRequest);

		}

		catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}

	}
	

	/**
	 * the messageIntoPackets takes a message and breaks it up into 24 packets,
	 *  and then adds an Id to each packet
	 * @param messageInPackets the arrayList of a message that is divided up into packets
	 * @param message the String that will be broken into packets
	 */
	private static void messageIntoPackets(ArrayList<String> messageInPackets, String message) {
		// total number of packets message is broken into
		int numPackets = 24;
	
		// get the length of the message
		int msgLength = message.length();
		
		int packetSize = packetSize(message, numPackets);
	
		// id number to add to each packet, starting with 10
		int y = 10;
	
		for (int x = 0; x < (msgLength - 1); x += 4) {
			String packet;
	
			if ((msgLength - x) < 4) {
				// get each packet
				packet = message.substring(x, msgLength);
				
				//add id to each packet
				addIdToPacket(messageInPackets, y, packet);
			}
	
			else {
	
				packet = message.substring(x, (x + packetSize));
				//add id to each packet
				addIdToPacket(messageInPackets, y, packet);
	
			}
	
			// increment variable holding id
			y += 1;
		}
	
		addTotalNumPacketsToLast(messageInPackets, numPackets);
	
	}

	/**
	 * the packetSize method gets the whole message as a String and the total number of packets 
	 * and determines the packet size
	 * @param message the entire message that will be broken down to packets
	 * @param numPackets the total number of packets there will be
	 * @return the packetSize
	 */
	private static int packetSize(String message, int numPackets) {
		
		// get the length of the message
		int msgLength = message.length();

		// create a packet size based on length of message and the total number of
		// packets to be sent
		int packetSize = (msgLength / numPackets);

		// if there are remaining packets, add one char to each packet size
		if ((msgLength % numPackets) > 0) {
			packetSize += 1;
		}
		
		return packetSize;
		
	}

	/**
	 * the addIdToPacket method adds an id to each packet in array, starting from 10
	 * @param messageInPackets the arrayList of a message that is divided up into packets
	 * @param y the counter holding the id value
	 * @param packet the packet in messageInPackets that adding Id to
	 */
	private static void addIdToPacket(ArrayList<String> messageInPackets, int y, String packet) {
		// make id number into string and add to beginning packet
		String id = String.valueOf(y);
		packet = id.concat(packet);
	
		// add the last packet to array
		messageInPackets.add(packet);
	}

	/**
	 * the addTotalNumPacketsToLast method adds the totalNum packets to end of last packet
	 * String as part of the protocol
	 * @param messageInPackets the arrayList of a message that is divided up into packets
	 * @param numPackets the total number of packets as an int
	 */
	private static void addTotalNumPacketsToLast(ArrayList<String> messageInPackets, int numPackets) {
		
		// get the total number of packets as a String value
		String totalNumPackets = String.valueOf(numPackets);
		
		// Concatenate the number of packets to end of last packet - as per protocol
		String lastPacket = (messageInPackets.get(numPackets - 1)).concat(totalNumPackets);
		
		// put the last packet with total number of packets into array
		messageInPackets.set((numPackets - 1), lastPacket);
	}


	/**
	 * the createCopyMessageInPacketsShuffled method copies the messageInPackets array into a new array
	 * and then shuffles the new array
	 * @param messageInPackets the array of messages divided into packets
	 * @param copyMessageInPackets the array that is a deep copy of messageInPackets
	 */
	private static void createCopyMessageInPacketsShuffled(ArrayList<String> messageInPackets, ArrayList<String> copyMessageInPackets) {
		for(int x = 0; x < messageInPackets.size() - 1; x++) {
			String packet = messageInPackets.get(x);
			copyMessageInPackets.add(packet);
		}
		//shuffle the array
		Collections.shuffle(copyMessageInPackets);

	}

	/**
	 * the sendPacketsToClient method send all the packets(except last one) with 80% probability
	 * @param copyMessageInPackets the deep copy of messageInPackets
	 * @param randomNumber the Random object to generate random number
	 * @param responseWriter the printwriter obj to write to client
	 */
	private static void sendPacketsToClient(ArrayList<String> copyMessageInPackets, Random randomNumber,
			PrintWriter responseWriter) {
		//send all packets(except the last) with 80% probability
		for(int y = 0; y < copyMessageInPackets.size(); y++) {
	
			//get packet to send from arraylist
			String packetToSend = copyMessageInPackets.get(y);
	
	
			double testValue = randomNumber.nextDouble();
			//only send to client with 80% probability
			if(testValue > .2) {
				responseWriter.println(packetToSend);
			}
	
			//display that packet sent (assuming all did)
			System.out.println("packet " + packetToSend + " sent.");
	
		}
	}

	/**
	 * the send sendLastPacketToClient method attaches a code to last packet and sends it to the client
	 * @param messageInPackets the arraylist of a message divided into packets
	 * @param responseWriter the printWriter obj that writes to the client
	 */
	private static void sendLastPacketToClient(ArrayList<String> messageInPackets, PrintWriter responseWriter) {
		//get the final packet and send to client
		String lastPacket = messageInPackets.get(messageInPackets.size() - 1);
		//add the protocol code for last packet
		String finalPacketCode = "%";
		//add % 
		lastPacket = finalPacketCode.concat(lastPacket);
		//write last packet to client
		responseWriter.println(lastPacket);
		//display that last packet sent
		System.out.println("Last packet sent: " + lastPacket);
	}

	/**
	 * the readClientsRequestForPackets method reads the clients request for missing packets 
	 * and adds it to an arra
	 * @param requestReader the bufferedReaderobj that reads from the client 
	 * @param clientRequest the request from client for missing Ids
	 * @param clientMissing the array holding missing Ids
	 * @return the clientsRequest
	 * @throws IOException
	 */
	private static String readClientsRequestForPackets(BufferedReader requestReader, String clientRequest,
			ArrayList<String> clientMissing) throws IOException {
		while(!clientRequest.equals("done")) {  //when client sends 'done', all requests have been sent		
			//display the clients request
			System.out.println("Client requested packet " + clientRequest);
			//add the client request to array of missing packets
			clientMissing.add(clientRequest);
			//read the clients next request
			clientRequest = requestReader.readLine();
		}
		return clientRequest;
	}

	/**
	 * the resendMissingPacketToClient method gets the missing packet, based on requested Id,
	 * and then resends packet with 80% probability
	 * @param messageInPackets the arraylist of a message divided into packets 
	 * @param randomNumber the Random obj to generate a random number
	 * @param responseWriter he printWriter obj that writes to the client
	 * @param clientMissing the arraylist of missing packets
	 */
	private static void resendMissingPacketToClient(ArrayList<String> messageInPackets, Random randomNumber,
			PrintWriter responseWriter, ArrayList<String> clientMissing) {
		//for each packet the client is missing,
		for(int z = 0; z < clientMissing.size(); z++) {
			//get the missing packet from messageInPackets array
			String missingPacket = getMissingPacket(clientMissing.get(z), messageInPackets);
			//
			/*if(missingPacket == null) {
					System.out.println(clientMissing.get(z) + " returns a null value. ");
					break;
				}*/
			System.out.println("Sending missing packet " + missingPacket);
	
			double testerValue = randomNumber.nextDouble();
			//only send to client with 80% probability
			if(testerValue > .2) {	
				responseWriter.println(missingPacket);
			}	
		}
	}

	/**
	 * the getMissingPacket method gets a clients request and the array of messgeInPackets
	 * and returns the requested packet
	 * @param clientRequest the id as a String the client requested
	 * @param messageInPackets the arraylist of a message divided into packets
	 * @return the requested packet
	 */
	private static String getMissingPacket(String clientRequest, ArrayList<String> messageInPackets) {
		//for each packet in the array of packets
		for(int x = 0; x < messageInPackets.size(); x++) {
			//if the message contains the id the client requested, return it to method that called it
			if((messageInPackets.get(x)).contains(clientRequest)) {
				return messageInPackets.get(x);
			}
		}
		return null;
	}
}
