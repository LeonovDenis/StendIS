package ru.pelengator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.PopupWindow;
import javafx.util.StringConverter;
import javafx.util.converter.*;
import org.jfree.chart.JFreeChart;
import ru.pelengator.model.Connector;
import ru.pelengator.charts.ModernChart;
import ru.pelengator.charts.SampleBarChart;
import ru.pelengator.model.DocMaker;
import ru.pelengator.utils.ManualResetEvent;

import static ru.pelengator.PropFile.*;
import static ru.pelengator.charts.ModernChart.*;
import static ru.pelengator.utils.Utils.getChNumber;

public class SecondaryController {
    public static ArrayList<Button> buttons = new ArrayList<>();
    public static ObservableList<String> category;
    public static ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

    //флаг работы МКС
    static Boolean fl_MKS_Working = false;

    static {
        //заполнение списка номеров каналов
        category = FXCollections.observableArrayList();
        for (int i = 0; i < CHANNELNUMBER; i++) {
            category.add(String.valueOf(i + 1));
        }
    }

    @FXML
    public
    Button bt_parload;
    ///////////////статистика по каналам
    @FXML
    public
    Label lb_out1_max;
    @FXML
    public
    Label lb_out2_max;
    @FXML
    public
    Label lb_out3_max;
    @FXML
    public
    Label lb_out4_max;
    @FXML
    public
    Label lb_out1_min;
    @FXML
    public
    Label lb_out2_min;
    @FXML
    public
    Label lb_out3_min;
    @FXML
    public
    Label lb_out4_min;
    @FXML
    public
    Label lb_out1_sred;
    @FXML
    public
    Label lb_out2_sred;
    @FXML
    public
    Label lb_out3_sred;
    @FXML
    public
    Label lb_out4_sred;
    @FXML
    public
    Label lb_out1_sko;
    @FXML
    public
    Label lb_out2_sko;
    @FXML
    public
    Label lb_out3_sko;
    @FXML
    public
    Label lb_out4_sko;
    @FXML
    public
    Label lb_out_max;
    @FXML
    public
    Label lb_out_min;
    @FXML
    public
    Label lb_out_sred;
    @FXML
    public
    Label lb_out_sko;
    @FXML
    public
    ProgressBar pBar_sendQueue;
    @FXML
    public
    Label lab_sendQueue;
    ////////////////////////////////////////////////////////////////////////////
    @FXML
    public
    TextField tf_IDexp;
    @FXML
    public
    GridPane gp_text;
    @FXML
    public
    Tab tab_explore;
    @FXML
    public
    Tab tab_experiment;
    @FXML
    public
    TabPane tab_pan;
    // поле ско 30гр
    @FXML
    public
    Label lab_sco30;
    @FXML
    public
    Label lab_sco30Sootv;
    @FXML
    public
    Label lab_NEDTSootv;
    // поле ско 40гр
    @FXML
    public
    Label lab_sred40;
    @FXML
    public
    Label lab_deltaSign;
    @FXML
    public
    Label lab_sred30;
    @FXML
    public
    Label lab_countDeselPixel;
    @FXML
    public
    Label lab_countDeselPixelInLine;
    @FXML
    public
    Label lab_countDeselPixel_sootv;
    @FXML
    public

    Label lab_countDeselPixelInLine_sootv;
    @FXML
    public
    Label lab_exp_status;
    // поле NETD
    @FXML
    public
    Button but_start_40;
    @FXML
    public
    Button but_start_NEDT;
    @FXML
    public
    Button but_start_RESET;
    @FXML
    public
    Label lab_NETD;
    // поле количества отсчетов
    @FXML
    public
    TextField tex_count;
    // поле стартового канала эксп
    @FXML
    public
    TextField tex_firstChanExp;
    // поле конечного канала эесп
    @FXML
    public
    TextField tex_lastChanExp;
    // поле дельта брака
    @FXML
    public
    TextField tex_brak;
    @FXML
    public
    TextField tex_brak_count;
    @FXML
    public
    TextField tex_brak_countFPU;
    //прогресс бар эксперимента
    @FXML
    public ProgressBar pb_exp;
    @FXML
    public VBox vb_sko30;
    @FXML
    public VBox vb_sred_30_40;
    @FXML
    public VBox vb_netd;
    @FXML
    public VBox vb_netdALL;
    @FXML
    public HBox vb_temp;
    //диаграмма на главной
    @FXML
    public BarChart<String, Number> barChart_Exist;
    @FXML
    public BarChart<String, Number> barChart_Temp;
    @FXML
    public BarChart<String, Number> barChart_sko_30;
    @FXML
    public BarChart<String, Number> barChart_30_40;
    //диаграмма ЭШРТ
    @FXML
    public BarChart<String, Number> barChart_NETD_All;
    //диаграмма ЭШРТ распределения
    @FXML
    public BarChart<String, Number> barChart_NETD;
    @FXML
    public LineChart<String, Number> lineChart_time;
    @FXML
    public LineChart<Number, Number> lineChart_time2;
    // поле нормала
    @FXML
    public
    TextField tf_norm;
    @FXML
    public
    ImageView ap_dbConnect;
    @FXML
    public
    ImageView iv_1_1;
    @FXML
    public
    ImageView iv_1_2;
    @FXML
    public
    ImageView iv_1_3;
    @FXML
    public
    ImageView iv_2_1;
    @FXML
    public
    ImageView iv_2_2;
    @FXML
    public
    ImageView iv_2_3;
    @FXML
    public
    ImageView iv_3_1;
    @FXML
    public
    ImageView iv_3_2;
    @FXML
    public
    ImageView iv_3_3;
    @FXML
    public
    Button bt_saveOtchet;


    /**
     * Ссылка на вьюмодел
     */
    public DetectorViewModel detectorViewModel = new DetectorViewModel(this);
    /////////////////////////////////////
    //Поле вывода шумящих каналов
    @FXML
    public TextArea textArea_shum;
    //среднее значение маленький график
    @FXML
    public TextField tf_regim;
    //////////////////////////////////////
    //
    @FXML
    Button but_startMKS;
    @FXML
    Button but_regim;
    // кнопка включения
    @FXML
    Button but_powerOn;
    // кнопка выключения
    @FXML
    Button but_powerOff;
    // кнопка сброса графика
    @FXML
    Button but_resetBarchart;
    // кнопка сохранения графика
    @FXML
    Button but_setAllPixel;
    // кнопка выбора пикселей
    @FXML
    Button but_setMyPixel;
    // кнопка выключения пикселей
    @FXML
    Button but_setNonePixel;
    // кнопка работы с линеей А
    @FXML
    Button but_line_A;
    // кнопка работы с линеей B
    @FXML
    Button but_line_B;
    // кнопка работы с линеей C
    @FXML
    Button but_line_C;
    // кнопка работы с линеей D
    @FXML
    Button but_line_D;
    // кнопка работы с линеей E
    @FXML
    Button but_line_E;
    // кнопка работы с линеей F
    @FXML
    Button but_line_F;
    // кнопка работы с линеей G
    @FXML
    Button but_line_G;
    // кнопка работы с линеей H
    @FXML
    Button but_line_H;
    // кнопка установки темнового
    @FXML
    Button but_setDark;
    // кнопка запроса парам1
    @FXML
    Button but_param1;
    // кнопка запроса парам2
    @FXML
    Button but_param2;
    // кнопка старта эксперимента
    @FXML
    Button but_start_Shum;
    // кнопка сохранения данных эксперимента
    @FXML
    Button but_saveFileExp;
    @FXML
    Button but_updateFileExp;
    @FXML
    Button but_loadFileExp;
    @FXML
    Button but_loadExp;
    @FXML
    ToggleButton tBut_OUT1;
    @FXML
    ToggleButton tBut_OUT2;
    @FXML
    ToggleButton tBut_OUT3;
    @FXML
    ToggleButton tBut_OUT4;
    @FXML
    Button bt_reconnect;
    //напряжения
    @FXML
    private TextField tf_UC;
    @FXML
    private TextField tf_VU4;
    @FXML
    private TextField tf_VVA;
    @FXML
    private TextField tf_VR0;
    @FXML
    private TextField tf_TINT;
    @FXML
    private ChoiceBox chob_CCC;
    @FXML
    private ChoiceBox chob_DIR;
    @FXML
    private ChoiceBox chob_mode;
    @FXML
    private CheckBox cheb_Avdd;
    @FXML
    private CheckBox cheb_reset;
    @FXML
    private GridPane gridPane;
    //поле статуса
    @FXML
    private Label lab_status;
    //поле фио
    @FXML
    private Label lab_fio;
    //поле названия изделия
    @FXML
    private Label lab_izd;
    //поле зав. номера изделия
    @FXML
    private Label lab_number;
    //слайдер VR0
    @FXML
    private Slider sl_VR0;
    //слайдер VVA
    @FXML
    private Slider sl_VVA;
    @FXML
    private Label lb_reset;
    @FXML
    private Label lb_tSence;
    @FXML
    private TextField tf_tSence;
    //первый канал
    @FXML
    private TextField tf_first_ch;
    //последний канал
    @FXML
    private TextField tf_last_ch;
    //частота кадров
    @FXML
    private TextField tf_data_fps;
    //частота получения видео
    @FXML
    private TextField tf_pause_video;
    private boolean fl_par = false;//парлоад
    //скорость обновления параметров
    private static ArrayList<ImageView> listView = new ArrayList<>();

    @FXML
    public void initialize() {
        but_start_40.setDisable(true);//отключение кнопки 2 в эксперименте
        but_start_NEDT.setDisable(true);//отключение кнопки 3 в эксперименте
        textArea_shum.setFocusTraversable(false);//отработка возможной ошибки
        setFrames(App.getLoader());//загрузка графиков и кнопок деселекции
        initHandlers();//инициализация обработчиков
        initBindings();//инициализация связей
        initPic();
    }

    private void initPic() {
        listView.add(iv_1_1);
        listView.add(iv_1_2);
        listView.add(iv_1_3);
        listView.add(iv_2_1);
        listView.add(iv_2_2);
        listView.add(iv_2_3);
        listView.add(iv_3_1);
        listView.add(iv_3_2);
        listView.add(iv_3_3);
    }

    /**
     * Загрузка в окно приложения
     *
     * @param loader
     */
    public void setFrames(FXMLLoader loader) {
        barChart_Exist = getBar_chart(loader, "chart_pane", 0, fiveK, fiveK / 10);//главный график
        lineChart_time = getline_chart(loader, "time_pane");//временной график
        barChart_Temp = getBar_chartTemp(loader, "vb_temp", 0, fiveK, fiveK / 10);// микрокопия главного графика
        setChartsToReset(loader);//инициализация сбрасываемых графиков
        getMatrix(loader);// загрузка миатрицы деселекции
    }

    /**
     * Отдельный метод для сбрасывыаемых графиков
     *
     * @param loader
     */
    public void setChartsToReset(FXMLLoader loader) {
        barChart_sko_30 = getBar_chart_sko_30(loader, "vb_sko30", 0, 1, 0.1);//график шума
        barChart_30_40 = getBar_chart30_40(loader, "vb_sred_30_40", 0, fiveK, fiveK / 10);//графие сигнала 30 и 40
        barChart_NETD = getBar_chart_NEDT(loader, "vb_netd", 0, 50, 5); //график НЕДТ
        barChart_NETD_All = getBar_chart_Raspred(loader, "vb_netdALL", 0, 288, 28);// график распределения НЕДТ

    }

    /**
     * инициализация связей
     */
    private void initBindings() {
        ManualResetEvent hendler = Connector.hendler;//обработчик хаотичной очереди
        pBar_sendQueue.visibleProperty().bind(hendler.isAllDoneProperty().not());//видимость прогресс бара очереди
        lab_sendQueue.visibleProperty().bind(hendler.isAllDoneProperty().not());//видимость текста очереди
        /////связи полей статистики кадра
        Bindings.bindBidirectional(lb_out_sko.textProperty(), detectorViewModel.frame_SKOProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out1_sko.textProperty(), detectorViewModel.OUT1_SKOProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out2_sko.textProperty(), detectorViewModel.OUT2_SKOProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out3_sko.textProperty(), detectorViewModel.OUT3_SKOProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out4_sko.textProperty(), detectorViewModel.OUT4_SKOProperty(), (StringConverter) new MyFloatConverter());

        Bindings.bindBidirectional(lb_out_max.textProperty(), detectorViewModel.frame_maxProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out1_max.textProperty(), detectorViewModel.OUT1_maxProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out2_max.textProperty(), detectorViewModel.OUT2_maxProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out3_max.textProperty(), detectorViewModel.OUT3_maxProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out4_max.textProperty(), detectorViewModel.OUT4_maxProperty(), (StringConverter) new MyFloatConverter());

        Bindings.bindBidirectional(lb_out_sred.textProperty(), detectorViewModel.frame_midProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out1_sred.textProperty(), detectorViewModel.OUT1_midProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out2_sred.textProperty(), detectorViewModel.OUT2_midProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out3_sred.textProperty(), detectorViewModel.OUT3_midProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out4_sred.textProperty(), detectorViewModel.OUT4_midProperty(), (StringConverter) new MyFloatConverter());

        Bindings.bindBidirectional(lb_out_min.textProperty(), detectorViewModel.frame_minProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out1_min.textProperty(), detectorViewModel.OUT1_minProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out2_min.textProperty(), detectorViewModel.OUT2_minProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out3_min.textProperty(), detectorViewModel.OUT3_minProperty(), (StringConverter) new MyFloatConverter());
        Bindings.bindBidirectional(lb_out4_min.textProperty(), detectorViewModel.OUT4_minProperty(), (StringConverter) new MyFloatConverter());
        ////////
        /**
         * Отработка перехода в соседние закладки(табы)
         */
        tab_explore.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    detectorViewModel.allStop();
                    detectorViewModel.dbIsAlive(ap_dbConnect);
                    for (ImageView im :
                            listView) {
                        detectorViewModel.changeIv(im);
                    }
                } else {
                    detectorViewModel.allStart();
                }
            }
        });
        //конфигурирование слайдов VR0 и VVA
        sl_VR0.setMax(2500.0);
        sl_VR0.setMin(0.0);
        sl_VR0.setValue(1000.0);
        Bindings.bindBidirectional(tf_VR0.textProperty(), sl_VR0.valueProperty(), (StringConverter) new MyDoubleConverter());
        sl_VVA.setMax(2100.0);
        sl_VVA.setMin(0.0);
        sl_VVA.setValue(0.0);
        Bindings.bindBidirectional(tf_VVA.textProperty(), sl_VVA.valueProperty(), (StringConverter) new MyDoubleConverter());
        //привязка полей данных по детектору
        lab_izd.textProperty().bind(detectorViewModel.detectorNameProperty());
        lab_fio.textProperty().bind(detectorViewModel.testerFIOProperty());
        lab_number.textProperty().bind(detectorViewModel.numbersDevisesProperty());
        /**
         * границы нормального графика
         */
        Bindings.bindBidirectional(tf_norm.textProperty(), detectorViewModel.normProperty(), (StringConverter) new MyIntConverter());
        /**
         * напряжение VR0
         */
        Bindings.bindBidirectional(tf_VR0.textProperty(), detectorViewModel.vr0Property(), (StringConverter) new MyIntConverter());
        /**
         * напряжение VVA
         */
        Bindings.bindBidirectional(tf_VVA.textProperty(), detectorViewModel.vvaProperty(), (StringConverter) new MyIntConverter());
        /**
         * напряжение VU4
         */
        Bindings.bindBidirectional(tf_VU4.textProperty(), detectorViewModel.vu4Property(), (StringConverter) new MyIntConverter());
        /**
         * напряжение UC
         */
        Bindings.bindBidirectional(tf_UC.textProperty(), detectorViewModel.vucProperty(), (StringConverter) new MyIntConverter());
        /**
         * время интегрирования
         */
        Bindings.bindBidirectional(tf_TINT.textProperty(), detectorViewModel.tIntProperty(), (StringConverter) new MyIntConverter());
        /**
         * опрос датчика температуры
         */
        Bindings.bindBidirectional(lb_tSence.textProperty(), detectorViewModel.tempProperty(), (StringConverter) new MyIntConverter());
        Bindings.bindBidirectional(tf_tSence.textProperty(), detectorViewModel.tempProperty(), (StringConverter) new MyIntConverter());
        /**
         * сброс
         */
        Bindings.bindBidirectional(lb_reset.textProperty(), detectorViewModel.resetProperty(), new BooleanStringConverter() {
            @Override
            public Boolean fromString(String value) {
                return value.equals("Идет сброс");
            }

            @Override
            public String toString(Boolean value) {
                if (value) {
                    return "Идет сброс";
                } else {
                    return "--";
                }
            }
        });
        /**
         * ёмкости
         */
        Bindings.bindBidirectional(chob_CCC.valueProperty(), detectorViewModel.cccProperty());
        /**
         * направление
         */
        Bindings.bindBidirectional(chob_DIR.valueProperty(), detectorViewModel.dirProperty());
        /**
         * режим
         */
        Bindings.bindBidirectional(chob_mode.valueProperty(), detectorViewModel.modeProperty());
        /**
         * сброс?
         */
        Bindings.bindBidirectional(cheb_reset.selectedProperty(), detectorViewModel.resetProperty());
        /**
         * видимость статус бара эксп
         */
        pb_exp.visibleProperty().bind(detectorViewModel.getExp_shum_service().runningProperty().or(detectorViewModel.getExp_40_service().runningProperty()));
        /**
         * текст статуса коннекта с платой
         */
        Bindings.bindBidirectional(lab_status.textProperty(), detectorViewModel.isOkProperty(), new MyBoolConverter() {
            @Override
            public Boolean fromString(String value) {
                return value.equals("");
            }

            @Override
            public String toString(Boolean value) {
                if (value) {
                    return "Есть коннект";
                } else {
                    return "!!!Нет коннекта c платой!!!!";
                }
            }
        });

        /**
         *
         * чекбокс подачи напряжения VDDA
         */
        //       Bindings.bindBidirectional(cheb_Avdda.selectedProperty(), detectorViewModel.AVDDAProperty());
        /**
         * чекбокс подачи напряжения VDD
         */
        Bindings.bindBidirectional(cheb_Avdd.selectedProperty(), detectorViewModel.isPowerOnProperty());
        /**
         * значение первого канала
         */
        Bindings.bindBidirectional(tf_first_ch.textProperty(), detectorViewModel.first_chProperty(), (StringConverter) new MyIntConverter());
        /**
         * значение последнего канала
         */
        Bindings.bindBidirectional(tf_last_ch.textProperty(), detectorViewModel.last_chProperty(), (StringConverter) new MyIntConverter());
        /**
         * частота кадров
         */
        Bindings.bindBidirectional(tf_data_fps.textProperty(), detectorViewModel.pause_dataProperty(), (StringConverter) new MyIntConverter());
        /**
         * частота паузы видео
         */
        Bindings.bindBidirectional(tf_pause_video.textProperty(), detectorViewModel.pause_videoProperty(), (StringConverter) new MyIntConverter());
        /**
         * количество отсчетов
         */
        Bindings.bindBidirectional(tex_count.textProperty(), detectorViewModel.frameCountExpProperty(), (StringConverter) new MyIntConverter());
        /**
         * начальный канал в эксперим
         */
        Bindings.bindBidirectional(tex_firstChanExp.textProperty(), detectorViewModel.firstChanExpProperty(), (StringConverter) new MyIntConverter());
        /**
         * конечный канал в эксперим
         */
        Bindings.bindBidirectional(tex_lastChanExp.textProperty(), detectorViewModel.lastChanExpProperty(), (StringConverter) new MyIntConverter());
        //отработка изменения значения номера последнего канала
        tf_last_ch.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) { /* при получении фокуса */
                tf_last_ch.textProperty().unbindBidirectional(detectorViewModel.last_chProperty());
            } else { /* при потере */
                Bindings.bindBidirectional(tf_last_ch.textProperty(), detectorViewModel.last_chProperty(), (StringConverter) new MyIntConverter());
            }
        });
        //отработка изменения значения номера первого канала
        tf_first_ch.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) { /* при получении фокуса */
                tf_first_ch.textProperty().unbindBidirectional(detectorViewModel.first_chProperty());
            } else { /* при потере */
                Bindings.bindBidirectional(tf_first_ch.textProperty(), detectorViewModel.first_chProperty(), (StringConverter) new MyIntConverter());
            }
        });

    }

    /**
     * Инициализация обработчиков нажатия  текстовых полей
     */
    private void initHandlers() {
        //отработка смены диапазона темнового тока
        tf_norm.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.setNorm(i);
                    but_setDark.fire();
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).selectAll();
            }
        });
        //отработка кнопки номера минимального канала
        tf_first_ch.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.setFirst_ch(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).getParent().requestFocus();
            }
        });
        //отработка кнопки номера максимального канала
        tf_last_ch.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.setLast_ch(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).getParent().requestFocus();
            }
        });
        //отработка кнопки
        tf_UC.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.UCTiped(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).selectAll();
            }
        });
        //отработка кнопки
        tf_VR0.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.VR0Tiped(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).selectAll();
            }

        });
        //отработка кнопки
        tf_VVA.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.VVATiped(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).selectAll();
            }
        });
        //отработка кнопки
        tf_VU4.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.VU4Tiped(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).selectAll();
            }
        });
        //отработка кнопки
        tf_TINT.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.TINTTiped(i);
                } catch (NumberFormatException ignore) {
                }
                ((TextField) event.getSource()).selectAll();
            }
        });
        //отработка параметра
        tf_pause_video.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.restartVideo();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                ((TextField) event.getSource()).getParent().requestFocus();
            }
        });
        tf_pause_video.setTooltip(new Tooltip("Частота выборки видео из потока, мс"));
        tf_data_fps.setTooltip(new Tooltip("Частота вывода видео картинки, мс"));
        //отработка параметра
        tf_data_fps.setOnKeyPressed(event -> {
            if (enterTiped(event)) {
                try {
                    int i = Integer.parseUnsignedInt(((TextField) event.getSource()).getText());
                    detectorViewModel.data_fps_change();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                ((TextField) event.getSource()).getParent().requestFocus();
            }
        });
        //инициализация Ч_боксов
        initMode();
        initDIR();
        initCCC();
        //инициализация центральных кнопок
        initButtons();
    }

    /**
     * проверка на нажатие enter
     *
     * @param e
     * @return
     */
    private boolean enterTiped(KeyEvent e) {
        return e.getCode().getName().equalsIgnoreCase("ENTER");
    }

    /////////////////////////////////////////////////////////////////////////////////
    private void initCCC() {
        ObservableList<?> pass = FXCollections.observableArrayList("0.2", "0.4", "0.6", "0.8", "1.0", "1.2", "1.4", "1.6");
        chob_CCC.setItems(pass);
    }

    private void initDIR() {
        ObservableList<?> pass = FXCollections.observableArrayList("Прямое", "Обратное");
        chob_DIR.setItems(pass);
    }

    private void initMode() {
        Separator separator = new Separator();
        ObservableList<?> pass = FXCollections.observableArrayList("ВЗН",
                separator,
                "1-Bypass",
                "2-Bypass",
                "3-Bypass",
                "4-Bypass");
        chob_mode.setItems(pass);
    }

    //обработчик нажатий кнопок
    private void initButtons() {
        //обработка сохранения отчета
        bt_saveOtchet.setOnAction(event -> {
            JFreeChart[] chartViewer = detectorViewModel.getOrder().getChartViewer();
            if (chartViewer[0]!=null&&chartViewer[1]!=null&&chartViewer[2]!=null&&
                    chartViewer[3]!=null&&chartViewer[4]!=null&&chartViewer[5]!=null) {
                boolean res = new DocMaker(detectorViewModel).savePDF();
                checkBT(res, bt_saveOtchet, "Сохранено", "Ошибка");

            }else{
                checkBT(false, bt_saveOtchet, "Сохранено", "Нет данных");
            }

        });
        //обработка включения всех пикселей
        but_setAllPixel.setOnAction(event ->

        {
            detectorViewModel.setAllPixel();
        });
        //обработка выключения всех пикселей
        but_setNonePixel.setOnAction(event ->

        {
            detectorViewModel.setNonePixel();
        });
        //обработка пикселей
        but_setMyPixel.setOnAction(event ->

        {
            detectorViewModel.manual();
        });
        //обработка выключения линии А
        but_line_A.setOnAction(event ->

        {
            detectorViewModel.line_A();
        });
        //обработка выключения линии B
        but_line_B.setOnAction(event ->

        {
            detectorViewModel.line_B();
        });
        //обработка выключения линии C
        but_line_C.setOnAction(event ->

        {
            detectorViewModel.line_C();
        });
        //обработка выключения линии D
        but_line_D.setOnAction(event ->

        {
            detectorViewModel.line_D();
        });
        //обработка выключения линии E
        but_line_E.setOnAction(event ->

        {
            detectorViewModel.line_E();
        });
        //обработка выключения линии F
        but_line_F.setOnAction(event ->

        {
            detectorViewModel.line_F();
        });
        //обработка выключения линии G
        but_line_G.setOnAction(event ->

        {
            detectorViewModel.line_G();
        });
        //обработка выключения линии H
        but_line_H.setOnAction(event ->

        {
            detectorViewModel.line_H();
        });
        //обработка кнопки темновой
        but_setDark.setOnAction(event ->

        {
            detectorViewModel.setDark(but_setDark);
        });
        //обработка кнопки запроса параметров 1
        but_param1.setOnAction(event ->

        {
            detectorViewModel.param1();
        });
        //обработка кнопки запроса параметров 2
        but_param2.setOnAction(event ->

        {
            for (int i = 0; i < 6; i++) {
                detectorViewModel.param2((byte) i);
            }
        });
        //кнопка реконнекта
        bt_reconnect.setOnAction(event ->

        {
            detectorViewModel.reconnectDriver();
        });
        //обработка кнопки старта наработки
        but_regim.setOnAction(event ->

        {
            detectorViewModel.regimService();
        });

        //обработка кнопки старта работы МКС
        but_startMKS.setOnAction(event ->

        {
            fl_MKS_Working = !fl_MKS_Working;
            if (fl_MKS_Working) {
                but_startMKS.setText("Выключить");
            } else {
                but_startMKS.setText("Запуск!");
            }
            detectorViewModel.startMKS(fl_MKS_Working);
        });
        //обработка кнопки ресета графика
        but_resetBarchart.setOnAction(event ->

        {
            detectorViewModel.resetBarchart(CHANNEL_START, CHANNEL_STOP);
        });
        ////////////////////////////////////////
        //обработка кнопки старта эксперимента
        but_start_Shum.setOnAction(event ->

        {
            if (but_start_Shum.getText().startsWith("1.") && but_start_40.getText().startsWith("2.") && but_start_NEDT.getText().startsWith("3.")) {
                //освобождаем прогресс бар
                pb_exp.progressProperty().unbind();
                lab_exp_status.textProperty().unbind();
                //останавливаем главный график
                detectorViewModel.getMain_chart_service().cancel();
                //подвязываем прогресс бар
                lab_exp_status.textProperty().bind(detectorViewModel.getExp_shum_service().messageProperty());
                pb_exp.progressProperty().bind(detectorViewModel.getExp_shum_service().progressProperty());
                //отрабатываем кнопки
                if (but_start_Shum.getText().endsWith(("[ОК]"))) {
                    but_start_40.setStyle("-fx-background-color: #FFD700");
                    but_start_40.setText("2. Накопление при 40");
                    but_start_NEDT.setStyle("-fx-background-color: #FFD700");
                    but_start_NEDT.setText("3. Итоговый расчет");
                }
                but_start_Shum.setStyle("-fx-background-color: #FF7F50");
                but_start_Shum.setText("Накопление...");
                //стартуем эксперимент
                detectorViewModel.startExp(barChart_sko_30, barChart_30_40);
            }
        });

        //обработка кнопки старта эксперимента
        but_start_40.setOnAction(event ->

        {
            if (but_start_Shum.getText().startsWith("1.") && but_start_40.getText().startsWith("2.") && but_start_NEDT.getText().startsWith("3.")) {
                //освобождаем прогресс бар
                pb_exp.progressProperty().unbind();
                lab_exp_status.textProperty().unbind();
                //останавливаем главный график
                detectorViewModel.getMain_chart_service().cancel();
                //подвязываем прогресс бар
                lab_exp_status.textProperty().bind(detectorViewModel.getExp_40_service().messageProperty());
                pb_exp.progressProperty().bind(detectorViewModel.getExp_40_service().progressProperty());
                //отрабатываем кнопки
                //отрабатываем кнопки
                if (but_start_40.getText().endsWith(("[ОК]"))) {
                    but_start_NEDT.setStyle("-fx-background-color: #FFD700");
                    but_start_NEDT.setText("3. Итоговый расчет");
                }
                but_start_40.setStyle("-fx-background-color: #FF7F50");
                but_start_40.setText("Накопление...");
                //стартуем эксперимент
                detectorViewModel.startExp40(barChart_30_40);
            }
        });
        //обработка кнопки старта эксперимента
        but_start_NEDT.setOnAction(event ->

        {
            if (but_start_Shum.getText().startsWith("1.") && but_start_40.getText().startsWith("2.") && but_start_NEDT.getText().startsWith("3.")) {
                //освобождаем прогресс бар
                pb_exp.progressProperty().unbind();
                lab_exp_status.textProperty().unbind();
                //останавливаем главный график
                detectorViewModel.getMain_chart_service().cancel();
                //подвязываем прогресс бар
                lab_exp_status.textProperty().bind(detectorViewModel.getExp_NEDT_service().messageProperty());
                pb_exp.progressProperty().bind(detectorViewModel.getExp_NEDT_service().progressProperty());
                //отрабатываем кнопки
                //отрабатываем кнопки
                but_start_NEDT.setStyle("-fx-background-color: #FF7F50");
                but_start_NEDT.setText("Расчет...");
                //стартуем эксперимент
                detectorViewModel.startExpNEDT(barChart_NETD, barChart_NETD_All);
            }
        });
        //обработка кнопки сброса эксперимента
        but_start_RESET.setOnAction(event ->

        {
            //освобождаем прогресс бар
            pb_exp.progressProperty().unbind();
            lab_exp_status.textProperty().unbind();
            //останавливаем главный график
            //подвязываем прогресс бар
            lab_exp_status.textProperty().bind(detectorViewModel.getExp_Reset().messageProperty());
            pb_exp.progressProperty().bind(detectorViewModel.getExp_Reset().progressProperty());
            //отрабатываем кнопки
            but_start_Shum.setStyle("-fx-background-color: #FFD700");
            but_start_Shum.setText("1. Накопление при 30");
            but_start_40.setStyle("-fx-background-color: #FFD700");
            but_start_40.setText("2. Накопление при 40");
            but_start_NEDT.setStyle("-fx-background-color: #FFD700");
            but_start_NEDT.setText("3. Итоговый расчет");
            textArea_shum.setText("");
            //стартуем эксперимент

            detectorViewModel.resetExp();
        });
        //обработка кнопки сохранения файла эксперимента
        but_saveFileExp.setOnAction(event ->

        {
            boolean res = detectorViewModel.saveFileExp();
            checkBT(res, but_saveFileExp, "Сохранено", "Ошибка БД");
        });
        but_updateFileExp.setOnAction(event ->

        {
            boolean res = detectorViewModel.updateFileExp();
            checkBT(res, but_updateFileExp, "Обновлено", "Ошибка БД");
        });
        but_loadFileExp.setOnAction(event ->

        {
            String text;
            if (tf_IDexp.getText() != null || !tf_IDexp.getText().isEmpty()) {
                text = tf_IDexp.getText().trim();
            } else {
                checkBT(false, but_loadFileExp, "", "Укажите ID");
                return;
            }
            long l = 0;
            try {
                l = Long.parseLong(text);
            } catch (NumberFormatException e) {
                checkBT(false, but_loadFileExp, "", "Укажите ID");
                return;
            }
            boolean res = detectorViewModel.loadFileExp(l);
            checkBT(res, but_loadFileExp, "Загружено", "Ошибка БД");
        });
        but_loadExp.setOnAction(event ->

        {
            boolean res = detectorViewModel.loadFileExp(0);
            checkBT(res, but_loadExp, "Загружено", "Ошибка");
        });
        //обработка кнопки включения
        but_powerOn.setOnAction(event ->

        {
            detectorViewModel.powerOn(but_powerOn, but_powerOff);
        });
        //обработка кнопки выключения
        but_powerOff.setOnAction(event ->

        {
            detectorViewModel.powerOff(but_powerOn, but_powerOff);
        });
    }

    /**
     * Обработка  надписей кнопок
     *
     * @param res     случай
     * @param but     кнопка
     * @param goodTXT при tru
     * @param badTXT  при false
     */
    private void checkBT(boolean res, Button but, String goodTXT, String badTXT) {
        String text = but.getText();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    but.setText(text);
                });
            }
        }, 1000);

        Platform.runLater(() -> {
            if (res) {
                but.setText(goodTXT);
            } else {
                but.setText(badTXT);
            }
            timer.purge();
        });


    }

    //отработка корректности слайдеров
    @FXML
    private void sliderVR0Fire() {
        try {
            int i = Integer.parseUnsignedInt(tf_VR0.getText());
            detectorViewModel.VR0Tiped(i);
        } catch (NumberFormatException ignore) {
        }
        tf_VR0.getParent().requestFocus();
    }

    @FXML
    private void sliderVVAFire() {
        try {
            int i = Integer.parseUnsignedInt(tf_VVA.getText());
            detectorViewModel.VVATiped(i);
        } catch (NumberFormatException ignore) {
        }
        tf_VVA.getParent().requestFocus();
    }

    /**
     * Формирование поля деселекции
     *
     * @param loader
     */
    private void getMatrix(FXMLLoader loader) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        AnchorPane matrix = (AnchorPane) namespace.get("matrix");
        GridPane root = new GridPane();
        root.setPadding(new Insets(5, 5, 5, 17));
        root.setId("buttons");
        root.setHgap(4.2);
        root.setVgap(5);
        root.setMinSize(900, 120);
        root.setPrefSize(900, 120);
        root.setMaxSize(1189, 120);
        root.setGridLinesVisible(false);
        int row = 0;
        int bt = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < CHANNEL_STOP; j++) {
                if (((j % 2 == 0) && i > 4) || ((j % 2 == 1) && i < 4)) {
                    Button btn = new Button(" ");
                    Tooltip tooltip = new Tooltip(btn.getId());
                    tooltip.setFont(new Font(20));
                    tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
                    btn.setTooltip(tooltip);
                    Bindings.bindBidirectional(btn.tooltipProperty().get().textProperty(), btn.idProperty());
                    btn.setOnAction(event -> {
                        byte[] matr = Arrays.copyOf(DetectorViewModel.getMatrix(), DetectorViewModel.getMatrix().length);
                        Button source = (Button) event.getSource();
                        String id = source.getId();
                        String[] split = id.split(":");
                        matr[Integer.parseInt(split[0]) - 1] = (byte) ((matr[Integer.parseInt(split[0]) - 1]) ^ (0b1 << (8 - (Integer.parseInt(split[1])))));
                        setPixel(matr);
                    });
                    btn.setStyle("-fx-font: 3.4 arial; -fx-base: #17c355;");
                    btn.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(0.4D, 0.4D, 0.4D, 0.4D, false, false, false, false))));//////////////////////////////
                    btn.setAlignment(Pos.CENTER);
                    root.add(btn, j, i, 2, 1);
                    buttons.add(btn);
                }
            }
        }
        String ch_numb = "";
        for (int k = 0; k < buttons.size(); k++) {
            bt = 1 + (k % (CHANNELNUMBER / 2));
            row = 8 - (k / (CHANNELNUMBER / 2));
            if (row < 5) {
                ch_numb = String.valueOf(bt * 2 - 1);
            } else {
                ch_numb = String.valueOf(bt * 2);
            }
            buttons.get(k).setId(bt + ":" + row + ":" + "Включен" + ":" + ch_numb + " канал");
        }
        matrix.getChildren().add(root);
    }

    /**
     * Создание главного графика
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @param low    - нижний предел
     * @param up     - верхний предел
     * @param tick   - количество делений
     * @return BarChart<String, Number>
     */
    public BarChart<String, Number> getBar_chart(FXMLLoader loader, String name, double low, double up, double tick) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        Pane nm = (Pane) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis categoryAxis = new CategoryAxis(category);
        NumberAxis numberAxis = new NumberAxis();
        BarChart<String, Number> stringNumberBarChart = new BarChart<>(categoryAxis, numberAxis);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(low);
        numberAxis.setUpperBound(up);
        numberAxis.setTickUnit(tick);
        categoryAxis.setTickLength(5);
        categoryAxis.setAutoRanging(false);
        categoryAxis.setGapStartAndEnd(false);
        categoryAxis.setTickLabelGap(0);
        stringNumberBarChart.setAnimated(false);
        stringNumberBarChart.setAlternativeColumnFillVisible(false);
        stringNumberBarChart.setAlternativeRowFillVisible(false);
        stringNumberBarChart.setHorizontalGridLinesVisible(true);
        stringNumberBarChart.setVerticalGridLinesVisible(false);
        stringNumberBarChart.setBarGap(0);
        stringNumberBarChart.setLegendVisible(false);
        stringNumberBarChart.setCategoryGap(0);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        for (int i = 0; i < CHANNELNUMBER; i++) {
            XYChart.Data data = new XYChart.Data(String.valueOf(i + 1), getTestData(i));
            series1.getData().addAll(data);
            series.add(series1);
        }
        stringNumberBarChart.getData().add(series1);
        stringNumberBarChart.setPrefSize(1189.0, 330.0);
        stringNumberBarChart.setMinSize(-1, -1);
        nm.getChildren().add(stringNumberBarChart);
        int i = 0;
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(0).getData()) {
            Tooltip tooltip = new Tooltip("Канал № " + data.getXValue());
            tooltip.setFont(new Font(15));
            tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> checkButtonsTrue(data));
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, event -> checkButtonsFalse(data));
            setChColor(i++, data);
        }
        return stringNumberBarChart;
    }

    /**
     * Раскраска каналов по видеовыходам
     *
     * @param i    номер канала
     * @param data бар на графике
     */
    public static void setChColor(int i, XYChart.Data<String, Number> data) {

        i = getChNumber(i);
        if (i == 0) {
            data.getNode().setStyle("-fx-background-color: #eb9e81");
        }
        if (i == 1) {
            data.getNode().setStyle("-fx-background-color: #e84606");
        }
        if (i == 2) {
            data.getNode().setStyle("-fx-background-color: #2f1106");
        }
        if (i == 3) {
            data.getNode().setStyle("-fx-background-color: #e84606");
        }
    }

    /**
     * Подсветка элементов на матрице деселекции при наведении на бар
     *
     * @param data
     */
    private void checkButtonsTrue(XYChart.Data<String, Number> data) {
        Button button;
        int i1 = Integer.parseInt(data.getXValue());
        int i2 = i1 % 2;
        for (int j = 0; j < 4; j++) {
            if (i2 == 1) {
                button = buttons.get(576 + (i1 / 2) + 144 * j);
            } else {
                button = buttons.get(-1 + (i1 / 2) + 144 * j);
            }
            button.setStyle("-fx-font: 3.4 arial; -fx-base: #FF7F50");
        }
    }

    /**
     * Подсветка элементов на матрице деселекции при снятии наведения
     *
     * @param data
     */
    private void checkButtonsFalse(XYChart.Data<String, Number> data) {
        Button button;
        int i1 = Integer.parseInt(data.getXValue());
        int i2 = i1 % 2;
        for (int j = 0; j < 4; j++) {
            if (i2 == 1) {
                button = buttons.get(576 + (i1 / 2) + 144 * j);
            } else {
                button = buttons.get(-1 + (i1 / 2) + 144 * j);
            }
            String[] split = button.getId().split(":");
            if (split[2].equals("Включен")) {
                button.setStyle("-fx-font: 3.4 arial; -fx-base: #17c355;");
            } else {
                button.setStyle("-fx-font: 3.4 arial; -fx-base: #c32b17;");
            }
        }
    }

    /**
     * Тестовое заполнение главного графика
     *
     * @param i номер канала
     * @return уровень мсигнала
     */
    private int getTestData(int i) {
        i++;
        i = i % 4;
        if (i == 1) {
            return 3000;
        }
        if (i == 2) {
            return 3200;
        }
        if (i == 3) {
            return 3400;
        }
        if (i == 0) {
            return 3600;
        }
        return 0;
    }

    /**
     * Создание вспомогательного графика в закладке эксперимент
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @param low    - нижний предел
     * @param up     - верхний предел
     * @param tick   - количество делений
     * @return BarChart<String, Number>
     */
    public BarChart<String, Number> getBar_chartTemp(FXMLLoader loader, String name, double low, double up, double tick) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        HBox nm = (HBox) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis categoryAxis = new CategoryAxis(category);
        NumberAxis numberAxis = new NumberAxis();
        BarChart<String, Number> stringNumberBarChart = new BarChart<>(categoryAxis, numberAxis);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(low);
        numberAxis.setUpperBound(up);
        numberAxis.setTickUnit(tick);
        categoryAxis.setTickLength(5);
        categoryAxis.setAutoRanging(false);
        categoryAxis.setGapStartAndEnd(false);
        categoryAxis.setTickLabelGap(0);
        stringNumberBarChart.setAnimated(false);
        stringNumberBarChart.setAlternativeColumnFillVisible(false);
        stringNumberBarChart.setAlternativeRowFillVisible(false);
        stringNumberBarChart.setHorizontalGridLinesVisible(true);
        stringNumberBarChart.setVerticalGridLinesVisible(false);
        stringNumberBarChart.prefHeightProperty().bind(nm.prefHeightProperty());
        stringNumberBarChart.prefWidthProperty().bind(nm.prefWidthProperty().multiply(0.6));
        stringNumberBarChart.getXAxis().setTickLabelsVisible(false);
        stringNumberBarChart.getXAxis().setTickMarkVisible(false);
        stringNumberBarChart.setBarGap(0);
        stringNumberBarChart.setLegendVisible(false);
        stringNumberBarChart.setCategoryGap(0);
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        for (int i = 0; i < CHANNELNUMBER; i++) {
            XYChart.Data data = new XYChart.Data(String.valueOf(i + 1), getTestData(i));
            series1.getData().addAll(data);
            series.add(series1);
        }
        stringNumberBarChart.getData().add(series1);
        nm.getChildren().add(stringNumberBarChart);
        int i = 0;
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(0).getData()) {
            setChColor(i++, data);
        }
        return stringNumberBarChart;
    }

    /**
     * Создание графика шума
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @param low    - нижний предел
     * @param up     - верхний предел
     * @param tick   - количество делений
     * @return BarChart<String, Number>
     */
    public BarChart<String, Number> getBar_chart_sko_30(FXMLLoader loader, String name, double low, double up, double tick) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        Pane nm = (Pane) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis categoryAxis = new CategoryAxis(category);
        NumberAxis numberAxis = new NumberAxis();
        BarChart<String, Number> stringNumberBarChart = new BarChart<>(categoryAxis, numberAxis);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(low);
        numberAxis.setUpperBound(up);
        numberAxis.setTickUnit(tick);
        categoryAxis.setTickLength(5);
        categoryAxis.setAutoRanging(false);
        categoryAxis.setGapStartAndEnd(false);
        categoryAxis.setTickLabelGap(0);
        stringNumberBarChart.setAnimated(false);
        stringNumberBarChart.setAlternativeColumnFillVisible(false);
        stringNumberBarChart.setAlternativeRowFillVisible(false);
        stringNumberBarChart.setHorizontalGridLinesVisible(true);
        stringNumberBarChart.setVerticalGridLinesVisible(false);
        stringNumberBarChart.setBarGap(0);
        stringNumberBarChart.setLegendVisible(false);
        stringNumberBarChart.setCategoryGap(0.25);
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        for (int i = 0; i < CHANNELNUMBER; i++) {
            XYChart.Data data = new XYChart.Data(String.valueOf(i + 1), 0);
            series1.getData().addAll(data);
        }
        stringNumberBarChart.getData().add(series1);
        stringNumberBarChart.setPrefSize(1189.0, 330.0);
        stringNumberBarChart.setMinSize(-1, -1);
        nm.getChildren().add(stringNumberBarChart);
        int i = 0;
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(0).getData()) {
            setChColor(i++, data);
        }
        /**
         * Вызов подробного графика при клике на основной
         */
        stringNumberBarChart.setOnMouseClicked(event -> {
            if ((detectorViewModel.getExperiment() != null) && (detectorViewModel.getExperiment().getDataArraySKO30() != null)) {
                new ModernChart().start("Подробный график",
                        "Шум по фотоприёмнику", "Каналы", "Шум, мВ",
                        detectorViewModel.getFirstChanExp(), detectorViewModel.getLastChanExp(), TIPE_Dataset30_40,
                        detectorViewModel.getExperiment().getDataArraySKO30());
            }
        });
        return stringNumberBarChart;
    }

    /**
     * Создание графика сигналов 30 и 40
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @param low    - нижний предел
     * @param up     - верхний предел
     * @param tick   - количество делений
     * @return BarChart<String, Number>
     */
    public BarChart<String, Number> getBar_chart30_40(FXMLLoader loader, String name, double low, double up, double tick) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        Pane nm = (Pane) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis categoryAxis = new CategoryAxis(category);
        NumberAxis numberAxis = new NumberAxis();
        BarChart<String, Number> stringNumberBarChart = new BarChart<>(categoryAxis, numberAxis);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(low);
        numberAxis.setUpperBound(up);
        numberAxis.setTickUnit(tick);
        categoryAxis.setTickLength(5);
        categoryAxis.setAutoRanging(false);
        categoryAxis.setGapStartAndEnd(false);
        categoryAxis.setTickLabelGap(0);
        stringNumberBarChart.setAnimated(false);
        stringNumberBarChart.setAlternativeColumnFillVisible(false);
        stringNumberBarChart.setAlternativeRowFillVisible(false);
        stringNumberBarChart.setHorizontalGridLinesVisible(true);
        stringNumberBarChart.setVerticalGridLinesVisible(false);
        stringNumberBarChart.setBarGap(0);
        stringNumberBarChart.setLegendVisible(false);
        stringNumberBarChart.setCategoryGap(0);
        XYChart.Series<String, Number> series30 = new XYChart.Series<>();
        XYChart.Series<String, Number> series40 = new XYChart.Series<>();
        for (int i = 0; i < CHANNELNUMBER; i++) {
            XYChart.Data data30 = new XYChart.Data(String.valueOf(i + 1), 0);
            XYChart.Data data40 = new XYChart.Data(String.valueOf(i + 1), 0);
            series30.getData().addAll(data30);
            series40.getData().addAll(data40);
            series.addAll(series30, series40);
        }
        stringNumberBarChart.getData().addAll(series30, series40);
        stringNumberBarChart.setPrefSize(1189.0, 330.0);
        stringNumberBarChart.setMinSize(-1, -1);
        nm.getChildren().add(stringNumberBarChart);
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(0).getData()) {
            data.getNode().setStyle("-fx-background-color: #e84606");
        }
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(1).getData()) {
            data.getNode().setStyle("-fx-background-color: #3e67b9");
        }
        /**
         * Вызов подробного графика при клике на основной
         */
        stringNumberBarChart.setOnMouseClicked(event -> {
            if ((detectorViewModel.getExperiment() != null) && (detectorViewModel.getExperiment().getDataArraySKO30() != null)) {
                new ModernChart().start("Подробный график",
                        "Средние значения выходного сигнала", "Каналы", "Напряжение, мВ",
                        detectorViewModel.getFirstChanExp(), detectorViewModel.getLastChanExp(), TIPE_Dataset30_40,
                        detectorViewModel.getExperiment().getDataArraySred_30(), detectorViewModel.getExperiment().getDataArraySred_40());
            }
        });
        return stringNumberBarChart;
    }

    /**
     * Создание графика НЕДТ
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @param low    - нижний предел
     * @param up     - верхний предел
     * @param tick   - количество делений
     * @return BarChart<String, Number>
     */
    public BarChart<String, Number> getBar_chart_NEDT(FXMLLoader loader, String name, double low, double up, double tick) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        Pane nm = (Pane) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis categoryAxis = new CategoryAxis(category);
        NumberAxis numberAxis = new NumberAxis();
        BarChart<String, Number> stringNumberBarChart = new BarChart<>(categoryAxis, numberAxis);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(low);
        numberAxis.setUpperBound(up);
        numberAxis.setTickUnit(tick);
        categoryAxis.setTickLength(5);
        categoryAxis.setAutoRanging(false);
        categoryAxis.setGapStartAndEnd(false);
        categoryAxis.setTickLabelGap(0);
        stringNumberBarChart.setAnimated(false);
        stringNumberBarChart.setAlternativeColumnFillVisible(false);
        stringNumberBarChart.setAlternativeRowFillVisible(false);
        stringNumberBarChart.setHorizontalGridLinesVisible(true);
        stringNumberBarChart.setVerticalGridLinesVisible(false);
        stringNumberBarChart.setBarGap(0);
        stringNumberBarChart.setLegendVisible(false);
        stringNumberBarChart.setCategoryGap(0);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < CHANNELNUMBER; i++) {
            XYChart.Data data = new XYChart.Data(String.valueOf(i + 1), 0);
            series.getData().addAll(data);
        }
        stringNumberBarChart.getData().add(series);
        stringNumberBarChart.setPrefSize(1189.0, 330.0);
        stringNumberBarChart.setMinSize(-1, -1);
        nm.getChildren().add(stringNumberBarChart);
        int i = 0;
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(0).getData()) {
            setChColor(i++, data);
        }
        /**
         * Вызов подробного графика при клике на основной
         */
        stringNumberBarChart.setOnMouseClicked(event -> {
            if ((detectorViewModel.getExperiment() != null) && (detectorViewModel.getExperiment().getDataArrayNEDT() != null)) {
                new ModernChart().start("Подробный график",
                        "ЭШРТ выходных каналов", "Каналы", "ЭШРТ, мК",
                        detectorViewModel.getFirstChanExp(), detectorViewModel.getLastChanExp(), TIPE_DatasetNEDT,
                        detectorViewModel.getExperiment().getDataArrayNEDT());
            }
        });
        return stringNumberBarChart;
    }

    /**
     * Создание графика распределения НЕДТ
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @param low    - нижний предел
     * @param up     - верхний предел
     * @param tick   - количество делений
     * @return BarChart<String, Number>
     */
    public BarChart<String, Number> getBar_chart_Raspred(FXMLLoader loader, String name, double low, double up, double tick) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        VBox nm = (VBox) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis categoryAxis = new CategoryAxis();
        NumberAxis numberAxis = new NumberAxis();
        BarChart<String, Number> stringNumberBarChart = new BarChart<>(categoryAxis, numberAxis);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(low);
        numberAxis.setUpperBound(up);
        numberAxis.setTickUnit(tick);
        categoryAxis.setTickLength(5);
        categoryAxis.setAutoRanging(true);
        categoryAxis.setGapStartAndEnd(true);
        categoryAxis.setTickLabelGap(0);
        categoryAxis.setTickLabelsVisible(true);
        categoryAxis.setTickMarkVisible(true);
        categoryAxis.setStyle("-fx-font: 3.4 arial;");
        stringNumberBarChart.setAnimated(false);
        stringNumberBarChart.setAlternativeColumnFillVisible(false);
        stringNumberBarChart.setAlternativeRowFillVisible(false);
        stringNumberBarChart.setHorizontalGridLinesVisible(true);
        stringNumberBarChart.setVerticalGridLinesVisible(false);
        stringNumberBarChart.setBarGap(0);
        stringNumberBarChart.setLegendVisible(false);
        stringNumberBarChart.setCategoryGap(10);
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        for (int i = -4; i < 5; i++) {
            XYChart.Data data = new XYChart.Data(String.valueOf(i), 0);
            series1.getData().add(data);
        }
        stringNumberBarChart.getData().addAll(series1);
        nm.getChildren().add(stringNumberBarChart);
        for (XYChart.Data<String, Number> data : stringNumberBarChart.getData().get(0).getData()) {
            data.getNode().setStyle("-fx-background-color: #3e67b9");
        }
        /**
         * Вызов подробного графика при клике на основной
         */
        stringNumberBarChart.setOnMouseClicked(event -> {
            if ((detectorViewModel.getExperiment() != null) && (detectorViewModel.getExperiment().getDataArrayNEDT() != null)) {
                new SampleBarChart().start("Подробный график",
                        "Распределение ЭШРТ", "Диапазон ЭШРТ", "Количество вхождений, шт", detectorViewModel.getExperiment().getRaspredMap());
            }
        });
        return stringNumberBarChart;
    }

    /**
     * Создание временного графика
     *
     * @param loader - ссылка на график
     * @param name   - имя родителя графика
     * @return LineChart<String, Number>
     */
    public LineChart<String, Number> getline_chart(FXMLLoader loader, String name) {
        ObservableMap<String, Object> namespace = loader.getNamespace();
        AnchorPane nm = (AnchorPane) namespace.get(name);
        if (nm.getChildren().size() > 0) {
            nm.getChildren().clear();
        }
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Вся шкала 1 мин");
        xAxis.setAnimated(false);
        yAxis.setLabel("Значение, мВ");
        yAxis.setAnimated(false);
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(true);
        lineChart.setLegendSide(Side.TOP);
        lineChart.prefHeightProperty().bind(nm.prefHeightProperty());
        lineChart.prefWidthProperty().bind(nm.prefWidthProperty().multiply(0.93));
        lineChart.setCreateSymbols(false);
        lineChart.getXAxis().setTickLabelsVisible(false);
        //серии данных
        XYChart.Series<String, Number> ser_sred = new XYChart.Series<>();
        XYChart.Series<String, Number> ser_max = new XYChart.Series<>();
        XYChart.Series<String, Number> ser_min = new XYChart.Series<>();
        XYChart.Series<String, Number> ser_sko = new XYChart.Series<>();
        ser_sred.setName("Среднее значение");
        ser_max.setName("Максимальное значение");
        ser_min.setName("Минимальное значение");
        ser_sko.setName("Среднеквадратичное отклонение");
        lineChart.getData().addAll(ser_sred, ser_max, ser_min, ser_sko);
        nm.getChildren().add(lineChart);
        return lineChart;
    }

    @FXML
    /**
     * Отработка вывода выбранных каналов в главном графике
     */
    public void chenMode() {
        byte bytte = 0;
        if (tBut_OUT1.isSelected()) {
            bytte = (byte) (bytte | 0b1000);//1
        }
        if (tBut_OUT2.isSelected()) {
            bytte = (byte) (bytte | (0b100));//2
        }
        if (tBut_OUT3.isSelected()) {
            bytte = (byte) (bytte | (0b10));//4
        }
        if (tBut_OUT4.isSelected()) {
            bytte = (byte) (bytte | (0b1));//8
        }
        if (bytte == 0) {
            Platform.runLater(() -> {
                tBut_OUT1.setSelected(true);
                tBut_OUT2.setSelected(true);
                tBut_OUT3.setSelected(true);
                tBut_OUT4.setSelected(true);
            });
            return;
        }
        detectorViewModel.getMain_chart_service().setOutMode(bytte);
    }

    public BarChart<String, Number> getBarChart_Exist() {
        return barChart_Exist;
    }

    public LineChart<String, Number> getLineChart_time() {
        return lineChart_time;
    }

    void setPixel(byte[] bytes) {
        detectorViewModel.setPixel(bytes);
    }

    public DetectorViewModel getDetectorViewModel() {
        return detectorViewModel;
    }

    //вложенные классы конверторы
    private static class MyIntConverter extends IntegerStringConverter {
        public MyIntConverter() {
            super();
        }

        @Override
        public Integer fromString(String value) {
            try {
                Integer.parseInt(value);
                return super.fromString(value);
            } catch (NumberFormatException exception) {
                return 0;
            }
        }

        @Override
        public String toString(Integer value) {
            return super.toString(value);
        }
    }

    private static class MyDoubleConverter extends DoubleStringConverter {
        private static final String DEFAULT_FORMAT = "0";
        private static final NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);

        public MyDoubleConverter() {
            super();
        }

        @Override
        public Double fromString(String value) {
            try {
                Double.parseDouble(value);
                return (double) Math.round(super.fromString(value));
            } catch (NumberFormatException exception) {
                return 0D;
            }
        }

        @Override
        public String toString(Double value) {
            return FORMATTER.format(value);
        }
    }

    private static class MyBoolConverter extends BooleanStringConverter {
        public MyBoolConverter() {
            super();
        }

        @Override
        public Boolean fromString(String value) {
            return value.equals("Подано");
        }

        @Override
        public String toString(Boolean value) {
            if (value) {
                return "Подано";
            } else {
                return "Снято";
            }
        }
    }

    private static class MyLongConverter extends LongStringConverter {
        public MyLongConverter() {
            super();
        }

        @Override
        public Long fromString(String value) {
            try {
                Long.parseLong(value);
                return super.fromString(value);
            } catch (NumberFormatException exception) {
                return 0l;
            }
        }

        @Override
        public String toString(Long value) {
            return String.format("%d мин, %d сек", TimeUnit.MILLISECONDS.toMinutes(value),
                    TimeUnit.MILLISECONDS.toSeconds(value) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(value)));
        }
    }

    private static class MyFloatConverter extends FloatStringConverter {
        public MyFloatConverter() {
            super();
        }

        @Override
        public Float fromString(String value) {
            try {
                Float.parseFloat(value);
                return super.fromString(value);
            } catch (NumberFormatException exception) {
                return 0f;
            }
        }

        @Override
        public String toString(Float value) {
            return String.format("%.2f", value);
        }

    }

    public BarChart<String, Number> getBarChart_Temp() {
        return barChart_Temp;
    }

    public Button getBut_start_Shum() {
        return but_start_Shum;
    }

    public Button getBut_start_40() {
        return but_start_40;
    }

    public Button getBut_start_NEDT() {
        return but_start_NEDT;
    }

    public TextField getTf_regim() {
        return tf_regim;
    }

    public void setTf_regim(TextField tf_regim) {
        this.tf_regim = tf_regim;
    }

    public Button getBut_startMKS() {
        return but_startMKS;
    }

    public void setBut_startMKS(Button but_startMKS) {
        this.but_startMKS = but_startMKS;
    }

    public Button getBut_powerOn() {
        return but_powerOn;
    }

    public void setBut_powerOn(Button but_powerOn) {
        this.but_powerOn = but_powerOn;
    }

    public Button getBut_powerOff() {
        return but_powerOff;
    }

    public void setBut_powerOff(Button but_powerOff) {
        this.but_powerOff = but_powerOff;
    }

    public static ArrayList<ImageView> getListView() {
        return listView;
    }

    public static void setListView(ArrayList<ImageView> listView) {
        SecondaryController.listView = listView;
    }
}



