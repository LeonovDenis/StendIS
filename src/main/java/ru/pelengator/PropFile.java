package ru.pelengator;

import at.favre.lib.bytes.Bytes;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Класс с настройками/параметрами
 */
public class PropFile {

    //настройки стенда
    private static final Properties appProps;

    public static final int MINUTA = 60;
    public static final long PAUSE = 200;//пауза сценария
    public static final long RECON_PAUSE = 2000;//пауза реконнекта
    public static final int WATECOUNT = 20;//количество полученных больших сообщений для отпуска потока отправки
    public static final int fiveK = 5000;//константа динам диапазона
    public static int CHANNEL_START = 1;//нумерация крайних каналов
    public static int CHANNEL_STOP = 288;
    public static int CHANNELNUMBER;//количество каналов
    public static double DELTA_TEMP = 10.0D;//дельта температуры (30-40)
    public static double MNOSHITEL = 1_000_000;// дополнительный множитель
    public static int ONE_K = 1000; //константа для перевода в милли
    public static int SYCLE;//число циклов наработки

    public static final int SIZEMINMSG = 60;//максимальная длина маленького сообщения
    public static byte DEV_ID;
    ///////////////////////////////
    public static boolean TestForWork;
    public static byte MASK;
    public static byte[] SETPOWER;
    public static byte[] SETSERIAL;
    public static byte[] SETINT;
    public static byte[] SETVR0;
    public static byte[] SETVVA;
    public static byte[] SETVU4;
    public static byte[] SETVUC;
    public static byte[] SETDESEL;
    public static byte[] SETRESET;
    public static byte[] SETID;
    public static byte[] SETMKSPOWER;
    public static byte[] SETDINAMDIAP;
    public static byte[] RESALLPARAMS;
    public static byte[] RESDESEL;
    public static Bytes header;
    public static float MASHTAB;//масштаб из зайцев в мВ
    public static int RANGE;
    public static int ACPNUMBER;
    public static int LINENUMBER;
    //наименование детектора
    public static String DETECTORNAME;
    public static String JDBC_DRIVER;
    public static String DB_URL;
    public static String TAB_NAME;
    public static String USER;
    public static String PASS;

    //загрузка ресурсов
    static {
        appProps = new Properties();
        try {

            ///////////////////////////////загрузка настроек///////////////////////////////////////
            InputStream resourceAsStream = PropFile.class.getResourceAsStream("props.properties");
            appProps.load(resourceAsStream);
            //////////////////////////////////////////////////////////////////////////////////////
            String str_LINENUMBER = appProps.getProperty("dev.LINENUMBER");
            String str_TestForWork = appProps.getProperty("test.work");

            String str_MASK = appProps.getProperty("msg.MASK");
            String str_DEV_ID = appProps.getProperty("msg.ID");
            String str_SETPOWER = appProps.getProperty("msg.SETPOWER");
            String str_SETSERIAL = appProps.getProperty("msg.SETSERIAL");
            String str_SETINT = appProps.getProperty("msg.SETINT");
            String str_SETVR0 = appProps.getProperty("msg.SETVR0");
            String str_SETVVA = appProps.getProperty("msg.SETVVA");
            String str_SETVU4 = appProps.getProperty("msg.SETVU4");
            String str_SETVUC = appProps.getProperty("msg.SETVUC");
            String str_SETDESEL = appProps.getProperty("msg.SETDESEL");
            String str_SETRESET = appProps.getProperty("msg.SETRESET");
            String str_SETID = appProps.getProperty("msg.SETID");
            String str_SETMKSPOWER = appProps.getProperty("msg.SETMKSPOWER");
            String str_SETDINAMDIAP = appProps.getProperty("msg.SETDINAMDIAP");
            String str_RESALLPARAMS = appProps.getProperty("msg.RESALLPARAMS");
            String str_RESDESEL = appProps.getProperty("msg.RESDESEL");
            String str_RANGE = appProps.getProperty("dev.RANGE");
            String str_ACPNUMBER = appProps.getProperty("dev.ACPNUMBER");
            String str_SYCLE = appProps.getProperty("dev.sycle");

            JDBC_DRIVER = appProps.getProperty("bd.JDBC_DRIVER");
            DB_URL = appProps.getProperty("bd.DB_URL");
            USER = appProps.getProperty("bd.USER");
            PASS = appProps.getProperty("bd.PASS");
            TAB_NAME = appProps.getProperty("bd.TAB_NAME");
            /////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////
            LINENUMBER = Integer.parseInt(str_LINENUMBER);
            CHANNELNUMBER = 2 * LINENUMBER;
            CHANNEL_STOP = CHANNELNUMBER;

            TestForWork = Objects.equals(str_TestForWork, "true");
            DETECTORNAME = appProps.getProperty("dev.DETECTORNAME");

            MASK = Bytes.parseHex(str_MASK).toByte();
            DEV_ID = Bytes.parseHex(str_DEV_ID).toByte();
            SETPOWER = Bytes.parseHex(str_SETPOWER).array();
            SETSERIAL = Bytes.parseHex(str_SETSERIAL).array();
            SETINT = Bytes.parseHex(str_SETINT).array();
            SETVR0 = Bytes.parseHex(str_SETVR0).array();
            SETVVA = Bytes.parseHex(str_SETVVA).array();
            SETVU4 = Bytes.parseHex(str_SETVU4).array();
            SETVUC = Bytes.parseHex(str_SETVUC).array();
            SETDESEL = Bytes.parseHex(str_SETDESEL).array();
            SETRESET = Bytes.parseHex(str_SETRESET).array();
            SETID = Bytes.parseHex(str_SETID).array();
            SETMKSPOWER = Bytes.parseHex(str_SETMKSPOWER).array();
            SETDINAMDIAP = Bytes.parseHex(str_SETDINAMDIAP).array();
            RESALLPARAMS = Bytes.parseHex(str_RESALLPARAMS).array();
            RESDESEL = Bytes.parseHex(str_RESDESEL).array();
            header = Bytes.from(MASK, DEV_ID);
            RANGE = Integer.parseInt(str_RANGE);
            ACPNUMBER = Integer.parseInt(str_ACPNUMBER);
            MASHTAB = (RANGE / (float) (Math.pow(2, ACPNUMBER)));
            SYCLE = Integer.parseInt(str_SYCLE);

            /////////////////////////////////////////////////////////////////////////////////////
        } catch (
                IOException e) {
            System.out.println("Не загрузились настройки");
            e.printStackTrace();
        }
    }
}
