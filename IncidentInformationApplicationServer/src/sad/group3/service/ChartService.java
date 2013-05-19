package sad.group3.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Spacer;
import org.jfree.chart.TextTitle;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer3D;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.data.DefaultPieDataset;

import com.google.gson.Gson;

import sad.group3.domain.DistrictIncQty;
import sad.group3.domain.MonthIncQty;
import sad.group3.domain.PeriodCallQty;
import sad.group3.domain.SectorIncQty;
import sad.group3.utils.DES;
import sad.group3.utils.SqlHelper;
import sad.group3.utils.StreamTool;

public class ChartService {

	// ----------------------------------��ͼ���-------------------------------------------------
	private DefaultPieDataset data = new DefaultPieDataset();

	public void setPieChartValue(String key, double value) {
		data.setValue(key, value);
	}

	public String generatePieChart(String title, HttpSession session,
			PrintWriter pw,int w, int h) {
		String filename = null;
		try {
			// ����chart����
			JFreeChart chart = ChartFactory.createPieChart(title, data, true,
					true, false);
			PiePlot plot = (PiePlot) chart.getPlot();
			plot.setSectionLabelType(PiePlot.PERCENT_LABELS);
			plot.setPercentFormatString("#,###0.0#%");
			plot.setInsets(new Insets(0, 5, 5, 5));
			plot.setOutlineStroke(new BasicStroke(1.5f));
			plot.setNoDataMessage("No Data Available Now!");
			plot.setCircular(true);
			plot.setToolTipGenerator(new StandardPieToolTipGenerator());
			Font font = new Font("宋体", Font.CENTER_BASELINE, 20);
			plot.setSectionLabelFont(new Font("宋体", Font.CENTER_BASELINE, 15));
			plot.setSeriesLabelFont(new Font("宋体", Font.CENTER_BASELINE, 30));
			
			chart.setBackgroundPaint(java.awt.Color.white);// ����ͼƬ�ı���ɫ
			TextTitle _title = new TextTitle(title);
			_title.setFont(font);
			chart.setTitle(_title);

			// ����ɵ�ͼƬ�ŵ���ʱĿ¼
			ChartRenderingInfo info = new ChartRenderingInfo(
					new StandardEntityCollection());
			// 500��ͼƬ���ȣ�300��ͼƬ�߶�
			filename = ServletUtilities.saveChartAsPNG(chart, w, h, info,
					session);
			ChartUtilities.writeImageMap(pw, filename, info);
			pw.flush();
			pw.close();
		} catch (Exception e) {
			System.out.println("Exception - " + e.toString());
			e.printStackTrace(System.out);
			filename = "public_error_500x300.png";
		}
		return filename;
	}

	// ��ȡ��ͼ�е����,ÿ��district��ĳһ���incident Qty
	public List<SectorIncQty> getSectorsData(String reportYear) {
		List<SectorIncQty> al = new ArrayList<SectorIncQty>();
		String sql = "select sector.sector_name,count(inc_id) as qty from (select sector_name from district group by sector_name) sector left join (select district.sector_name,inc_id from incident,district where incident.dist_num=district.dist_num and to_char(inc_date,'YYYY')=?) incidents on incidents.sector_name=sector.sector_name group by sector.sector_name";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { reportYear });
			while (rs.next()) {
				SectorIncQty sectorIncQty = new SectorIncQty();
				sectorIncQty.setSectorName(rs.getString("sector_name"));
				sectorIncQty.setQuantity(rs.getString("qty"));
				sectorIncQty.setYear(reportYear);
				sectorIncQty.setDistrictIncQties(this.getDistrictsData(rs
						.getString("sector_name"), reportYear));
				al.add(sectorIncQty);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return al;
	}

	// ��ȡÿ��disctrict��ĳһ��ķ�������
	public List<DistrictIncQty> getDistrictsData(String sectorName,
			String reportYear) {
		List<DistrictIncQty> al = new ArrayList<DistrictIncQty>();
		String sql = "select district.dist_num, count(inc_id) as qty from district left join incident on incident.dist_num=district.dist_num and to_char(incident.inc_date,'yyyy')=? where district.sector_name=? group by district.dist_num order by district.dist_num";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { reportYear,
					sectorName });
			while (rs.next()) {
				DistrictIncQty districtIncQty = new DistrictIncQty();
				districtIncQty.setDistNum(rs.getString("dist_num"));
				districtIncQty.setQuantity(rs.getString("qty"));
				al.add(districtIncQty);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return al;
	}

	public byte[] getCharPic(String filePath) throws Exception {
		File file = new File(filePath);
		return StreamTool.read(new FileInputStream(file));
	}

	// ----------------------------------------------------��״ͼ���-------------------------------------------------
	private DefaultCategoryDataset dataset = new DefaultCategoryDataset();

	public void setBarChartValue(String key1, double value) {
		dataset.addValue(value, "", key1);
	}

	public String generateBarChart(String title, String itemName,
			String sizeName, HttpSession session, PrintWriter pw, int w, int h) {
		String filename = null;
		// ����
		JFreeChart chart = ChartFactory.createBarChart3D(title, itemName,
				sizeName, dataset, PlotOrientation.VERTICAL, false, false,
				false);
		// �������屳��ɫ
		chart.setBackgroundPaint(new Color(0xE1E1E1));
		// �ɸ��plot��setBackgroundPaint(Color.White)������񱳾�ɫ
		// �ɸ��plot.setDomainGridlinePaint(Color.pink)������������ɫ
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setOutlineStroke(new BasicStroke(1.5f));
		// ����Y����ʾ����
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		CategoryAxis domainAxis = plot.getDomainAxis();
		// ���þ���ͼƬ��˾���
		domainAxis.setLowerMargin(0.05);
		// ��ʾÿ�������ֵ�����޸ĸ����ֵ���������
		BarRenderer3D renderer = new BarRenderer3D();
		renderer
				.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		// ���������ɫ
		renderer.setSeriesPaint(0, new Color(0xff00));
		plot.setRenderer(renderer);

		try {
			/*------�õ�chart�ı���·��----*/
			ChartRenderingInfo info = new ChartRenderingInfo(
					new StandardEntityCollection());
			filename = ServletUtilities.saveChartAsPNG(chart, w, h, info,
					session);
			/*------ʹ��printWriter���ļ�д��----*/
			ChartUtilities.writeImageMap(pw, filename, info, true);
			pw.flush();
		} catch (IOException e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
		}
		return filename;
	}

	// ���ĳʱ��εĵ绰����
	public List<MonthIncQty> getMonthData(String year) {
		List<MonthIncQty> monthIncQties = new ArrayList<MonthIncQty>();

		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("Jan", "0");
		hm.put("Feb", "0");
		hm.put("Mar", "0");
		hm.put("Apr", "0");
		hm.put("May", "0");
		hm.put("Jun", "0");
		hm.put("Jul", "0");
		hm.put("Aug", "0");
		hm.put("Sep", "0");
		hm.put("Oct", "0");
		hm.put("Nov", "0");
		hm.put("Dec", "0");

		String sql = "select to_char(inc_date,'Mon','nls_date_language = ''American''') as month,count(inc_id) as qty from incident where to_char(inc_date,'yyyy')=? group by to_char(inc_date,'Mon','nls_date_language = ''American''')";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { year });
			while (rs.next()) {
				if (hm.containsKey(rs.getString("month"))) {
					hm.put(rs.getString("month"), rs.getString("qty"));
				}
			}

			monthIncQties.add(new MonthIncQty("Jan", hm.get("Jan")));
			monthIncQties.add(new MonthIncQty("Feb", hm.get("Feb")));
			monthIncQties.add(new MonthIncQty("Mar", hm.get("Mar")));
			monthIncQties.add(new MonthIncQty("Apr", hm.get("Apr")));
			monthIncQties.add(new MonthIncQty("May", hm.get("May")));
			monthIncQties.add(new MonthIncQty("Jun", hm.get("Jun")));
			monthIncQties.add(new MonthIncQty("Jul", hm.get("Jul")));
			monthIncQties.add(new MonthIncQty("Aug", hm.get("Aug")));
			monthIncQties.add(new MonthIncQty("Sep", hm.get("Sep")));
			monthIncQties.add(new MonthIncQty("Oct", hm.get("Oct")));
			monthIncQties.add(new MonthIncQty("Nov", hm.get("Nov")));
			monthIncQties.add(new MonthIncQty("Dec", hm.get("Dec")));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return monthIncQties;
	}

	// ----------------------------------------------------����ͼ���-------------------------------------------------
	
	private DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();

	public void setLineChartValue(String key1, double value) {
		dataset1.addValue(value, "", key1);
	}

	public String generateLineChart(String title, String itemName,
			String sizeName, HttpSession session, PrintWriter pw, int w, int h) {
		String filename = null;
		// ����
		JFreeChart chart = ChartFactory.createLineChart(title, itemName,
				sizeName, dataset1, PlotOrientation.VERTICAL, false, false,
				false);
		// �������屳��ɫ
		chart.setBackgroundPaint(new Color(0xE1E1E1));
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setOutlineStroke(new BasicStroke(1.5f));
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 0.2, 0.2, 0.2, 0.2));
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinesVisible(true);

		// ����Y����ʾ����
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		CategoryAxis domainAxis = plot.getDomainAxis();
		ValueAxis valueAxis = plot.getRangeAxis();
		// ���þ���ͼƬ��˾���
		domainAxis.setCategoryMargin(0.5);
		domainAxis.setLowerMargin(0.05);
		domainAxis.setUpperMargin(0.05);
		valueAxis.setLowerMargin(0.3);
		valueAxis.setUpperMargin(0.3);

		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
				.getRenderer();
		renderer.setBaseItemLabelsVisible(true);
		renderer.setDrawShapes(true);
		renderer
				.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

		plot.setRenderer(renderer);

		try {
			/*------�õ�chart�ı���·��----*/
			ChartRenderingInfo info = new ChartRenderingInfo(
					new StandardEntityCollection());
			filename = ServletUtilities.saveChartAsPNG(chart, w, h, info,
					session);
			/*------ʹ��printWriter���ļ�д��----*/
			ChartUtilities.writeImageMap(pw, filename, info, true);
			pw.flush();
		} catch (IOException e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
		}
		return filename;
	}

	public List<PeriodCallQty> getPeriodData(String year) {
		List<PeriodCallQty> periodCallQties = new ArrayList<PeriodCallQty>();

		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("00:00-02:00", "0");
		hm.put("02:00-04:00", "0");
		hm.put("04:00-06:00", "0");
		hm.put("06:00-08:00", "0");
		hm.put("08:00-10:00", "0");
		hm.put("10:00-12:00", "0");
		hm.put("12:00-14:00", "0");
		hm.put("14:00-16:00", "0");
		hm.put("16:00-18:00", "0");
		hm.put("18:00-20:00", "0");
		hm.put("20:00-22:00", "0");
		hm.put("22:00-00:00", "0");

		String sql = "select to_char((trunc(sysdate)+trunc((call_date-trunc(sysdate))*24*60/120)*120/60/24),'hh24:mi')||'-'||to_char((trunc(sysdate)+trunc((call_date-trunc(sysdate))*24*60/120+1)*120/60/24),'hh24:mi') as period, count(*) as qty from call_incident where to_char(call_date,'yyyy')=? group by to_char((trunc(sysdate)+trunc((call_date-trunc(sysdate))*24*60/120)*120/60/24),'hh24:mi')||'-'||to_char((trunc(sysdate)+trunc((call_date-trunc(sysdate))*24*60/120+1)*120/60/24),'hh24:mi') order by 1";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { year });
			while (rs.next()) {
				if (hm.containsKey(rs.getString("period"))) {
					hm.put(rs.getString("period"), rs.getString("qty"));
				}
			}

			periodCallQties.add(new PeriodCallQty("00:00-02:00", hm
					.get("00:00-02:00")));
			periodCallQties.add(new PeriodCallQty("02:00-04:00", hm
					.get("02:00-04:00")));
			periodCallQties.add(new PeriodCallQty("04:00-06:00", hm
					.get("04:00-06:00")));
			periodCallQties.add(new PeriodCallQty("06:00-08:00", hm
					.get("06:00-08:00")));
			periodCallQties.add(new PeriodCallQty("08:00-10:00", hm
					.get("08:00-10:00")));
			periodCallQties.add(new PeriodCallQty("10:00-12:00", hm
					.get("10:00-12:00")));
			periodCallQties.add(new PeriodCallQty("12:00-14:00", hm
					.get("12:00-14:00")));
			periodCallQties.add(new PeriodCallQty("14:00-16:00", hm
					.get("14:00-16:00")));
			periodCallQties.add(new PeriodCallQty("16:00-18:00", hm
					.get("16:00-18:00")));
			periodCallQties.add(new PeriodCallQty("18:00-20:00", hm
					.get("18:00-20:00")));
			periodCallQties.add(new PeriodCallQty("20:00-22:00", hm
					.get("20:00-22:00")));
			periodCallQties.add(new PeriodCallQty("22:00-24:00", hm
					.get("22:00-00:00")));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return periodCallQties;
	}

	public static void main(String[] args) throws Exception {
		// ChartService chartService = new ChartService();
		// List<MonthIncQty> monthIncQties = chartService.getMonthData("2012");
		// for (MonthIncQty monthIncQty : monthIncQties) {
		// System.out.println(monthIncQty.getMonth() + " "
		// + monthIncQty.getQuantity());
		// }
		
		 ChartService chartService = new ChartService();
		 List<SectorIncQty> sectorIncQties = chartService.getSectorsData("2012");
//		 for (SectorIncQty sectorIncQty : sectorIncQties) {
//		 System.out.println(sectorIncQty.getSectorName() + " "
//		 + sectorIncQty.getQuantity());
//		 }
		 Gson gson=new Gson();
		 String json=gson.toJson(sectorIncQties);
		 System.out.println(json);
		 String jsonEn=DES.encryptDES(json, DES.KEY);
		 String jsonDescyted=DES.decryptDES(jsonEn, DES.KEY);
		 System.out.println(jsonDescyted);
//		ChartService chartService = new ChartService();
//		List<PeriodCallQty> periodCallQties = chartService
//				.getPeriodData("2012");
//		for (PeriodCallQty periodCallQty : periodCallQties) {
//			System.out.println(periodCallQty.getTimePeriod() + " "
//					+ periodCallQty.getQuantity());
//		}
	}

}
