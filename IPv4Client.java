/*****************************************
*	Nathan Asdarjian
*	Alfredo Ceballos
*	CS 380 - Computer Networks
*	Project 3
*	Professor Nima Davarpanah
*****************************************/

import java.io.*;
import java.net.*;

public class IPv4Client
{
	public static short checksum(byte[] bytes)
	{
		int b = (bytes.length + (bytes.length % 2)) / 2;	// pairs of bytes
		short s;	// temp short
		int sum = 0;	// 32 bit storage space
		
		for (short i = 0; i < b; i++) {	// sum pairs of bytes
			
			s = (short) ((bytes[i * 2] & 0xFF) << 8);	// align bytes in short
			if ((i * 2) + 1 < bytes.length) {
				s += (bytes[(i * 2) + 1] & 0xFF);
			}
			
			sum += (s & 0xFFFF);	// add new short to sum
			
			if ((sum & 0xFFFF0000) > 0)	// if overflow,
			{
				sum &= 0x0000FFFF;	// drop first 16 bits
				sum++;				// wrap around overflow
			}
		}
		
		s = (short) ~(sum & 0x0000FFFF);	// one's complement
		return s;	// return checksum
	}// End of checksum
	
	
	public static byte[] IPv4Packet(byte[] data) throws Exception
	{
		byte[] header = new byte[20];
			
		header[0] = 69;	// 01000101 : Version 0100, HLen 0101
		header[1] = 0;	// TOS
		
		short length = (short) (data.length + header.length);
		header[2] = (byte) ((length & 0xFF00) >> 8);
		header[3] = (byte) (length & 0x00FF);	// Length
		
		header[4] = 0;
		header[5] = 0;	// Identification	
		header[6] = 64; // 01000000 : Flags 010, Offset 00000
		header[7] = 0;	// Offset
		header[8] = 50; // TTL : 50 seconds
		header[9] = 6;	// Protocol : 0x06 for TCP
						// skip checksum until after header is filled
						
		// Fetching client's actual IP address and converting it into 
		// byte values, then inserting them into the packet
		URL getIP = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(getIP.openStream(), "UTF-8"));
		String[] src = in.readLine().split("\\.");
		for (int i = 0; i < src.length; i++) {
			int j = Integer.parseInt(src[i]);
			header[i + 12] = (byte) j;	// source address bytes
		}
		
		header[16] = (byte) 18;
		header[17] = (byte) 221;
		header[18] = (byte) 102;
		header[19] = (byte) 182;	// destination address bytes
		
		short cks = checksum(header);	// checksum of header
		header[10] = (byte) ((cks & 0xFF00) >> 8);
		header[11] = (byte) (cks & 0x00FF);
		
		// The packet's header data is already in the header byte array, here
		// it is copied into the packet byte array, which is the size of 
		// of the header plus the size of the data generated, that data is then
		// also copied in at the end
		byte[] packet = new byte[header.length + data.length];
		for (int j = 0; j < header.length; j++) {packet[j] = header[j];}
		for (int k = 0; k < data.length; k++) {packet[header.length + k] = data[k];}
		
		return packet;
	} // End of IPv4Packet
	
	
	// Since the data to be added at the end of the IPv4 packet is in powers
	// of 2, this method generates a byte array who's size if a power of 2, 
	// filled with random data
	public static byte[] genByteArray(int n) 
	{
		int numBytes = (int)Math.pow(2, n);
		byte[] arr = new byte[numBytes];
		System.out.println("size of data " + arr.length);
		
		for(int i = 0; i  < numBytes; i++){
			arr[i] = (byte)(Math.random() * 255);
		}
		
		return arr;
	}
	
	
	
	public static void main(String[] args) throws Exception
	{
		try (Socket socket = new Socket("18.221.102.182", 38003))
		{
			// Creating client input/output streams to receive 
			// and send messages from and to server	
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			OutputStream os = socket.getOutputStream();
			
			for(int i = 1; i <= 12; i++) {
				byte[] d = genByteArray(i);	// Random data array created
				byte[] a = IPv4Packet(d);	//IPv4 packet created
				os.write(a);	// Packet sent to server
				System.out.println(br.readLine());	// Reading server response
			}
		}	
	}
	
}// End of file