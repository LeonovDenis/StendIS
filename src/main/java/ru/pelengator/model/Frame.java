package ru.pelengator.model;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

/**
 * Класс видео кадра
 */
public class Frame implements Cloneable {

    private Timestamp time;
    private long id;
    private int[] data;

    public Frame() {
        this(new Timestamp(System.currentTimeMillis()), 0, null);
    }

    public Frame(Timestamp time, long id, int[] data) {
        this.time = time;
        this.id = id;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frame frame = (Frame) o;
        return id == frame.id && Objects.equals(time, frame.time) && Arrays.equals(data, frame.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(time, id);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public Frame clone() throws CloneNotSupportedException {
        return (Frame) super.clone();
    }

    @Override
    public String toString() {
        return "Frame{" +
                "time= " + time +
                ", id= " + id +
                ", data= " + Arrays.toString(data) +
                '}';
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }
}
