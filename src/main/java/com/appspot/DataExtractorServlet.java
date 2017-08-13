package com.appspot;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

// This example extends DataSourceServlet
public class DataExtractorServlet extends DataSourceServlet {

    @Override
    public DataTable generateDataTable(Query query, HttpServletRequest request) {
        // Create a data table,
        DataTable data;
        data = new DataTable();

        String parameter = "";
        String station = "";
        String timevalue = "";

        if (request.getParameter("parameter") != null) {
            parameter = request.getParameter("parameter");
        }
        if (request.getParameter("station") != null) {
            station = request.getParameter("station");
            if (station=="") {station=null;}
        }
        ArrayList cd = new ArrayList();
        cd.add(new ColumnDescription("date", ValueType.DATE, "Date"));
        cd.add(new ColumnDescription("measurement", ValueType.NUMBER, "Value"));
        ColumnDescription tooltip = new ColumnDescription("tlv", ValueType.TEXT, "TLV");
        tooltip.setCustomProperty("role", "tooltip");
        tooltip.setCustomProperty("p", "{'html': true}");
        cd.add(tooltip);
        cd.add(new ColumnDescription("measurementexceding", ValueType.NUMBER, "Value exceding TLV"));

        ColumnDescription tooltip1 = new ColumnDescription("tlv2", ValueType.TEXT, "TLV for exceding");
        tooltip1.setCustomProperty("role", "tooltip");
        tooltip1.setCustomProperty("p", "{'html': true}");
        cd.add(tooltip1);
        data.addColumns(cd);
        //cache
        List<Measurement> datasets;

        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        datasets = (List<Measurement>) syncCache.get(parameter + " " + station + " " + timevalue); // Read from cache.


        //com.googlecode.objectify.Key<MeasurementParameter> keyToSelectParameters=Key.create(MeasurementParameter.class, parameter);
        //System.out.println(keyToSelectParameters.getId());

        List<MeasurementParameter> measurementParametersList = ObjectifyService.ofy()
                .load()
                .type(MeasurementParameter.class) // We want only Greetings
                //.ancestor(theBook)    // Anyone in this book
                //.order("-date")       // Most recent first - date is indexed.
                //.limit(5)             // Only show 5 of them.
                .list();
        MeasurementParameter measurementParameterToSelect = null;
        for (MeasurementParameter st : measurementParametersList) {
            if (st.parameterName.equals(parameter)) {
                measurementParameterToSelect = st;
            }
        }
        System.out.println(measurementParameterToSelect.parameterName);

        if (datasets == null) {
            //if (count != -1) {
            if (station != null) {
                datasets = ObjectifyService.ofy()
                        .load()
                        .type(Measurement.class)
                        .ancestor(new MeasurementStation(station)).
                        filter("parameterString = ", parameter).
                        //.orderKey(true)
                        //.limit(count)             // Only show 5 of them.
                        list();
            } else {
                datasets = ObjectifyService.ofy()
                        .load()
                        .type(Measurement.class).
                        filter("parameterString = ", parameter).
                        //.orderKey(true)
                        //.limit(count)             // Only show 5 of them.
                        list();
            }

            System.out.println(datasets);
            syncCache.put(parameter + " " + station + " " + timevalue, datasets);

        }
        //Collections.reverse(datasets);
        for (Measurement de : datasets) {
            Date pointDate = new Date(de.dateStart);

            // Fill the data table.
            try {
                //data.addRowFromValues(11,12,-1);

                TableRow tr = new TableRow();
                SimpleDateFormat df = new SimpleDateFormat("yyyy");
                String year = df.format(pointDate);

                Calendar cal = Calendar.getInstance();
                cal.setTime(pointDate);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                tr.addCell(new DateValue(Integer.parseInt(year), pointDate.getMonth(), day));
                if (de.tlvExceds) {
                    tr.addCell(Value.getNullValueFromValueType(ValueType.NUMBER));
                    tr.addCell(Value.getNullValueFromValueType(ValueType.TEXT));
                    tr.addCell(de.value);
                    if (de.tlv != null) {
                        tr.addCell(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(de.dateStart)+"-"+new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(de.dateStop) +": "+ de.value +"; Превышение ПДК " + de.tlv +" "+de.parameter.getName()+" на "+de.station.getName());
                    }
                } else {
                    tr.addCell(de.value);
                    if (de.tlv != null) {
                        tr.addCell(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(de.dateStart)+"-"+new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(de.dateStop) +": "+ de.value +"; Нет превышения ПДК " + de.tlv +" "+de.parameter.getName()+" на "+de.station.getName());
                    }
                    tr.addCell(Value.getNullValueFromValueType(ValueType.NUMBER));
                    tr.addCell(Value.getNullValueFromValueType(ValueType.TEXT));
                }

                data.addRow(tr);

                //data.addRowFromValues(new DateValue(calendar),12,-1);
                //data.addRowFromValues(new SimpleDateFormat("dd.MM.yyyy").parse("12.12.2015"),5.5,0);
                //data.addRowFromValues(new SimpleDateFormat("dd.MM.yyyy").parse("15.12.2015"),14,0.01);
                //data.addRowFromValues(new SimpleDateFormat("dd.MM.yyyy").parse("18.12.2015"),7,4);
            } catch (TypeMismatchException e) {
                System.out.println("Invalid type!");
                e.printStackTrace();
            }
            // Populate cache.
        }
        return (DataTable) data;
    }
}