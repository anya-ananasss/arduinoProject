import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.time.LocalTime;

public class Logic {

    int workWithTime(int hourFrom, int minuteFrom, int hourTo, int minuteTo, LocalTime currTime) {
        //1 - true, включаем silentMode; 0 - false, silentMode выключен
        final int SILENT_MODE_OFF = 0;
        final int SILENT_MODE_ON = 1;

        int currHour = currTime.getHour();
        int currMinute = currTime.getMinute();

        if (hourFrom <= hourTo) {
            // интервал не пересекает полночь
            if ((currHour > hourFrom || (currHour == hourFrom && currMinute >= minuteFrom)) &&
                    (currHour < hourTo || (currHour == hourTo && currMinute <= minuteTo))) {
                return SILENT_MODE_ON;
            }
        } else {
            //интервал пересекает полночь
            if ((currHour > hourFrom || (currHour == hourFrom && currMinute >= minuteFrom)) ||
                    (currHour < hourTo || (currHour == hourTo && currMinute <= minuteTo))) {
                return SILENT_MODE_ON;
            }
        }

        return SILENT_MODE_OFF;
    }

    void inputToArduino(SerialPort sp, int i, int mode) throws IOException, InterruptedException {
        StringBuilder strB = new StringBuilder();
        strB.append(i).append(" ");

        int[] defaultParams = defineParamsForDefaultMode(mode);
        int h = defaultParams[0];
        int H = defaultParams[1];
        int t = defaultParams[2];
        int T = defaultParams[3];

        strB.append(h).append(" ");
        strB.append(H).append(" ");
        strB.append(t).append(" ");
        strB.append(T).append("\n");

        String toSend = strB.toString();
        sp.writeBytes(toSend.getBytes(), toSend.length());
    }

    void inputToArduino(SerialPort sp, int s, int h, int H, int t, int T) throws IOException, InterruptedException {
        String toSend = s + " " +
                h + " " +
                H + " " +
                t + " " +
                T + "\n";
        sp.writeBytes(toSend.getBytes(), toSend.length());
    }

    int[] outputFromArduino(SerialPort sp, int[] prevData) {
        int[] intArray = new int[2];
        try {
            if (sp.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[sp.bytesAvailable()];
                sp.readBytes(readBuffer, readBuffer.length);
                String data = new String(readBuffer).trim(); //данные из порта

                String[] numbers = data.split(" ");

                for (int i = 0; i < 2; i++) {
                    intArray[i] = Integer.parseInt(numbers[i]); // строки в целочисленные значения
                }
            }
            return intArray;
        } catch (Exception e) {
            System.out.println(":(");
            return prevData;
        }
    }

    boolean isStateOk(int[] params, int mode) {
        int[] defaultParams = defineParamsForDefaultMode(mode);
        int h = defaultParams[0];
        int H = defaultParams[1];
        int t = defaultParams[2];
        int T = defaultParams[3];

        int realH = params[0];
        int realT = params[1];

        return (realH >= h && realH <= H && realT >= t && realT <= T);
    }

    boolean isStateOk(int[] params, int h, int H, int t, int T) {
        int realH = params[0];
        int realT = params[1];
        return (realH >= h && realH <= H && realT >= t && realT <= T);
    }

    int[] defineParamsForDefaultMode(int mode) {
        int h, H, t, T;
        switch (mode) {
            case 0:
                h = 70;
                H = 90;
                t = 20;
                T = 24;
                break;
            case 1:
                h = 40;
                H = 60;
                t = 20;
                T = 24;
                break;
            case 2:
                h = 40;
                H = 70;
                t = 21;
                T = 24;
                break;
            case 3:
                h = 40;
                H = 60;
                t = 25;
                T = 35;
                break;
            default:
                h = 0;
                H = 0;
                t = 0;
                T = 0;
        }
        return new int[]{h, H, t, T};
    }
}

