package com.appspot;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import se.anyro.tgbotapi.TgBotApi;

import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Syntax.Java;

/**
 * Created by eljah32 on 3/4/2017.
 */
@Cache
@Entity
public class Measurement implements Serializable {
    //@Parent
    @Parent
    Key<MeasurementStation> station;
    //@Index
    Key<MeasurementParameter> parameter;

    @Id
    @Index
    public String stationAndParameterAndDate;
    @Index
    public String parameterString;
    @Index
    public Long dateStart;
    @Index
    public Long dateStop;
    @Index
    public Long dateConcrete;
    @Index
    public Date dateStartDate;
    @Index
    public Date dateStopDate;
    @Index
    public Date dateConcreteDate;

    @Index
    public Double value;
    @Index
    public Double tlv;

    @Index
    public Boolean tlvExceds;

    Measurement() {

    }

    Measurement(Date start, Date stop, Date concrete, Double value, Double tlv, String parameter, String unit, String station) {
        this.stationAndParameterAndDate = station + parameter + start.getTime() + stop.getTime();
        this.dateStart = start.getTime();
        this.dateStop = start.getTime();
        this.dateConcrete = concrete.getTime();
        this.dateStartDate = start;
        this.dateStopDate = stop;
        this.dateConcreteDate = concrete;
        this.value = value;
        this.tlv = tlv;
        this.parameter = Key.create(MeasurementParameter.class, parameter);
        this.parameterString = parameter;
        new MeasurementParameter(parameter, unit);
        this.station = Key.create(MeasurementStation.class, station);
        this.tlvExceds = this.value > this.tlv;

        if (tlvExceds) {
            System.out.println("TLV exceed!!!");
            MeasurementTLVExceedNotification possiblyAlreadyExisiting=ObjectifyService.ofy()
                    .load()
                    .type(MeasurementTLVExceedNotification.class).id(stationAndParameterAndDate).now();

            if (possiblyAlreadyExisiting==null) {
                System.out.println("But notification is not sent yet since  it doesn't exist in the db");
                MeasurementTLVExceedNotification measurementTLVExceedNotification=new MeasurementTLVExceedNotification();
                measurementTLVExceedNotification.dateConcrete=dateConcrete;
                measurementTLVExceedNotification.dateConcreteDate=dateConcreteDate;
                measurementTLVExceedNotification.dateStart=dateStart;
                measurementTLVExceedNotification.dateStop=dateStop;
                measurementTLVExceedNotification.dateStartDate=dateStartDate;
                measurementTLVExceedNotification.dateStopDate=dateStopDate;
                measurementTLVExceedNotification.stationAndParameterAndDate=stationAndParameterAndDate;
                measurementTLVExceedNotification.value=value;
                measurementTLVExceedNotification.tlv=tlv;
                measurementTLVExceedNotification.tlvExceds=tlvExceds;
                measurementTLVExceedNotification.parameterString=parameterString;

                Properties prop = new Properties();
                InputStream input = null;

                try {

                    String filename = "telegram.properties";
                    input = this.getClass().getClassLoader().getResourceAsStream(filename);
                    if(input==null){
                        System.out.println("Sorry, unable to find " + filename);
                        return;
                    }

                    //load a properties file from class path, inside static method
                    prop.load(input);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally{
                    if(input!=null){
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("Sending message to @TatPollution telegram channel");
                String TOKEN = prop.getProperty("token");
                long OWNER = Long.parseLong(prop.getProperty("owner"));
                TgBotApi api = new TgBotApi(TOKEN, OWNER);

                DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("ru"));
                String formattedStartDate = df.format(dateStart);
                String formattedStopDate = df.format(dateStop);
                try {
                    api.sendMessage("@tatpollution", "Превышение предельно допустимого показателя " + tlv + " на станции " + station + " для параметра " + parameter + " по измерениям " + formattedStartDate + "-" + formattedStopDate + " в " + value / tlv + " раз");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                ObjectifyService.ofy().save().entity(measurementTLVExceedNotification).now();
            }
            else
            {
                System.out.println("Notification already exist in db so it will not be sent");
            }

        }


        ObjectifyService.ofy().save().entity(this).now();
    }
}

