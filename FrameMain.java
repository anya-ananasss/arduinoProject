import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FrameMain extends JFrame {

    private JPanel panelMain;
    private JComboBox<String> modeComboBox;
    private int mode;
    private JCheckBox silentModeCheckBox;
    private JSpinner hoursFromSpinner;
    private int hoursFrom;
    private JSpinner minutesFromSpinner;
    private int minutesFrom;
    private JSpinner hoursToSpinner;
    private int hoursTo;
    private JSpinner minutesToSpinner;
    private int minutesTo;
    private JTextArea realHumidityTextArea;
    private JTextArea hTextArea;
    private int h;
    private int H;
    private int t;
    private int T;
    private JTextArea tTextArea;
    private JTextArea realTemperatureTextArea;
    private JLabel hyphen;
    private JTextArea stateTextArea;
    private JButton saveTimeButton;
    private JCheckBox constSilModeCheckBox;
    boolean buttonPressed;

    public FrameMain() throws IOException {
        this.setTitle("Система контроля температуры и влажности");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        setSize(1200, 1200);

        SpinnerNumberModel hoursModel1 = new SpinnerNumberModel(0, 0, 23, 1);
        SpinnerNumberModel hoursModel2 = new SpinnerNumberModel(0, 0, 23, 1);
        hoursFromSpinner.setModel(hoursModel1);
        hoursToSpinner.setModel(hoursModel2);

        SpinnerNumberModel minModel1 = new SpinnerNumberModel(0, 0, 59, 1);
        SpinnerNumberModel minModel2 = new SpinnerNumberModel(0, 0, 59, 1);
        minutesFromSpinner.setModel(minModel1);
        minutesToSpinner.setModel(minModel2);

        hoursFromSpinner.setVisible(false);
        hoursToSpinner.setVisible(false);
        hyphen.setVisible(false);
        minutesFromSpinner.setVisible(false);
        minutesToSpinner.setVisible(false);
        saveTimeButton.setVisible(false);
        constSilModeCheckBox.setVisible(false);

        hTextArea.setText("70-90%");
        tTextArea.setText("20-24 °C");

        buttonPressed = false;
        silentModeCheckBox.addActionListener(e -> {
            if (silentModeCheckBox.isSelected()) {
                hoursFromSpinner.setVisible(true);
                hoursToSpinner.setVisible(true);
                hyphen.setVisible(true);
                minutesFromSpinner.setVisible(true);
                minutesToSpinner.setVisible(true);
                saveTimeButton.setVisible(true);
                constSilModeCheckBox.setVisible(true);
            } else {
                hoursFromSpinner.setVisible(false);
                hoursToSpinner.setVisible(false);
                hyphen.setVisible(false);
                minutesFromSpinner.setVisible(false);
                minutesToSpinner.setVisible(false);
                saveTimeButton.setVisible(false);
                constSilModeCheckBox.setVisible(false);
                buttonPressed = false;
            }
        });
        hoursFrom = 0;
        minutesTo = 0;
        hoursTo = 0;
        minutesFrom = 0;

        saveTimeButton.addActionListener(e -> {
            hoursFrom = (int) hoursFromSpinner.getValue();
            hoursTo = (int) hoursToSpinner.getValue();
            minutesFrom = (int) minutesFromSpinner.getValue();
            minutesTo = (int) minutesToSpinner.getValue();
            buttonPressed = true;
            System.out.println("сохранили режим");
        });


        modeComboBox.addActionListener(e -> {
            String selected = (String) modeComboBox.getSelectedItem();
            switch (Objects.requireNonNull(selected)) {
                case ("Улитки"):
                    hTextArea.setText("70-90%");
                    tTextArea.setText("20-24 °C");
                    h = 70;
                    H = 90;
                    t = 20;
                    T = 24;
                    mode = 0;
                    System.out.println("кинули");
                    break;
                case ("Хомяки, морские свинки"):
                    hTextArea.setText("40-60%");
                    tTextArea.setText("20-24 °C");
                    h = 40;
                    H = 60;
                    t = 20;
                    T = 24;
                    mode = 1;
                    System.out.println("кинули");
                    break;
                case ("Птицы"):
                    hTextArea.setText("40-70%");
                    tTextArea.setText("21-24 °C");
                    h = 40;
                    H = 70;
                    t = 21;
                    T = 24;
                    mode = 2;
                    System.out.println("кинули");
                    break;
                case ("Ящерицы"):
                    hTextArea.setText("40-60%");
                    tTextArea.setText("25-35 °C");
                    h = 40;
                    H = 60;
                    t = 25;
                    T = 35;
                    mode = 3;
                    System.out.println("кинули");
                    break;
                case ("Свой режим"):
                    ownModeLogic();
                    break;
            }
        });

        Logic logic = new Logic();
        final SerialPort[] sp = {openPortConnection()};
        int[] prevData = new int[]{-1, -1};
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!sp[0].isOpen()){
                    sp[0] = openPortConnection();
                }

                int silMode;
                if (silentModeCheckBox.isSelected() && buttonPressed) {
                    silMode = logic.workWithTime(hoursFrom, minutesFrom, hoursTo, minutesTo, LocalTime.now());
                } else if (constSilModeCheckBox.isSelected()) {
                    silMode = 1;
                } else {
                    if (!silentModeCheckBox.isSelected()) {
                        buttonPressed = false;
                    }
                    silMode = 0;
                }
                try {
                    if (mode != 4) {
                        logic.inputToArduino(sp[0], silMode, mode);
                    } else {
                        logic.inputToArduino(sp[0], silMode, h, H, t, T);
                    }

                    System.out.println("prev data:");
                    for (int j : prevData) {
                        System.out.print(j + " ");
                    }
                    System.out.println(" ");
                    int[] realValues = logic.outputFromArduino(sp[0], prevData);
                    if (realValues[0] <= 0 || realValues[1] <= 0 && sp[0].isOpen()) {
                        System.arraycopy(prevData, 0, realValues, 0, prevData.length);
                    }
                    System.out.println("real vals:");
                    for (int j : realValues) {
                        System.out.print(j + " ");
                    }


                    System.arraycopy(realValues, 0, prevData, 0, realValues.length);


                    System.out.println(" ");
                    realHumidityTextArea.setText(((Integer) realValues[0]).toString());
                    realTemperatureTextArea.setText(((Integer) realValues[1]).toString());

                    if (mode != 4) {
                        if (logic.isStateOk(realValues, mode)) {
                            stateTextArea.setText("Параметры соответсвуют рекомендуемым :)");
                        } else {
                            stateTextArea.setText("Параметры не соответсвуют рекомендуемым!");
                        }
                    } else {
                        if (logic.isStateOk(realValues, h, H, t, T)) {
                            stateTextArea.setText("Параметры соответсвуют рекомендуемым :)");
                        } else {
                            stateTextArea.setText("Параметры не соответсвуют рекомендуемым!");
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }, 0, 1000);
    }

    boolean ownModeCondition_humidity(String idealHumidityHighest, String idealHumidityLowest) {
        return (Integer.parseInt(idealHumidityHighest) <= Integer.parseInt(idealHumidityLowest) ||
                Integer.parseInt(idealHumidityHighest) <= 0 || Integer.parseInt(idealHumidityHighest) > 100 ||
                Integer.parseInt(idealHumidityLowest) <= 0 || Integer.parseInt(idealHumidityLowest) > 100);
    }

    boolean ownModeCondition_temperature(String idealTemperatureHighest, String idealTemperatureLowest) {
        return (Integer.parseInt(idealTemperatureHighest) <= Integer.parseInt(idealTemperatureLowest) ||
                Integer.parseInt(idealTemperatureHighest) < 0 || Integer.parseInt(idealTemperatureHighest) > 50 ||
                Integer.parseInt(idealTemperatureLowest) < 0 || Integer.parseInt(idealTemperatureLowest) > 50);
    }

    void ownModeLogic() {
        JFrame frame = new JFrame();
        boolean correctHumidData = false;
        boolean correctTempData = false;
        String idealHumidityLowest = "0";
        String idealHumidityHighest = "0";

        String idealTemperatureLowest = "0";
        String idealTemperatureHighest = "0";
        do {

            try {
                if (!correctHumidData) {
                    idealHumidityLowest = JOptionPane.showInputDialog(frame,
                            "Введите нижнюю границу рекомендуемой влажности (1-99%):", "Humidity", JOptionPane.QUESTION_MESSAGE);
                    if (idealHumidityLowest == null){
                        return;
                    }
                    idealHumidityHighest = JOptionPane.showInputDialog(frame,
                            "Введите верхнюю границу рекомендуемой влажности (1-99%):", "Humidity", JOptionPane.QUESTION_MESSAGE);
                    if (idealHumidityHighest == null){
                        return;
                    }
                }
                if (ownModeCondition_humidity(idealHumidityHighest, idealHumidityLowest)) {
                    JOptionPane.showMessageDialog(frame, "Введен неверный диапозон влажности!");
                    correctHumidData = false;
                }
                else {
                    correctHumidData = true;
                }


                if (!correctTempData) {
                    idealTemperatureLowest = JOptionPane.showInputDialog(frame,
                            "Введите нижнюю границу рекомендуемой температуры (0-50 °C):", "Temperature", JOptionPane.QUESTION_MESSAGE);
                    if (idealTemperatureLowest == null){
                        return;
                    }
                    idealTemperatureHighest = JOptionPane.showInputDialog(frame,
                            "Введите верхнюю границу рекомендуемой температуры (0-50 °C):", "Temperature", JOptionPane.QUESTION_MESSAGE);
                    if (idealTemperatureHighest == null){
                        return;
                    }
                }
                if (ownModeCondition_temperature(idealTemperatureHighest, idealTemperatureLowest)) {
                    JOptionPane.showMessageDialog(frame, "Введен неверный диапозон температур!");
                    correctTempData = false;
                } else {
                    correctTempData = true;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Введите числовые значения!");
            }


        } while (!correctHumidData || !correctTempData);
        h = Integer.parseInt(idealHumidityLowest);
        H = Integer.parseInt(idealHumidityHighest);
        t = Integer.parseInt(idealTemperatureLowest);
        T = Integer.parseInt(idealTemperatureHighest);

        hTextArea.setText(idealHumidityLowest + "-" + idealHumidityHighest + "%");
        tTextArea.setText(idealTemperatureLowest + "-" + idealTemperatureHighest + " °C");
        mode = 4;
    }
    SerialPort openPortConnection(){
        SerialPort[] ports = SerialPort.getCommPorts();
        String connected = "";
        for (SerialPort port : ports) {
            String desc = port.getDescriptivePortName();
            if (desc.startsWith("USB")){
                connected = port.getSystemPortName();
            }
        }
        SerialPort sp = SerialPort.getCommPort(connected);
        sp.setComPortParameters(57600, 8, 1, 0);
        sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        sp.openPort();

        return sp;
    }
}
