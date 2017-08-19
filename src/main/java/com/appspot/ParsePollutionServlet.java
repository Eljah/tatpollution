package com.appspot;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eljah32 on 2/25/2017.
 */
public class ParsePollutionServlet extends HttpServlet {
    final String BASE_URL = "http://www.tatarmeteo.ru";
    //http://www.tatarmeteo.ru/ru/monitoring-okruzhayushhej-sredyi/monitoring-zagryazneniya-atmosfernogo-vozduxa-(s-interaktivnoj-kartoj-zagryazneniya-g.-kazani,-vozmozhno-vyidelit-otdelnyim-punktom).html
    private static final Logger log = Logger.getLogger(ParsePollutionServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {

        ArrayList<Measurement> mes = new ArrayList<Measurement>();

        MemcacheServiceFactory.getMemcacheService().clearAll(); //clearing cache since we are updating the db

        String specificFile = req.getParameter("file");
        String specificDate = req.getParameter("date");
        URL urlXLS = null;
        Date date = null;

        if (specificFile == null && specificDate == null) {
            URL url = null;
            try {
                url = new URL(BASE_URL + "/ru/monitoring-okruzhayushhej-sredyi/monitoring-zagryazneniya-atmosfernogo-vozduxa-(s-interaktivnoj-kartoj-zagryazneniya-g.-kazani,-vozmozhno-vyidelit-otdelnyim-punktom).html");
            } catch (MalformedURLException e) {
                log.severe("Document path has error in " + BASE_URL + "/ru/monitoring-okruzhayushhej-sredyi/monitoring-zagryazneniya-atmosfernogo-vozduxa-(s-interaktivnoj-kartoj-zagryazneniya-g.-kazani,-vozmozhno-vyidelit-otdelnyim-punktom).html");
                e.printStackTrace();
            }

            Document document = null;
            try {
                document = Jsoup.parse(url.openStream(), "UTF-8", url.toString());
                log.info("Page containing link to the document downloaded well from " + url.toString());
            } catch (IOException e) {
                log.severe("Page containing link to the document can't be loaded at " + url.toString());
                e.printStackTrace();
            }
            //System.out.println(document.body());

            List<Element> elements = document.body().select("script");
            Element elementWithYmap = null;
            for (Element element : elements) {
                if (element.toString().contains("ymap")) {
                    elementWithYmap = element;
                }
            }

            System.out.println(elementWithYmap.toString().replaceAll("[\n\r]", ""));

            Pattern pattern = Pattern.compile(".*?Placemark\\(\\[(\\d{2}\\.\\d+),.*?(\\d{2}.\\d+)\\].*?\\{.*?balloonContent:.*?'(<b.*?>.*?table>)'.*?\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(elementWithYmap.toString().replaceAll("[\n\r]", ""));

            Pattern pattern2 = Pattern.compile("(<b.*?>(.*?)<\\/b>)(?:.*?(\\d{2}\\.\\d{2}\\.\\d{4}).*?(<table.*?>.*?<\\/table>))?(?:.*?(\\d{2}\\.\\d{2}\\s*?\\d{2}\\.\\d{2}\\.\\d{2}).*?(\\d{2}\\.\\d{2}\\s*?\\d{2}\\.\\d{2}\\.\\d{2}).*?(<table.*?>.*?<\\/table>))?", Pattern.DOTALL);

            while (matcher.find()) {
                System.out.println("0:" + matcher.group(0));
                System.out.println("0:" + matcher.group(1));
                System.out.println("1:" + matcher.group(2));
                System.out.println("2:" + matcher.group(3));

                Matcher matcher2 = pattern2.matcher(matcher.group(3));
                while (matcher2.find()) {
                    System.out.println("1:" + matcher2.group(1));
                    System.out.println("2:" + matcher2.group(2));
                    String mesurementStationName = matcher2.group(2);
                    MeasurementStation measurementStation = new MeasurementStation(mesurementStationName);
                    System.out.println("3:" + matcher2.group(3));
                    Date dateAutomated = null;
                    if (matcher2.group(3) != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                            sdf.setTimeZone(TimeZone.getTimeZone("MSK"));
                            dateAutomated = sdf.parse(matcher2.group(3));
                        } catch (ParseException e) {
                            e.printStackTrace();

                        }
                    }
                    System.out.println(dateAutomated);


                    System.out.println("4:" + matcher2.group(4));
                    if (matcher2.group(4) != null) {
                        Document doc = Jsoup.parse(matcher2.group(4));
                        Element table = doc.select("table").get(0); //select the first table.
                        Elements rows = table.select("tr");

                        for (int i = 2; i < rows.size(); i++) { //first row is the col names so skip it.
                            Element row = rows.get(i);
                            Elements cols = row.select("td");

                            String parameterName = cols.get(0).text();
                            String[] measurementParameter = parameterName.split(",");
                            String measure1 = cols.get(1).text();
                            String measure2 = cols.get(2).text();
                            String measure3 = cols.get(3).text();
                            String measure4 = cols.get(4).text();
                            System.out.println(measure1 + " " + measure2 + " " + measure3 + " " + measurementParameter[0] + " " + measurementParameter[1]);
                            String pdk1 = cols.get(5).text();

                            Double measure1d = null;
                            try {
                                measure1d = Double.parseDouble(measure1);
                            } catch (NumberFormatException nfe) {
                            }
                            Double measure2d = null;
                            try {
                                measure2d = Double.parseDouble(measure2);
                            } catch (NumberFormatException nfe) {
                            }
                            Double measure3d = null;
                            try {
                                measure3d = Double.parseDouble(measure3);
                            } catch (NumberFormatException nfe) {
                            }
                            Double measure4d = null;
                            try {
                                measure4d = Double.parseDouble(measure4);
                            } catch (NumberFormatException nfe) {
                            }
                            System.out.println("P1"+measurementParameter[0]);
                            System.out.println("P2"+measurementParameter[1]);

                            MeasurementParameter measurementParameter1 = new MeasurementParameter(measurementParameter[0], measurementParameter[1]);
                            System.out.println("M"+measurementParameter1.parameterName);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dateAutomated);
                            cal.add(Calendar.HOUR, 8);
                            Date mes1dateend = cal.getTime();
                            if (measure1d != null) {
                                Measurement measurement1 = new Measurement(dateAutomated, mes1dateend, dateAutomated, measure1d, Double.parseDouble(pdk1), measurementParameter[0],measurementParameter[1], mesurementStationName);
                                mes.add(measurement1);
                            }
                            cal.add(Calendar.HOUR, 8);
                            Date mes2dateend = cal.getTime();
                            if (measure2d != null) {
                                Measurement measurement2 = new Measurement(mes1dateend, mes2dateend, mes1dateend, measure2d, Double.parseDouble(pdk1), measurementParameter[0],measurementParameter[1], mesurementStationName);
                                mes.add(measurement2);
                            }
                            cal.add(Calendar.HOUR, 8);
                            Date mes3dateend = cal.getTime();

                            if (measure3d != null) {
                                Measurement measurement3 = new Measurement(mes2dateend, mes3dateend, mes2dateend, measure3d, Double.parseDouble(pdk1), measurementParameter[0],measurementParameter[1], mesurementStationName);
                                mes.add(measurement3);
                            }
                            cal.add(Calendar.HOUR, 8);
                            Date mes4dateend = cal.getTime();

                            if (measure4d != null) {
                                Measurement measurement4 = new Measurement(mes3dateend, mes4dateend, mes3dateend, measure4d, Double.parseDouble(pdk1), measurementParameter[0],measurementParameter[1], mesurementStationName);
                                mes.add(measurement4);
                            }

                        }
                    }
                    System.out.println("5:" + matcher2.group(5));

                    Date dateManual1 = null;
                    if (matcher2.group(5) != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("hh.mm dd.MM.yy");
                            sdf.setTimeZone(TimeZone.getTimeZone("MSK"));
                            dateManual1 = sdf.parse(matcher2.group(5));
                        } catch (ParseException e) {
                            e.printStackTrace();

                        }
                    }

                    System.out.println("6:" + matcher2.group(6));

                    Date dateManual2 = null;
                    if (matcher2.group(6) != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("hh.mm dd.MM.yy");
                            sdf.setTimeZone(TimeZone.getTimeZone("MSK"));
                            dateManual2 = sdf.parse(matcher2.group(6));
                        } catch (ParseException e) {
                            e.printStackTrace();

                        }
                    }

                    System.out.println("7:" + matcher2.group(7));

                    if (matcher2.group(7) != null) {
                        Document doc = Jsoup.parse(matcher2.group(7));
                        Element table = doc.select("table").get(0); //select the first table.
                        Elements rows = table.select("tr");

                        for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
                            Element row = rows.get(i);
                            Elements cols = row.select("td");

                            String parameterName = cols.get(0).text();
                            String[] measurementParameter = parameterName.split(",");
                            String measure = cols.get(1).text();
                            String time = cols.get(2).text();
                            String pdk1 = cols.get(3).text(); //это не ПДК! это показание во сколько раз измерение меньше, чем ПДК! значит, для получения пдк надо показание делить на это значение

                            System.out.println(time.trim());
                            if (!time.trim().equals("")) {
                                Double measured = null;
                                try {
                                    measured = Double.parseDouble(measure);
                                } catch (NumberFormatException nfe) {
                                }
                                Date measure_date = null;
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(dateManual1);
                                cal.add(Calendar.HOUR, Integer.parseInt(time.split(":")[0]));
                                measure_date = cal.getTime();

                                if (measured != null) {
                                    Measurement measurement = new Measurement(dateManual1, dateManual2, measure_date, measured, measured/Double.parseDouble(pdk1), measurementParameter[0],  measurementParameter[1],mesurementStationName);
                                    mes.add(measurement);
                                }
                            }
                        }
                    }
                }

                //allMatches.add(m.group());

                //(<b.*?>(.*?)<\/b>)(.*?(\d{2}\.\d{2}\.\d{4}).*?(<table.*?>.*?<\/table>))+(.*?(\d{2}\.\d{2}\s*?\d{2}\.\d{2}\.\d{2}).*?(\d{2}\.\d{2}\s*?\d{2}\.\d{2}\.\d{2}).*?(<table.*?>.*?<\/table>))+
            }


            resp.setContentType("text/plain");
            PrintWriter wr = null;
            resp.setCharacterEncoding("UTF-8");
            try {
                wr = resp.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //removed as too expensive
            if (req.getParameter("debug") != null) {
                for (Measurement sg : mes) {
                    wr.println(sg.stationAndParameterAndDate + " : " + sg.value + " : " + sg.dateConcrete);

                }
            }
            wr.println("Success!");
        }
    }
}
