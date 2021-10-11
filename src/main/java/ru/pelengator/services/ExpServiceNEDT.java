package ru.pelengator.services;


import at.favre.lib.bytes.Bytes;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import org.decimal4j.util.DoubleRounder;
import ru.pelengator.App;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;
import ru.pelengator.model.Experiment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static ru.pelengator.PropFile.*;

/**
 * Сервис расчета ключевых параметров
 */
public class ExpServiceNEDT extends Service<Void> {
    //ссылки
    private static SecondaryController controller;
    private static DetectorViewModel detectorViewModel;
    //исходные данные
    private Experiment currentExp;

    public double[] dataArraySred_30;
    public double[] dataArraySred_40;
    public double[] dataArraySKO30;
    private int start_ch;
    private int stop_ch;
    private double brak_po_kanalu_NEDT;
    private double brak_po_fpu_NEDT;
    //вспомогательные параметры
    private StringBuilder sb;
    private int countChannel;
    private static double raspred_delta;
    private static int[] raspred_otrezki_massiv;
    private double maxNEDT;
    private double minNEDT;
    private int upperBoundNEDT_raspred;
    //итоговые
    public double[] dataArrayNEDT;
    private static double FINAL_NEDT;
    private int countDeselPixel;
    private int maxCountDeselPixelInLine;
    private BarChart<String, Number> chartNEDT;
    private BarChart<String, Number> chartNEDT_Raspred;
    private Map<String, Number> raspredMap;

    public ExpServiceNEDT(SecondaryController controller, DetectorViewModel detectorViewModel) {
        this.controller = controller;
        this.detectorViewModel = detectorViewModel;
    }

    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Старт Сервиса расчета ЭШРТ");
                updateProgress(0.0, 1);
                updateMessage("Сброс переменных");
                //сброс данных
                resetParams();
                //инициализация данных эксперимента
                updateMessage("Инициализация данных");
                initParams();
                updateProgress(0.2, 1);
                //проверка наличия данных
                if (dataArraySred_30 == null || dataArraySred_40 == null) {
                    updateMessage("Выход .... нет данных");
                    updateProgress(1, 1);
                    loadnextBDdata();
                    cancelled();
                    return null;
                }
                //расчет недт
                FINAL_NEDT = calculateNEDT(dataArraySred_30, dataArraySred_40, dataArraySKO30);
                updateMessage("Рассчитал NEDT ....");
                updateProgress(0.4, 1);
                //обработка количества вхождений НЕДТ
                takeRaspredNEDT();
                updateMessage("Расчет количества вхождений НЕДТ ....");
                updateProgress(0.6, 1);
                takeCountOfPixelAndShow();
                updateProgress(0.7, 1);
                //сохранение полученных данных
                saveExpData();
                updateProgress(0.8, 1);
                //отображение данных
                showResults();
                updateProgress(0.95, 1);
                updateMessage("Готов");
                updateProgress(1, 1);
                loadnextBDdata();//при загрузке из бд сбрасывает флаг
                return null;
            }


            /**
             * Отработка успешного завершения эксперимента
             */
            @Override
            protected void succeeded() {
                super.succeeded();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#10d015", "3. Итоговый расчет [ОК]");

            }

            /**
             * Обработка отмены сервиса
             */
            @Override
            protected void cancelled() {
                super.cancelled();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#FFD700", "3. Итоговый расчет");
            }

            /**
             * Печать ошибки. Разблокировка кнопок
             */
            @Override
            protected void failed() {
                super.failed();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#FFD700", "3. Итоговый расчет");
            }
        };
    }

    /**
     * Отображение данных из БД
     */
    private void loadnextBDdata() {
        if (detectorViewModel.isRELOADCHARTS()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> {
                        detectorViewModel.setRELOADCHARTS(false);
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Создание узла с текстром ошибки
     *
     * @param exception
     * @return
     */
    private Node setErrorMSG(Throwable exception) {
        Pane pane = new Pane();
        pane.getChildren().add(new TextArea(exception.getMessage()));
        return pane;
    }

    /**
     * Инициализация параметров
     */
    private void initParams() {
        start_ch = detectorViewModel.getFirstChanExp();
        stop_ch = detectorViewModel.getLastChanExp();
        countChannel = stop_ch - start_ch + 1;
        brak_po_kanalu_NEDT = Double.parseDouble(controller.tex_brak_count.getText()) / ONE_K;
        brak_po_fpu_NEDT = Double.parseDouble(controller.tex_brak_countFPU.getText()) / ONE_K;
        ////////////////////////инициализация массивов
        currentExp = DetectorViewModel.getExperiment();
        dataArraySred_30 = currentExp.getDataArraySred_30();
        dataArraySred_40 = currentExp.getDataArraySred_40();
        dataArraySKO30 = currentExp.getDataArraySKO30();
        dataArrayNEDT = new double[CHANNELNUMBER];
        maxNEDT = Double.MIN_VALUE;
        minNEDT = Double.MAX_VALUE;
        raspredMap = new LinkedHashMap<>();
        sb = new StringBuilder();
    }

    /**
     * Сброс переменных
     */
    private void resetParams() {
        raspred_delta = 0;
        maxNEDT = 0;
        upperBoundNEDT_raspred = 0;
        FINAL_NEDT = 0;
        minNEDT = Double.MAX_VALUE;
    }

    /**
     * Расчет недт по каналу
     *
     * @param i-номер канала
     * @return НЕДТ i-го канала
     */
    private double takeNEDT(int i, double[] dataArraySred_30, double[] dataArraySred_40, double[] dataArraySKO30) {

        double vdelta = dataArraySred_40[i] - dataArraySred_30[i];
        //проверка дельты сигнала
        if (vdelta == 0) {
            sb.append("Канал " + (i + 1) + " не работает!").append("\n");
            vdelta = -1D;
        } else if (vdelta < 0) {
            sb.append("Канал " + (i + 1) + " значение уменьшилось!").append("\n");
            vdelta = -1D;
        }
        double delenie_na_sko = vdelta / dataArraySKO30[i];
        double nedt_po_kanalu = DELTA_TEMP / delenie_na_sko;
        return nedt_po_kanalu;
    }

    private void showAlert(StringBuilder s) {
        if (s.toString().isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Найдены плохие каналы");
        alert.setTitle("НЕДТ");
        Pane pane = new Pane();
        pane.getChildren().add(new TextArea(s.toString()));
        alert.setGraphic(pane);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }

    /**
     * Определение общего НЕДТ и заполнение массива НЕДТ по каналам
     *
     * @param dataArraySred_30
     * @param dataArraySred_40
     * @param dataArraySKO30
     * @return
     */
    private double calculateNEDT(double[] dataArraySred_30, double[] dataArraySred_40, double[] dataArraySKO30) {
        double temp_summaNEDT = 0;
        for (int i = 0; i < CHANNELNUMBER; i++) {
            dataArrayNEDT[i] = takeNEDT(i, dataArraySred_30, dataArraySred_40, dataArraySKO30);
        }
        for (int i = start_ch - 1; i < stop_ch; i++) {
            double nedt = dataArrayNEDT[i];
            temp_summaNEDT = temp_summaNEDT + nedt;
        }
        return temp_summaNEDT / (countChannel);
    }

    /**
     * Расчет массива количества вхождений и созание карты вхождений
     */
    private void takeRaspredNEDT() {
        //расчет количества отрезков для распределения
        double tempVal = 3.322D * Math.log10(countChannel);
        double round = DoubleRounder.round(tempVal, 3);// округляем
        int raspred_count_Otrezkov = 1 + (int) (round);//количество отрезков statisticsUtils-массив
        //пределение крайних значений
        for (int i = 0; i < dataArrayNEDT.length; i++) {
            if (dataArrayNEDT[i] > maxNEDT) {
                maxNEDT = dataArrayNEDT[i];
            }
            if (dataArrayNEDT[i] < minNEDT) {
                minNEDT = dataArrayNEDT[i];
            }
        }

        raspred_delta = (maxNEDT - minNEDT) / raspred_count_Otrezkov;
        raspred_otrezki_massiv = new int[raspred_count_Otrezkov];

        //отработка вхождений
        for (int i = start_ch - 1; i < stop_ch; i++) {
            boolean entered = false;
            for (int j = 0; j < raspred_count_Otrezkov; j++) {

                if (((minNEDT + raspred_delta * j) <= dataArrayNEDT[i]) && (dataArrayNEDT[i] < (minNEDT + raspred_delta * (j + 1)))) {
                    raspred_otrezki_massiv[j] += 1;
                    entered = true;
                }
            }
            if (!entered) {
                raspred_otrezki_massiv[raspred_count_Otrezkov - 1] += 1;
            }
        }
        //определение верхней границы графика на распределении
        for (int i = 0; i < raspred_otrezki_massiv.length; i++) {
            if (raspred_otrezki_massiv[i] > upperBoundNEDT_raspred) {
                upperBoundNEDT_raspred = raspred_otrezki_massiv[i];
            }
        }

        for (int i = 0; i < raspred_otrezki_massiv.length; i++) {
            double niznyaGranicaOtrezka = (minNEDT + raspred_delta * i) * ONE_K;
            double verhnyaGranicaOtrezka = (minNEDT + raspred_delta * (i + 1)) * ONE_K;
            String strGran = String.format("[%.1f-%.1f]", niznyaGranicaOtrezka, verhnyaGranicaOtrezka);
            raspredMap.put(strGran, raspred_otrezki_massiv[i]);
        }
    }

    /**
     * Расчет допуска по деселектированным пиксилям
     */
    private void takeCountOfPixelAndShow() {
        countDeselPixel = 0;
        maxCountDeselPixelInLine = 0;
        if (currentExp.getMode().equals("ВЗН")) {
            byte[] matrix = currentExp.getMatrix();
            for (byte b :
                    matrix) {
                BitSet bitSet = Bytes.from(b).toBitSet();
                int cardinality = bitSet.cardinality();
                if (cardinality < 8) {
                    countDeselPixel = countDeselPixel + (8 - cardinality);
                    if (maxCountDeselPixelInLine < (8 - cardinality))
                        maxCountDeselPixelInLine = (8 - cardinality);
                }
            }
        } else {
        //по нулям
        }
        Platform.runLater(() -> {
            controller.lab_countDeselPixel.setText(String.valueOf(countDeselPixel));
            controller.lab_countDeselPixelInLine.setText(String.valueOf(maxCountDeselPixelInLine));
            if (countDeselPixel <= 12) {
                controller.lab_countDeselPixel_sootv.setTextFill(Paint.valueOf("#10d015"));
                controller.lab_countDeselPixel_sootv.setText("Да");
            } else {
                controller.lab_countDeselPixel_sootv.setTextFill(Paint.valueOf("#e81010"));
                controller.lab_countDeselPixel_sootv.setText("Нет");
            }
            if (maxCountDeselPixelInLine <= 2) {
                controller.lab_countDeselPixelInLine_sootv.setTextFill(Paint.valueOf("#10d015"));
                controller.lab_countDeselPixelInLine_sootv.setText("Да");
            } else {
                controller.lab_countDeselPixelInLine_sootv.setTextFill(Paint.valueOf("#e81010"));
                controller.lab_countDeselPixelInLine_sootv.setText("Нет");
            }
        });
    }

    /**
     * Отображение результатов
     */
    private void showResults() {
        Platform.runLater(() -> {
            //отображение графиков
            showCharts(maxNEDT, upperBoundNEDT_raspred, start_ch, stop_ch, controller, detectorViewModel);
            resetButton("#10d015", "2. Расчет ЭШРТ [ОК]");
            //вывод текстовой информации
            setText(controller, detectorViewModel);

        });
    }

    /**
     * Отображение полученных графиков
     *
     * @param maxNEDT
     * @param upperBoundNEDT_raspred
     * @param start_ch
     * @param stop_ch
     * @param controller
     * @param detectorViewModel
     */
    private void showCharts(double maxNEDT, int upperBoundNEDT_raspred, int start_ch, int stop_ch, SecondaryController controller, DetectorViewModel detectorViewModel) {

        double upNEDT = (int) ((maxNEDT) * ONE_K) + 5.0D;//верхняя граница НЕДТ
        chartNEDT = controller.getBar_chart_NEDT(App.getLoader(), "vb_netd", 0, upNEDT, (int) upNEDT / 10.0);
        ObservableList<XYChart.Data<String, Number>> dataNEDT_Data = chartNEDT.getData().get(0).getData();
        //перебор данных графика
        //график  недт
        for (XYChart.Data<String, Number> points : dataNEDT_Data) {
            if ((Integer.parseInt(points.getXValue()) < start_ch) || (Integer.parseInt(points.getXValue()) > stop_ch)) {
                points.setYValue(0);
            } else {
                points.setYValue(detectorViewModel.getExperiment().getDataArrayNEDT()[Integer.parseInt(points.getXValue()) - 1] * ONE_K);
            }
        }
        /////////////////////////////
        //график распределения
        chartNEDT_Raspred = controller.getBar_chart_Raspred(App.getLoader(), "vb_netdALL", 0, (int) upperBoundNEDT_raspred + 30, (int) (upperBoundNEDT_raspred / 10));
        ObservableList<XYChart.Data<String, Number>> chartNEDT_Raspred_Data = chartNEDT_Raspred.getData().get(0).getData();
        chartNEDT_Raspred_Data.clear();
        for (int i = 0; i < raspred_otrezki_massiv.length; i++) {
            //создаем данные бара
            XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(i - raspred_otrezki_massiv.length / 2), raspred_otrezki_massiv[i]);
            Text text = new Text("" + raspred_otrezki_massiv[i]);//текст подсказки
            text.setStyle("-fx-font-size: 8pt;");
            TextFlow textFlow = new TextFlow(text);
            textFlow.setTextAlignment(TextAlignment.CENTER);
            chartNEDT_Raspred_Data.addAll(data);//добавляем данные
            Node node = data.getNode();//узел бара
            //обработчик размеров текстового поля
            node.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
                textFlow.setLayoutX(newValue.getCenterX() - text.prefWidth(-1) / 2);
                textFlow.setLayoutY(newValue.getMinY() - 20);
            });
            ((Group) node.getParent()).getChildren().add(textFlow);//добавка текста к бару
        }
    }

    /**
     * Вывод текста на экран
     *
     * @param controller
     * @param detectorViewModel
     */
    private void setText(SecondaryController controller, DetectorViewModel detectorViewModel) {

        String DEFAULT_FORMAT = "0.###";
        NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);

        double valueNEDT = detectorViewModel.getExperiment().getNEDT() * ONE_K;
        String subString_NEDT = FORMATTER.format(valueNEDT);
        controller.lab_NETD.setText(subString_NEDT);
        //расчет дельты сигнала
        double deltaSign = (detectorViewModel.getExperiment().getSredZnach40() - detectorViewModel.getExperiment().getSredZnach30()) * MASHTAB;
        double round = DoubleRounder.round(deltaSign, 3);// округляем
        String subStringDELTA = FORMATTER.format(round);
        controller.lab_deltaSign.setText(subStringDELTA);
        detectorViewModel.setNETD((float) valueNEDT);
        //Заполнение текстового поля
        controller.textArea_shum.appendText("\nЭШРТ по ФПУ : " + subString_NEDT + " мК.");
        obrabotkaPKG(detectorViewModel);
    }

    /**
     * Обработка пригодности
     */
    private void obrabotkaPKG(DetectorViewModel detectorViewModel) {
        int countShumyashih = 0;
        boolean flaf_ploho = false;
        for (int i = start_ch - 1; i < stop_ch; i++) {
            if ((detectorViewModel.getExperiment().getDataArrayNEDT()[i]) > (detectorViewModel.getExperiment().getBrakCHannelCount())) {
                String format = String.format("\nНайден шумящий канал [№ %d : %.3f мК].", (i + 1), detectorViewModel.getExperiment().getDataArrayNEDT()[i] * ONE_K);
                controller.textArea_shum.appendText(format);
                countShumyashih++;
            }
        }
        if (detectorViewModel.getExperiment().getNEDT() > detectorViewModel.getExperiment().getBrakFPUCount()) {
            flaf_ploho = true;
            String format = String.format("\nФПУ не пригоден!!!!!");
            controller.textArea_shum.appendText(format);
        }
        if (countShumyashih == 0) {
            controller.lab_NEDTSootv.setTextFill(Paint.valueOf("#10d015"));
            controller.lab_NEDTSootv.setText("Да");
        } else {
            controller.lab_NEDTSootv.setTextFill(Paint.valueOf("#e81010"));
            controller.lab_NEDTSootv.setText("Нет [" + countShumyashih + "]");
        }
        if (flaf_ploho) {
            controller.lab_NEDTSootv.setText("!Нет!");
            controller.lab_NEDTSootv.setTextFill(Paint.valueOf("#e81010"));
        }
    }

    /**
     * обработка кнопки
     *
     * @param color
     * @param Text
     */
    private void resetButton(String color, String Text) {
        controller.getBut_start_NEDT().setStyle("-fx-background-color: " + color);
        controller.getBut_start_NEDT().setText(Text);
    }

    /**
     * Установка ссылок на графики
     *
     * @param barChart_netd
     * @param barChart_netd_all
     */
    public void setChart(BarChart<String, Number> barChart_netd, BarChart<String, Number> barChart_netd_all) {
        this.chartNEDT = barChart_netd;
        this.chartNEDT_Raspred = barChart_netd_all;

    }

    /**
     * Сохраняем полученные данные
     */
    private void saveExpData() {
        currentExp.setBrakCHannelCount(brak_po_kanalu_NEDT);
        currentExp.setBrakFPUCount(brak_po_fpu_NEDT);
        currentExp.setRaspredMap(raspredMap);
        currentExp.setNEDT(FINAL_NEDT);
        currentExp.setDataArrayNEDT(dataArrayNEDT);
        currentExp.setCountDeselPixel(countDeselPixel);
        currentExp.setCountDeselPixelInLine(maxCountDeselPixelInLine);
        detectorViewModel.setExperiment(currentExp);
    }
}


