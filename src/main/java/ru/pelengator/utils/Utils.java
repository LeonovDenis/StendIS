package ru.pelengator.utils;

import at.favre.lib.bytes.Bytes;
import javafx.collections.ObservableList;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.model.Frame;

import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {
    /**
     * Определение номера видеовыхода по номеру канала
     *
     * @param i номер канала от 0 до 287
     * @return номер видеовыхода от 0 до 3
     */
    public static int getChNumber(int i) {
        if (i < 144) {
            //либо выход 0 либо 1
            if (i % 2 == 0) {
                //выход 0
                i = 0;
            } else {
                //выход 1
                i = 1;
            }
        } else {
            if (i % 2 == 0) {
                //выход 2
                i = 2;
            } else {
                //выход 3
                i = 3;
            }
        }
        return i;
    }
    /**
     * Конвертер байтового массива в интовый с изменением порядка байт . Значения по 2 байта
     *
     * @param bb Байтовый необработанный массив
     * @return Интовый обработанный массив
     */
    public static Bytes convertBytesArrayToIntArray(Bytes bb) {
        byte[] bbArray = bb.array();
        Bytes intArray = Bytes.from(new int[0]);
        for (int i = 0; i < bb.length(); i = i + 2) {
            char c = Bytes.from(new byte[]{bbArray[i], bbArray[i + 1]}).reverse().toChar();
            intArray = intArray.append((int) c);
        }
        return intArray;
    }
    public static void savefile(ObservableList<Frame> frameArrayList, String fileName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream("./" + fileName + "_" + DetectorViewModel.getVr0() + ".txt", false)) {
            StringBuffer sb = new StringBuffer();
            /**sb.append("-----")
             .append("\n")
             .append(new Timestamp(System.currentTimeMillis()))
             .append(" VR0= ").append(DetectorViewModel.getVr0())
             .append("\n")
             .append("-----")
             .append("\n");*/
            for (int i = 0; i < 288; i++) {
                sb.append("Channel").append(i);
                if (i != 287) {
                    sb.append(",");
                }
            }
            sb.append("\n");
            for (Frame fr : frameArrayList) {
                int[] data = fr.getData();
                for (int i = 0; i < 288; i++) {
                    sb.append(data[i]);
                    if (i != 287) {
                        sb.append(",");
                    }
                }
                sb.append("\n");
            }
            byte[] buff = sb.toString().getBytes();
            fileOutputStream.write(buff, 0, buff.length-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
