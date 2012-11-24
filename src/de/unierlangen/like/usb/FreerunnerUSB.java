/**
 * 
 */
package de.unierlangen.like.usb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.better.wakelock.Logger;

/**
 * Used to control Freerunner's USB
 * 
 * @author kulikov
 */
public class FreerunnerUSB {
    /**
     * Sets USB state to "host" if isHost is true and to "device" if isHost is
     * false
     * 
     * @param isHost
     * @throws IOException
     */
    static public void setUSBHostState(boolean isHost) throws IOException {

        String logicState = "device";
        String powerState = "0";

        if (isHost == true) {
            logicState = "host";
            powerState = "1";
        }

        try {

            /** Create OutputStream to usb port driver */
            FileChannel hostDriverOutputChannel = new FileOutputStream(
                    "/sys/devices/platform/s3c-ohci/usb_mode").getChannel();
            hostDriverOutputChannel.write(ByteBuffer.wrap(logicState.getBytes()));
            hostDriverOutputChannel.close();

            FileOutputStream powerDriverOutputStream = new FileOutputStream(
                    "/sys/class/i2c-adapter/i2c-0/0-0073/neo1973-pm-host.0/hostmode");
            FileChannel powerDriverOutputChannel = powerDriverOutputStream.getChannel();
            powerDriverOutputChannel.write(ByteBuffer.wrap(powerState.getBytes()));
            powerDriverOutputChannel.close();

        } catch (FileNotFoundException e) {
            Logger.e("Catched: ", e);
        } catch (IOException e) {
            Logger.e("Catched: ", e);
            throw e;
        }

    }

}
