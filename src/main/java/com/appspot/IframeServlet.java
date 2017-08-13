package com.appspot;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

/**
 * Created by eljah32 on 12/21/2015.
 */
public class IframeServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<Measurement> measurementList = ObjectifyService.ofy()
                .load()
                .type(Measurement.class)//.filter("tlvExceds!=",null)
                // Only show 5 of them.
                .list();
        System.out.println(measurementList.toString());

        PrintWriter wr = null;
        resp.setCharacterEncoding("UTF-8");
        try {
            wr = resp.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Measurement m : measurementList) {
            Measurement m2=new Measurement(new Date(m.dateStart), new Date(m.dateStop), new Date(m.dateConcrete), m.value, m.tlv, m.parameter.getName(),m.parameter.getName(), m.station.getName());
            if (req.getParameter("debug") != null) {
                wr.println(m.stationAndParameterAndDate + "is deleted");
            }
            ObjectifyService.ofy().delete().entity(m);
            ObjectifyService.ofy().save().entity(m2);

        }


    }


}




