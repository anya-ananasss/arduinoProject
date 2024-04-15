import com.fazecast.jSerialComm.*;

public class ComPortListener implements SerialPortDataListener {
    private static String bufferReadToString = ""; // empty, but not null
    private static int cutoffASCII = 10; // ASCII code of the character used for cut-off between received messages

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; //returns data on the Serial Port
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
        event.getSerialPort().readBytes(buffer, buffer.length);

        String s = new String(buffer);
        bufferReadToString = bufferReadToString.concat(s); //converts the bytes read from the Serial port to a string

        if ((bufferReadToString.indexOf(cutoffASCII) + 1) > 0) {
            System.out.println(bufferReadToString); //prints out the recived data
        }
    }
}