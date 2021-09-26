package ru.pelengator.dao;

import at.favre.lib.bytes.Bytes;
import ru.pelengator.model.Experiment;

import java.sql.*;
import java.util.ArrayList;

import static ru.pelengator.PropFile.*;
import static ru.pelengator.dao.BDService.TypeColums.type_ALL;

/**
 * Сервис работы с БД
 */
public class BDService {

    private static Connection conn;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;

    static {//Регистрация драйвера для БД
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Пустой конструктор
     */
    public BDService() {
    }

    /**
     * Сохранение данных в БД
     *
     * @param exp Эксперимент
     */
    public boolean saveExpDataToBD(Experiment exp) {
        boolean result = false;
        try {
            //Создание соединения
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            // запрос на SQL
            String sql = "insert into " +
                    TAB_NAME +//наименование таблицы
                    //номер колонки в БД
                    " (detectorID, " +//2
                    "testerName, " +//3
                    "startExpDate, " +//4
                    "endExpDate, " +//5
                    "topology, " +//6
                    "vr0, " +//7
                    "vva, " +//8
                    "vu4, " +//9
                    "vuc, " +//10
                    "tInt, " +//11
                    "temp, " +//12
                    "modde, " +//13
                    "dir, " +//14
                    "ccc, " +//15
                    "frameArrayList30, " +//16
                    "frameArrayList40, " +//17
                    "shum, " +//18
                    "sredZnach30, " +//19
                    "sredZnach40, " +//20
                    "NEDT) "//21
                    + "values "
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int i = 1;
            //подготавливаем запрос
            preparedStatement = conn.prepareStatement(sql);
            //вставляем данные
            preparedStatement.setString(i++, exp.getDetectorSerial());
            preparedStatement.setString(i++, exp.getTesterName());
            preparedStatement.setTimestamp(i++, exp.getStartExpDate());
            preparedStatement.setTimestamp(i++, exp.getEndExpDate());
            preparedStatement.setBytes(i++, exp.getMatrix());
            preparedStatement.setInt(i++, exp.getVr0());
            preparedStatement.setInt(i++, exp.getVva());
            preparedStatement.setInt(i++, exp.getVu4());
            preparedStatement.setInt(i++, exp.getVuc());
            preparedStatement.setInt(i++, exp.gettInt());
            preparedStatement.setInt(i++, exp.getTemp());
            preparedStatement.setString(i++, exp.getMode());
            preparedStatement.setString(i++, exp.getDir());
            preparedStatement.setString(i++, exp.getCcc());
            preparedStatement.setString(i++, exp.getFrameList30());
            preparedStatement.setString(i++, exp.getFrameList40());
            preparedStatement.setDouble(i++, exp.getShum());
            preparedStatement.setDouble(i++, exp.getSredZnach30());
            preparedStatement.setDouble(i++, exp.getSredZnach40());
            preparedStatement.setDouble(i++, exp.getNEDT());
            //выполняем запрос
            preparedStatement.execute();
            result = true;
        } catch (SQLException e) {
            //Ошибки SQL
            e.printStackTrace();
        } catch (Exception e) {
            //Ошибки драйвера
            e.printStackTrace();
        } finally {
            //Закрытие ресурсов
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                //ignore
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return result;
    }

    /**
     * Загрузка с БД
     *
     * @param typeColums наименование колонки
     * @param value      значение колонки
     * @param <T>
     * @return Список экспериментов
     */
    public <T> ArrayList<Experiment> readExpFromBD(TypeColums typeColums, T value) {
        ArrayList<Experiment> experiments = new ArrayList<>();
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = conn.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("select ").append(" * ").append(" from ").append(TAB_NAME);
            if (typeColums != type_ALL) {
                sql.append(" where ").append(typeColums.getValue()).append(" = ").append(value);
            }
            // Отправка запроса
            resultSet = statement.executeQuery(sql.toString());
            //пробегаем по строкам результата
            while (resultSet.next()) {
                Experiment experiment = new Experiment();
                int i = 1;
                experiment.setID(resultSet.getLong(i++));
                experiment.setDetectorName(DETECTORNAME);//заглушка
                experiment.setDetectorSerial(resultSet.getString(i++));
                experiment.setTesterName(resultSet.getString(i++));
                experiment.setStartExpDate(resultSet.getTimestamp(i++));
                experiment.setEndExpDate(resultSet.getTimestamp(i++));
                experiment.setMatrix(resultSet.getBytes(i++));

                experiment.setCountDeselPixel(0);//Заглушка
                experiment.setCountDeselPixelInLine(0);//Заглушка
                experiment.setBrakTimes(2.0);//Заглушка
                experiment.setBrakFPUCount(0.03);//Заглушка
                experiment.setBrakCHannelCount(0.05);//Заглушка
                experiment.setMashtab(5.0);//Заглушка

                experiment.setVr0(resultSet.getInt(i++));
                experiment.setVva(resultSet.getInt(i++));
                experiment.setVu4(resultSet.getInt(i++));
                experiment.setVuc(resultSet.getInt(i++));
                experiment.settInt(resultSet.getInt(i++));
                experiment.setTemp(resultSet.getInt(i++));
                experiment.setMode(resultSet.getString(i++));
                experiment.setDir(resultSet.getString(i++));
                experiment.setCcc(resultSet.getString(i++));
                experiment.setFrameList30(resultSet.getString(i++));
                experiment.setFrameList40(resultSet.getString(i++));
                experiment.setShum(resultSet.getDouble(i++));
                experiment.setSredZnach30(resultSet.getDouble(i++));
                experiment.setSredZnach40(resultSet.getDouble(i++));
                experiment.setNEDT(resultSet.getDouble(i++));

                experiments.add(experiment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                //ignore
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return experiments;
    }

    /**
     * Перечень колонок (В работе)
     */
    public enum TypeColums {
        type_ID("ID"),
        type_startExpDate("startExpDate"),
        type_vr0("vr0"),
        type_ALL("*");

        TypeColums(String value) {
            this.value = value;
        }

        public final String value;

        public String getValue() {
            return this.value;
        }
    }

    /**
     * Добавление данных в существующий эксперимент в БД
     *
     * @param typeColums тип колонки
     * @param value      значение
     * @param exp        эксперимент
     * @param <T>
     * @return
     */
    public <T> boolean updateExpFromBD(TypeColums typeColums, T value, Experiment exp) {
        boolean result = false;
        ArrayList<Experiment> experiments = readExpFromBD(typeColums, value);
        Experiment currentExperimentBD = experiments.get(0);
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(TAB_NAME).append("\nSET ");
            for (int i = 0; i < 2; i++) {//сравнение параметров и добавление в SQL запрос
                int k = 1;
                if (!(currentExperimentBD.getDetectorName()).equals(exp.getDetectorName())) {
                    if (i == 0) {
                        sql.append("detectorID = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getDetectorSerial());
                    }
                }
                if (!(currentExperimentBD.getTesterName()).equals(exp.getTesterName())) {
                    if (i == 0) {
                        sql.append("testerName = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getTesterName());
                    }
                }
                if (!(currentExperimentBD.getStartExpDate().toString()).equals(exp.getStartExpDate().toString())) {
                    if (i == 0) {
                        sql.append("startExpDate = ? ,");
                    } else {
                        preparedStatement.setTimestamp(k++, exp.getStartExpDate());
                    }
                }
                if (!(currentExperimentBD.getEndExpDate().toString()).equals(exp.getEndExpDate().toString())) {
                    if (i == 0) {
                        sql.append("endExpDate = ? ,");
                    } else {
                        preparedStatement.setTimestamp(k++, exp.getEndExpDate());
                    }
                }
                if (!Bytes.from(currentExperimentBD.getMatrix()).equals(exp.getMatrix())) {
                    if (i == 0) {
                        sql.append("topology = ? ,");
                    } else {
                        preparedStatement.setBytes(k++, exp.getMatrix());
                    }
                }
                if ((currentExperimentBD.getVr0()) != (exp.getVr0())) {
                    if (i == 0) {
                        sql.append("vr0 = ? ,");
                    } else {
                        preparedStatement.setInt(k++, exp.getVr0());
                    }
                }
                if ((currentExperimentBD.getVva()) != (exp.getVva())) {
                    if (i == 0) {
                        sql.append("vva = ? ,");
                    } else {
                        preparedStatement.setInt(k++, exp.getVva());
                    }
                }
                if ((currentExperimentBD.getVu4()) != (exp.getVu4())) {
                    if (i == 0) {
                        sql.append("vu4 = ? ,");
                    } else {
                        preparedStatement.setInt(k++, exp.getVu4());
                    }
                }
                if ((currentExperimentBD.getVuc()) != (exp.getVuc())) {
                    if (i == 0) {
                        sql.append("vuc = ? ,");
                    } else {
                        preparedStatement.setInt(k++, exp.getVuc());
                    }
                }
                if ((currentExperimentBD.gettInt()) != (exp.gettInt())) {
                    if (i == 0) {
                        sql.append("tInt = ? ,");
                    } else {
                        preparedStatement.setInt(k++, exp.gettInt());
                    }
                }
                if ((currentExperimentBD.getTemp()) != (exp.getTemp())) {
                    if (i == 0) {
                        sql.append("temp = ? ,");
                    } else {
                        preparedStatement.setInt(k++, exp.getTemp());
                    }
                }
                if (!(currentExperimentBD.getMode()).equals(exp.getMode())) {
                    if (i == 0) {
                        sql.append("modde = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getMode());
                    }
                }
                if (!(currentExperimentBD.getDir()).equals(exp.getDir())) {
                    if (i == 0) {
                        sql.append("dir = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getDir());
                    }
                }
                if (!(currentExperimentBD.getCcc()).equals(exp.getCcc())) {
                    if (i == 0) {
                        sql.append("ccc = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getCcc());
                    }
                }
                if (!(currentExperimentBD.getFrameList30()).equals(exp.getFrameList30())) {
                    if (i == 0) {
                        sql.append("frameArrayList30 = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getFrameList30());
                    }
                }
                if (!(currentExperimentBD.getFrameList40()).equals(exp.getFrameList40())) {
                    if (i == 0) {
                        sql.append("frameArrayList40 = ? ,");
                    } else {
                        preparedStatement.setString(k++, exp.getFrameList40());
                    }
                }
                if ((currentExperimentBD.getShum()) != (exp.getShum())) {
                    if (i == 0) {
                        sql.append("shum = ? ,");
                    } else {
                        preparedStatement.setDouble(k++, exp.getShum());
                    }
                }
                if ((currentExperimentBD.getSredZnach30()) != (exp.getSredZnach30())) {
                    if (i == 0) {
                        sql.append("sredZnach30 = ? ,");
                    } else {
                        preparedStatement.setDouble(k++, exp.getSredZnach30());
                    }

                }
                if ((currentExperimentBD.getSredZnach40()) != (exp.getSredZnach40())) {
                    if (i == 0) {
                        sql.append("sredZnach40 = ? ,");
                    } else {
                        preparedStatement.setDouble(k++, exp.getSredZnach40());
                    }

                }
                if ((currentExperimentBD.getNEDT()) != (exp.getNEDT())) {
                    if (i == 0) {
                        sql.append("NEDT = ? ,");
                    } else {
                        preparedStatement.setDouble(k++, exp.getNEDT());
                    }
                }
                if (i == 0) {
                    sql.delete(sql.length() - 1, sql.length());
                    sql.append("\nWHERE ").append(typeColums.getValue()).append(" = ").append(value);
                    preparedStatement = conn.prepareStatement(sql.toString());
                } else {
                    if (k == 1) {
                        result = false;
                    } else {
                        //Выполнение подготовленного запроса
                        preparedStatement.executeUpdate();
                        result = true;
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                //ignore
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return result;
    }

    /**
     * Получение ID крайней записи в БД
     *
     * @return long ID
     */
    public long takeLastIDFromBD() {
        long aLong = 0;
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = conn.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ").append("ID").append(" FROM ").append(TAB_NAME);
            sql.append(" ORDER BY ID DESC LIMIT 1");
            // Отправка запроса
            resultSet = statement.executeQuery(sql.toString());
            //пробегаем по строкам результата
            while (resultSet.next()) {
                aLong = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            //ignore
        } catch (Exception e) {
            //ignore
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se) {
                //ignore
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return aLong;
    }

    /**
     * Проверка доступности базы
     *
     * @return
     */
    public boolean bdIsAlive() {
        boolean res = false;
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            res = true;
        } catch (Exception e) {
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
        return res;
    }

}