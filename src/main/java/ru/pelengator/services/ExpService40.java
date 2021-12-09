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
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;
import ru.pelengator.model.Experiment;
import ru.pelengator.model.Frame;
import ru.pelengator.utils.StatisticsUtils;


import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;


import static ru.pelengator.PropFile.*;
import static ru.pelengator.utils.Utils.toArrayList;


/**
 * Сервис сбора данных при 40
 */
public class ExpService40 extends Service<Void> {

    private static SecondaryController controller;
    private static DetectorViewModel detectorViewModel;

    private static long lasID = -1L;
    private static int[][] dataArray40;
    private static double sredZnach40;
    public double[] dataArraySred_40;
    public StatisticsUtils[] dataArrayStat_40;
    ObservableList<Frame> frameArrayList;
    private Experiment currentExp;
    private int frame_number;
    private int start_ch;
    private int stop_ch;
    private int countChannel;
    private BarChart<String, Number> chartMid40;

    public ExpService40(SecondaryController controller, DetectorViewModel detectorViewModel) {
        this.controller = controller;
        this.detectorViewModel = detectorViewModel;
    }

    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                currentExp = detectorViewModel.getExperiment();
                updateMessage("Старт Сервиса расчета 40 градусов");
                updateProgress(0.0, 1);
                ///инициализация данных эксперимента
                updateMessage("Инициализация параметров");
                initParams();
                updateMessage("Задание на выборку: " + frame_number + " значений. Каналы: [" + start_ch + "|" + stop_ch + "]");
                //набор массива кадров
                if (detectorViewModel.isRELOADCHARTS()) {//в случае загрузки из БД
                    if (currentExp==null||currentExp.getFrameArrayList40() == null) {
                        updateMessage("Нет кадров в БД");
                        updateProgress(1, 1);
                        Platform.runLater(() -> {
                            detectorViewModel.setRELOADCHARTS(false);
                        });
                        cancelled();
                        return null;
                    }
                    frameArrayList = FXCollections.observableArrayList(currentExp.getFrameArrayList40());
                    updateMessage("Кадры набраны из БД");
                    updateProgress(0.9D, 1);
                } else {//В случае эксперимента
                    initParams();
                    while (frameArrayList.size() < frame_number) {
                        Frame cloneFrame = DetectorViewModel.getMyFrame().clone();//клонируем кадр
                        if (lasID == cloneFrame.getId() || cloneFrame.getData() == null) {
                            continue;
                        } else {
                            frameArrayList.add(cloneFrame);
                            lasID = cloneFrame.getId();
                        }
                        updateMessage("Жду...набор кадров " + "[" + frameArrayList.size() + "/" + frame_number + "]");
                        updateProgress(0.9D * frameArrayList.size() / (double) frame_number, 1.0);
                    }
                }
                updateMessage("Набрал кадры...." + frameArrayList.size() + " кадров");
                //набираем массив данных
                takeData();
                updateMessage("Набрал массив ...." + frameArrayList.size() + " кадров");
                takeDataArraySred40();
                takeSredZnach();
                updateMessage("Рассчитал среднее значение ...." + frameArrayList.size() + " кадров");
                updateProgress(0.95, 1);
                saveExpData();
                showResults();
                if (currentExp.getShum() != 0) {
                    updateMessage("Есть данные для 30 и 40 градусов ...");
                } else {
                    updateMessage("Готов");
                }
                updateProgress(1, 1);
                loadnextBDdata();//при загрузке из бд грузит следующий сервис
                showStatus();
                return null;
            }
            private void showStatus() {
                for (ImageView im:
                        controller.getListView() ) {
                    detectorViewModel.changeIv(im);
                }
            }

            /**
             * Показ результатов эксперимента
             */
            @Override
            protected void succeeded() {
                super.succeeded();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#10d015", "2. Накопление при 40 [ОК]");//добавка

            }

            /**
             * Обработка отмены сервиса
             */
            @Override
            protected void cancelled() {
                super.cancelled();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#FFD700", "2. Накопление при 40");//добавка
                Platform.runLater(() -> {
                    //проверка разблокировки кнопки
                    if (controller.getBut_start_NEDT().isDisabled()) {
                        controller.getBut_start_NEDT().setDisable(false);
                    }
                });
            }

            /**
             * Печать ошибки. Разблокировка кнопок
             */
            @Override
            protected void failed() {
                super.failed();
                detectorViewModel.getMain_chart_service().restart();
                resetButton("#FFD700", "2. Накопление при 40");//добавка
                Platform.runLater(() -> {
                    //проверка разблокировки кнопки
                    if (controller.getBut_start_NEDT().isDisabled()) {
                        controller.getBut_start_NEDT().setDisable(false);
                    }
                });

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
                        controller.getBut_start_NEDT().fire();
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Сброс кнопки
     *
     * @param color
     * @param Text
     */
    private void resetButton(String color, String Text) {
        controller.getBut_start_40().setStyle("-fx-background-color: " + color);
        controller.getBut_start_40().setText(Text);
    }

    /**
     * Инициализация параметров
     */
    private void initParams() {
        start_ch = detectorViewModel.getFirstChanExp();
        stop_ch = detectorViewModel.getLastChanExp();
        frame_number = detectorViewModel.getFrameCountExp();
        countChannel = stop_ch - start_ch + 1;
        ////////////////////////инициализация массивов
        dataArray40 = new int[CHANNELNUMBER][frame_number];
        dataArraySred_40 = new double[CHANNELNUMBER];
        dataArrayStat_40 = new StatisticsUtils[CHANNELNUMBER];
        frameArrayList = FXCollections.observableArrayList();
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
     * Расчет среднего значения по ФПУ
     */
    private void takeSredZnach() {
        double summ = 0D;
        for (int i = start_ch - 1; i < stop_ch; i++) {
            summ = summ + dataArraySred_40[i];
        }
        sredZnach40 = summ / countChannel;
    }

    /**
     * Расчет среднего значения по каналам
     */
    private void takeDataArraySred40() {
        //пробегаем по каналам
        for (int i = 0; i < CHANNELNUMBER; i++) {
            StatisticsUtils statisticsUtils = new StatisticsUtils();
            //пробегаем по кадрам
            for (int j = 0; j < frame_number; j++) {
                statisticsUtils.addValue(dataArray40[i][j]);
            }
            dataArrayStat_40[i] = statisticsUtils;
        }
        //пробегаем по каналам
        for (int i = 0; i < CHANNELNUMBER; i++) {
            dataArraySred_40[i] = dataArrayStat_40[i].getMean();
        }
    }

    /**
     * Отображение результатов
     */
    private void showResults() {
        Platform.runLater(() -> {
            //отображение графика
            showChart40(start_ch, stop_ch, detectorViewModel);
            //вывод текстовой информации
            setText(controller, detectorViewModel);
            //проверка разблокировки кнопки
            if (controller.getBut_start_NEDT().isDisabled()) {
                controller.getBut_start_NEDT().setDisable(false);
            }
        });
    }

    /**
     * Показ графика на 40
     *
     * @param start_ch
     * @param stop_ch
     * @param detectorViewModel
     */
    private void showChart40(int start_ch, int stop_ch, DetectorViewModel detectorViewModel) {
        ObservableList<XYChart.Data<String, Number>> chartMid40_Data = chartMid40.getData().get(1).getData();
        for (XYChart.Data<String, Number> points : chartMid40_Data) {
            if ((Integer.parseInt(points.getXValue()) < start_ch) || (Integer.parseInt(points.getXValue()) > stop_ch)) {
                points.setYValue(0);
            } else {
                points.setYValue(detectorViewModel.getExperiment().getDataArraySred_40()[Integer.parseInt(points.getXValue()) - 1] * MASHTAB);
            }
        }
    }

    /**
     * Отображение текста в окне
     *
     * @param controller
     * @param detectorViewModel
     */
    private void setText(SecondaryController controller, DetectorViewModel detectorViewModel) {
        String DEFAULT_FORMAT = "0.###";
        NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);

        double valueSredZnach = detectorViewModel.getExperiment().getSredZnach40() * MASHTAB;
        detectorViewModel.setSred40((float) valueSredZnach);
        String subStringSred = FORMATTER.format(valueSredZnach);
        controller.lab_sred40.setText(subStringSred);
        controller.textArea_shum.appendText("\nСреднее значение при 40 С: " + subStringSred + " мВ." +
                "\n///////////////////////////////////////////////");
    }


    public void setChart(BarChart<String, Number> barChart_30_40) {
        this.chartMid40 = barChart_30_40;
    }

    /**
     * Сохраняем полученные данные
     */
    private void saveExpData() {
        currentExp.setDataArray40(dataArray40);
        currentExp.setDataArraySred_40(dataArraySred_40);
        currentExp.setSredZnach40(sredZnach40);
        currentExp.setFrameArrayList40(toArrayList(frameArrayList));
        if(!detectorViewModel.isRELOADCHARTS()){//в случае работы с кадром не из базы - записываем дату
            currentExp.setEndExpDate(new Timestamp(System.currentTimeMillis()));
        }
        detectorViewModel.setExperiment(currentExp);
        saveOrderData(currentExp);
    }

    /**
     * Сохранение эксперимента для отчета
     * @param exp
     */
    private void saveOrderData(Experiment exp) {
        if (exp.getDir().equals("Прямое") && exp.getMode().equals("ВЗН")) {
            detectorViewModel.getOrder().setVZN_pr(exp);
        } else if (exp.getDir().equals("Обратное") && exp.getMode().equals("ВЗН")) {
            detectorViewModel.getOrder().setVZN_ob(exp);
        //} else if (exp.getMode().equals("4-Bypass")) {
        } else if (exp.getMode().endsWith("-Bypass")) {
            detectorViewModel.getOrder().setBPS(exp);
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
                dataArray40[i][j] = data[i];
            }
        }
    }

}


