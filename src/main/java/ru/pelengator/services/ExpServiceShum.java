package ru.pelengator.services;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import ru.pelengator.App;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;
import ru.pelengator.model.Experiment;
import ru.pelengator.model.Frame;
import ru.pelengator.utils.StatisticsUtils;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static ru.pelengator.PropFile.*;
import static ru.pelengator.utils.Utils.toArrayList;

/**
 * Сервис сбора данных при 30 градусх и подсчета шума
 */
public class ExpServiceShum extends Service<Void> {

    private static SecondaryController controller;
    private static DetectorViewModel detectorViewModel;

    private static long lasID = -1L;
    private static int[][] dataArray30;
    private static byte[] matrix;
    private static double shum;
    private static double sredZnach30;

    private double[] dataArraySred_30;
    private double maxSKO = 0d;
    private double maxSred = 0d;
    private Experiment currentExp;
    public double[] dataArraySKO30;
    public static StatisticsUtils[] dataArrayStat_30;
    static ObservableList<Frame> frameArrayList;
    private static int frame_number;
    private static int start_ch;
    private static int stop_ch;
    private static int countChannel;
    private static BarChart<String, Number> chartSKO;
    private static BarChart<String, Number> chartMid30;
    private static double brakTimes;

    public ExpServiceShum(SecondaryController controller, DetectorViewModel detectorViewModel) {
        this.controller = controller;
        this.detectorViewModel = detectorViewModel;
    }

    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                //создаем эксперимент
                currentExp = new Experiment(detectorViewModel.getDetectorName(), detectorViewModel.getNumbersDevises(),
                        detectorViewModel.getTesterFIO(), new Timestamp(System.currentTimeMillis()));
                updateMessage("Старт Сервиса расчета шума");
                updateProgress(0.0, 1);
                updateMessage("Сброс переменных");
                //сброс данных
                resetParams();
                ///инициализация данных эксперимента
                updateMessage("Инициализация данных");
                initParams();
                updateMessage("Задание на выборку: " + frame_number + " значений. Каналы: [" + start_ch + "|" + stop_ch + "]");
                //набор массива кадров
                if (detectorViewModel.isRELOADCHARTS()) {//в случае загрузки из БД
                    if (currentExp.getFrameArrayList30() == null) {
                        updateMessage("Нет кадров в БД");
                        updateProgress(1, 1);
                        return null;
                    }
                    frameArrayList = FXCollections.observableArrayList(currentExp.getFrameArrayList30());
                    updateMessage("Кадры набраны из БД");
                    updateProgress(0.9D, 1);
                } else {//В случае эксперимента
                    while (frameArrayList.size() < frame_number) {
                        Frame cloneFrame = DetectorViewModel.getMyFrame().clone();//клонируем кадр
                        if (lasID == cloneFrame.getId() || cloneFrame.getData() == null) {
                            continue;
                        } else {
                            frameArrayList.add(cloneFrame);
                            lasID = cloneFrame.getId();
                        }
                        updateMessage("Жду...набор кадров " + "[" + frameArrayList.size() + "/" + frame_number + "]");
                        updateProgress(0.9D * frameArrayList.size() / (double) frame_number, 1);
                    }
                }
                updateMessage("Набрал кадры...." + frameArrayList.size() + " кадров");
                //набираем массив данных
                takeData();
                updateMessage("Набрал массив ...." + frameArrayList.size() + " кадров");
                //считаем СКО
                takeSKO();
                updateMessage("Рассчитал СКО ...." + frameArrayList.size() + " кадров");
                //считаем шум
                CalculateShum();
                updateMessage("Рассчитал ШУМ ...." + frameArrayList.size() + " кадров");
                //сохраняем эксперимент
                saveExpData();
                updateMessage("Сохранил данные эксперимента ...." + frameArrayList.size() + " кадров");
                updateMessage("Готов");
                updateProgress(0.98, 1);
                //Отображаем полученные расчеты
                showResults();
                return null;
            }

            /**
             * Отработка успешного завершения эксперимента
             */
            @Override
            protected void succeeded() {
                super.succeeded();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#10d015", "1. Накопление при 30 [ОК]");

            }

            /**
             * Обработка отмены сервиса
             */
            @Override
            protected void cancelled() {
                super.cancelled();
                System.err.println("cancelled!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Шум не посчитался....Ошибка");
                alert.setTitle("Отмена сервиса");
                alert.initModality(Modality.WINDOW_MODAL);
                alert.show();
            }

            /**
             * Печать ошибки. Разблокировка кнопок
             */
            @Override
            protected void failed() {
                super.failed();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Шум не посчитался....Ошибка");
                alert.setTitle("Ошибка сервиса");
                alert.setGraphic(setErrorMSG(this.getException()));
                alert.initModality(Modality.WINDOW_MODAL);
                alert.show();
                controller.getBut_start_40().setDisable(false);
                resetButton("#10d015", "1. Накопление при 30 [ОК]");
            }
        };
    }

    /**
     * Инициализация параметров
     */
    private void initParams() {
        start_ch = detectorViewModel.getFirstChanExp();
        stop_ch = detectorViewModel.getLastChanExp();
        frame_number = detectorViewModel.getFrameCountExp();
        matrix = detectorViewModel.getMatrix();
        countChannel = stop_ch - start_ch + 1;
        brakTimes = Double.parseDouble(controller.tex_brak.getText());
        ////////////////////////кол. канал////кол кадров///
        dataArray30 = new int[CHANNELNUMBER][frame_number];
        dataArraySKO30 = new double[CHANNELNUMBER];
        dataArrayStat_30 = new StatisticsUtils[CHANNELNUMBER];
        dataArraySred_30 = new double[CHANNELNUMBER];
        frameArrayList = FXCollections.observableArrayList();
    }

    /**
     * Сброс переменных
     */
    private void resetParams() {
        maxSKO = 0;
        maxSred = 0;
        frameArrayList = null;
        dataArray30 = null;
        dataArraySKO30 = null;
        dataArrayStat_30 = null;
        dataArraySred_30 = null;
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
     * Расчет значения
     * СКО и скреднего значения
     */
    private void CalculateShum() {
        double summ = 0d;
        double summSred = 0d;
        for (int i = start_ch - 1; i < stop_ch; i++) {
            summ += dataArraySKO30[i];
            summSred += dataArrayStat_30[i].getMean();
            dataArraySred_30[i] = dataArrayStat_30[i].getMean();
            if (dataArraySKO30[i] > maxSKO) {
                maxSKO = dataArraySKO30[i];
            }
            if (dataArrayStat_30[i].getMean() > maxSred) {
                maxSred = dataArrayStat_30[i].getMean();
            }
        }
        shum = summ / countChannel;
        sredZnach30 = summSred / countChannel;
    }

    /**
     * Набираем массив СКО
     */
    private void takeSKO() {
        //пробегаем по каналам
        for (int i = 0; i < CHANNELNUMBER; i++) {
            StatisticsUtils statisticsUtils = new StatisticsUtils();
            //пробегаем по кадрам
            for (int j = 0; j < frame_number; j++) {
                statisticsUtils.addValue(dataArray30[i][j]);
            }
            dataArrayStat_30[i] = statisticsUtils;
        }
        //пробегаем по каналам
        for (int i = 0; i < CHANNELNUMBER; i++) {
            dataArraySKO30[i] = dataArrayStat_30[i].getStdDev();
        }
    }

    /**
     * Набираем массив данных из кадров
     */
    private void takeData() {
        for (int j = 0; j < frame_number; j++) {
            int[] data = frameArrayList.get(j).getData();
            //пробегаем по каналам
            for (int i = 0; i < CHANNELNUMBER; i++) {
                dataArray30[i][j] = data[i];
            }
        }
    }

    /**
     * Отображение результатов
     */
    private void showResults() {

        Platform.runLater(() -> {
            //отображение графиков
            showChartSKO(maxSKO, start_ch, stop_ch, controller, detectorViewModel);
            //вывод текстовой информации
            setText(start_ch, stop_ch, controller, detectorViewModel);
            //проверка разблокировки кнопки
            if (controller.getBut_start_40().isDisabled()) {
                controller.getBut_start_40().setDisable(false);
            }
        });
    }

    /**
     * Показ графика ско и 30 градусов
     *
     * @param maxSKO     верхняя граница ско
     * @param start_ch
     * @param stop_ch
     * @param controller
     */
    private void showChartSKO(double maxSKO, int start_ch, int stop_ch, SecondaryController controller, DetectorViewModel detectorViewModel) {

        double upSKO = (int) ((maxSKO) * MASHTAB) + 1.0;//расчитываем верхнюю границу графика
        chartSKO = controller.getBar_chart_sko_30(App.getLoader(), "vb_sko30", 0, upSKO, upSKO / 10.0);//создаем новый график
        ObservableList<XYChart.Data<String, Number>> dataSKO = chartSKO.getData().get(0).getData();//ссылка на первую серию и ее данные
        //ссылка на данные графика при 30 градусах
        ObservableList<XYChart.Data<String, Number>> dataMid = chartMid30.getData().get(0).getData();//ссылка на первую серию и ее данные
        //перебор данных графика СКО
        for (XYChart.Data<String, Number> points : dataSKO) {
            if ((Integer.parseInt(points.getXValue()) < start_ch) || (Integer.parseInt(points.getXValue()) > stop_ch)) {
                points.setYValue(0);
            } else {
                points.setYValue(detectorViewModel.getExperiment().getDataArraySKO30()[Integer.parseInt(points.getXValue()) - 1] * MASHTAB);
            }
        }
        //перебор данных графика 30-40
        for (XYChart.Data<String, Number> points : dataMid) {
            if ((Integer.parseInt(points.getXValue()) < start_ch) || (Integer.parseInt(points.getXValue()) > stop_ch)) {
                points.setYValue(0);
            } else {
                points.setYValue(detectorViewModel.getExperiment().getDataArraySred_30()[Integer.parseInt(points.getXValue()) - 1] * MASHTAB);
            }
        }
    }

    /**
     * Вывод текста в окно и проверка на шумящие каналы
     *
     * @param start_ch
     * @param stop_ch
     * @param controller
     * @param detectorViewModel
     */
    private void setText(int start_ch, int stop_ch, SecondaryController controller, DetectorViewModel detectorViewModel) {

        String DEFAULT_FORMAT = "0.###";
        NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);

        double shumValueMV = detectorViewModel.getExperiment().getShum() * MASHTAB;
        double sredZnach30ValueMV = detectorViewModel.getExperiment().getSredZnach30() * MASHTAB;

        detectorViewModel.setSco30((float) shumValueMV);
        detectorViewModel.setSred30((float) sredZnach30ValueMV);

        String subStringShum = FORMATTER.format(shumValueMV);//форматируем значение
        String subStringSred = FORMATTER.format(sredZnach30ValueMV);//форматируем значение

        controller.lab_sco30.setText(subStringShum);
        controller.lab_sred30.setText(subStringSred);
        //Заполнение текстового поля
        controller.textArea_shum.appendText("\n///////////////////////////////////////////////" +
                "\nСредний шум(СКО): " + subStringShum + " мВ." +
                "\nСреднee значение при 30 : " + subStringSred + " мВ.");

        int countShumyashih = 0;
        for (int i = start_ch - 1; i < stop_ch; i++) {

            if ((detectorViewModel.getExperiment().getDataArraySKO30()[i]) > (detectorViewModel.getExperiment().getShum() * detectorViewModel.getExperiment().getBrakTimes())) {
                String format = String.format("\nНайден шумящий канал [№ %d : %.3f мВ].", (i + 1), (detectorViewModel.getExperiment().getDataArraySKO30()[i] * MASHTAB));
                controller.textArea_shum.appendText(format);
                countShumyashih++;
            }
        }
        if (countShumyashih == 0) {
            controller.lab_sco30Sootv.setTextFill(Paint.valueOf("#10d015"));
            controller.lab_sco30Sootv.setText("Да");
        } else {
            controller.lab_sco30Sootv.setTextFill(Paint.valueOf("#e81010"));
            controller.lab_sco30Sootv.setText("Нет[" + countShumyashih + "]");
        }
    }

    /**
     * Сброс кнопки
     *
     * @param color
     * @param Text
     */
    private void resetButton(String color, String Text) {
        controller.getBut_start_Shum().setStyle("-fx-background-color: " + color);
        controller.getBut_start_Shum().setText(Text);
    }

    /**
     * установка ссылок графиков
     *
     * @param chartSKO
     * @param chartMid30
     */
    public void setChart(BarChart<String, Number> chartSKO, BarChart<String, Number> chartMid30) {
        this.chartSKO = chartSKO;
        this.chartMid30 = chartMid30;
    }

    /**
     * Сохраняем полученные данные
     */
    private void saveExpData() {
        currentExp.setDataArray30(dataArray30);//копируем массив
        currentExp.setDataArraySred_30(dataArraySred_30);//среднее значение по каналам
        currentExp.setSredZnach30(sredZnach30);//копируем среднее значение сигнала по ФПУ
        currentExp.setDataArraySKO30(dataArraySKO30);//копируем среднее значение ско по каналам
        currentExp.setShum(shum);//копируем итоговое значение шума по ФПУ
        //сохраняем браковочные показатели
        currentExp.setBrakTimes(brakTimes);//параметр брака по шуму на канал
        currentExp.setMatrix(matrix);//матрица деселекции
        currentExp.setFrameArrayList30(toArrayList(frameArrayList));//массив кадров
        //сохраняем параметры стенда
        saveStendParams(currentExp, detectorViewModel);
        //устанавливаем текущий эксперимент
        detectorViewModel.setExperiment(currentExp);
    }

    /**
     * Копируем параметры стенда в эксперимент
     *
     * @param currentExp
     * @param detectorViewModel
     */
    private void saveStendParams(Experiment currentExp, DetectorViewModel detectorViewModel) {
        currentExp.setVr0(detectorViewModel.getVr0());
        currentExp.setVva(detectorViewModel.getVva());
        currentExp.setVu4(detectorViewModel.getVu4());
        currentExp.setVuc(detectorViewModel.getVuc());
        currentExp.settInt(detectorViewModel.gettInt());
        currentExp.setTemp(detectorViewModel.getTemp());
        currentExp.setMode(detectorViewModel.getMode());
        currentExp.setDir(detectorViewModel.getDir());
        currentExp.setCcc(detectorViewModel.getCcc());
    }


    public static int getStart_ch() {
        return start_ch;
    }

    public static int getStop_ch() {
        return stop_ch;
    }


    public static int getFrame_number() {
        return frame_number;
    }

}
