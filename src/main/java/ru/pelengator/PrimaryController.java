package ru.pelengator;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pelengator.model.Connector;

import static ru.pelengator.PropFile.DETECTORNAME;

public class PrimaryController {
    @FXML
    public
    TextField tfield_FIO;
    @FXML
    public
    TextField tfield_FPU;
    @FXML
    public
    TextField tfield_nomer_kriost;
    @FXML
    public
    TextField tfield_nomer_MKS;
    @FXML
    public
    TextField tfield_CRC32;
    @FXML
    public
    Button b_start;
    @FXML
    private void initialize() {

        tfield_CRC32.setText(Long.toHexString(calculate()).toUpperCase());//расчет контр суммы
        createBatLoader();//создание загрузчиков
        /**
         *обработка нажатия кнопки окна
         *Если наименование детектора совпадает, загрузить нужное кно
         */
        b_start.setOnAction(event -> {

            if (tfield_FPU.getText().trim().equals(DETECTORNAME)) {
                Scene scene = new Scene(App.getRoot());
                Stage stage = App.getPrimaryStage();
                stage.hide();
                stage.setResizable(false);
                setOnMidl(stage);
                stage.setScene(scene);
                stage.show();
                //отработка закрытия окна
                stage.setOnCloseRequest(t -> {
                    Connector.driver2.close();
                    Platform.exit();
                    System.exit(0);
                });

                SecondaryController controller = App.getLoader().getController();
                DetectorViewModel detectorViewModel = controller.getDetectorViewModel();
                //установка введенных данных
                detectorViewModel.setTesterFIO(tfield_FIO.getText());
                detectorViewModel.setDetectorName(tfield_FPU.getText().trim());
                detectorViewModel.setNumbersDevises(tfield_nomer_kriost.getText() + "/" + tfield_nomer_MKS.getText());
            }
        });
    }

    /**
     * Создание загрузчиков
     */
    private void createBatLoader() {

        String AppName = "StendIS.jar";
        String loaderName = "StendIS.bat";
        String loaderConfig = "";
        byte[] buf = null;
        File bat = new File(loaderName);
        if (!bat.exists()) {
            loaderConfig = "java -jar " + AppName;
            buf = loaderConfig.getBytes(StandardCharsets.UTF_8);
            try (FileOutputStream fos =
                         new FileOutputStream(bat)) {
                fos.write(buf, 0, buf.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File vbs = new File("StendIS.vbs");
        if (!vbs.exists()) {
            String pass = bat.getAbsolutePath();
            loaderConfig = "Set WshShell = CreateObject(\"WScript.Shell\")\n" +
                    "WshShell.Run chr(34) & \"" +
                    pass +
                    "\" & Chr(34), 0\n" +
                    "Set WshShell = Nothing";
            buf = loaderConfig.getBytes(StandardCharsets.UTF_8);
            try (FileOutputStream fos =
                         new FileOutputStream(vbs)) {
                fos.write(buf, 0, buf.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Установка окна в центр
     *
     * @param stage
     */
    private void setOnMidl(Stage stage) {
        stage.setY(0d);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthScreen = screenSize.getWidth();
        stage.setX(widthScreen - 1610);


    }

    /**
     * Расчет контрольной суммы
     *
     * @return
     */
    private long calculate() {
        CRC32 cs = new CRC32();
        String szPath = "StendIS.jar";
        long s = 0;
        byte[] buf = new byte[8000];
        int nLength = 0;

        try (FileInputStream fis =
                     new FileInputStream(szPath)) {
            while (true) {
                nLength = fis.read(buf);
                if (nLength < 0)
                    break;
                cs.update(buf, 0, nLength);
            }
        } catch (IOException e) {
            System.out.println("CRC =0 так как работаем в IDE");
        }
        s = cs.getValue();
        return s;
    }
}


