package ru.pelengator.services;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ru.pelengator.App;
import ru.pelengator.DetectorViewModel;

import java.awt.*;
import java.util.ArrayList;

/**
 * Сервис сброса эксперимента
 */
public class ExpReset extends ScheduledService<Void> {

    private DetectorViewModel detectorViewModel;//ссылка на DVM
    private ArrayList<Label> list=new ArrayList<>();

    public ExpReset(DetectorViewModel detectorViewModel) {
        this.detectorViewModel = detectorViewModel;

    }

    /**
     * Создаём список полей
     * @param detectorViewModel
     */
    private void getListt(DetectorViewModel detectorViewModel) {
        list.add(detectorViewModel.getController().lab_sco30);
        list.add(detectorViewModel.getController().lab_sco30Sootv);
        list.add(detectorViewModel.getController().lab_sred30);
        list.add(detectorViewModel.getController().lab_sred40);
        list.add(detectorViewModel.getController().lab_deltaSign);
        list.add(detectorViewModel.getController().lab_NETD);
        list.add(detectorViewModel.getController().lab_NEDTSootv);
        list.add(detectorViewModel.getController().lab_countDeselPixel);
        list.add(detectorViewModel.getController().lab_countDeselPixel_sootv);
        list.add(detectorViewModel.getController().lab_countDeselPixelInLine);
        list.add(detectorViewModel.getController().lab_countDeselPixelInLine_sootv);
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                getListt(detectorViewModel);
                updateMessage("Сброс эксперимента");
                updateProgress(0.1, 1);
                //обновление эксперимента
                detectorViewModel.setExperiment(null);
                updateProgress(0.4, 1);
                //очистка графиков
                Platform.runLater(() -> detectorViewModel.getController().setChartsToReset(App.getLoader()));
                updateProgress(0.6, 1);
                //очистка текстовых полей
                Platform.runLater(() -> {
                    for (Label node:
                    list) {
                        node.setText("---");
                        node.setTextFill(Color.BLACK);
                    }
                });
                updateMessage("Нет данных по эксперименту ...");
                updateProgress(1, 1);
                cancel();
                return null;
            }
        };
    }
}
