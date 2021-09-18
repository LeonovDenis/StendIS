package ru.pelengator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.pelengator.model.Connector;

import javax.swing.*;
import java.io.*;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Parent root;
    private static Scene scene;
    static FXMLLoader loader;//окно рабочее
    static FXMLLoader loader_starter;//окно приветствия

    private static PropFile props;
    private static String ftd3XX;

    static {
        props = new PropFile();
    }

    private static Stage primaryStage;


    @Override
    public void start(Stage stage) throws IOException {
        ftd3XX = loadJarDll("FTD3XX.dll");
        //загрузчик основного окна
        loader = loadFXML("secondaryPage");
        //загрузчик первого окна
        loader_starter = loadFXML("primaryPage");
        //показ первого окна
        root = loader.load();
        scene = new Scene(loader_starter.load());

        stage.setTitle("StendIS");
        stage.setScene(scene);

        stage.show();
        primaryStage = stage;
        /**
         * Обработка закрытия окна
         */
        stage.setOnCloseRequest(t -> {
            Connector.driver2.close();
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Обработчик загрузки конфигурации окна из ресурсов
     *
     * @param fxml Имя файла
     * @return Загрузчик окна
     * @throws IOException в случае отсутствия файла описания
     */
    private static FXMLLoader loadFXML(String fxml) throws IOException {

        return new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            //Обработка ошибок при запуске программы.
            //Вывод в файл и диалоговое окно
            String name = "./Error.txt";//имя файла ошибки
            File file = new File(name);
            try (FileOutputStream fl = new FileOutputStream(file);
                 PrintWriter pw = new PrintWriter(fl);) {
                JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + "Смотри file: " + file.getAbsolutePath());
                e.printStackTrace();//вывод в консоль
                e.printStackTrace(pw);//вывод в файл
            } catch (IOException e1) {
                e1.printStackTrace();//отработка отсутствия файла ошибок
            }
        }
    }

    /**
     * Загрузка DLL файла библиотеки во временную директорию
     *
     * @param name имя библиотеки драйвера USB
     * @return путь расположения файла dll библиотеки
     */
    public static String loadJarDll(String name) {
        InputStream in = App.class.getResourceAsStream(name);//загрузка файла
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = null;
        FileOutputStream fos = null;
        try {
            temp = File.createTempFile(name, "");//создание временного файла
            fos = new FileOutputStream(temp);
            //копирование файла библиотеки
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp.getAbsolutePath();//ссылка на временный файл
    }

    public static Scene getScene() {
        return scene;
    }

    public static void setScene(Scene scene) {
        App.scene = scene;
    }


    public static String getFtd3XX() {
        return ftd3XX;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static FXMLLoader getLoader() {
        return loader;
    }

    public static Parent getRoot() {
        return root;
    }

}