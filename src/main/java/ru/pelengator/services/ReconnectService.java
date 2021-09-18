package ru.pelengator.services;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import ru.pelengator.DetectorViewModel;

/**
 * Сервис реконнекта к плате
 */
public class ReconnectService extends ScheduledService<Void> {

    private DetectorViewModel detectorViewModel;//ссылка на DVM

    public ReconnectService(DetectorViewModel detectorViewModel) {
        this.detectorViewModel = detectorViewModel;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Connecting...");
                detectorViewModel.reconnectDriver();//реконнект
                return null;
            }
        };
    }
}
