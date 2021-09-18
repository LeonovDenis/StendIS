package ru.pelengator.driver;

import at.favre.lib.bytes.Bytes;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.StdCallLibrary;

import static ru.pelengator.App.getFtd3XX;

/**
 * Драйвер с java на c++
 */
public class Jna2 {

    //Размер буфера для отправки сообщения
    private static final int MB = 1024;
    //буфер для передачи байтов
    private static Pointer pBuff = new Memory(MB);
    //обработчик устройства
    private static Pointer hendler = new Memory(MB);
    //количество переданных байтов
    private static LongByReference byteTrans = new LongByReference(0);

///////////////////////////////////////////////////////////////////////////////////

    /**
     * Конструктор
     */
    public Jna2() {
        this.create();
    }

    /**
     * Нативный интерфейс
     */
    public interface FTD3XX extends StdCallLibrary {

        FTD3XX FTD3XX_INSTANCE = (FTD3XX) Native.loadLibrary(getFtd3XX(), FTD3XX.class);

        /////////////////////////////////////////////////////методы интерфейса//////////////////////////
        //создание интерфейса
        int FT_Create(String pvArg, byte dwFlags, Pointer pftHandle);

        //синхронная запись
        int FT_WritePipe(Pointer ftHandle, byte ucPipeID, Pointer pucBuffer, long ulBufferLength, LongByReference pulBytesTransferred, Structure pOverlapped);

        //синхронное чтение
        int FT_ReadPipe(Pointer ftHandle, byte ucPipeID, Pointer pucBuffer, long ulBufferLength, LongByReference pulBytesTransferred, Structure pOverlapped);

        //закрытие интерфейса
        int FT_Close(Pointer ftHandle);

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////
    //
    //рабочие методы
    //////////////////////////////////////////////////////////////

    /**
     * Создание обработчика
     * Инициализация работы
     *
     * @return Статус FT_
     */
    public FT_STATUS create() {
        int i = FTD3XX.FTD3XX_INSTANCE.FT_Create("FTDI SuperSpeed-FIFO Bridge", (byte) 0x0A, hendler);
        FT_STATUS status = FT_STATUS.values()[i];
        return status;
    }

    /**
     * Синхронная запись по обработчику **hendler**
     *
     * @param data Массив для записи
     * @return
     */
    public FT_STATUS writePipe(Bytes data) {
        pBuff.write(0, data.array(), 0, data.length());//запись данных в буфер
        int i = FTD3XX.FTD3XX_INSTANCE.FT_WritePipe(hendler.getPointer(0), (byte) 0x02, pBuff, data.length(), byteTrans, (Structure) null);
        FT_STATUS status = FT_STATUS.values()[i];
        return status;
    }

    /**
     * чтение массива
     *
     * @return Обернутый массив
     */
    public Bytes readPipe() {
        int i = FTD3XX.FTD3XX_INSTANCE.FT_ReadPipe(hendler.getPointer(0), (byte) 0x82, pBuff, MB, byteTrans, (Structure) null);
        byte[] byteArray = pBuff.getByteArray(0, (int) byteTrans.getValue());// из буфера в массив
        Bytes from = Bytes.from(byteArray);
        return from;
    }

    /**
     * Завершение работы
     * Закрытие обработчика
     *
     * @return Статус FT_
     */
    public FT_STATUS close() {

        int i = FTD3XX.FTD3XX_INSTANCE.FT_Close(hendler.getPointer(0));
        return FT_STATUS.values()[i];
    }

}


