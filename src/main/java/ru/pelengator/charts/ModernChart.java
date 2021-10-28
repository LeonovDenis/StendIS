package ru.pelengator.charts;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static ru.pelengator.PropFile.MASHTAB;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.decimal4j.util.DoubleRounder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.overlay.CrosshairOverlayFX;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Класс вспомогательных графиков
 */
public class ModernChart {
    static JFreeChart charttt;
    public static final int TIPE_Dataset30_40 = 0;
    public static final int TIPE_DatasetNEDT = 1;
    public static final int TIPE_DatasetNEDT_RASP = 2;
    /**
     * Вложенный класс панели графика
     */
    public static class MyPane extends StackPane implements ChartMouseListenerFX {

        private ChartViewer chartViewer;//окно для вывода графика
        private Crosshair xCrosshair;//перекрестие
        private Crosshair yCrosshair;//перекрестие
        private Crosshair yCrosshair2;//перекрестие

        /**
         * Конструктор
         *
         * @param title  Заголовок
         * @param xLable Подпись по оси Х
         * @param yLable Подпись по оси У
         * @param start  Первое значение
         * @param end    Последнее значение
         * @param tipe   Тип графика
         * @param mass   Массив данных
         */
        public MyPane(String title, String xLable, String yLable, int start, int end, int tipe, double[]... mass) {
            XYDataset dataset = null;
            if (tipe == TIPE_Dataset30_40) {
                dataset = createDataset30_40(start, end, mass);
            } else if (tipe == TIPE_DatasetNEDT) {
                dataset = createDatasetNEDT(start, end, mass);
            } else if (tipe == TIPE_DatasetNEDT_RASP) {
                dataset = createDatasetNEDT_RASP(start, end, mass);
            }
            JFreeChart chart = createChart(dataset, title, xLable, yLable);
            charttt=chart;
            XYPlot xyPlot = chart.getXYPlot();
            if (tipe == TIPE_DatasetNEDT_RASP) {
                xyPlot.getDomainAxis().setLowerBound(start - ((mass[0].length - 1) / 2) - 10);
                xyPlot.getDomainAxis().setUpperBound(end - ((mass[0].length - 1) / 2) + 10);
            } else if (tipe == TIPE_Dataset30_40) {
                xyPlot.getDomainAxis().setLowerBound(start - 10);
                xyPlot.getDomainAxis().setUpperBound(end + 10);
            } else {
                xyPlot.getDomainAxis().setLowerBound(start - 10);
                xyPlot.getDomainAxis().setUpperBound(end + 10);
            }

            this.chartViewer = new ChartViewer(chart);
            this.chartViewer.addChartMouseListener(this);
            getChildren().add(this.chartViewer);
            CrosshairOverlayFX crosshairOverlay = new CrosshairOverlayFX();
            this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.xCrosshair.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.xCrosshair.setLabelVisible(true);
            this.yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.yCrosshair.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.yCrosshair.setLabelVisible(true);
            this.yCrosshair2 = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.yCrosshair2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.yCrosshair2.setLabelVisible(true);
            this.yCrosshair2.setLabelFont(new Font("Tahoma", 0, 15));
            this.yCrosshair2.setLabelOutlineVisible(false);
            this.yCrosshair2.setLabelXOffset(5);
            this.yCrosshair2.setLabelYOffset(5);
            this.yCrosshair2.setLabelBackgroundPaint(new Color(0, 0, 0, 0));
            this.yCrosshair.setLabelFont(new Font("Tahoma", 0, 15));
            this.xCrosshair.setLabelFont(new Font("Tahoma", 0, 15));
            this.xCrosshair.setLabelOutlineVisible(false);
            this.xCrosshair.setLabelXOffset(5);
            this.xCrosshair.setLabelYOffset(5);
            this.yCrosshair.setLabelOutlineVisible(false);
            this.yCrosshair.setLabelXOffset(5);
            this.yCrosshair.setLabelYOffset(5);
            this.xCrosshair.setLabelBackgroundPaint(new Color(0, 0, 0, 0));
            this.yCrosshair.setLabelBackgroundPaint(new Color(0, 0, 0, 0));
            crosshairOverlay.addDomainCrosshair(xCrosshair);
            crosshairOverlay.addRangeCrosshair(yCrosshair);
            crosshairOverlay.addRangeCrosshair(yCrosshair2);
            Platform.runLater(() -> {
                this.chartViewer.getCanvas().addOverlay(crosshairOverlay);
            });
        }

        @Override
        public void chartMouseClicked(ChartMouseEventFX event) {
            // ignore
        }

        @Override
        public void chartMouseMoved(ChartMouseEventFX event) {
            Rectangle2D dataArea = this.chartViewer.getCanvas().getRenderingInfo().getPlotInfo().getDataArea();
            JFreeChart chart = event.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
                    RectangleEdge.BOTTOM);
            // убирает перекрестие если указатель за пределами графика
            if (!xAxis.getRange().contains(x)) {
                x = Double.NaN;
            }
            double y = DatasetUtils.findYValue(plot.getDataset(), 0, (int) x);
            this.xCrosshair.setValue((int) x);
            this.yCrosshair.setValue(y);

            if (plot.getDataset().getSeriesCount() == 2) {
                double y2 = DatasetUtils.findYValue(plot.getDataset(), 1, (int) x);
                this.yCrosshair2.setValue(y2);
            }
        }

    }

    /**
     * Создание датасета данных для 30 и 40
     *
     * @param start
     * @param end
     * @param mass
     * @return
     */
    private static XYDataset createDataset30_40(int start, int end, double[]... mass) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        int gr = 30;
        for (double[] a :
                mass) {
            if (a == null) {
                return dataset;
            }
            XYSeries series = new XYSeries(gr + " Градусов");
            for (int x = start - 1; x < end; x++) {
                double v = a[x] * MASHTAB;
                double round = DoubleRounder.round(v, 3);
                series.add(x + 1.0, round);
            }
            dataset.addSeries(series);
            gr += 10;
        }
        return dataset;
    }

    /**
     * Создание датасета для НЕДТ
     *
     * @param start
     * @param end
     * @param mass
     * @return
     */
    private static XYDataset createDatasetNEDT(int start, int end, double[]... mass) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (double[] a :
                mass) {
            if (a == null) {
                return dataset;
            }
            XYSeries series = new XYSeries(" ЭШРТ");
            for (int x = start - 1; x < end; x++) {
                double v = a[x] * 1000;
                double round = DoubleRounder.round(v, 3);
                series.add(x + 1, round);
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    /**
     * Создание датасета распределения
     *
     * @param start
     * @param end
     * @param mass
     * @return
     */
    private static XYDataset createDatasetNEDT_RASP(int start, int end, double[]... mass) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (double[] a :
                mass) {
            if (a == null) {
                return dataset;
            }
            XYSeries series = new XYSeries(" Распределение ЭШРТ");
            for (int x = start; x < end; x++) {
                series.add(x - (a.length - 1) / 2, a[x]);
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    /**
     * Создание графика
     *
     * @param dataset
     * @param title
     * @param xLable
     * @param yLable
     * @return
     */
    private static JFreeChart createChart(XYDataset dataset, String title, String xLable, String yLable) {
        JFreeChart chart = ChartFactory.createXYBarChart(title, xLable, false, yLable, (IntervalXYDataset) dataset);
        return chart;
    }

    /**
     * Точка входа в класс
     *
     * @param winTitle
     * @param title
     * @param xLable
     * @param yLable
     * @param start
     * @param end
     * @param tipe
     * @param mass
     */
    public void start(String winTitle, String title, String xLable, String yLable, int start, int end, int tipe, double[]... mass) {
        Scene scene = new Scene(new MyPane(title, xLable, yLable, start, end, tipe, mass), 1600, 800);
        Stage newWindow = new Stage();
        newWindow.setTitle(winTitle);
        newWindow.setScene(scene);
        newWindow.show();
    }

    public JFreeChart startView(String winTitle, String title, String xLable, String yLable, int start, int end, int tipe, double[]... mass) {
        MyPane myPane = new MyPane(title, xLable, yLable, start, end, tipe, mass);

       return getCharttt();
    }

    public static JFreeChart getCharttt() {
        return charttt;
    }

    public static void setCharttt(JFreeChart charttt) {
        ModernChart.charttt = charttt;
    }
}
