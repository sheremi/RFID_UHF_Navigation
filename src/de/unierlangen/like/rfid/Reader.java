package de.unierlangen.like.rfid;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.util.Log;
import de.unierlangen.like.serialport.OnStringReceivedListener;
import de.unierlangen.like.serialport.SerialPort;

public class Reader implements OnStringReceivedListener{

	private static final String TAG = "Reader";
	SerialPort readerSerialPort;	
	private String amountOfTags;
	private String response = "";
	//private Lock mLock;
	
	public static enum Configuration {DEFAULT, LOW_POWER, HIGH_POWER};
	/**
	 * Constructor opens serial port and performs handshake
	 * @param sp
	 * @throws InvalidParameterException
	 * @throws SecurityException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Reader(SharedPreferences sp) throws InvalidParameterException, SecurityException, IOException, InterruptedException{
		String path = sp.getString("DEVICE", "");
		int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
		readerSerialPort = new SerialPort(path, baudrate);
		readerSerialPort.setOnStringReceivedListener(this);
		synchronized (this) {
			readerSerialPort.writeString("preved");
			this.wait(700);		
		}
		
		if (response.contains("medved")){
			//everything is fine
			return;
		} else if (response.contains("error")){
			throw new IOException("Reader MCU reported error:" + response);
		} else if (response.length()!=0){
			throw new IOException("Reader MCU behaves odd, check baudrate: " + response);
		} else {
			throw new IOException("No response from reader");
		}
	}
	
	public void initialize(Configuration configuration) throws IOException{
		//XXX choose configuration from enum
		switch (configuration){
			case LOW_POWER:
				break;
			case HIGH_POWER:
				break;
			case DEFAULT:
			default:
				readerSerialPort.writeString("a");
		}
		
	}
	
	public ArrayList<GenericTag> performRound() throws IOException {
		//Create container
		ArrayList<GenericTag> tags = new ArrayList<GenericTag>();
		//Tell reader MCU to start inventory round
		//String tagsString = "tags,3,EBA123,14,FA894,1,BEEF666,30";
		//readerSerialPort.readString();
		synchronized (this) {
			response="";
			readerSerialPort.writeString("req r tags");
			//mLock.wait();
			try {
				this.wait(700);
			} catch (InterruptedException e) {
				Log.e(TAG, "Unexpected interrupt",e);
			}
			if (response.contains("error")) {
				throw new IOException("Reader MCU reported error:" + response);
			}	
		}

		Pattern oneElement = Pattern.compile(",");
		ArrayList<String> strings = new ArrayList<String>(Arrays.asList(oneElement.split(response)));
		Iterator<String> iterator = strings.iterator();
		// Check if string header is correct
		if (iterator.next()!="resp r tags")
			throw new IOException("response should start with 'resp r tags'");
		// TODO use amountOfTags where it should be used
		amountOfTags = iterator.next();
		while (iterator.hasNext()){
			tags.add(new GenericTag(iterator.next(),Integer.parseInt(iterator.next()), true));
		}
		
		return tags;
	}
	
	public ArrayList<String> displayRegisters() throws IOException, InterruptedException{
		// TODO create class for registers
		synchronized (this) {
			response="";
			readerSerialPort.writeString("req r regs");
			try {
				this.wait(700);
			} catch (InterruptedException e) {
				Log.e(TAG, "Unexpected interrupt",e);
			}
		}
		if (response.contains("error")) {
			throw new IOException("Reader MCU reported error:" + response);
		}
		Pattern oneElement = Pattern.compile(",");
		ArrayList<String> strings = new ArrayList<String>(Arrays.asList(oneElement.split(response)));
		// Check if string header is correct
		if (strings.remove(0)!="resp r regs")
			throw new IOException("response should start with 'resp r regs'");
		
		return strings;
	}
	
	public String getAmountOfTags() {
		return amountOfTags;
	}

	/**
	 * Called when {@link de.unierlangen.like.serialport.SerialPort SerialPort}
	 * receives string from serial port. Stores response and unlocks the thread.
	 */
	public void onStringReceived(String string) {
		synchronized (this) {
			response = response.concat(string);
			if (response.contains("\n")){
				Log.d(TAG, "Response: "+response);
				this.notify();	
			}
		}
	}
}
