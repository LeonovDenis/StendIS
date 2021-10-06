package ru.pelengator.model;


import at.favre.lib.bytes.Bytes;

import ru.pelengator.driver.FT_STATUS;
import ru.pelengator.driver.Jna2;
import ru.pelengator.utils.ManualResetEvent;

import java.util.concurrent.TimeUnit;

import static ru.pelengator.PropFile.*;
import static ru.pelengator.utils.ManualResetEvent.getThreadArrayList;
import static ru.pelengator.utils.ManualResetEvent.threadArrayList;

/**
 * Обработчик команд
 */
public class Connector {

    //Библиотека
    public static final Jna2 driver2 = new Jna2();
    //Замок
    public static final ManualResetEvent hendler = new ManualResetEvent(true);
    //счетчик попыток
    private static long counter;
    //инициализация

    /**
     * Конструктор
     */
    public Connector() {
    }
    /**
     * Включение парлоада
     *
     * @param set - 0xFF -включение (true); 0x00 - выключение
     * @return
     */
    public FT_STATUS setParload(boolean set) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        byte data;
        if (set) {
            data = (byte) 0xFF;//on
        } else {
            data = (byte) 0x00;//off
        }
        Bytes msg = header              //маска+ID
                .append(SETPOWER[0])    //функция
                .append((byte) 0x02)    //размер[команда+данные]||
                .append((byte) 0x00)    //команда               |
                .append(data);          //данные               _|
        return driver2.writePipe(msg);
    }


    /**
     * Подача питания VDD, VDDA
     *
     * @param set - 0xFF -включение (true); 0x00 - выключение
     * @return
     */
    public FT_STATUS setPower(boolean set) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        byte data;
        if (set) {
            data = (byte) 0xFF;//on
        } else {
            data = (byte) 0x00;//off
        }
        Bytes msg = header              //маска+ID
                .append(SETPOWER[0])    //функция
                .append((byte) 0x02)    //размер[команда+данные]||
                .append(SETPOWER[1])    //команда               |
                .append(data);          //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Установка режима
     *
     * @param mode- режим работы
     * @param dir   - направление
     * @param cc    - ёмкость
     * @return
     */
    public FT_STATUS setMode(byte mode, byte dir, byte cc) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        byte data = (byte) (mode | dir | cc);
        data = (byte) reverse(data, 8);
        Bytes msg = header               //маска+ID
                .append(SETSERIAL[0])    //функция
                .append((byte) 0x02)     //размер[команда+данные]||
                .append(SETSERIAL[1])    //команда               |
                .append(data);           //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * установка направления
     *
     * @param mode- режим работы
     * @param dir   - направление
     * @param cc    -ёмкость
     * @return
     */
    public FT_STATUS setDirection(byte mode, byte dir, byte cc) {
        return setMode(mode, dir, cc);
    }

    /**
     * установка ёмкости
     *
     * @param mode- режим работы
     * @param dir   - направление
     * @param cc    -ёмкость
     * @return
     */
    public FT_STATUS setCapacity(byte mode, byte dir, byte cc) {
        return setMode(mode, dir, cc);
    }

    /**
     * Установка времени интегрирования
     *
     * @param time- в милисекундах
     * @return
     */
    public FT_STATUS setIntTime(int time) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        byte[] data = Bytes.from(time).resize(2).reverse().array();
        Bytes msg = header            //маска+ID
                .append(SETINT[0])    //функция
                .append((byte) 0x03)  //размер[команда+данные]||
                .append(SETINT[1])    //команда               |
                .append(data);        //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Установка напр. смещения
     *
     * @param value - в миливольтах
     * @return
     */
    public FT_STATUS setVR0(int value) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        float floatValue = value / 1000f;
        byte[] data = Bytes.from(floatValue).reverse().array();
        Bytes msg = header            //маска+ID
                .append(SETVR0[0])    //функция
                .append((byte) 0x05)  //размер[команда+данные]||
                .append(SETVR0[1])    //команда               |
                .append(data);        //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Установка напр. антиблюминга
     *
     * @param value - в миливольтах
     * @return
     */
    public FT_STATUS setVVA(int value) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        float floatValue = value / 1000f;
        byte[] data = Bytes.from(floatValue).reverse().array();
        Bytes msg = header            //маска+ID
                .append(SETVVA[0])    //функция
                .append((byte) 0x05)  //размер[команда+данные]||
                .append(SETVVA[1])    //команда               |
                .append(data);        //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Установка напр. на ёмкости с4
     *
     * @param value - в миливольтах
     * @return
     */
    public FT_STATUS setVU4(int value) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        float floatValue = value / 1000f;
        byte[] data = Bytes.from(floatValue).reverse().array();
        Bytes msg = header            //маска+ID
                .append(SETVU4[0])    //функция
                .append((byte) 0x05)  //размер[команда+данные]||
                .append(SETVU4[1])    //команда               |
                .append(data);        //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Установка напр. на ёмкостях с1-3
     *
     * @param value - в миливольтах
     * @return
     */
    public FT_STATUS setUC(int value) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        float floatValue = value / 1000f;
        byte[] data = Bytes.from(floatValue).reverse().array();
        Bytes msg = header            //маска+ID
                .append(SETVUC[0])    //функция
                .append((byte) 0x05)  //размер[команда+данные]||
                .append(SETVUC[1])    //команда               |
                .append(data);        //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * отправка деселекции
     *
     * @param value - байт конфигурации
     * @param line  - номер линии
     * @return
     */
    public FT_STATUS setDesel(byte value, int line) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        value = (byte) reverse(value, 9);
        byte[] data = Bytes.from((byte) line).append(value).array();
        Bytes msg = header              //маска+ID
                .append(SETDESEL[0])    //функция
                .append((byte) 0x03)    //размер[команда+данные]||
                .append(SETDESEL[1])    //команда               |
                .append(data);          //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Сброс устройства
     *
     * @param isReset - true - ресет
     * @return
     */
    public FT_STATUS setReset(boolean isReset) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        byte data;
        if (!isReset) {
            data = (byte) 0xFF;//work
        } else {
            data = (byte) 0x00;//reset
        }
        Bytes msg = header              //маска+ID
                .append(SETRESET[0])    //функция
                .append((byte) 0x02)    //размер[команда+данные]||
                .append(SETRESET[1])    //команда               |
                .append(data);          //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Установка id устройства
     *
     * @return
     */
    public FT_STATUS setID() {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        Bytes msg = header           //маска+ID
                .append(SETID[0])    //функция
                .append((byte) 0x02) //размер[команда+данные]||
                .append(SETID[1])    //команда               |
                .append(DEV_ID);     //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * установка напря на мкс
     *
     * @param bb- в вольтах
     * @return
     */
    public FT_STATUS setMKSPower(int bb) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        byte[] data = Bytes.from(bb).resize(1).array();
        Bytes msg = header               //маска+ID
                .append(SETMKSPOWER[0])  //функция
                .append((byte) 0x02)     //размер[команда+данные]||
                .append(SETMKSPOWER[1])  //команда               |
                .append(data);           //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * установка динамического диапазона
     *
     * @param bb 0x03- 3,3В; 0x05- 5,0В
     * @return
     */
    public FT_STATUS setDinamDiap(byte bb) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        Bytes msg = header                  //маска+ID
                .append(SETDINAMDIAP[0])    //функция
                .append((byte) 0x02)        //размер[команда+данные]||
                .append(SETDINAMDIAP[1])    //команда               |
                .append(bb);                //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Чтение данных
     *
     * @return
     */
    public synchronized Bytes readData() {
        Bytes bytes = driver2.readPipe();
        deleteLock(bytes);
        return bytes;
    }

    /**
     * Запрос основных параметров
     * A20200 19 [добавка 01] id=02 port=00 int=0000 40A00000 40A00000 40A00000 00000000 00000000 42 00
     * A2020019 02 00 00 00 40 A0 00 00 40 A0 00 00 40 A0 00 00 00 00 00 00 00 00 00 00 42 00
     * A202001E 01 id=02 port=20 int=0014 40A00000 40A00000 40A00000 40A00000 00000000  00000000 42
     *
     * @return
     */
    public FT_STATUS resAllParams() {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        Bytes msg = header                   //маска+ID
                .append(RESALLPARAMS[0])    //функция
                .append((byte) 0x01)        //размер[команда+данные]||
                .append(RESALLPARAMS[1]);   //команда              _|
        return driver2.writePipe(msg);
    }

    /**
     * Запрос веселекции и серлоад- первый байт в ответе
     * A20200 18 [добавка 02] 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     * A202001B 02 05 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14
     *
     * @param bb- один из 6 групп массива деселекции
     *            -->     0x00-[0-23]
     * @return
     */
    public FT_STATUS resDeselSerLoadParams(byte bb) {
        setLock();
        //для теста
        if (TestForWork) {
            return FT_STATUS.FT_TESTING;
        }
        Bytes msg = header            //маска+ID
                .append(RESDESEL[0])  //функция
                .append((byte) 0x02)  //размер[команда+данные]||
                .append(RESDESEL[1])  //команда               |
                .append(bb);          //данные               _|
        return driver2.writePipe(msg);
    }

    /**
     * Блокируем отправку до подтверждения
     */
    private void setLock() {
        try {
            hendler.waitOne();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Подтверждение доставки сообщения
     *
     * @param bytes полученный массив
     */
    private void deleteLock(Bytes bytes) {

        int length = bytes.length();
        if (!getThreadArrayList().isEmpty()) {//Если есть стопнутые потоки
            //если пришло подтверждение
            if (length < SIZEMINMSG && length > 3) {//если сообщение имеет размер подтверждения доставки
                hendler.set();//отпустить поток
                counter = 0;//сбросить счетчик
            } else if (counter > WATECOUNT) {//если количество пришедших сообщений больше заданного значения (плата не ответила)
                hendler.set();//отпустить поток
                counter = 0;//сбросить счетчик
            }
            if (length >= SIZEMINMSG) {//если тип сообщения не подтверждение доставки
                counter++;//увеличиваем счетчик
            }
        }
    }

    /**
     * Функция переприсоединенрия к плате
     */
    public void reconnect() {

        driver2.close();//закрываем соединение с платой
        try {
            TimeUnit.MILLISECONDS.sleep(PAUSE);//ждем
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FT_STATUS ft_status = driver2.create();//создаём соединение с платой

        if (ft_status == FT_STATUS.FT_OK) {//если статус соединения ОК
            while (!threadArrayList.isEmpty()) {//освобождаем очередь отправки
                hendler.set();//отпускаем поток
                try {
                    TimeUnit.MILLISECONDS.sleep(PAUSE / 2);//ждем
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Thread thread = new Thread(() -> {
                setID();//установка ID устройства
            });
            thread.setName("Реконнект");
            thread.setDaemon(true);
            thread.start();
            try {
                TimeUnit.MILLISECONDS.sleep(PAUSE);//ждем
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hendler.set();//отпускаем ответ не нужен
        }
    }

    /**
     * Ревер байтов. Ошибка протокола
     * @param i байт
     * @param t Сколько райб реверснуть
     * @return
     */
    public static int reverse(int i, int t) {
        int Shift = t - 1;
        int LowMask = 1;
        int HighMask = 1 << Shift;
        int R;
        for (R = 0; Shift >= 0; LowMask <<= 1, HighMask >>= 1, Shift -= 2)
            R |= ((i & LowMask) << Shift) | ((i & HighMask) >> Shift);
        return (R >> 1);
    }
}
