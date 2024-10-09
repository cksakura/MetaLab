package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IO;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;

public class GlycoMicroNovoDrawer {
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	public static JFreeChart drawBlank(){

		double BarWidth = 1d;
		XYSeriesCollection collection = new XYSeriesCollection();

		XYBarRenderer renderer = new XYBarRenderer();

		XYBarDataset dataset = new XYBarDataset(collection, BarWidth);

		NumberAxis numberaxis = new NumberAxis();
		numberaxis.setLabel("m/z");
		NumberAxis domainaxis = new NumberAxis();
		domainaxis.setLabel("Relative Abundance");

		// MyXYPointerAnnotation3[] anns3 = ans.getAnnotations3();
		// for (int i = 0; i < anns3.length; i++) {
		// }
		renderer.setSeriesPaint(0, new Color(0, 0, 0));
		renderer.setSeriesFillPaint(0, null);
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);

		XYPlot xyplot = new XYPlot(dataset, numberaxis, domainaxis, renderer);
		xyplot.setBackgroundPaint(Color.white);
		xyplot.setDomainGridlinePaint(Color.white);
		xyplot.setRangeGridlinePaint(Color.white);
		xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));

		java.awt.Font titleFont = new java.awt.Font("Times", java.awt.Font.BOLD, 16);
		java.awt.Font legentFont = new java.awt.Font("Times", java.awt.Font.PLAIN, 60);
		java.awt.Font labelFont = new java.awt.Font("Times", java.awt.Font.BOLD, 20);
		java.awt.Font tickFont = new java.awt.Font("Times", java.awt.Font.BOLD, 20);

		numberaxis.setAutoRange(false);
		numberaxis.setLabelFont(labelFont);
		NumberTickUnit unit = new NumberTickUnit(250);
		numberaxis.setTickUnit(unit);
		numberaxis.setTickLabelFont(tickFont);

		domainaxis.setAutoRange(false);
		NumberTickUnit unit2 = new NumberTickUnit(0.2);
		domainaxis.setTickUnit(unit2);
		domainaxis.setLabelFont(labelFont);
		domainaxis.setUpperBound(1.1);
		domainaxis.setTickLabelFont(tickFont);

		JFreeChart jfreechart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, xyplot, false);
//		TextTitle texttitle = new TextTitle();
//		jfreechart.setTitle(texttitle);
//		texttitle.setBorder(0.0D, 0.0D, 1.0D, 0.0D);
//		texttitle.setBackgroundPaint(new GradientPaint(0.0F, 0.0F, Color.red, 350F, 0.0F, Color.white, true));
//		texttitle.setExpandToFitSpace(true);
		jfreechart.setBackgroundPaint(Color.WHITE);
		
		return jfreechart;
	
	}
	
	public static JFreeChart createXYBarChart(Peak[] peaks, HashMap<Integer, GlycoPeakNode> nodeMap) {

		double BarWidth = 1d;
		XYSeriesCollection collection = new XYSeriesCollection();

		XYSeries series1 = new XYSeries("Not matched");
		XYSeries series2 = new XYSeries("Matched");
		XYSeries series3 = new XYSeries("Oxonium");
		
		double maxMz = peaks[peaks.length-1].getMz();
		double baseIntensity = 0;
		for (int i = 0; i < peaks.length; i++) {
			if (peaks[i].getIntensity() > baseIntensity)
				baseIntensity = peaks[i].getIntensity();
		}

		XYBarRenderer renderer = new XYBarRenderer();

		for (int i = 0; i < peaks.length; i++) {

			double mz = peaks[i].getMz();
			double inten = peaks[i].getIntensity() / baseIntensity;

			if (nodeMap.containsKey(i)) {
				GlycoPeakNode node = nodeMap.get(i);
				if (node.isOxonium()) {
					series3.add(mz, inten);
					XYPointerAnnotation xya = new XYPointerAnnotation(df2.format(mz), mz, inten, -1.571d);
					xya.setBaseRadius(25d);
					xya.setArrowStroke(new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1,
							new float[] { 3, 1 }, 0));
					xya.setArrowWidth(1.5);
					renderer.addAnnotation(xya);
				} else {
					series2.add(mz, inten);
					String label = "Y" + node.getSid();
					XYPointerAnnotation xya = new XYPointerAnnotation(label, mz, inten, -1.571d);
					xya.setBaseRadius(25d);
					xya.setArrowStroke(new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1,
							new float[] { 3, 1 }, 0));
					xya.setArrowWidth(1.5);
					renderer.addAnnotation(xya);
				}

			}else{
				series1.add(mz, inten);
			}
		}
		collection.addSeries(series1);
		collection.addSeries(series2);
		collection.addSeries(series3);

		XYBarDataset dataset = new XYBarDataset(collection, BarWidth);

		NumberAxis numberaxis = new NumberAxis();
		numberaxis.setLabel("m/z");
		NumberAxis domainaxis = new NumberAxis();
		domainaxis.setLabel("Relative Abundance");

		renderer.setSeriesPaint(0, new Color(0, 0, 0));
		renderer.setSeriesFillPaint(0, null);
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);

		XYPlot xyplot = new XYPlot(dataset, numberaxis, domainaxis, renderer);
		xyplot.setBackgroundPaint(Color.white);
		xyplot.setDomainGridlinePaint(Color.white);
		xyplot.setRangeGridlinePaint(Color.white);
		xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));

		java.awt.Font titleFont = new java.awt.Font("Times", java.awt.Font.BOLD, 16);
		java.awt.Font legentFont = new java.awt.Font("Times", java.awt.Font.PLAIN, 60);
		java.awt.Font labelFont = new java.awt.Font("Times", java.awt.Font.BOLD, 20);
		java.awt.Font tickFont = new java.awt.Font("Times", java.awt.Font.BOLD, 20);

		double xUpperBound = 0;
		double xUnit = 150;
		while (true) {
			if (xUnit * 6 > maxMz) {
				xUpperBound = xUnit * 6.3;
				break;
			} else {
				xUnit += 50;
			}
		}
		
		numberaxis.setAutoRange(false);
		numberaxis.setLabelFont(labelFont);
		NumberTickUnit unit = new NumberTickUnit(xUnit);
		numberaxis.setTickUnit(unit);
//		numberaxis.setLowerBound(xLowerBound);
		numberaxis.setUpperBound(xUpperBound);
		numberaxis.setTickLabelFont(tickFont);

		domainaxis.setAutoRange(false);
		NumberTickUnit unit2 = new NumberTickUnit(0.2);
		domainaxis.setTickUnit(unit2);
		domainaxis.setLabelFont(labelFont);
		domainaxis.setUpperBound(1.1);
		domainaxis.setTickLabelFont(tickFont);

		JFreeChart jfreechart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, xyplot, false);
//		TextTitle texttitle = new TextTitle();
//		jfreechart.setTitle(texttitle);
//		texttitle.setBorder(0.0D, 0.0D, 1.0D, 0.0D);
//		texttitle.setBackgroundPaint(new GradientPaint(0.0F, 0.0F, Color.red, 350F, 0.0F, Color.white, true));
//		texttitle.setExpandToFitSpace(true);
		jfreechart.setBackgroundPaint(Color.WHITE);
		
		return jfreechart;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
