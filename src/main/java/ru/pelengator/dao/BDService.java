package ru.pelengator.dao;

import ru.pelengator.model.Experiment;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ru.pelengator.PropFile.*;
import static ru.pelengator.dao.BDService.TypeColums.type_ALL;

public class BDService {

    private static Connection conn;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;

    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public BDService() {
    }

    public void saveExpDataToBD(Experiment exp) {
        try {
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");
            System.out.println("Execute query in given database...");
            statement = conn.createStatement();
            // запрос на SQL
            String sql = "insert into " +
                    TAB_NAME +//наименование таблицы
                    //номер колонки в БД
                    " (detectorName, " +//2
                    "detectorSerial, " +//3
                    "testerName, " +//4
                    "startExpDate, " +//5
                    "endExpDate, " +//6
                    "countDeselPixel, " +//7
                    "countDeselPixelInLine, " +//8
                    "brakTimes, " +//9
                    "brakFPUCount, " +//10
                    "brakCHannelCount, " +//11
                    "mashtab, " +//12
                    "vr0, " +//13
                    "vva, " +//14
                    "vu4, " +//15
                    "vuc, " +//16
                    "tInt, " +//17
                    "temp, " +//18
                    "modde, " +//19
                    "dir, " +//20
                    "ccc, " +//21
                    "frameArrayList30, " +//22
                    "frameArrayList40, " +//23
                    "shum, " +//24
                    "sredZnach30, " +//25
                    "sredZnach40, " +//26
                    "NEDT) "//27
                    + "values "
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int i = 1;
            preparedStatement = conn.prepareStatement(sql);//подготавливаем запрос

            preparedStatement.setString(i++, exp.getDetectorName());
            preparedStatement.setString(i++, exp.getDetectorSerial());
            preparedStatement.setString(i++, exp.getTesterName());
            preparedStatement.setTimestamp(i++, exp.getStartExpDate());
            preparedStatement.setTimestamp(i++, exp.getEndExpDate());
            preparedStatement.setInt(i++, exp.getCountDeselPixel());
            preparedStatement.setInt(i++, exp.getCountDeselPixelInLine());
            preparedStatement.setDouble(i++, exp.getBrakTimes());
            preparedStatement.setDouble(i++, exp.getBrakFPUCount());
            preparedStatement.setDouble(i++, exp.getBrakCHannelCount());
            preparedStatement.setDouble(i++, exp.getMashtab());
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

            preparedStatement.execute();//выполняем запрос

            //  stmt.executeUpdate(sql);
            System.out.println("Successfully...");
        } catch (SQLException e) {
            //Handle errors for JDBC
            e.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //Закрытие ресурсов
            try {
                if (statement != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }


    public <T> ArrayList<Experiment> readExpFromBD(TypeColums typeColums, T value) {
        ArrayList<Experiment> experiments = new ArrayList<>();
        try {
           System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");
            statement = conn.createStatement();
            StringBuilder sql=new StringBuilder();
            sql.append("select ").append(" * ").append(" from ").append(TAB_NAME);

            if(typeColums!=type_ALL){
                sql.append(" where ").append(typeColums.getValue()).append(" = ").append(value);
            }

            // executing SELECT query
            resultSet = statement.executeQuery(sql.toString());
            //пробегаем по строкам
            while (resultSet.next()) {
                Experiment experiment = new Experiment();
                int i=1;
                experiment.setID(resultSet.getLong(i++));
                experiment.setDetectorName(resultSet.getString(i++));
                experiment.setDetectorSerial(resultSet.getString(i++));
                experiment.setTesterName(resultSet.getString(i++));
                experiment.setStartExpDate(resultSet.getTimestamp(i++));
                experiment.setEndExpDate(resultSet.getTimestamp(i++));
                experiment.setCountDeselPixel(resultSet.getInt(i++));
                experiment.setCountDeselPixelInLine(resultSet.getInt(i++));
                experiment.setBrakTimes(resultSet.getDouble(i++));
                experiment.setBrakFPUCount(resultSet.getDouble(i++));
                experiment.setBrakCHannelCount(resultSet.getDouble(i++));
                experiment.setMashtab(resultSet.getDouble(i++));
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
            System.out.println("Created table in given database...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (statement != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
      return   experiments;
    }//end main
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
    public <T> boolean updateExpFromBD(TypeColums typeColums, T value,Experiment exp) {
        ArrayList<Experiment> experiments = readExpFromBD(typeColums, value);
        Experiment currentExperiment = experiments.get(0);
        HashMap<String,String> maps=equalsMap(currentExperiment,exp);
        try {
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");
            System.out.println("Execute query in given database...");
            statement = conn.createStatement();

            StringBuilder sql=new StringBuilder();
            sql.append("UPDATE ").append(TAB_NAME).append(" set ");
            for (Map.Entry<String, String> map : maps.entrySet()) {
            sql.append(map.getKey()).append(" = ").append(map.getValue());
        }
            sql.append(" where ").append(typeColums.getValue()).append(" = ").append(value);
            // executing SELECT query
            int numColUpdated = statement.executeUpdate(sql.toString());

            System.out.println("Created table in given database..." +numColUpdated);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (statement != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
        return true;
    }

    private HashMap<String, String> equalsMap(Experiment currentExperiment, Experiment exp) {



    return null;}
}
