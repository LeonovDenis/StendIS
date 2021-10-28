package ru.pelengator.model;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;

public class Order {

    Experiment VZN_pr;
    Experiment VZN_ob;
    Experiment BPS;

    JFreeChart[] chartViewer=new JFreeChart[6];

    public Experiment getVZN_pr() {
        return VZN_pr;
    }

    public void setVZN_pr(Experiment VZN_pr) {
        this.VZN_pr = VZN_pr;
    }

    public Experiment getVZN_ob() {
        return VZN_ob;
    }

    public void setVZN_ob(Experiment VZN_ob) {
        this.VZN_ob = VZN_ob;
    }

    public Experiment getBPS() {
        return BPS;
    }

    public void setBPS(Experiment BPS) {
        this.BPS = BPS;
    }

    public JFreeChart[] getChartViewer() {
        return chartViewer;
    }

    public void setChartViewer(JFreeChart[] chartViewer) {
        this.chartViewer = chartViewer;
    }
}
