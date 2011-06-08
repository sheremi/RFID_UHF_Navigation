/**
 * 
 */
package de.unierlangen.like.serialport;

/**
 * @author Yuriy
 *
 */
public interface ConsoleThreadListener {
	void handleSymbolsSent(String sentString);
	void handleSymbolsReceived(String receivedString);
}
