package sad.group3.service;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import sad.group3.domain.MonthIncQty;
import sad.group3.domain.PeriodCallQty;
import sad.group3.domain.Report;
import sad.group3.domain.SectorIncQty;

public class ReportService {
	private ChartService chartService=new ChartService();
	
	public Report getSectorReport(HttpServletRequest request,String year,PrintWriter out) throws Exception{
		List<SectorIncQty> sectorIncQties=chartService.getSectorsData(year);
		for(SectorIncQty sectorIncQty: sectorIncQties){
			chartService.setPieChartValue(sectorIncQty.getSectorName(), new Double(sectorIncQty.getQuantity()));
		}
		String filename=chartService.generatePieChart(year+" Incident Rate Based On Sectors", request.getSession(), out,600,500);
		Report report=new Report();
		report.setChartPic(chartService.getCharPic(System.getProperty("java.io.tmpdir")+"/"+filename));
		report.setSectorIncQties(sectorIncQties);
		report.setYear(year);
		
		return report;
	}
	
	public Report getMonthReport(HttpServletRequest request,String year,PrintWriter out) throws Exception{
		List<MonthIncQty> monthIncQties=chartService.getMonthData(year);
		for(MonthIncQty monthIncQty:monthIncQties){
			chartService.setBarChartValue(monthIncQty.getMonth(),new Double(monthIncQty.getQuantity()));
		}
		String filename=chartService.generateBarChart(year+" Incident Quantity Based On Months","Month","Incident Quantity", request.getSession(), out,600,500);
		Report report=new Report();
		report.setChartPic(chartService.getCharPic(System.getProperty("java.io.tmpdir")+"/"+filename));
		report.setMonthIncQties(monthIncQties);
		report.setYear(year);
		
		return report;
	}

	public Report getTimeReport(HttpServletRequest request, String year,
			PrintWriter out) throws Exception {
		List<PeriodCallQty> periodCallQties=chartService.getPeriodData(year);
		for(PeriodCallQty periodCallQty:periodCallQties){
			chartService.setLineChartValue(periodCallQty.getTimePeriod(),new Double(periodCallQty.getQuantity()));
		}
		String filename=chartService.generateLineChart(year+" Call Quantity Based On Time Period","Time Period","Call Quantity", request.getSession(), out,600,500);
		Report report=new Report();
		report.setChartPic(chartService.getCharPic(System.getProperty("java.io.tmpdir")+"/"+filename));
		report.setPeriodCallQties(periodCallQties);
		report.setYear(year);
		
		return report;
	}
}
