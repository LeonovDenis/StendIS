package ru.pelengator;


import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.util.Duration;

import org.decimal4j.util.DoubleRounder;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.time.Second;
import ru.pelengator.charts.ModernChart;
import ru.pelengator.charts.TimeChart;
import ru.pelengator.dao.BDService;
import ru.pelengator.model.Experiment;
import ru.pelengator.model.Frame;
import ru.pelengator.model.Connector;
import ru.pelengator.model.Order;
import ru.pelengator.services.*;


import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static ru.pelengator.App.loadJarDll;
import static ru.pelengator.PropFile.*;
import static ru.pelengator.dao.BDService.TypeColums.type_ID;

public class DetectorViewModel {
    //график наработки
    public static TimeChart timeChart;
    //флаг отправки деселекции
    private static boolean SENDDESEL = true;
    //Flag перегрузки графиков
    private static transient boolean RELOADCHARTS = false;
    //сценарий вкл/выкл
    private static Thread scenario = new Thread();
    //флаг темнового графика. Для отобр кнопок
    private boolean fl_normal = false;
    //блокирующие объектs
    private static Object inBlock = new Object();
    //пауза временного графика
    private static int millis_timer_medianChart = 500;
    //ссылка на контроллер
    private final SecondaryController controller;
    //ссылка на Отчет
    private static Order order = new Order();
    //Текущий кадр
    private static transient Frame myFrame = new Frame();

    ////////////////////////////////////////////параметры прибора//////////////////////////////////////////////
    private static final transient IntegerProperty vr0 = new SimpleIntegerProperty(950);
    private final transient IntegerProperty vva = new SimpleIntegerProperty(0);
    private final transient IntegerProperty vu4 = new SimpleIntegerProperty(5_000);
    private final transient IntegerProperty vuc = new SimpleIntegerProperty(5_000);

    private final transient IntegerProperty tInt = new SimpleIntegerProperty(20);

    private final transient StringProperty mode = new SimpleStringProperty("ВЗН");
    private final transient StringProperty dir = new SimpleStringProperty("Прямое");
    private final transient StringProperty ccc = new SimpleStringProperty("0.2");

    private static byte[] tempMatrix = new byte[144];
    private static final transient ObjectProperty<byte[]> matrix = new SimpleObjectProperty<>(new byte[LINENUMBER]);
    private transient BooleanProperty reset = new SimpleBooleanProperty(false);
    private transient IntegerProperty vddVddaPower = new SimpleIntegerProperty(0);
    private transient IntegerProperty temp = new SimpleIntegerProperty(918);
    private transient BooleanProperty isScenariyGoing = new SimpleBooleanProperty(false);
    /////////////////////////////
    private static transient IntegerProperty first_ch = new SimpleIntegerProperty(1);
    private static transient IntegerProperty last_ch = new SimpleIntegerProperty(288);
    private transient IntegerProperty pause_data = new SimpleIntegerProperty(20);
    private transient IntegerProperty pause_video = new SimpleIntegerProperty(200);
    private transient IntegerProperty norm = new SimpleIntegerProperty(500);

    private transient StringProperty detectorName = new SimpleStringProperty("");
    private transient StringProperty testerFIO = new SimpleStringProperty("");
    private transient StringProperty numbersDevises = new SimpleStringProperty("");

    ////////////////////////////////////////////////////////////////////////////
    private transient BooleanProperty isPowerOn = new SimpleBooleanProperty(false);
    ////////////////////////////////////////////////////////////////////////////
    private final transient BooleanProperty isOk = new SimpleBooleanProperty(true);//флаг подключения к плате
    // ///////////////////////////////////////////////////////////
    //статистика по кадру
    private final transient FloatProperty frame_SKO = new SimpleFloatProperty(0.0f);
    private transient FloatProperty frame_mid = new SimpleFloatProperty(0);
    private transient FloatProperty frame_min = new SimpleFloatProperty(0);
    private transient FloatProperty frame_max = new SimpleFloatProperty(0);

    private final transient FloatProperty OUT1_SKO = new SimpleFloatProperty(0.0f);
    private transient FloatProperty OUT1_mid = new SimpleFloatProperty(0);
    private transient FloatProperty OUT1_min = new SimpleFloatProperty(0);
    private transient FloatProperty OUT1_max = new SimpleFloatProperty(0);

    private final transient FloatProperty OUT2_SKO = new SimpleFloatProperty(0.0f);
    private transient FloatProperty OUT2_mid = new SimpleFloatProperty(0);
    private transient FloatProperty OUT2_min = new SimpleFloatProperty(0);
    private transient FloatProperty OUT2_max = new SimpleFloatProperty(0);

    private final transient FloatProperty OUT3_SKO = new SimpleFloatProperty(0.0f);
    private transient FloatProperty OUT3_mid = new SimpleFloatProperty(0);
    private transient FloatProperty OUT3_min = new SimpleFloatProperty(0);
    private transient FloatProperty OUT3_max = new SimpleFloatProperty(0);

    private final transient FloatProperty OUT4_SKO = new SimpleFloatProperty(0.0f);
    private transient FloatProperty OUT4_mid = new SimpleFloatProperty(0);
    private transient FloatProperty OUT4_min = new SimpleFloatProperty(0);
    private transient FloatProperty OUT4_max = new SimpleFloatProperty(0);

    ////////Эксперимент///////////////////////////////////////////////////////////////////////////

    private final transient FloatProperty sco30 = new SimpleFloatProperty(0.0f);
    private final transient FloatProperty sred30 = new SimpleFloatProperty(0.0f);
    private final transient FloatProperty sred40 = new SimpleFloatProperty(0.0f);
    private final transient FloatProperty NETD = new SimpleFloatProperty(0.0f);
    private final transient IntegerProperty firstChanExp = new SimpleIntegerProperty(1);
    private final transient IntegerProperty lastChanExp = new SimpleIntegerProperty(288);
    private final transient IntegerProperty frameCountExp = new SimpleIntegerProperty(128);

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// Services//////////////////////////////////////////
    /////////////////////////////////Сервис обработки команд/////////////////////////////////////
    private Connector connector = new Connector();
    /////////////////////////////////Сервис отрисовки основного графика//////////////////////////
    private Animator main_chart_service;
    /////////////////////////////////Сервис отрисовки графика среднего значения /////////////////
    private TimeChartService median_chart_service;
    /////////////////////////////////Сервис получения данных ////////////////////////////////////
    private DataReciever dataReciever_service;
    /////////////////////////////////Сервис расчета шума/////////////////////////////////////
    private ExpServiceShum exp_shum_service;
    //////////////////////////////////Сервис расчета при 40////////////////////////////////////////
    private ExpService40 exp_40_service;
    //////////////////////////////////Сервис расчета при NEDT////////////////////////////////////////
    private ExpServiceNEDT exp_NEDT_service;
    //////////////////////////сервис реконнекта к плате///////////////////////////////////
    private ReconnectService reconnect_service;
    //////////////////////////сервис ресета эксперимента///////////////////////////////////
    private ExpReset exp_Reset;
    ////////////////////////////Сервис поиска параметров////////////////////////////////////////
    private SearchGoodParamsService exp_Search;
    /////////////////////////////////////////////////////////////////////////////////////////////

    private static Experiment experiment = new Experiment();//ссылка на текущий эксперимент

    private long startNarabTime;//время старта наработки
    private long stopNarabTime;//время остановки наработки
    private volatile boolean fl_narab = false;//флаг наработки
    private Thread thread;//поток наработки
    private boolean done = false;

    /**
     * Инициализация
     */
    public DetectorViewModel(SecondaryController con) {
        this.controller = con;
        initDataReciever_service();//инициализация сервиса получения данных
        initMain_chart_service();//инициализация сервиса главного графика
        initMedian_chart_service();//инициализация сервиса времеонного графика
        initExp_shum_service();//инициализация сервиса расчета шума и 30 градусов
        initExp_40_service();//инициализация сервиса расчета 40 градусов
        initExp_NEDT_service();//инициализация сервиса расчета НЕДТ
        initReconnectService();//инициализация сервиса реконнекта
        initExp_Reset_service();//инициализация сервиса ресета
        initExp_Search_service();//инициализация сервиса поиска параметров
        initNarabThread();//инициализация потока наработки
        //при смене флага реконнект
        isOk.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                //   reconnect_service.cancel();
            } else {
                //    reconnect_service.restart();
            }
        });
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Arrays.fill(getMatrix(), (byte) 0xFF);//заполнение массива деселекции единицами
        /////////////////////////////////////////добавление слушателей и обработчиков/////////////////////////////////////
        /**
         * Обработка поля деселекции
         */
        matrixProperty().addListener((observable, oldValue, newValue) -> {
            done = false;
            for (int i = 0; i < LINENUMBER; i++) {
                if (oldValue[i] != newValue[i]) {
                    String str = String.valueOf(i + 1);
                    byte i1 = (byte) (oldValue[i] ^ newValue[i]);
                    for (int j = 0; j < 8; j++) {
                        if (((i1 >> j) & 0b1) == 1) {
                            String str2 = String.valueOf(8 - j);
                            for (Button b : SecondaryController.buttons
                            ) {
                                if (b.getId().startsWith(str + ":" + str2 + ":")) {
                                    String[] split = b.getId().split(":");
                                    if (split[2].equals("Включен")) {
                                        b.setStyle("-fx-font: 3.4 arial; -fx-base: #c32b17;");
                                        b.setId(str + ":" + str2 + ":" + "Выключен" + ":" + split[3]);
                                    } else {
                                        b.setStyle("-fx-font: 3.4 arial; -fx-base: #17c355;");
                                        b.setId(str + ":" + str2 + ":" + "Включен" + ":" + split[3]);
                                    }
                                }
                            }
                        }
                    }
                    /**
                     * Если разрешена отсылка деселекции и режим взн, то отправляем на селектирование
                     */
                    if (SENDDESEL && (findMode()[0] == (byte) 0x00)) {
                        int finalI = i;
                        Thread thread = new Thread(() -> new Connector().setDesel(newValue[finalI], finalI));
                        thread.setName("Отработка матрицы");
                        thread.setDaemon(true);
                        thread.start();
                    }
                }
            }
            SENDDESEL = true;
            done = true;
        });
        /**
         * Обработка типа устройства и установка ID.
         */
        detectorNameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().equals(DETECTORNAME)) {
                Thread thread = new Thread(() -> new Connector().setID());
                thread.setName("Отработка ID детектора");
                thread.setDaemon(true);
                thread.start();
            }
        });

        /**
         * обработка установки питания
         */
        isPowerOnProperty().addListener((observable, oldValue, newValue) -> {
            Thread thread = new Thread(() -> new Connector().setPower(newValue));
            thread.setName("Отработка включение питания");
            thread.setDaemon(true);
            thread.start();
        });

        /**
         * обработка емкостей
         */
        cccProperty().addListener((observable, oldValue, newValue) -> {
            byte b;
            switch (newValue) {
                case "0.2":
                    b = (byte) 0b0;
                    break;
                case "0.4":
                    b = (byte) 0b0100;
                    break;
                case "0.6":
                    b = (byte) 0b10;
                    break;
                case "0.8":
                    b = (byte) 0b110;
                    break;
                case "1.0":
                    b = (byte) 0b1;
                    break;
                case "1.2":
                    b = (byte) 0b101;
                    break;
                case "1.4":
                    b = (byte) 0b11;
                    break;
                case "1.6":
                    b = (byte) 0b111;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + newValue);
            }
            byte[] mode = findMode();//получение текущих значений ёмкостей
            Thread thread = new Thread(() -> new Connector().setCapacity(mode[0], mode[1], b));
            thread.setName("Отработка ёмкости");
            thread.setDaemon(true);
            thread.start();
        });
        /**
         *обработка направления сканирования
         */
        dirProperty().addListener((observable, oldValue, newValue) -> {
            if (getMode().equals("ВЗН")) {
                byte b;
                switch (newValue) {
                    case "Прямое":
                        b = (byte) 0x00;
                        break;
                    case "Обратное":
                        b = (byte) 0x01 << 3;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + newValue);
                }
                byte cc = findCC();//определение текущей ёмкости
                Thread thread = new Thread(() -> new Connector().setDirection((byte) 0x00, b, cc));
                thread.setName("Отработка направление сканирования");
                thread.setDaemon(true);
                thread.start();
            }
        });

        /**
         *обработка режима работы
         */
        modeProperty().addListener((observable, oldValue, newValue) -> {
            byte b;
            byte dir;
            switch (newValue) {
                case "ВЗН":
                    b = (byte) 0x00;
                    dir = 0;
                    myMatrix((byte) 0xFF, false);
                    if (oldValue.equals("1-Bypass") || oldValue.equals("2-Bypass")
                            || oldValue.equals("3-Bypass") || oldValue.equals("4-Bypass")) {
                        setPixel(tempMatrix);
                    }
                    break;
                case "1-Bypass":
                    b = (byte) 0b1 << 4;
                    dir = 0;
                    if (oldValue.equals("ВЗН")) {
                        tempMatrix = getMatrix();
                    }
                   // myMatrix((byte) 0x81, false);
                    myMatrix((byte) 0x88, false);
                    break;
                case "2-Bypass":
                    b = (byte) 0b1 << 4;
                    dir = 0x01 << 3;
                    if (oldValue.equals("ВЗН")) {
                        tempMatrix = getMatrix();
                    }
                   // myMatrix((byte) 0x42, false);
                    myMatrix((byte) 0x44, false);
                    break;
                case "3-Bypass":
                    b = (byte) 0b110000;
                    dir = 0;
                    if (oldValue.equals("ВЗН")) {
                        tempMatrix = getMatrix();
                    }
                    //myMatrix((byte) 0x24, false);
                    myMatrix((byte) 0x22, false);
                    break;
                case "4-Bypass":
                    b = (byte) 0b110000;
                    dir = 0x01 << 3;
                    if (oldValue.equals("ВЗН")) {
                        tempMatrix = getMatrix();
                    }
                    //myMatrix((byte) 0x18, false);
                    myMatrix((byte) 0x11, false);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + newValue);
            }
            Thread thread = new Thread(() -> new Connector().setMode(b, dir, findCC()));
            thread.setName("Отработка режима");
            thread.setDaemon(true);
            thread.start();
        });

        /**
         *обработка сброса
         */
        resetProperty().addListener((observable, oldValue, newValue) -> {
            Thread thread = new Thread(() -> new Connector().setReset(newValue));
            thread.setName("Отработка resetа");
            thread.setDaemon(true);
            thread.start();
        });
    }
    ////////////////////////////////////////инициализация сервисов//////////////////////////

    /**
     * инициализация сервиса наработки
     */
    private void initNarabThread() {
        thread = new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[dd.MM] HH:mm");
            timeChart = new TimeChart();
            Platform.runLater(() -> {
                timeChart.start();
            });
            String msg = "";
            for (int i = 0; i < SYCLE; i++) {
                boolean fl_on = true;
                Platform.runLater(() -> {
                    controller.getBut_startMKS().fire();
                });
                startNarabTime = System.currentTimeMillis();
                sb.append("\n[" + (i + 1) + "]" + "Start " + simpleDateFormat.format(new Timestamp(System.currentTimeMillis())));
                Platform.runLater(() -> {
                    addTimeStamp(sb);
                });
                while ((System.currentTimeMillis() - startNarabTime) <= (TimeUnit.HOURS.toMillis(8))) {
                    try {
                        java.time.Duration millis = java.time.Duration.ofMillis(System.currentTimeMillis() - startNarabTime);
                        msg = "Работа[" + (i + 1) +
                                "] " + millis.toHours() +
                                "h " + millis.minusHours(millis.toHours()).toMinutes() +
                                "m";
                        String finalMsg = msg;
                        Platform.runLater(() -> {
                            timeChart.getDataset().getSeries(0).add(new Second(new Date()), 24);
                            timeChart.getDataset().getSeries(1).add(new Second(new Date()), DoubleRounder.round(getFrame_mid() / ONE_K, 3));
                        });

                        Platform.runLater(() -> controller.getTf_regim().setText(finalMsg));
                        TimeUnit.MINUTES.sleep(10);
                        if (((System.currentTimeMillis() - startNarabTime) >= (TimeUnit.MINUTES.toMillis(10))) && (fl_on)) {
                            fl_on = !fl_on;
                            Platform.runLater(() -> controller.getBut_powerOn().fire());
                        }
                    } catch (InterruptedException e) {
                        //  ignore
                    }
                }
                Platform.runLater(() -> controller.getBut_startMKS().fire());
                Platform.runLater(() -> controller.getBut_powerOff().fire());
                stopNarabTime = System.currentTimeMillis();
                sb.append("\n[" + (i + 1) + "]" + "Stop " + simpleDateFormat.format(new Timestamp(System.currentTimeMillis())));
                Platform.runLater(() -> {
                    addTimeStamp(sb);
                });
                while ((System.currentTimeMillis() - stopNarabTime) <= (TimeUnit.MINUTES.toMillis(30))) {
                    try {
                        java.time.Duration millis = java.time.Duration.ofMillis(System.currentTimeMillis() - stopNarabTime);
                        msg = "Отдых[" + (i + 1) +
                                "] " + millis.toHours() +
                                "h " + millis.minusHours(millis.toHours()).toMinutes() +
                                "m";
                        String finalMsg1 = msg;
                        Platform.runLater(() -> {
                            timeChart.getDataset().getSeries(0).add(new Second(new Date()), 0);
                            timeChart.getDataset().getSeries(1).add(new Second(new Date()), DoubleRounder.round(getFrame_mid() / ONE_K, 3));
                        });
                        Platform.runLater(() -> controller.getTf_regim().setText(finalMsg1));
                        TimeUnit.MINUTES.sleep(10);

                    } catch (InterruptedException e) {
                        //   ignore
                    }
                }
            }
            msg = "Готово";
            String finalMsg2 = msg;
            Platform.runLater(() -> {
                controller.getTf_regim().setText(finalMsg2);
            });
            sb.append("\nГотово " + simpleDateFormat.format(new Timestamp(System.currentTimeMillis())));
            Platform.runLater(() -> {
                addTimeStamp(sb);
            });
        });
        thread.setDaemon(true);
    }

    private void addTimeStamp(StringBuilder sb) {
        Marker start = new ValueMarker(24);
        start.setLabelFont(new Font("Dialog", 1, 12));
        start.setPaint(new Color(0, 0, 0, 0));
        start.setLabel(sb.toString());
        start.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
        start.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        timeChart.getPlot().addRangeMarker(start);
    }


    /**
     * инициализация сервиса реконнекта
     */
    private void initReconnectService() {
        reconnect_service = new ReconnectService(this);
        reconnect_service.setPeriod(Duration.millis(RECON_PAUSE));
        reconnect_service.setRestartOnFailure(true);
    }

    /**
     * инициализация сервиса получения данных
     */
    private void initDataReciever_service() {
        dataReciever_service = new DataReciever(this);
        dataReciever_service.setPeriod(Duration.millis(getPause_data()));
        dataReciever_service.setRestartOnFailure(true);
        new Thread(() -> {
            try {
                //отложенный старт чтобы подгрузились окна
                Thread.sleep(RECON_PAUSE * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dataReciever_service.restart();
        }).start();
    }
    /////////////////////////////// инициализация графиков

    /**
     * инициализация временного графика
     */
    private void initMedian_chart_service() {
        median_chart_service = new TimeChartService(controller, this, millis_timer_medianChart);
        median_chart_service.setPeriod(Duration.millis(millis_timer_medianChart));
        median_chart_service.setRestartOnFailure(true);
        new Thread(() -> {
            try {
                //отложенный старт чтобы подгрузились окна
                Thread.sleep(RECON_PAUSE * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            median_chart_service.restart();
        }).start();
    }

    /**
     * инициализация основного графика
     */
    private void initMain_chart_service() {
        main_chart_service = new Animator(controller, this);
        main_chart_service.setPeriod(Duration.millis(getPause_video()));
        main_chart_service.setRestartOnFailure(true);
        new Thread(() -> {
            try {
                //отложенный старт чтобы подгрузились окна
                Thread.sleep(RECON_PAUSE * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            main_chart_service.restart();
        }).start();
    }

    /**
     * инициализация сервиса расчета шума и расчета массива данных при 30 градусах
     */
    private void initExp_shum_service() {
        exp_shum_service = new ExpServiceShum(controller, this);
    }

    /**
     * инициализация сервиса расчета массива данных при 40 градусах
     */
    private void initExp_40_service() {
        exp_40_service = new ExpService40(controller, this);
    }

    /**
     * инициализация сервиса расчета NEDT
     */
    private void initExp_NEDT_service() {
        exp_NEDT_service = new ExpServiceNEDT(controller, this);
    }

    /**
     * инициализация сервиса поиска параметров
     */
    private void initExp_Search_service() {

        exp_Search = new SearchGoodParamsService(controller, this);
    }

    /**
     * инициализация сервиса ресета эксперимента
     */
    private void initExp_Reset_service() {
        exp_Reset = new ExpReset(this);
    }

//////////////////////////////////////отработка нажатий кнопок интерфейса/////////////////////////

    /**
     * обработка время интегрирования
     */
    public void TINTTiped(Integer value) {
        if ((value >= 0) && (value <= 10_000)) {
            Thread thread = new Thread(() -> new Connector().setIntTime(value));
            thread.setName("Отработка интегрирования");
            thread.setDaemon(true);
            thread.start();
        } else {
            showAlert("Введено неправильное время интегрирования: " + value + " мc", "Допустимый диапазон от 0 до 10000 мс", 0);
        }
    }

    /**
     * обработка напряжение VU4
     */
    public void VU4Tiped(Integer value) {
        if ((value >= 3_000) && (value <= 5_000)) {
            Thread thread = new Thread(() -> new Connector().setVU4(value));
            thread.setName("Отработка VU4");
            thread.setDaemon(true);
            thread.start();
        } else {
            showAlert("Введено неправильное значение VU4: " + value + " мВ", "Допустимый диапазон от 3000 до 5000 мВ", 0);
        }
    }

    /**
     * обработка напряжение VVA
     */
    public void VVATiped(Integer value) {
        if ((value >= 0) && (value <= 2_100)) {
            Thread thread = new Thread(() -> new Connector().setVVA(value));
            thread.setName("Отработка VVA");
            thread.setDaemon(true);
            thread.start();
        } else {
            showAlert("Введено неправильное значение VVA: " + value + " мВ", "Допустимый диапазон от 0 до 2100 мВ", 0);
        }
    }

    /**
     * обработка напряжение UC
     */
    public void UCTiped(Integer value) {
        if ((value >= 3_000) && (value <= 5_000)) {
            Thread thread = new Thread(() -> new Connector().setUC(value));
            thread.setDaemon(true);
            thread.setName("Отработка UC");
            thread.start();
        } else {
            showAlert("Введено неправильное значение UC: " + value + " мВ", "Допустимый диапазон от 3000 до 5000 мВ", 0);
        }
    }

    /**
     * обработка напряжение VR0
     */
    public void VR0Tiped(Integer value) {
        if ((value >= 0) && (value <= 2_500)) {
            Thread thread = new Thread(() -> new Connector().setVR0(value));
            thread.setName("Отработка VR0");
            thread.setDaemon(true);
            thread.start();
        } else {
            showAlert("Введено неправильное значение VR0: " + value + " мВ", "Допустимый диапазон от 0 до 2500 мВ", 0);
        }
    }

    /**
     * обработка включения всех пикселей
     */
    public void setAllPixel() {
        byte[] bytes = new byte[LINENUMBER];
        Arrays.fill(bytes, (byte) 0xFF);
        setMatrix(bytes);
    }

    /**
     * обработка выключения всех пикселей
     */
    public void setNonePixel() {
        byte[] bytes = new byte[LINENUMBER];
        Arrays.fill(bytes, (byte) 0x00);
        setMatrix(bytes);
    }

    /**
     * Установка избранного массива деселекции
     *
     * @param bytes - Конкретный массив [144]
     */
    public void setPixel(byte[] bytes) {
        setMatrix(bytes);
    }

    /**
     * обработка выключения линии А
     */
    public void line_A() {
        setMatrixLine((byte) (0b1 << 7));
    }

    /**
     * обработка выключения линии B
     */
    public void line_B() {
        setMatrixLine((byte) (0b1 << 6));
    }

    /**
     * обработка выключения линии C
     */
    public void line_C() {
        setMatrixLine((byte) (0b1 << 5));
    }

    /**
     * обработка выключения линии D
     */
    public void line_D() {
        setMatrixLine((byte) (0b1 << 4));
    }

    /**
     * обработка выключения линии E
     */
    public void line_E() {
        setMatrixLine((byte) (0b1 << 3));
    }

    /**
     * обработка выключения линии F
     */
    public void line_F() {
        setMatrixLine((byte) (0b1 << 2));
    }

    /**
     * обработка выключения линии G
     */
    public void line_G() {
        setMatrixLine((byte) (0b1 << 1));
    }

    /**
     * обработка выключения линии H
     */
    public void line_H() {
        setMatrixLine((byte) (0b1));
    }

    /**
     * Установка напр. питания МКС
     *
     * @param power d в вольтах 0x18-24B; 0x0C -12B; 0x00 -выключить
     */
    public void startMKS(boolean power) {
        if (power) {
            Thread thread = new Thread(() -> new Connector().setMKSPower((byte) 0x18));
            thread.setDaemon(true);
            thread.start();
        } else {
            Thread thread = new Thread(() -> new Connector().setMKSPower((byte) 0x00));
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Обработка вызова основных параметров
     */
    public void param1() {
        Thread thread = new Thread(() -> new Connector().resAllParams());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Обработка вызова основных параметров с матрицей деселекции
     *
     * @param bb -Количество массивов от 1 до 6
     */
    public void param2(byte bb) {
        Thread thread = new Thread(() -> new Connector().resDeselSerLoadParams(bb));
        thread.setDaemon(true);
        thread.start();
    }

    ////////////////////////////////вспомогательные кнопки/////////////////////////////

    /**
     * обработка кнопки темновой ток. Смена главного графика
     *
     * @param bt - ссылка на кнопку
     */
    public void setDark(Button bt) {

        if (!fl_normal) {
            controller.barChart_Exist = controller.getBar_chart(App.getLoader(), "chart_pane", -getNorm(), getNorm(), getNorm() / 5);
            fl_normal = true;
            bt.setText("Сброс темнового");
        } else {
            controller.barChart_Exist = controller.getBar_chart(App.getLoader(), "chart_pane", 0, fiveK, fiveK / 10);
            fl_normal = false;
            bt.setText("Темновой ток");
        }
        main_chart_service.setFl_normaliz_chart(fl_normal);//сообщение сервису о темновом
    }

    /**
     * Установка границ графика
     *
     * @param lineStart  -начальный канал
     * @param lineLength - конечный канал
     */
    public void resetBarchart(int lineStart, int lineLength) {
        setFirst_ch(lineStart);
        setLast_ch(lineLength);
    }

    /**
     * обработка кнопки рестарта видео
     */
    public void restartVideo() {
        restartService(main_chart_service, getPause_video(), "вывода видео");
    }

    /**
     * обработка кнопки осткановки/старта чтения данных
     */
    public void data_fps_change() {
        restartService(dataReciever_service, getPause_data(), "получения данных");
    }

    /**
     * обработка кнопки включения
     */
    public void powerOn(Button but_powerOn, Button but_powerOff) {
        new Thread(() -> {
            if (scenario.isInterrupted()) {
                try {
                    scenario.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int time = (int) (PAUSE * 2);
            try {
                Thread thread = new Thread(() -> new Connector().setPower(true));
                thread.setName("Отработка включение питания");
                thread.setDaemon(true);
                thread.start();
                setIsPowerOn(true);
                TimeUnit.MILLISECONDS.sleep(time);
                Platform.runLater(() -> {
                    VVATiped(0);
                    setVva(0);
                });
                TimeUnit.MILLISECONDS.sleep(time);
                Platform.runLater(() -> {
                    UCTiped(5000);
                    setVuc(5000);
                });
                TimeUnit.MILLISECONDS.sleep(time);
                Platform.runLater(() -> {
                    VU4Tiped(5000);
                    setVu4(5000);
                });
                TimeUnit.MILLISECONDS.sleep(time);
                Platform.runLater(() -> {
                    TINTTiped(20);
                    settInt(20);
                });
                TimeUnit.MILLISECONDS.sleep(time);
                Platform.runLater(() -> {
                    VR0Tiped(950);
                    setVr0(950);
                });
                TimeUnit.MILLISECONDS.sleep(time);
                setMode("ВЗН");
                TimeUnit.MILLISECONDS.sleep(time);
                setDir("Прямое");
                TimeUnit.MILLISECONDS.sleep(time);
                setCcc("0.2");
                TimeUnit.MILLISECONDS.sleep(time * 3);
                setReset(true);
                TimeUnit.MILLISECONDS.sleep(time);
                setReset(false);
                Platform.runLater(() -> {
                    but_powerOn.setText("Включено");
                    but_powerOff.setText("Выключить");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * обработка кнопки выключения
     *
     * @return
     */
    public Thread powerOff(Button but_powerOn, Button but_powerOff) {
        scenario = new Thread(() -> {
            Platform.runLater(() -> {
                int time = (int) (PAUSE * 2);
                try {
                    TimeUnit.MILLISECONDS.sleep(time);
                    VR0Tiped(0);
                    setVr0(0);
                    TimeUnit.MILLISECONDS.sleep(time);
                    VVATiped(0);
                    setVva(0);
                    TimeUnit.MILLISECONDS.sleep(time);
                    Thread thread = new Thread(() -> new Connector().setPower(false));
                    thread.setName("Отработка включение питания");
                    thread.setDaemon(true);
                    thread.start();
                    setIsPowerOn(false);
                    TimeUnit.MILLISECONDS.sleep(time);
                    setReset(true);
                    Platform.runLater(() -> {
                        but_powerOn.setText("Включить");
                        but_powerOff.setText("Выключено");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
        scenario.start();
        return scenario;
    }
/////////////////////////////вспомогательные функции///////////////////////////////////////

    /**
     * Конвертирование текущей  ёмкости в байт
     *
     * @return байтовая кодировка . Проверить основной метод. 0xFF- ошибка
     */
    private byte findCC() {
        String ccc = getCcc();
        byte b;
        switch (ccc) {
            case "0.2":
                b = (byte) 0b0;
                break;
            case "0.4":
                b = (byte) 0b0100;
                break;
            case "0.6":
                b = (byte) 0b10;
                break;
            case "0.8":
                b = (byte) 0b110;
                break;
            case "1.0":
                b = (byte) 0b1;
                break;
            case "1.2":
                b = (byte) 0b101;
                break;
            case "1.4":
                b = (byte) 0b11;
                break;
            case "1.6":
                b = (byte) 0b111;
                break;
            default:
                b = (byte) 0XFF;
                throw new IllegalStateException("Ошибка конвертации ёмкости- value: " + ccc);
        }
        return b;
    }

    /**
     * Конвертирование режима в байт
     *
     * @return байтовая кодировка. Проверить основной метод. 0xFFFF- ошибка
     */
    private byte[] findMode() {
        String mode = getMode();
        byte b;
        byte dir = 0;
        switch (mode) {
            case "ВЗН":
                b = (byte) 0x00;
                String dir1 = getDir();
                dir = (dir1.equals("Прямое")) ? (byte) 0x00 : (byte) 0x01 << 3;
                break;
            case "1-Bypass":
                b = (byte) 0b1 << 4;
                dir = 0;
                break;
            case "2-Bypass":
                b = (byte) 0b1 << 4;
                dir = 0b1000;
                break;
            case "3-Bypass":
                b = (byte) 0b110000;
                dir = 0;
                break;
            case "4-Bypass":
                b = (byte) 0b110000;
                dir = 0x01 << 3;
                break;
            default:
                b = (byte) 0XFF;
                dir = (byte) 0XFF;
                throw new IllegalStateException("Ошибка конвертации режима- value: " + mode);
        }
        return new byte[]{b, dir};
    }

    /**
     * Отображение деселекции для BYPASS режима без/с отправки(кой) в прибор
     *
     * @param bb         байт деселекции
     * @param sendMatrix флаг разрешения отправки
     */
    private void myMatrix(byte bb, boolean sendMatrix) {
        SENDDESEL = sendMatrix;
        byte[] bytes = new byte[LINENUMBER];
        Arrays.fill(bytes, bb);
        setMatrix(bytes);
    }

    /**
     * Установка байта в каждую строку матрицы деселекции
     *
     * @param bb байт строки деселекции
     */
    private void setMatrixLine(byte bb) {
        byte[] bytes = new byte[LINENUMBER];
        Arrays.fill(bytes, bb);
        byte[] matrix = Arrays.copyOf(getMatrix(), LINENUMBER);
        for (int j = 0; j < LINENUMBER; j++) {
            bytes[j] = (byte) (matrix[j] ^ bytes[j]);
        }
        setMatrix(bytes);
    }

    /**
     * Рестарт сервиса для поля с параметром
     *
     * @param service сервис
     * @param pause   период
     * @param msg     сообщение
     */
    private void restartService(ScheduledService<?> service, int pause, String msg) {
        service.cancel();
        service.setPeriod(Duration.millis(pause));
        service.restart();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Рестарт драйвера при разъединении
     */
    public synchronized void reconnectDriver() {
        Thread thread = new Thread(() -> new Connector().reconnect());
        thread.setDaemon(true);
        thread.start();
    }

    //обработка кнопки старта наработки
    public void regimService() {
        fl_narab = !fl_narab;
        if (!fl_narab) {//при отмене
            String msg = "";
            thread.interrupt();
            Platform.runLater(() -> {
                controller.getBut_startMKS().fire();
                controller.getTf_regim().setText(msg);
            });

        } else {//при старте
            initNarabThread();
            thread.start();

        }
    }

    //обработка кнопки сохранения файла
    public void saveFile1() {
    }

    //обработка кнопки старта расчета шума
    public void startExp(BarChart<String, Number> chartSKO, BarChart<String, Number> chart30_40) {
        take_sko30(chartSKO, chart30_40);
    }

    //обработка кнопки старта расчета 40
    public void startExp40(BarChart<String, Number> barChart_30_40) {
        take_40(barChart_30_40);
    }

    //обработка кнопки старта расчета недт
    public void startExpNEDT(BarChart<String, Number> barChart_NETD, BarChart<String, Number> barChart_NETD_All) {
        take_NEDT(barChart_NETD, barChart_NETD_All);
    }

    //отработка конкретного массива
    public void manual() {
        tempMatrix = getMatrix();
        myMatrix((byte) 0xFF, false);
        Thread thread = new Thread(() -> {
            while (!done) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setPixel(tempMatrix);
        });
        thread.setDaemon(true);
        thread.start();
    }


    //стандартный запуск расчета эксперимента
    private void take_NEDT(BarChart<String, Number> barChart_NETD, BarChart<String, Number> barChart_NETD_All) {
        exp_NEDT_service.setChart(barChart_NETD, barChart_NETD_All);
        exp_NEDT_service.restart();
    }

    //стандартный запуск эксперимента 40
    private void take_40(BarChart<String, Number> barChart_30_40) {
        exp_40_service.setChart(barChart_30_40);
        exp_40_service.restart();
    }

    //стандартный запуск эксперимента 30
    private void take_sko30(BarChart<String, Number> chartSKO, BarChart<String, Number> chart30_40) {
        exp_shum_service.setChart(chartSKO, chart30_40);
        exp_shum_service.restart();
    }

    //ресет эксперимента
    public void resetExp() {
        exp_Reset.restart();
        order = new Order();
        for (ImageView im :
                controller.getListView()) {
            changeIv(im);
        }
    }

    //обработка кнопки сохранения файла эксперимента
    public boolean saveFileExp() {
        BDService bDsaveData = new BDService();
        boolean res = bDsaveData.saveExpDataToBD(getExperiment());
        return res;
    }

    //обработка кнопки обновления файла эксперимента
    public boolean updateFileExp() {
        BDService bDsaveData = new BDService();
        long ID = getExperiment().getID();
        if (ID == 0) {
            return false;
        }
        boolean res = bDsaveData.updateExpFromBD(type_ID, ID, getExperiment());
        return res;
    }

    //обработка кнопки загрузки файла эксперимента
    public boolean loadFileExp(long ID) {
        BDService bDsaveData = new BDService();
        long l = bDsaveData.takeLastIDFromBD();
        if (ID > l) {
            return false;
        } else if (ID == 0) {
            Experiment exp = null;
            if (getDir().equals("Прямое") && getMode().equals("ВЗН")) {
                exp = getOrder().getVZN_pr();
            } else if (getDir().equals("Обратное") && getMode().equals("ВЗН")) {
                exp = getOrder().getVZN_ob();
                //} else if (getMode().equals("4-Bypass")) {
            } else if (getMode().endsWith("-Bypass")) {
                exp = getOrder().getBPS();
            }
            setExperiment(exp);
            refrashCharts();
            return true;
        } else {
            ArrayList<Experiment> experiments = bDsaveData.readExpFromBD(type_ID, ID);
            if (experiments.size() == 0) {
                return false;
            } else {
                if (experiments.get(0).getStartExpDate() == null) {
                    return false;
                } else {
                    setExperiment(experiments.get(0));
                    refrashCharts();
                    return true;
                }
            }
        }

    }

    //запрос последнего ID БД
    public long lastID() {
        BDService bDsaveData = new BDService();
        long l = bDsaveData.takeLastIDFromBD();
        return l;
    }

    /**
     * Обновление графиков при загрузке из БД
     */
    private void refrashCharts() {
        setRELOADCHARTS(true);
        Platform.runLater(() -> {
            controller.getBut_start_Shum().fire();
        });

    }

    // остановка главных графиков
    public void allStop() {
        main_chart_service.setTab_exp(false);
        median_chart_service.cancel();
    }

    //старт главных графиков
    public void allStart() {
        main_chart_service.setTab_exp(true);
        median_chart_service.restart();
    }

    /**
     * Показ всплывающего окна
     *
     * @param header заголовок
     * @param text   подвал
     * @param tipe   тип 0- error;1- info
     */
    public void showAlert(String header, String text, int tipe) {
        Alert alert;
        if (tipe == 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Инфо");
        }
        alert.setHeaderText(header);

        alert.setContentText(text);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }

    /**
     * Отработка отображения коннекта к БД
     * *
     *
     * @param ap_dbConnect
     */
    public void dbIsAlive(ImageView ap_dbConnect) {
        String text = "";
        if (new BDService().bdIsAlive()) {
            text = "images/icon2.png";
        } else {
            text = "images/icon1.png";
        }
        String finalText = text;
        Platform.runLater(() -> {
            InputStream resourceAsStream = getClass().getResourceAsStream(finalText);
            ap_dbConnect.setImage(new Image(resourceAsStream));
        });
    }

    /**
     * Отработка отображения выполненных измерений
     * В работе
     *
     * @param imageView
     */
    public void changeIv(ImageView imageView) {
        if (order == null) {
            return;
        }
        String text = "";
        String id = imageView.getId();
        boolean status = false;
        if (id.equals("iv_1_1")) {
            if (order.getVZN_pr() == null) {
                status = false;
            } else if (order.getVZN_pr().getDataArray30() != null) {
                status = true;
            }
        } else if (id.equals("iv_1_2")) {
            if (order.getVZN_ob() == null) {
                status = false;
            } else if (order.getVZN_ob().getDataArray30() != null) {
                status = true;
            }
        } else if (id.equals("iv_1_3")) {
            if (order.getBPS() == null) {
                status = false;
            } else if (order.getBPS().getDataArray30() != null) {
                status = true;
            }
        } else if (id.equals("iv_2_1")) {
            if (order.getVZN_pr() == null) {
                status = false;
            } else if (order.getVZN_pr().getDataArray40() != null) {
                status = true;
            }
        } else if (id.equals("iv_2_2")) {
            if (order.getVZN_ob() == null) {
                status = false;
            } else if (order.getVZN_ob().getDataArray40() != null) {
                status = true;
            }
        } else if (id.equals("iv_2_3")) {
            if (order.getBPS() == null) {
                status = false;
            } else if (order.getBPS().getDataArray40() != null) {
                status = true;
            }
        } else if (id.equals("iv_3_1")) {
            if (order.getVZN_pr() == null) {
                status = false;
            } else if (order.getVZN_pr().getDataArrayNEDT() != null) {
                status = true;
            }
        } else if (id.equals("iv_3_2")) {
            if (order.getVZN_ob() == null) {
                status = false;
            } else if (order.getVZN_ob().getDataArrayNEDT() != null) {
                status = true;
            }
        } else if (id.equals("iv_3_3")) {
            if (order.getBPS() == null) {
                status = false;
            } else if (order.getBPS().getDataArrayNEDT() != null) {
                status = true;
            }
        }
        if (status) {
            text = "images/yes.png";
        } else {
            text = "images/no.png";
        }
        String finalText = text;
        Platform.runLater(() -> {
            InputStream resourceAsStream = getClass().getResourceAsStream(finalText);
            imageView.setImage(new Image(resourceAsStream));
        });
    }
    ////////////////////////////////////////геттеры+сеттеры//////////////////////////////////////

    public static int getVr0() {
        return vr0.get();
    }

    public void setVr0(int vr0) {
        this.vr0.set(vr0);
    }

    public IntegerProperty vr0Property() {
        return vr0;
    }

    public int getVva() {
        return vva.get();
    }

    public void setVva(int vva) {
        this.vva.set(vva);
    }

    public IntegerProperty vvaProperty() {
        return vva;
    }

    public int getVu4() {
        return vu4.get();
    }

    public void setVu4(int vu4) {
        this.vu4.set(vu4);
    }

    public IntegerProperty vu4Property() {
        return vu4;
    }

    public int getVuc() {
        return vuc.get();
    }

    public void setVuc(int vuc) {
        this.vuc.set(vuc);
    }

    public IntegerProperty vucProperty() {
        return vuc;
    }

    public int gettInt() {
        return tInt.get();
    }

    public void settInt(int tInt) {
        this.tInt.set(tInt);
    }

    public IntegerProperty tIntProperty() {
        return tInt;
    }

    public void setFirst_ch(int first_ch) {
        this.first_ch.set(first_ch);
    }

    public IntegerProperty first_chProperty() {
        return first_ch;
    }

    public void setLast_ch(int last_ch) {
        this.last_ch.set(last_ch);
    }

    public IntegerProperty last_chProperty() {
        return last_ch;
    }

    public int getPause_data() {
        return pause_data.get();
    }

    public IntegerProperty pause_dataProperty() {
        return pause_data;
    }

    public String getMode() {
        return mode.get();
    }

    public void setMode(String mode) {
        Platform.runLater(() -> {
            this.mode.set(mode);
        });
    }

    public StringProperty modeProperty() {
        return mode;
    }

    public String getDir() {
        return dir.get();
    }

    public void setDir(String dir) {
        Platform.runLater(() -> {
            this.dir.set(dir);
        });
    }

    public StringProperty dirProperty() {
        return dir;
    }

    public String getCcc() {
        return ccc.get();
    }

    public void setCcc(String ccc) {
        Platform.runLater(() -> {
            this.ccc.set(ccc);
        });
    }

    public StringProperty cccProperty() {
        return ccc;
    }

    public static byte[] getMatrix() {
        return matrix.get();
    }

    public void setMatrix(byte[] matrix) {
        this.matrix.set(matrix);
    }

    public ObjectProperty<byte[]> matrixProperty() {
        return matrix;
    }

    public boolean isReset() {
        return reset.get();
    }

    public void setReset(boolean reset) {
        Platform.runLater(() -> {
            this.reset.set(reset);
        });
    }

    public BooleanProperty resetProperty() {
        return reset;
    }

    public int getTemp() {
        return temp.get();
    }

    public IntegerProperty tempProperty() {
        return temp;
    }

    public void setIsPowerOn(boolean isPowerOn) {
        Platform.runLater(() -> {
            this.isPowerOn.set(isPowerOn);
        });
    }

    public BooleanProperty isPowerOnProperty() {
        return isPowerOn;
    }

    public int getPause_video() {
        return pause_video.get();
    }

    public int getFrameCountExp() {
        return frameCountExp.get();
    }

    public IntegerProperty frameCountExpProperty() {
        return frameCountExp;
    }

    public IntegerProperty pause_videoProperty() {
        return pause_video;
    }

    public int getNorm() {
        return norm.get();
    }

    public void setNorm(int norm) {
        this.norm.set(norm);
    }

    public IntegerProperty normProperty() {
        return norm;
    }

    public Connector getConnector() {
        return connector;
    }

    public String getDetectorName() {
        return detectorName.get();
    }

    public StringProperty detectorNameProperty() {
        return detectorName;
    }

    public void setDetectorName(String detectorName) {
        this.detectorName.set(detectorName);
    }

    public String getTesterFIO() {
        return testerFIO.get();
    }

    public StringProperty testerFIOProperty() {
        return testerFIO;
    }

    public void setTesterFIO(String testerFIO) {
        this.testerFIO.set(testerFIO);
    }

    public String getNumbersDevises() {
        return numbersDevises.get();
    }

    public StringProperty numbersDevisesProperty() {
        return numbersDevises;
    }

    public void setNumbersDevises(String numbersDevises) {
        this.numbersDevises.set(numbersDevises);
    }

    public ExpServiceShum getExp_shum_service() {
        return exp_shum_service;
    }

    public ExpService40 getExp_40_service() {
        return exp_40_service;
    }

    public int getFirstChanExp() {
        return firstChanExp.get();
    }

    public IntegerProperty firstChanExpProperty() {
        return firstChanExp;
    }

    public int getLastChanExp() {
        return lastChanExp.get();
    }

    public IntegerProperty lastChanExpProperty() {
        return lastChanExp;
    }

    public static Frame getMyFrame() {
        synchronized (inBlock) {
            return myFrame;
        }
    }

    public static void setMyFrame(Frame myFrame) {
        synchronized (inBlock) {
            DetectorViewModel.myFrame = myFrame;
        }
    }

    public FloatProperty frame_SKOProperty() {
        return frame_SKO;
    }

    public void setFrame_SKO(float frame_SKO) {
        Platform.runLater(() -> {
            this.frame_SKO.set(frame_SKO);
        });
    }

    public FloatProperty frame_midProperty() {
        return frame_mid;
    }

    public void setFrame_mid(float frame_mid) {
        Platform.runLater(() -> {
            this.frame_mid.set(frame_mid);
        });
    }

    public float getFrame_mid() {
        return frame_mid.get();
    }

    public FloatProperty frame_minProperty() {
        return frame_min;
    }

    public void setFrame_min(float frame_min) {
        Platform.runLater(() -> {
            this.frame_min.set(frame_min);
        });

    }

    public FloatProperty frame_maxProperty() {
        return frame_max;
    }

    public void setFrame_max(float frame_max) {
        Platform.runLater(() -> {
            this.frame_max.set(frame_max);
        });

    }

    public FloatProperty OUT1_SKOProperty() {
        return OUT1_SKO;
    }

    public void setOUT1_SKO(float OUT1_SKO) {
        Platform.runLater(() -> {
            this.OUT1_SKO.set(OUT1_SKO);
        });

    }

    public FloatProperty OUT1_midProperty() {
        return OUT1_mid;
    }

    public void setOUT1_mid(float OUT1_mid) {
        Platform.runLater(() -> {
            this.OUT1_mid.set(OUT1_mid);
        });
    }

    public FloatProperty OUT1_minProperty() {
        return OUT1_min;
    }

    public void setOUT1_min(float OUT1_min) {
        Platform.runLater(() -> {
            this.OUT1_min.set(OUT1_min);
        });

    }

    public FloatProperty OUT1_maxProperty() {
        return OUT1_max;
    }

    public void setOUT1_max(float OUT1_max) {
        Platform.runLater(() -> {
            this.OUT1_max.set(OUT1_max);
        });
    }

    public FloatProperty OUT2_SKOProperty() {
        return OUT2_SKO;
    }

    public void setOUT2_SKO(float OUT2_SKO) {
        Platform.runLater(() -> {
            this.OUT2_SKO.set(OUT2_SKO);
        });

    }

    public FloatProperty OUT2_midProperty() {
        return OUT2_mid;
    }

    public void setOUT2_mid(float OUT2_mid) {
        Platform.runLater(() -> {
            this.OUT2_mid.set(OUT2_mid);
        });

    }

    public FloatProperty OUT2_minProperty() {
        return OUT2_min;
    }

    public void setOUT2_min(float OUT2_min) {
        Platform.runLater(() -> {
            this.OUT2_min.set(OUT2_min);
        });

    }

    public FloatProperty OUT2_maxProperty() {
        return OUT2_max;
    }

    public void setOUT2_max(float OUT2_max) {
        Platform.runLater(() -> {
            this.OUT2_max.set(OUT2_max);
        });

    }

    public FloatProperty OUT3_SKOProperty() {
        return OUT3_SKO;
    }

    public void setOUT3_SKO(float OUT3_SKO) {
        Platform.runLater(() -> {
            this.OUT3_SKO.set(OUT3_SKO);
        });

    }

    public FloatProperty OUT3_midProperty() {
        return OUT3_mid;
    }

    public void setOUT3_mid(float OUT3_mid) {
        Platform.runLater(() -> {
            this.OUT3_mid.set(OUT3_mid);
        });

    }

    public FloatProperty OUT3_minProperty() {
        return OUT3_min;
    }

    public void setOUT3_min(float OUT3_min) {
        Platform.runLater(() -> {
            this.OUT3_min.set(OUT3_min);
        });

    }

    public FloatProperty OUT3_maxProperty() {
        return OUT3_max;
    }

    public void setOUT3_max(float OUT3_max) {
        Platform.runLater(() -> {
            this.OUT3_max.set(OUT3_max);
        });

    }

    public FloatProperty OUT4_SKOProperty() {
        return OUT4_SKO;
    }

    public void setOUT4_SKO(float OUT4_SKO) {
        Platform.runLater(() -> {
            this.OUT4_SKO.set(OUT4_SKO);
        });

    }

    public FloatProperty OUT4_midProperty() {
        return OUT4_mid;
    }

    public void setOUT4_mid(float OUT4_mid) {
        Platform.runLater(() -> {
            this.OUT4_mid.set(OUT4_mid);
        });

    }

    public FloatProperty OUT4_minProperty() {
        return OUT4_min;
    }

    public void setOUT4_min(float OUT4_min) {
        Platform.runLater(() -> {
            this.OUT4_min.set(OUT4_min);
        });

    }

    public FloatProperty OUT4_maxProperty() {
        return OUT4_max;
    }

    public void setOUT4_max(float OUT4_max) {
        Platform.runLater(() -> {
            this.OUT4_max.set(OUT4_max);
        });

    }

    public void setSco30(float sco30) {
        this.sco30.set(sco30);
    }

    public void setSred30(float sred30) {
        this.sred30.set(sred30);
    }

    public void setSred40(float sred40) {
        this.sred40.set(sred40);
    }

    public void setNETD(float NETD) {
        this.NETD.set(NETD);
    }

    public static Experiment getExperiment() {
        return experiment;
    }

    public static void setExperiment(Experiment experiment) {
        DetectorViewModel.experiment = experiment;
    }

    public Animator getMain_chart_service() {
        return main_chart_service;
    }

    public BooleanProperty isOkProperty() {
        return isOk;
    }

    public void setIsOk(boolean isOk) {
        Platform.runLater(() -> {
            this.isOk.set(isOk);
        });

    }

    public ExpServiceNEDT getExp_NEDT_service() {
        return exp_NEDT_service;
    }

    public ExpReset getExp_Reset() {
        return exp_Reset;
    }

    public SecondaryController getController() {
        return controller;
    }

    public static boolean isRELOADCHARTS() {
        return RELOADCHARTS;
    }

    public static void setRELOADCHARTS(boolean RELOADCHARTS) {
        DetectorViewModel.RELOADCHARTS = RELOADCHARTS;
    }

    public static Order getOrder() {
        return order;
    }

}