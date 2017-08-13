package com.appspot;

import com.googlecode.objectify.ObjectifyService;
import org.apache.commons.logging.Log;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by eljah32 on 8/12/2017.
 */
public class WelcomeServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String parameter="";
        String station="";
        String timevalue="";

        Date from= new Date();
        Date to= new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.add(Calendar.DATE, -1);
        from = cal.getTime();

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(from);
        cal2.add(Calendar.DATE, 1);
        to = cal2.getTime();


        if (req.getParameter("parameter") != null) {
            parameter = req.getParameter("parameter");
        }
        if (req.getParameter("station") != null) {
            station = req.getParameter("station");
            if (station=="") {station=null;}
        }
        if (req.getParameter("from") != null) {
            System.out.println(req.getParameter("from"));
            try {
                from=(new SimpleDateFormat("yyyy-MM-dd")).parse(req.getParameter("from"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (req.getParameter("to") != null) {
            System.out.println(req.getParameter("to"));
            try {
                to=(new SimpleDateFormat("yyyy-MM-dd")).parse(req.getParameter("to"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        List<String> measurementStationLabels=new ArrayList<String>(){};
        List<MeasurementStation> measurementStationList = ObjectifyService.ofy()
                .load()
                .type(MeasurementStation.class) // We want only Greetings
                //.ancestor(theBook)    // Anyone in this book
                //.order("-date")       // Most recent first - date is indexed.
                //.limit(5)             // Only show 5 of them.
                .list();
        for (MeasurementStation st: measurementStationList)
        {
            measurementStationLabels.add(st.stationName);
        }
        req.setAttribute("stations", measurementStationLabels);

        List<String[]> measurementParametersLabels=new ArrayList<String[]>(){};
        List<MeasurementParameter> measurementParametersList = ObjectifyService.ofy()
                .load()
                .type(MeasurementParameter.class) // We want only Greetings
                //.ancestor(theBook)    // Anyone in this book
                //.order("-date")       // Most recent first - date is indexed.
                //.limit(5)             // Only show 5 of them.
                .list();
        for (MeasurementParameter st: measurementParametersList)
        {
            measurementParametersLabels.add(new String[]{st.parameterName,st.parameterName+" ("+st.unit+")"});
        }
        req.setAttribute("parameters", measurementParametersLabels);

        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        String fromyear = year.format(from);
        String toyear = year.format(to);
        Calendar fromCal=Calendar.getInstance();
        fromCal.setTime(from);
        int frommonth = fromCal.get(Calendar.MONTH)+1;
        Calendar toCal=Calendar.getInstance();
        toCal.setTime(to);
        int tomonth = toCal.get(Calendar.MONTH)+1;


        SimpleDateFormat day = new SimpleDateFormat("dd");
        String fromday = day.format(from);
        String today = day.format(to);



        req.setAttribute("parameter", parameter);
        req.setAttribute("station", station);
        req.setAttribute("from", fromyear+"-"+String.format("%02d", frommonth)+"-"+fromday);
        req.setAttribute("to", toyear+"-"+String.format("%02d", tomonth)+"-"+today);
        req.setAttribute("fromC", fromyear+","+String.format("%02d", frommonth)+","+fromday);
        req.setAttribute("toС", toyear+","+String.format("%02d", tomonth)+","+today);
        System.out.println("Station selected: "+station);
        System.out.println("Parameter selected: "+parameter);
        System.out.println("From: "+fromyear+"-"+String.format("%02d", frommonth)+"-"+fromday);
        System.out.println("To: "+toyear+"-"+String.format("%02d", tomonth)+"-"+today);
        System.out.println("From: "+fromyear+","+String.format("%02d", frommonth)+","+fromday);
        System.out.println("To: "+toyear+","+String.format("%02d", tomonth)+","+today);

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/show.jsp");
        try

        {
            dispatcher.forward(req, resp);
        } catch (
                ServletException e
                ) {
            e.printStackTrace();
        }

    }
}
