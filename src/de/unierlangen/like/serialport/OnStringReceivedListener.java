package de.unierlangen.like.serialport;
/**
 * @author Yuriy
 *
 */
public interface OnStringReceivedListener {
	/** 
	 * Called when symbols are received
	 * @param string
	 */
	void onStringReceived(String string);
}
