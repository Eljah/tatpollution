package com.appspot;

import com.googlecode.objectify.ObjectifyService;
import org.apache.commons.logging.Log;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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

        if (req.getParameter("parameter") != null) {
            parameter = req.getParameter("parameter");
        }
        if (req.getParameter("station") != null) {
            station = req.getParameter("station");
            if (station=="") {station=null;}
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

        req.setAttribute("parameter", parameter);
        req.setAttribute("station", station);
        System.out.println("Station selected: "+station);
        System.out.println("Parameter selected: "+parameter);

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
