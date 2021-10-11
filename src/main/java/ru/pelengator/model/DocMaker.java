package ru.pelengator.model;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.fx.ChartViewer;
import ru.pelengator.DetectorViewModel;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static ru.pelengator.App.loadFilePath;

public class DocMaker {
    DetectorViewModel detectorViewModel;
    Map<String, Object> map;
    PDDocument pDDocument;
    PDAcroForm pDAcroForm;
    String fileName = "protokol.pdf";
    String savedFileName = "report.pdf";

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
        HashMap<String, Object> hashMap = new HashMap<>();


        return hashMap;
    }

    /**
     * Метод заполнения отчета
     * @return
     */
    public boolean savePDF() {
        try {
            String path = loadFilePath(fileName);
            File file = new File(path);
            this.pDDocument = PDDocument.load(file);
            this.pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();

            for (Map.Entry<String, Object> item : map.entrySet()) {
                String key = item.getKey();
                PDField field = pDAcroForm.getField(key);
                if (field != null) {
                    System.out.print("Form field with placeholder name: '" + key + "' found");

                    if (field instanceof PDTextField) {
                        System.out.println("(type: " + field.getClass().getSimpleName() + ")");
                        field.setValue((String) item.getValue());
                        System.out.println("value is set to: '" + item.getValue() + "'");

                    } else if (field instanceof PDPushButton) {
                        System.out.println("(type: " + field.getClass().getSimpleName() + ")");
                        PDPushButton pdPushButton = (PDPushButton) field;
                        saveImage(key, 4 + Integer.parseInt(String.valueOf(key.charAt(key.length() - 1))), DetectorViewModel.timeChart.getViewer());

                    } else {
                        System.err.print("Unexpected form field type found with placeholder name: '" + key + "'");
                    }
                } else {
                    System.err.println("No field found with name:" + key);
                }
            }
            //pDAcroForm.flatten(); если нужно убрать отсатки форм
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[dd.MM]_HH.mm_");
            String daTE = simpleDateFormat.format(new Timestamp(System.currentTimeMillis()));
            pDDocument.save(daTE+savedFileName);
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
        field.setValue(value);
        System.out.println("saved " + name + ":" + value);
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
        System.out.println("Image inserted Successfully.");
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
}
