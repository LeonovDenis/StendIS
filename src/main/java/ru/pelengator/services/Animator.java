package ru.pelengator.services;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;
import ru.pelengator.model.Frame;
import ru.pelengator.utils.StatisticsUtils;

import static ru.pelengator.PropFile.MASHTAB;
import static ru.pelengator.utils.Utils.getChNumber;

/**
 * Отрисовка главного графика+доп функции
 */
public class Animator extends ScheduledService<Void> {
    //Крайние значения
    private transient IntegerProperty start_channel = new SimpleIntegerProperty(1);
    private transient IntegerProperty end_channel = new SimpleIntegerProperty(288);

    private static SecondaryController controller; //ссылка на контроллер
    private static DetectorViewModel det;//ссылка на детектор

    private static long lasID = 0;

    private static long frameCounter = 0L;//счетчик отображенных кадров
    private static Frame normalframe;//эталонный кадр для темнового тока
    private static boolean fl_normaliz_chart = false;//флаг темнового
    private static int yValueInMV = 0;//значение в мВ
    private static int yValueRabbit = 0;//значение в зайцах
    private static ObservableList<XYChart.Data<String, Number>> dataList;
    private static BooleanProperty tab_exp = new SimpleBooleanProperty(true);


    private long pointCounter = 0L; //счетчик точек
    private float max = Float.MIN_VALUE;
    private float min = Float.MAX_VALUE;
    private float summ = 0f;
    private float mean = 0f;// среднее значение
    private StatisticsUtils utility1 = new StatisticsUtils();
    private StatisticsUtils utility2 = new StatisticsUtils();
    private StatisticsUtils utility3 = new StatisticsUtils();
    private StatisticsUtils utility4 = new StatisticsUtils();
    private StatisticsUtils utility = new StatisticsUtils();

    private static byte outMode = (byte) 15;//параметр отображения комбинации видеовыходов

    /**
     * Конструктор
     *
     * @param controller        ссылка
     * @param detectorViewModel ссылка
     */
    public Animator(SecondaryController controller, DetectorViewModel detectorViewModel) {
        this.controller = controller;
        this.det = detectorViewModel;
        //биндим края графика
        start_channel.bind(detectorViewModel.first_chProperty());
        end_channel.bind(detectorViewModel.last_chProperty());
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                Frame cloneFrame = DetectorViewModel.getMyFrame().clone();//клонируем кадр
                //Выход если ничего нет
                if (cloneFrame == null || controller.getBarChart_Exist() == null) {
                    return null;
                }
                //проверяем показывали ли этот кадр
                if (lasID == cloneFrame.getId()) {
                    return null;//выходим из задания
                }

                lasID = cloneFrame.getId();//запоменаем прочитанный ID кадра
                //Обработка эталонного кадра
                if (fl_normaliz_chart && normalframe == null) {//если флаг на нормал и нормал кадр не существет то клонируем
                    normalframe = cloneFrame;
                    return null;
                } else if (!fl_normaliz_chart && normalframe != null) {//если флаг на норм и нормал кадр существуют то чистим
                    normalframe = null;
                }
                //в зависимости от таба заполняем нужный график
                if (tab_exp.getValue()) {
                    dataList = controller.getBarChart_Exist().getData().get(0).getData();
                } else {
                    dataList = controller.getBarChart_Temp().getData().get(0).getData();
                }

                for (XYChart.Data<String, Number> data : dataList) {
                    int xNumber = Integer.parseInt(data.getXValue());
                    //берем старое значение Y
                    int yValue = (int) data.getYValue();
///////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //проверяем границы, если не попали, то значение в 0//отработка комбинации видеовыходов
                    if ((xNumber < getStart_channel()) || (xNumber > getEnd_channel()) ||
                            (((outMode & 0b1) == 0) && ((getChNumber(xNumber-1)) == 3)) || (((outMode & 0b10) == 0) && ((getChNumber(xNumber-1)) == 2)) ||
                            (((outMode & 0b100) == 0) && ((getChNumber(xNumber-1)) == 1)) || (((outMode & 0b1000) == 0) && ((getChNumber(xNumber-1)) == 0))) {
                        data.setYValue(0);
///////////////////////////////////////////////////////////////////////////////////////////////////////////
                    } else {//
                        //считаем новое значение Y
                        if (normalframe == null) {//для обычного графика
                            yValueInMV = (int) (cloneFrame.getData()[xNumber - 1] * MASHTAB);
                            yValueRabbit = cloneFrame.getData()[xNumber - 1];
                        } else {//для темнового графика
                            yValueInMV = (int) ((cloneFrame.getData()[xNumber - 1] - normalframe.getData()[xNumber - 1]) * MASHTAB);
                            yValueRabbit = cloneFrame.getData()[xNumber - 1] - normalframe.getData()[xNumber - 1];
                        }
                        //учитываем статистику
                        addtoutility(yValueRabbit, xNumber);
                        //если новое значение отличается от старого, то меняем его
                        if (yValue != yValueInMV) {
                            data.setYValue(yValueInMV);
                        }
                    }
                }
                setStatData();//устанавливаем статистику
                resetStat();//сбрасываем переменные статистики
                return null;
            }
        };
    }

    /**
     * Собираем поканальную статистику
     *
     * @param yValueInMV милливольты
     * @param i номер канала+1
     */
    private void addtoutility(int yValueInMV, int i) {
        i = getChNumber(i-1);
        if (i == 0) {
            utility1.addValue(yValueInMV);
        }
        if (i == 1) {
            utility2.addValue(yValueInMV);
        }
        if (i == 2) {
            utility3.addValue(yValueInMV);
        }
        if (i == 3) {
            utility4.addValue(yValueInMV);
        }
        utility.addValue(yValueInMV);
    }

    /**
     * сброс статистики
     */
    private void resetStat() {
        utility1.reset();
        utility2.reset();
        utility3.reset();
        utility4.reset();
        utility.reset();
    }

    /**
     * установка стат параметров для временного графика
     */
    private void setStatData() {

        det.setOUT1_mid((float) ((utility1.getMean())) * MASHTAB);
        det.setOUT1_max((float) ((utility1.getMax())) * MASHTAB);
        det.setOUT1_min((float) ((utility1.getMin())) * MASHTAB);
        det.setOUT1_SKO((float) utility1.getStdDev() * MASHTAB);

        det.setOUT2_mid((float) ((utility2.getMean())) * MASHTAB);
        det.setOUT2_max((float) ((utility2.getMax())) * MASHTAB);
        det.setOUT2_min((float) ((utility2.getMin())) * MASHTAB);
        det.setOUT2_SKO((float) utility2.getStdDev() * MASHTAB);

        det.setOUT3_mid((float) ((utility3.getMean())) * MASHTAB);
        det.setOUT3_max((float) ((utility3.getMax())) * MASHTAB);
        det.setOUT3_min((float) ((utility3.getMin())) * MASHTAB);
        det.setOUT3_SKO((float) utility3.getStdDev() * MASHTAB);


        det.setOUT4_mid((float) ((utility4.getMean())) * MASHTAB);
        det.setOUT4_max((float) ((utility4.getMax())) * MASHTAB);
        det.setOUT4_min((float) ((utility4.getMin())) * MASHTAB);
        det.setOUT4_SKO((float) utility4.getStdDev() * MASHTAB);

        det.setFrame_mid((float) ((utility1.getMean() + utility2.getMean() + utility3.getMean() + utility4.getMean()) * MASHTAB / 4.0));
        det.setFrame_max((float) ((utility1.getMax() + utility2.getMax() + utility3.getMax() + utility4.getMax()) * MASHTAB / 4.0));
        det.setFrame_min((float) ((utility1.getMin() + utility2.getMin() + utility3.getMin() + utility4.getMin()) * MASHTAB / 4.0));
        det.setFrame_SKO((float) ((utility1.getStdDev() + utility2.getStdDev() + utility3.getStdDev() + utility4.getStdDev()) * MASHTAB / 4.0));

    }

    public int getStart_channel() {
        return start_channel.get();
    }

    public int getEnd_channel() {
        return end_channel.get();
    }

    public void setFl_normaliz_chart(boolean fl_normaliz_chart) {
        this.fl_normaliz_chart = fl_normaliz_chart;
    }

    public void setTab_exp(boolean tab_exp) {
        this.tab_exp.set(tab_exp);
    }

    public static void setOutMode(byte outMode) {
        Animator.outMode = outMode;
    }

}


