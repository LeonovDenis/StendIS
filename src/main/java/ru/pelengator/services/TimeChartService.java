package ru.pelengator.services;

import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;

import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.pelengator.PropFile.MINUTA;
import static ru.pelengator.PropFile.ONE_K;

/**
 * Сервис построения временного графика
 */
public class TimeChartService extends ScheduledService<Void> {

    private static int WINDOW_SIZE;
    private static SecondaryController controller;
    private static Date now;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss:SS");

    private static FloatProperty srednee = new SimpleFloatProperty(0);
    private static FloatProperty max = new SimpleFloatProperty(0);
    private static FloatProperty min = new SimpleFloatProperty(0);
  //  private static FloatProperty sko = new SimpleFloatProperty(0);


    public TimeChartService(SecondaryController controller, DetectorViewModel detectorViewModel, int pause) {
        this.controller = controller;
        this.WINDOW_SIZE = MINUTA * ONE_K / pause;
        srednee.bind(detectorViewModel.frame_midProperty());
        max.bind(detectorViewModel.frame_maxProperty());
        min.bind(detectorViewModel.frame_minProperty());
   //     sko.bind(detectorViewModel.frame_SKOProperty());
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (controller.getLineChart_time() != null) {
                    now = new Date();//создаем дату
                    String str_date = simpleDateFormat.format(now);
                    XYChart.Data<String, Number> data_sred = new XYChart.Data<>(str_date, getSrednee());//создаем данные
                    XYChart.Data<String, Number> data_max = new XYChart.Data<>(str_date, getMax());//создаем данные
                    XYChart.Data<String, Number> data_min = new XYChart.Data<>(str_date, getMin());//создаем данные
                 //   XYChart.Data<String, Number> data_sko = new XYChart.Data<>(str_date, getSko());//создаем данные

                    Platform.runLater(() -> {
                        controller.getLineChart_time().getData().get(0).getData().add(data_sred);//добавляем данные в график
                        controller.getLineChart_time().getData().get(1).getData().add(data_max);//добавляем данные в график
                        controller.getLineChart_time().getData().get(2).getData().add(data_min);//добавляем данные в график
                //        controller.getLineChart_time().getData().get(3).getData().add(data_sko);//добавляем данные в график

                        //при переполнении графика удаляем первое значение
                        if (controller.getLineChart_time().getData().get(0).getData().size() > WINDOW_SIZE) {
                            controller.getLineChart_time().getData().get(0).getData().remove(0);
                            controller.getLineChart_time().getData().get(1).getData().remove(0);
                            controller.getLineChart_time().getData().get(2).getData().remove(0);
                 //           controller.getLineChart_time().getData().get(3).getData().remove(0);
                        }
                    });
                }
                return null;
            }
        };
    }

    public static float getSrednee() {
        return srednee.get();
    }

    public static float getMax() {
        return max.get();
    }

    public static float getMin() {
        return min.get();
    }

 /**   public static float getSko() {
        return sko.get();
    }*/
}
