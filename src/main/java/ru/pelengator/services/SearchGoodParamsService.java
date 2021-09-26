package ru.pelengator.services;


import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import ru.pelengator.DetectorViewModel;
import ru.pelengator.SecondaryController;

/**
 * Сервис по поиску наилучших параметров системы
 * В работе
 */
public class SearchGoodParamsService extends ScheduledService<Void> {


    int start_ch;
    int stop_ch;
    float mashtab = 0.305f;
    int cout_znach;

    SecondaryController con;
    DetectorViewModel det;

    private int UC;
    private int VU4;
    private int VR0;
    private int VVA;


    public SearchGoodParamsService(SecondaryController con, DetectorViewModel det) {
        this.con = con;
        this.det = det;


    }

    @Override
    protected Task<Void> createTask() {


        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {


                setDefault(); //ставим всё по дефаулту

                updateProgress(0.1, 1);

                for (int i = 0; i < 3; i++) {
                    updateProgress(0.2 + (0.2 * i), 1);

                    findVR0();//ищем VR0

                    findUC();//ищем UC

                    findVU4();//ищем VU4


                    if (i == 0) {
                        updateProgress(0.3, 1);
                        //устанавливаем обратное направление
                    } else if (i == 1) {
                        updateProgress(0.5, 1);
                        //устанавливаем режим BYPASS
                    }
                }

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
    private void setDefault() {
    }



}
