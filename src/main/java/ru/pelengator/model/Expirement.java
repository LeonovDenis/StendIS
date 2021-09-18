package ru.pelengator.model;


import javafx.collections.ObservableList;

import java.util.Date;
import java.util.Map;

import static ru.pelengator.PropFile.MASHTAB;

/**
 * Объект эксперимента
 */
public class Expirement {
    //данные по детектору
    private String detectorName;
    private String detectorSerial;
    private String testerName;
    //данные по эксперименту
    private Date startExpDate;
    private Date endExpDate;
    //данные по пикселям
    private int countDeselPixel;
    private int countDeselPixelInLine;
    /////////////параметры стенда////
    private double brakTimes;// параметр брака по шуму/разы
    private double   brakFPUCount;//паракметр брака по НЕДТ ФПУ/вольты
    private double  brakCHannelCount; //параметр брака по НЕДТ по каналу/вольты
    ////////////////////////////////////////////////
    // параметры стенда
    private double mashtab;//маштаб, В/отсчет
    private int vr0;//напряжение смещения, мВ
    private int vva;//напряжение антиблюма, мВ
    private int vu4;//напряжение на ёмкости 4, мВ
    private int vuc;//напряжение на ёмкостях 1-3, мВ
    private int tInt;//время интегрирования, мкс
    private int temp;//напряжение на термодатчике, мВ
    private String mode;//режим работы
    private String dir;//направление сканирования
    private String ccc;//ёмкость

    ////////////////////////////////
    private ObservableList<Frame> frameArrayList30;//массив кадров при 30/отсчеты
    private ObservableList<Frame> frameArrayList40;//массив кадров при 40/отсчеты
    private int[][] dataArray30;//массив данных при 30
    private int[][] dataArray40;//массив данных при 40
    private Map<String,Number> raspredMap;//карта распределения НЕДТ
    private double[] dataArraySred_30;//средний сигнал по каналам при 30
    private double [] dataArraySred_40;//средний сигнал по каналам при 40
    private static double[] dataArraySKO30; // СКО , шум при 30
    private double shum; //средний шум по ФПУ//отсчеты
    private double sredZnach30;//среднее значение сигнала ФПУ при 30
    private double sredZnach40;//среднее значение сигнала ФПУ при 40
    private double[] dataArrayNEDT;//массив НЕДТ
    private double NEDT;// итоговое значение НЕДТ
    private byte[] matrix;//массив деселекции

    /**
     * Конструктор
     * @param detectorName
     * @param detectorSerial
     * @param testerName
     * @param startExpDate
     */
    public Expirement(String detectorName, String detectorSerial, String testerName, Date startExpDate) {
        this.detectorName = detectorName;
        this.detectorSerial = detectorSerial;
        this.testerName = testerName;
        this.startExpDate = startExpDate;
        this.mashtab=MASHTAB;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }

    public String getDetectorSerial() {
        return detectorSerial;
    }

    public void setDetectorSerial(String detectorSerial) {
        this.detectorSerial = detectorSerial;
    }

    public String getTesterName() {
        return testerName;
    }

    public void setTesterName(String testerName) {
        this.testerName = testerName;
    }

    public Date getStartExpDate() {
        return startExpDate;
    }

    public void setStartExpDate(Date startExpDate) {
        this.startExpDate = startExpDate;
    }

    public Date getEndExpDate() {
        return endExpDate;
    }

    public void setEndExpDate(Date endExpDate) {
        this.endExpDate = endExpDate;
    }

    public int[][] getDataArray30() {
        return dataArray30;
    }

    public void setDataArray30(int[][] dataArray30) {
        this.dataArray30 = dataArray30;
    }

    public double[] getDataArraySKO30() {
        return dataArraySKO30;
    }

    public void setDataArraySKO30(double[] dataArraySKO30) {
        this.dataArraySKO30 = dataArraySKO30;
    }

    public ObservableList<Frame> getFrameArrayList30() {
        return frameArrayList30;
    }

    public void setFrameArrayList30(ObservableList<Frame> frameArrayList30) {
        this.frameArrayList30 = frameArrayList30;
    }

    public double getShum() {
        return shum;
    }

    public void setShum(double shum) {
        this.shum = shum;
    }

    public double getSredZnach30() {
        return sredZnach30;
    }

    public void setSredZnach30(double sredZnach30) {
        this.sredZnach30 = sredZnach30;
    }

    public double getBrakTimes() {
        return brakTimes;
    }

    public void setBrakTimes(double brakTimes) {
        this.brakTimes = brakTimes;
    }

    public int[][] getDataArray40() {
        return dataArray40;
    }

    public void setDataArray40(int[][] dataArray40) {
        this.dataArray40 = dataArray40;
    }

    public double getBrakCHannelCount() {
        return brakCHannelCount;
    }

    public void setBrakCHannelCount(double brakCHannelCount) {
        this.brakCHannelCount = brakCHannelCount;
    }

    public double getBrakFPUCount() {
        return brakFPUCount;
    }

    public void setBrakFPUCount(double brakFPUCount) {
        this.brakFPUCount = brakFPUCount;
    }

    public double[] getDataArrayNEDT() {
        return dataArrayNEDT;
    }

    public void setDataArrayNEDT(double[] dataArrayNEDT) {
        this.dataArrayNEDT = dataArrayNEDT;
    }

    public double getSredZnach40() {
        return sredZnach40;
    }

    public void setSredZnach40(double sredZnach40) {
        this.sredZnach40 = sredZnach40;
    }

    public double getNEDT() {
        return NEDT;
    }

    public void setNEDT(double NEDT) {
        this.NEDT = NEDT;
    }

    public ObservableList<Frame> getFrameArrayList40() {
        return frameArrayList40;
    }

    public void setFrameArrayList40(ObservableList<Frame> frameArrayList40) {
        this.frameArrayList40 = frameArrayList40;
    }

    public double [] getDataArraySred_30() {
        return dataArraySred_30;
    }

    public void setDataArraySred_30(double [] dataArraySred_30) {
        this.dataArraySred_30 = dataArraySred_30;
    }

    public double [] getDataArraySred_40() {
        return dataArraySred_40;
    }

    public void setDataArraySred_40(double [] dataArraySred_40) {
        this.dataArraySred_40 = dataArraySred_40;
    }

    public byte[] getMatrix() {
        return matrix;
    }

    public void setMatrix(byte[] matrix) {
        this.matrix = matrix;
    }

    public int getCountDeselPixel() {
        return countDeselPixel;
    }

    public void setCountDeselPixel(int countDeselPixel) {
        this.countDeselPixel = countDeselPixel;
    }

    public int getCountDeselPixelInLine() {
        return countDeselPixelInLine;
    }

    public void setCountDeselPixelInLine(int countDeselPixelInLine) {
        this.countDeselPixelInLine = countDeselPixelInLine;
    }

    public int getVr0() {
        return vr0;
    }

    public void setVr0(int vr0) {
        this.vr0 = vr0;
    }

    public int getVva() {
        return vva;
    }

    public void setVva(int vva) {
        this.vva = vva;
    }

    public int getVu4() {
        return vu4;
    }

    public void setVu4(int vu4) {
        this.vu4 = vu4;
    }

    public int getVuc() {
        return vuc;
    }

    public void setVuc(int vuc) {
        this.vuc = vuc;
    }

    public int gettInt() {
        return tInt;
    }

    public void settInt(int tInt) {
        this.tInt = tInt;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getCcc() {
        return ccc;
    }

    public void setCcc(String ccc) {
        this.ccc = ccc;
    }

    public Map<String, Number> getRaspredMap() {
        return raspredMap;
    }

    public void setRaspredMap(Map<String, Number> raspredMap) {
        this.raspredMap = raspredMap;
    }
}
