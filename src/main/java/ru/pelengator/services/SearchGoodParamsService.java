package ru.pelengator.services;


import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static ru.pelengator.PropFile.PAUSE;

/**
 * Сервис по поиску наилучших параметров системы
 * В работе
 */
public class SearchGoodParamsService extends ScheduledService<Void> {


    SecondaryController con;
    DetectorViewModel det;

    private ArrayList<Long> list;
    private int VR0;
    private int VVA;
    private int UC;
    private int VU4;

    public SearchGoodParamsService(SecondaryController con, DetectorViewModel det) {
        this.con = con;
        this.det = det;

    }

    @Override
    protected Task<Void> createTask() {


        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                setDefault(600, 0, 5000, 5000); //ставим всё по дефаулту

                updateProgress(0.1, 1);

                findVR0();

                //1. проверяем зашкал. диалог
                alert_Zashkal();

                findVVA();//находим VVA

                updateProgress(0.7, 1);

                //2. устанавливаем ВЗН прямое

                alert_Zashkal();//проверяем зашкал. диалог

                findVVA();//находим VVA
                updateProgress(0.8, 1);

                //3. устанавливаем ВЗН обратное

                alert_Zashkal();//проверяем зашкал. диалог
                findVVA();//находим VVA

                updateProgress(0.9, 1);

                // диалог счастья. Отображаем лучшие значения
                // запись в БД

                updateProgress(1, 1);
                this.cancel();
                return null;
            }
        };
    }

    /**
     * вывод окна и проверка зашкала
     */
    private void alert_Zashkal() {
    }

    /**
     * подбираем напряжения на VVA
     * * от 0 до 2В. Деф 0В
     */
    private void findVVA() {
    }

    /**
     * подбираем напряжения на VR0
     * * от 0 до 2В. Деф 0В
     */
    private void findVR0() {
        list.add(det.lastID());
        for (int i = this.VR0; i < 2000; i = i + 10) {
            det.VR0Tiped(i);
            det.setVr0(i);
            pause();
           con.getBut_start_Shum().fire();
            do {
                pause();
            } while (det.getExp_shum_service().getState() == State.RUNNING || det.getExp_40_service().getState() == State.RUNNING ||
                    det.getExp_NEDT_service().getState() == State.RUNNING);
            pause();
            det.saveFileExp();
            pause();
        }
        list.add(det.lastID());

        confirm();

        int k = this.VR0;
        for (long i = (list.get(0) + 1); i < list.get(1) + 1; i++) {

            det.VR0Tiped(k);
            det.setVr0(k);
            k = k + 10;
            det.loadFileExp(i);
            do {
                pause();
            } while (det.getExp_shum_service().getState() == State.RUNNING || det.getExp_40_service().getState() == State.RUNNING ||
                    det.getExp_NEDT_service().getState() == State.RUNNING);
            pause();
            con.getBut_start_40().fire();
            do {
                pause();
            } while (det.getExp_shum_service().getState() == State.RUNNING || det.getExp_40_service().getState() == State.RUNNING ||
                    det.getExp_NEDT_service().getState() == State.RUNNING);

            con.getBut_start_NEDT().fire();
            do {
                pause();
            } while (det.getExp_shum_service().getState() == State.RUNNING || det.getExp_40_service().getState() == State.RUNNING ||
                    det.getExp_NEDT_service().getState() == State.RUNNING);
            det.updateFileExp();
        }

    }

    private void confirm() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);//создаем окно подтверждения
        alert.setTitle("40?");
        alert.setContentText("Подтвердите наличие 40 градусов на АЧТ");
        //создаем кнопки "да" и "нет"
        ButtonType buttonTypeOne = new ButtonType("Да");
        ButtonType buttonTypeCancel = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);//додаем кнопки к самому окну подтверждения
        Optional<ButtonType> result = alert.showAndWait();//вызываем окно подтверждения
        if (result.get() == buttonTypeOne) {
            return;
        } else {
            //ignore
        }

    }

    private void pause() {
        try {
            TimeUnit.MILLISECONDS.sleep(PAUSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * подбираем напряжения на С4
     * * от 3 до 5В. Деф 5В
     */
    private void findVU4() {
    }

    /**
     * подбираем напряжения на С1-3
     * от 3 до 5В. Деф 5В
     */
    private void findUC() {
    }

    /**
     * Установка всех параметров в деф
     */
    private void setDefault(int VR0, int VVA, int UC, int VU4) {
        this.list = new ArrayList<>();
        this.VR0 = VR0;
        this.VVA = VVA;
        this.UC = UC;
        this.VU4 = VU4;

    }


}
