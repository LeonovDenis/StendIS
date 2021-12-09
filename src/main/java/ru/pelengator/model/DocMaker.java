package ru.pelengator.model;

import at.favre.lib.bytes.Bytes;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.decimal4j.util.DoubleRounder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import ru.pelengator.DetectorViewModel;

import java.awt.*;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static ru.pelengator.App.loadFilePath;
import static ru.pelengator.PropFile.MASHTAB;
import static ru.pelengator.PropFile.ONE_K;

public class DocMaker {
    DetectorViewModel detectorViewModel;
    Map<String, Object> map;
    PDDocument pDDocument;
    PDAcroForm pDAcroForm;
    PDResources pDResources;
    String fileName = "protokol.pdf";
    String savedFileName = "report.pdf";
    double koefTerm=2.054;
    double uTerm=1034;

    public DocMaker(DetectorViewModel detectorViewModel) {
        this.detectorViewModel = detectorViewModel;
        map = createList();
    }

    /**
     * Набивка полей
     *
     * @return
     */
    private Map<String, Object> createList() {
        Order order = DetectorViewModel.getOrder();
        HashMap<String, Object> hashMap = new HashMap<>();
        //печать серийных номеров
        String serial = order.getVZN_pr().getDetectorSerial();
        String[] serialSplited = serial.split("/");
        //детектор
        for (int i = 0; i < 10; i++) {
            hashMap.put("serial_" + (i + 1), serialSplited[0]);
        }
        hashMap.put("serial", serialSplited[0]);
        //МКС
        if (serialSplited.length >= 3) {
            hashMap.put("serial_M", serialSplited[2]);
        }
        //ФПУ
        hashMap.put("serial_F", serialSplited[1]);
        hashMap.put("serial_F1", serialSplited[1]);
        //Дата
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
        String format = simpleDateFormat.format(new Timestamp(System.currentTimeMillis()));
        hashMap.put("data", format);
        //фио
        hashMap.put("fio", order.getVZN_pr().getTesterName());
        //температура хол зоны
        int temp = order.getVZN_pr().getTemp();
        int i = (int) (77 + ((uTerm - temp) /koefTerm));
        hashMap.put("temp", String.valueOf(i));
        //режимы+шум+недт
        Experiment exp = null;
        for (int j = 0; j < 3; j++) {
            if (j == 0) {
                if (order.getVZN_pr() == null) {
                    continue;
                } else {
                    exp = order.getVZN_pr();
                }
            } else if (j == 1) {
                if (order.getVZN_ob() == null) {
                    continue;
                } else {
                    exp = order.getVZN_ob();
                }
            } else if (j == 2) {
                if (order.getBPS() == null) {
                    continue;
                } else {
                    exp = order.getBPS();
                }
            }
            try {
                hashMap.put("vr0_" + (j + 1), String.valueOf(DoubleRounder.round((exp.getVr0()*1.0) / ONE_K, 3)));
                hashMap.put("vva_" + (j + 1), String.valueOf(DoubleRounder.round((exp.getVva()*1.0) / ONE_K, 3)));
                hashMap.put("uc_" + (j + 1), String.valueOf(DoubleRounder.round((exp.getVuc()*1.0) / ONE_K, 3)));
                hashMap.put("vu4_" + (j + 1), String.valueOf(DoubleRounder.round((exp.getVu4()*1.0) / ONE_K, 3)));

                hashMap.put("shum_" + (j + 1), String.valueOf(DoubleRounder.round(exp.getShum() * MASHTAB, 3)));
                hashMap.put("nedt_" + (j + 1), String.valueOf(DoubleRounder.round(exp.getNEDT() * ONE_K, 2)));
            } catch (Exception e) {
                continue;
            }
        }
        //обработка деселекции
        setPixelFilds(order, hashMap);


        //вставка графиков
        for (int j = 0; j < 6; j++) {
            hashMap.put("chart_" + (j + 1), order.getChartViewer()[j]);
        }
        //подписи к графикам
        //шум
        hashMap.put("chart_1_1", String.valueOf(order.getVZN_pr().getCountDeselPixel()));
        hashMap.put("chart_2_1", String.valueOf(order.getVZN_pr().getCountDeselPixel()));
        hashMap.put("chart_3_1", String.valueOf(order.getBPS().getCountDeselPixel()));

        hashMap.put("chart_1_2", hashMap.get("shum_1"));
        hashMap.put("chart_2_2", hashMap.get("shum_2"));
        hashMap.put("chart_3_2", hashMap.get("shum_3"));

        hashMap.put("chart_1_3", String.valueOf(getCountSum(order.getVZN_pr())));
        hashMap.put("chart_2_3", String.valueOf(getCountSum(order.getVZN_ob())));

        //деселектировано элементов

        hashMap.put("chart_4_1", hashMap.get("chart_1_1"));
        hashMap.put("chart_5_1", hashMap.get("chart_2_1"));
        hashMap.put("chart_6_1", hashMap.get("chart_3_1"));

        hashMap.put("chart_4_2", hashMap.get("nedt_1"));
        hashMap.put("chart_5_2", hashMap.get("nedt_2"));
        hashMap.put("chart_6_2", hashMap.get("nedt_3"));

        hashMap.put("chart_4_3", String.valueOf(getCountNEDT(order.getVZN_pr())));
        hashMap.put("chart_5_3", String.valueOf(getCountNEDT(order.getVZN_ob())));

        return hashMap;
    }

    private void setPixelFilds(Order order, HashMap<String, Object> hashMap) {
        byte[] matrix = order.getVZN_pr().getMatrix();
        int count = 0;
        int count1 = 0;
        int count2 = 0;
        for (int i = 0; i < 144; i++) {
            Bytes bytee = Bytes.from(matrix[i]);
            BitSet bitSet = bytee.toBitSet();
            int cardinality = bitSet.cardinality();
            if (cardinality < 8) {

                if (cardinality == 7) {
                    count1++;
                    //один пиксель
                    count++;
                    pf(i, bitSet, count, hashMap);
                }
                if (cardinality == 6) {
                    count2++;
                    //два пикселя
                    for (int j = 0; j < 2; j++) {
                        count++;
                        BitSet pf = pf(i, bitSet, count, hashMap);
                        bitSet = pf;
                    }
                }
            }
        }
        hashMap.put("isp_13", String.valueOf(count));
        if(count<=12){
            hashMap.put("isp_13_1", "Соотв.");
        }else{
            hashMap.put("isp_13_1", "Не соотв.");
        }
        hashMap.put("isp_10", String.valueOf(count1));
        hashMap.put("isp_11", String.valueOf(count2));
        hashMap.put("isp_12", "0");
        hashMap.put("isp_12_1", "Соотв.");
    }

    private BitSet pf(int i, BitSet bitSet, int count, HashMap<String, Object> hashMap) {
        String sec = "";
        String ch = "";
        String num = "";
        String pix = "";
        int koef = 0;
        int lineP = 0;
        int id = 0;
        if (!bitSet.get(7)) {
            num = "1";
            pix = "11111110";
            koef = 0;
            id = 7;
        } else if (!bitSet.get(6)) {
            num = "2";
            pix = "11111101";
            koef = 0;
            id = 6;
        } else if (!bitSet.get(5)) {
            num = "3";
            pix = "11111011";
            koef = 0;
            id = 5;
        } else if (!bitSet.get(4)) {
            num = "4";
            pix = "11110111";
            koef = 0;
            id = 4;
        } else if (!bitSet.get(3)) {
            num = "4";
            pix = "11101111";
            koef = 1;
            id = 3;
        } else if (!bitSet.get(2)) {
            num = "3";
            pix = "11011111";
            koef = 1;
            id = 2;
        } else if (!bitSet.get(1)) {
            num = "2";
            pix = "10111111";
            koef = 1;
            id = 1;
        } else if (!bitSet.get(0)) {
            num = "1";
            pix = "01111111";
            koef = 1;
            id = 0;
        }
        hashMap.put("des_num_" + (count), num);
        hashMap.put("des_pix_" + (count), pix);
        if (i < 72) {
            sec = "A";
        } else {
            sec = "B";
        }
        hashMap.put("des_sec_" + (count), sec);
        if (koef == 0) {
            lineP = (i * 2 + 1);
            ch = String.valueOf(lineP);
        } else {
            lineP = (i * 2 + 2);
            ch = String.valueOf(lineP);
        }
        hashMap.put("des_ch_" + (count), ch);
        hashMap.put("des_line_" + (count), getLINE(lineP,i));
        bitSet.set(id);
        return bitSet;
    }

    private String getLINE(int lineP,int i) {
        String ret = "";
        String[] strings = {"001000001", "001000011", "001000111", "001001111", "001011111", "001111111", "001111110",
                "001111100", "001111000", "001110000", "001100000", "001000000", "011000001", "011000011", "011000111",
                "011001111", "011011111", "011111111", "011111110", "011111100", "011111000", "011110000", "011100000",
                "011000000", "111000001", "111000011", "111000111", "111001111", "111011111", "111111111", "111111110",
                "111111100", "111111000", "111110000", "111100000", "111000000", "110000001", "110000011", "110000111",
                "110001111", "110011111", "110111111", "110111110", "110111100", "110111000", "110110000", "110100000",
                "110000000", "100000001", "100000011", "100000111", "100001111", "100011111", "100111111", "100111110",
                "100111100", "100111000", "100110000", "100100000", "100000000", "000000001", "000000011", "000000111",
                "000001111", "000011111", "000111111", "000111110", "000111100", "000111000", "000110000", "000100000", "000000000"};

        if (i < 72) {
            ret = "0" + strings[i/2] + "1";
        } else {
            ret = "1" + strings[i/2] + "0";
        }
        return ret;
    }

    /**
     * Метод заполнения отчета
     *
     * @return
     */
    public boolean savePDF() {
        try {
            String path = loadFilePath(fileName);
            File file = new File(path);
            this.pDDocument = PDDocument.load(file);
            this.pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();
            pDResources=pDAcroForm.getDefaultResources();

            for (Map.Entry<String, Object> item : map.entrySet()) {
                String key = item.getKey();
                PDField field = pDAcroForm.getField(key);
                if (field != null) {
                      System.out.print("Form field with placeholder name: '" + key + "' found");

                    if (field instanceof PDTextField) {
                    //       System.out.println("(type: " + field.getClass().getSimpleName() + ")");
                        saveField(key, (String) item.getValue());
                    //       System.out.println("value is set to: '" + item.getValue() + "'");

                    } else if (field instanceof PDPushButton) {
                   //         System.out.println("(type: " + field.getClass().getSimpleName() + ")");
                        //    PDPushButton pdPushButton = (PDPushButton) field;
                        saveImage2(key, 4 + Integer.parseInt(String.valueOf(key.charAt(key.length() - 1))), (JFreeChart) item.getValue());
                    } else {
                    //    System.err.print("Unexpected form field type found with placeholder name: '" + key + "'");
                    }
                } else {
                 //   System.err.println("No field found with name:" + key);
                }
            }

            //pDAcroForm.flatten(); если нужно убрать отсатки форм
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[dd.MM]_HH.mm_");
            String daTE = simpleDateFormat.format(new Timestamp(System.currentTimeMillis()));
            pDDocument.save(daTE + savedFileName);
            pDDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Сохранение поля
     *
     * @param name  имя поля
     * @param value Значение
     * @throws IOException
     */
    public void saveField(String name, String value) throws IOException {
        PDField field = pDAcroForm.getField(name);
        pDAcroForm.setNeedAppearances(true);
        pDAcroForm.refreshAppearances();
        field.setValue(value);
        //   System.out.println("saved " + name + ":" + value);
    }

    /**
     * Сохранение графика
     *
     * @param name        имя поля
     * @param pageNumb    номер страницы
     * @param chartViewer график
     * @throws IOException
     */
    public void saveImage(String name, int pageNumb, ChartViewer chartViewer) throws IOException {
        ByteArrayOutputStream image = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(image, chartViewer.getCanvas().getChart(), (int) chartViewer.getWidth(),
                (int) chartViewer.getHeight());
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(pDDocument, image.toByteArray(),
                "myImage.jpg");
        setField(pDDocument, name, pageNumb, pdImage);
       //  System.out.println("Image inserted Successfully.");
    }

    public void saveImage2(String name, int pageNumb, JFreeChart chart) throws IOException {

        chart.getPlot().setBackgroundPaint(Color.lightGray);

      //  XYPlot xyPlot = chart.getXYPlot();
      //  XYItemRenderer renderer = xyPlot.getRenderer();
      //  renderer.setSeriesPaint(0, Color.BLACK);

        ByteArrayOutputStream image = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(image, chart, 1600,
                800);
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(pDDocument, image.toByteArray(),
                "myImage.jpg");
        setField(pDDocument, name, pageNumb, pdImage);
         //   System.out.println("Image inserted Successfully.");
    }

    /**
     * Вставка картинки в форму
     *
     * @param document документ
     * @param name     имя формы
     * @param page     номер страницы с 0
     * @param image    картинка
     * @throws IOException
     */
    public void setField(PDDocument document, String name, int page, PDImageXObject image)
            throws IOException {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField(name);
        if (field != null) {
            PDRectangle rectangle = getFieldArea(field);
            float height = rectangle.getHeight();
            float width = rectangle.getWidth();
            float x = rectangle.getLowerLeftX();
            float y = rectangle.getLowerLeftY();
            try (PDPageContentStream contentStream = new PDPageContentStream(document,
                    document.getPage(page), PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(image, x, y, width, height);
            }
        }
    }

    /**
     * Получение габаритов поля
     *
     * @param field имя поля
     * @return
     */
    private PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }

    private int getCountSum(Experiment ex) {
        int countShumyashih = 0;
        for (int i = 0; i < 288; i++) {
            if ((ex.getDataArraySKO30()[i]) > (ex.getShum() * ex.getBrakTimes())) {
                countShumyashih++;
            }
        }
        return countShumyashih;
    }

    private int getCountNEDT(Experiment ex) {
        int countShumyashih = 0;

        for (int i = 0; i < 288; i++) {
            if ((ex.getDataArrayNEDT()[i]) > (ex.getBrakCHannelCount())) {
                countShumyashih++;
            }
        }
        return countShumyashih;
    }
}
