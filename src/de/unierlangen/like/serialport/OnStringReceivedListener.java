package de.unierlangen.like.serialport;


/**
 * Interface used to get symbols received by serial port's internal thread.
 * Use with {@link setOnStringReceivedListener}
 * @author Kate
 *
 */
public interface OnStringReceivedListener {
	/** 
	 * Called when symbols are received
	 * @param string recieved from the port
	 */
	void onStringReceived(String string);
}
