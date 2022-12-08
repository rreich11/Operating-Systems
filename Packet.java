package module4Assignment;



public class Packet {
	
	private int packetID;
	private String packetMsg;
	
	public Packet(int packetID, String packetMsg) {
		this.packetID = packetID;
		this.packetMsg = packetMsg;
	}
	
	public int getPacketID() {
		return this.packetID;
	}
	public String getPacketMsg() {
		return this.packetMsg;
	}
	public void setPacketID(int packetID) {
		this.packetID = packetID;
	}
	
	public void setPacketMsg(String packetMsg) {
		this.packetMsg = packetMsg;
	}
	

}
