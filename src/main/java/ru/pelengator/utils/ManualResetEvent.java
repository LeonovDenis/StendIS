package ru.pelengator.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Монитор многопоточной отсылки команд
 */
public class ManualResetEvent {

    private final Object monitor = new Object();
    private volatile boolean open = false;
    public static transient ObservableList<Thread> threadArrayList = FXCollections.observableArrayList();
    private transient BooleanProperty isAllDone = new SimpleBooleanProperty(true);

    public ManualResetEvent(boolean open) {
        this.open = open;
    }

    public void waitOne() throws InterruptedException {
        synchronized (monitor) {
            setIsAllDone(false);
            threadArrayList.add(Thread.currentThread());
            while (open == false) {
                monitor.wait();
            }
            open = false;
            threadArrayList.remove(0);
            if (threadArrayList.isEmpty()) {
                setIsAllDone(true);
            }
        }
    }

    public boolean waitOne(long milliseconds) throws InterruptedException {
        synchronized (monitor) {
            if (open)
                return true;
            monitor.wait(milliseconds);
            return open;
        }
    }

    /**
     * Освобождение потока
     */
    public void set() {//open start

        synchronized (monitor) {
            open = true;
            monitor.notifyAll();
        }
    }

    public void reset() {//close stop

        synchronized (monitor) {
            open = false;
        }
    }

    public boolean isIsAllDone() {
        return isAllDone.get();
    }

    public BooleanProperty isAllDoneProperty() {
        return isAllDone;
    }

    public void setIsAllDone(boolean isAllDone) {
        this.isAllDone.set(isAllDone);
    }

    public static ObservableList<Thread> getThreadArrayList() {
        return threadArrayList;
    }

    public static void setThreadArrayList(ObservableList<Thread> threadArrayList) {
        ManualResetEvent.threadArrayList = threadArrayList;
    }

}
