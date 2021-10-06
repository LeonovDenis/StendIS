package ru.pelengator.services;

import at.favre.lib.bytes.Bytes;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.model.Frame;

import static ru.pelengator.PropFile.*;
import static ru.pelengator.model.Connector.reverse;
import static ru.pelengator.utils.Utils.convertBytesArrayToIntArray;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Парсер входных данных
 */
public class DataReciever extends ScheduledService<Integer> {

    private static long IDCounter = 0L;
    private static Bytes firstPartOfFrame;//первая часть видеокадра
    private static Bytes secondPartOfFrame;// вторая часть видеокадра
    private static boolean fl_capeFirstpart = false;//флаг захвата первой части кадра

    private final DetectorViewModel detectorViewModel;//ссылка
    private final transient BooleanProperty isExperiment = new SimpleBooleanProperty(false);

    /**
     * Конструктор
     *
     * @param detectorViewModel ссылка
     */
    public DataReciever(DetectorViewModel detectorViewModel) {
        this.detectorViewModel = detectorViewModel;
    }

    @Override
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //Читаем данные из буфера
                Bytes bytesData = detectorViewModel.getConnector().readData();
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //проверка на пустой массив
                if (bytesData.isEmpty()) {
                  //  System.out.println("В буфере ничего нет: " + bytesData);
                    detectorViewModel.setIsOk(false);// флаг на отсутствие коннекта с платой
                    return null;// выход
                }
                detectorViewModel.setIsOk(true);//флаг на коннект с платой
                //Если совпадает маска и айди
                if (bytesData.startsWith(header.array())) {
                    byte function = bytesData.byteAt(2);// читаем номер функции

                    switch (function) {
                        //обработка видео
                        case 0x28://Пишем первую часть кадра
                            Bytes byteDataWithoutHeader = bytesData.resize(bytesData.length() - 6);
                            firstPartOfFrame = convertBytesArrayToIntArray(byteDataWithoutHeader);
                            fl_capeFirstpart = true;
                            return null;
                        case 0x05://резерв
                        case 0x02://резерв
                            //Подтветрждение , что всё хорошо
                            fl_capeFirstpart = false;
                            break;
                        case 0x00://отработка отображения всех параметров
                            fl_capeFirstpart = false;
                            parseParams(bytesData);//обрабатываем сообщение
                            break;
                        default:
                            //Есть ошибка 7-й бит 1-нет такой функции//6-й бит 1-ошибка данных
                            fl_capeFirstpart = false;
                            System.err.println("Ошибка отправки :" + bytesData +
                                    "\n в Хексе: " + bytesData.encodeHex(true));
                            return null;
                    }
                } else {//Отсутствует заголовок у сообщения
                    if (fl_capeFirstpart && ((firstPartOfFrame.length() / 8 + bytesData.length() / 4) == LINENUMBER)) {//обработка второй части видеокадра
                        secondPartOfFrame = convertBytesArrayToIntArray(bytesData);
                        fl_capeFirstpart = false;
                        Bytes fullFrame = Bytes.from(firstPartOfFrame).append(secondPartOfFrame);
                        int[] intArray = fullFrame.toIntArray();
                        ///обработка ошибки платы
                        //создание кадра
                        Frame frame = new Frame(new Timestamp(System.currentTimeMillis()), ++IDCounter, intArray);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////складирование//////////////////////////////////////////////////////////////////////////////////////////
                        DetectorViewModel.setMyFrame(frame);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    }
                }
                return null;
            }
        };
    }

    /**
     * Обработка типа запрошенных параметров
     *
     * @param bytesData
     */
    private void parseParams(Bytes bytesData) {
        Bytes resize = bytesData.resize(bytesData.length() - 4);
        byte[] array = resize.array();
        BorderPane node = null;
        if (array[0] == 0x01) {//основные параметры
            node = createMSGFIRST(array);
            showParam(node, 1);
        } else {//деселекция
            node = createMSGSECOND(array);
            if (node != null) {
                showParam(node, 2);
            }
        }
    }

    /**
     * Обработка сообщения параметров второго типа
     *
     * @param array сообщение
     * @return окно
     */
    private BorderPane createMSGSECOND(byte[] array) {
        Bytes resize = Bytes.from(array).resize(array.length - 1);
        byte[] data = resize.array();
        SecondProps secondProps = new SecondProps(data);
        Node secNode = secondProps.getFirstNode();
        if (secNode == null) {
            return null;
        }
        Node matrix = secondProps.getMatrix();
        BorderPane borderPane = new BorderPane();
        BorderPane.setAlignment(secNode, Pos.CENTER);
        BorderPane.setMargin(secNode, new Insets(5, 5, 5, 5));
        borderPane.setTop(secNode);
        BorderPane.setMargin(matrix, new Insets(5, 5, 5, 5));
        BorderPane.setAlignment(matrix, Pos.CENTER);
        borderPane.setBottom(matrix);
        return borderPane;
    }

    /**
     * Обработка сообщения параметров первого типа
     *
     * @param array сообщение
     * @return окно
     */
    private BorderPane createMSGFIRST(byte[] array) {
        Bytes resize = Bytes.from(array).resize(array.length - 1);
        byte[] data = resize.array();
        Node firstNode = new MainProps(data).getFirstNode();
        BorderPane borderPane = new BorderPane();
        BorderPane.setAlignment(firstNode, Pos.CENTER);
        BorderPane.setMargin(firstNode, new Insets(5, 5, 5, 5));
        borderPane.setCenter(firstNode);
        return borderPane;
    }

    /**
     * Отображение окна параметров
     *
     * @param ss окно
     * @param i  тип
     */
    private void showParam(BorderPane ss, int i) {
        Platform.runLater(() -> {
            Scene scene = null;
            Stage newWindow = new Stage();
            newWindow.initModality(Modality.APPLICATION_MODAL);
            newWindow.setResizable(false);
            if (i == 1) {
                scene = new Scene(ss);
                newWindow.setTitle("Основные параметры:");
            } else {
                scene = new Scene(ss, 1180, 200);
                newWindow.setTitle("Дополнительные параметры:");
            }
            newWindow.setScene(scene);
            newWindow.showAndWait();
        });
    }

    /**
     * Вложенный класс парсер параметров
     */
    private static class MainProps {
        byte[] data;
        int i = 0;

        /**
         * Конструктор
         *
         * @param data сообщение
         */
        public MainProps(byte[] data) {
            this.data = data;
        }

        /**
         * Создание окна первого типа
         *
         * @return узел
         */
        public Node getFirstNode() {
            GridPane root = new GridPane();
            root.setGridLinesVisible(false);

            root.add(new Label("Устройство:"), 0, 0);
            if (data[i++] == DEV_ID) {
                root.add(new Label(DETECTORNAME), 1, 0);
            } else {
                root.add(new Label("Устройство не опознано:" + data[i - 1]), 1, 0);
            }

            root.add(new Label("Параллельный порт:"), 0, 1);
            BitSet bitSet = Bytes.from(data[i++]).toBitSet();
            root.add(new Label(bitSet.toString()), 1, 1);

            root.add(new Label("INT,мкс:"), 0, 2);
            short intt = Bytes.from(new byte[]{data[this.i++], data[this.i++]}).toShort();
            root.add(new Label(String.valueOf(intt)), 1, 2);

            root.add(new Label("Напряжение VR0 В:"), 0, 3);
            float vr0 = Bytes.from(new byte[]{data[i++], data[i++], data[i++], data[i++]}).toFloat();
            root.add(new Label(String.valueOf(vr0)), 1, 3);

            root.add(new Label("Напряжение VVA В:"), 0, 4);
            float vva = Bytes.from(new byte[]{data[i++], data[i++], data[i++], data[i++]}).toFloat();
            root.add(new Label(String.valueOf(vva)), 1, 4);

            root.add(new Label("Напряжение UC В:"), 0, 5);
            float uc = Bytes.from(new byte[]{data[i++], data[i++], data[i++], data[i++]}).toFloat();
            root.add(new Label(String.valueOf(uc)), 1, 5);

            root.add(new Label("Напряжение VU4 В:"), 0, 6);
            float vu4 = Bytes.from(new byte[]{data[i++], data[i++], data[i++], data[i++]}).toFloat();
            root.add(new Label(String.valueOf(vu4)), 1, 6);

            root.add(new Label("Напряжение на МКС, В:  "), 0, 7);
            float uu = Bytes.from(new byte[]{data[i++], data[i++], data[i++], data[i++]}).toFloat();
            root.add(new Label(String.valueOf(uu)), 1, 7);

            root.add(new Label("Ток на МКС, А:"), 0, 8);
            float ii = Bytes.from(new byte[]{data[i++], data[i++], data[i++], data[i++]}).toFloat();
            root.add(new Label(String.valueOf(ii)), 1, 8);

            root.add(new Label("Sys_conf:"), 0, 9);
            BitSet sys = Bytes.from(data[i++]).toBitSet();
            StringBuilder temp = new StringBuilder();
            if (!sys.get(0) && sys.get(1)) {
                temp.append("Диапазон 5 В").append("\n");
            } else {
                temp.append("Диапазон 3,3 В").append("\n");
            }
            if (sys.get(6)) {
                temp.append("Питание МКС: 12 В").append("\n");
            } else {
                temp.append("Питание МКС: 24 В").append("\n");
            }
            if (sys.get(7)) {
                temp.append("МКС включена").append("\n");
            } else {
                temp.append("МКС выключена").append("\n");
            }
            root.add(new Label(temp.toString()), 1, 9);
            return root;
        }

    }

    /**
     * Вложенный класс парсер параметров
     */
    private static class SecondProps {
        byte[] data;
        public static ArrayList<Button> buttons = new ArrayList<>();
        public static byte[] matr = new byte[144];
        public static ArrayList<String> list = new ArrayList<>();

        /**
         * Конструктор
         *
         * @param data сообщение второго типа
         */
        public SecondProps(byte[] data) {
            this.data = data;
        }

        /**
         * Создание окна
         *
         * @return
         */
        public Node getFirstNode() {
            GridPane root = new GridPane();
            root.setGridLinesVisible(false);

            String block = String.valueOf(data[0]);
            if (!list.contains(block)) {
                list.add(block);
                for (int i = 24 * data[0], j = 2; i < 24 + (24 * data[0]); i++, j++) {
                    matr[i] = data[j];
                }
            } else {
                return null;
            }
            if (list.size() == 6) {

                root.add(new Label("Ёмкость: "), 0, 0);
                BitSet bitSet = Bytes.from(data[1]).toBitSet();
                StringBuilder temp = new StringBuilder();
                //     SGAIN[2]-  SGAIN[1]-       SGAIN[0]
                if (bitSet.get(6) && bitSet.get(5) && bitSet.get(4)) {
                    temp.append(1.6).append(" пФ").append("\n");
                } else if (bitSet.get(6) && bitSet.get(5) && !bitSet.get(4)) {
                    temp.append(1.4).append(" пФ").append("\n");
                } else if (bitSet.get(6) && !bitSet.get(5) && bitSet.get(4)) {
                    temp.append(1.2).append(" пФ").append("\n");
                } else if (bitSet.get(6) && !bitSet.get(5) && !bitSet.get(4)) {
                    temp.append(1.0).append(" пФ").append("\n");
                } else if (!bitSet.get(6) && bitSet.get(5) && bitSet.get(4)) {
                    temp.append(0.8).append(" пФ").append("\n");
                } else if (!bitSet.get(6) && bitSet.get(5) && !bitSet.get(4)) {
                    temp.append(0.6).append(" пФ").append("\n");
                } else if (!bitSet.get(6) && !bitSet.get(5) && bitSet.get(4)) {
                    temp.append(0.4).append(" пФ").append("\n");
                } else if (!bitSet.get(6) && !bitSet.get(5) && !bitSet.get(4)) {
                    temp.append(0.2).append(" пФ").append("\n");
                }
                root.add(new Label(temp.toString()), 1, 0);
                temp = new StringBuilder();
                root.add(new Label("Режим :"), 0, 1);

                //     SDIR-     SBYPASS[1]-       SBYPASS[0]
                if (bitSet.get(3) && !bitSet.get(2) && !bitSet.get(1)) {
                    temp.append("ВЗН [").append(" Обратное напр.]").append("\n");
                } else if (!bitSet.get(3) && !bitSet.get(2) && !bitSet.get(1)) {
                    temp.append("ВЗН [").append(" Прямое направл.]").append("\n");
                } else if (!bitSet.get(3) && bitSet.get(2) && !bitSet.get(1)) {
                    temp.append("BYPASS [").append(" 1-я строка]").append("\n");
                } else if (bitSet.get(3) && bitSet.get(2) && !bitSet.get(1)) {
                    temp.append("BYPASS [").append(" 2-я строка]").append("\n");
                } else if (!bitSet.get(3) && bitSet.get(2) && bitSet.get(1)) {
                    temp.append("BYPASS [").append(" 3-я строка]").append("\n");
                } else if (bitSet.get(3) && bitSet.get(2) && bitSet.get(1)) {
                    temp.append("BYPASS [").append(" 4-я строка]").append("\n");
                }
                root.add(new Label(temp.toString()), 1, 1);

                return root;
            }
            return null;
        }

        /**
         * Создание набора пикселей
         *
         * @return узел
         */
        private Node getMatrix() {
            if (buttons != null) {
                buttons.clear();
            }
            GridPane root = new GridPane();
            root.setPadding(new Insets(5, 5, 5, 5));
            root.setHgap(4.2);
            root.setVgap(5);
            root.setMinSize(500, 120);
            root.setGridLinesVisible(false);
            int row = 0;
            int bt = 0;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 288; j++) {

                    if (((j % 2 == 0) && i > 4) || ((j % 2 == 1) && i < 4)) {
                        Button btn = new Button(" ");
                        Tooltip tooltip = new Tooltip(btn.getId());
                        tooltip.setFont(new Font(20));
                        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
                        btn.setTooltip(tooltip);
                        Bindings.bindBidirectional(btn.tooltipProperty().get().textProperty(), btn.idProperty());
                        btn.setStyle("-fx-font: 3.4 arial; -fx-base: #17c355;");
                        btn.setAlignment(Pos.CENTER);
                        root.add(btn, j, i, 2, 1);
                        buttons.add(btn);

                    }
                }
            }
            for (int k = 0; k < buttons.size(); k++) {
                bt = 1 + (k % (CHANNELNUMBER / 2));
                row = 8 - (k / (CHANNELNUMBER / 2));
                buttons.get(k).setId(bt + ":" + row + ":" + "Включен");
            }
            for (int i = 0; i < 144; i++) {
                String str = String.valueOf(i + 1);
                byte i1  = (byte) reverse(matr[i], 9);
               // byte i1 = matr[i];
                for (int j = 0; j < 8; j++) {
                    String str2 = String.valueOf(8 - j);
                    for (Button b : buttons) {
                        if (b.getId().startsWith(str + ":" + str2 + ":")) {
                            if (((i1 >> j) & 0b1) == 1) {
                                b.setStyle("-fx-font: 3.4 arial; -fx-base: #17c355;");
                                b.setId(str + ":" + str2 + ":" + "Включен");
                            } else {
                                b.setStyle("-fx-font: 3.4 arial; -fx-base: #c32b17;");
                                b.setId(str + ":" + str2 + ":" + "Выключен");
                            }
                        }
                    }
                }
            }
            list.clear();
            Arrays.fill(matr, (byte) 0x00);
            return root;
        }
    }
}