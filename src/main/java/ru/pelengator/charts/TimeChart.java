package ru.pelengator.charts;

import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;


public class TimeChart implements ChartMouseListenerFX {

    ChartViewer viewer;
    static XYPlot plot;

    private static TimeSeriesCollection dataset;

    /**
     * Создаем датасет
     *
     * @return The dataset.
     */
    private static XYDataset createDataset() {
        TimeSeries s1 = new TimeSeries("Работа МКС");
        TimeSeries s2 = new TimeSeries("Работа ФПУ");
        dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
        dataset.addSeries(s2);
        return dataset;
    }

    /**
     * Создаем график
     *
     * @param dataset датасет
     * @return График
     */
    private static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Наработка", null, "Напряжение, В", dataset);
        chart.setBackgroundPaint(Color.white);
        plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getRangeAxis().setAutoRange(true);
        DateAxis domainAxis = (DateAxis)plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("[dd.MM]HH:mm"));
        domainAxis.setAutoRange(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);
            renderer.setDrawSeriesLineAsPath(true);
            // set the default stroke for all series
            renderer.setAutoPopulateSeriesStroke(false);
            renderer.setDefaultStroke(new BasicStroke(3.0f));
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, new Color(24, 123, 58));
        }
        return chart;
    }
    /**
     * Точка входа в класс
     *
     */
    public void start() {
        XYDataset data = createDataset();

        JFreeChart chartt =createChart(data);
        viewer = new ChartViewer(chartt);
        viewer.addChartMouseListener(this);
        Scene scene = new Scene(viewer);
        Stage newWindow = new Stage();
        newWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                windowEvent.consume();
            }
        });
        newWindow.setTitle("");
        newWindow.setScene(scene);
        newWindow.show();
    }

    @Override
    public void chartMouseClicked(ChartMouseEventFX event) {
        // ignore
    }

    @Override
    public void chartMouseMoved(ChartMouseEventFX event) {
        // ignore
    }

    public static TimeSeriesCollection getDataset() {
        return dataset;
    }

    public static void setDataset(TimeSeriesCollection dataset) {
        TimeChart.dataset = dataset;
    }

    public ChartViewer getViewer() {
        return viewer;
    }

    public static XYPlot getPlot() {
        return plot;
    }

    public static void setPlot(XYPlot plot) {
        TimeChart.plot = plot;
    }
}
