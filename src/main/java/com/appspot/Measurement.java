package com.appspot;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.LatLng;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import se.anyro.tgbotapi.TgBotApi;
import se.anyro.tgbotapi.types.Location;
import se.anyro.tgbotapi.types.reply_markup.ReplyKeyboardMarkup;

import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    @Index
    public Boolean tlvApproached;
    @Index
    public Float latitude;
    @Index
    public Float longitude;
    @Index
    public String unit;

    Measurement() {

    }

    Measurement(Float latitude, Float longitude, Date start, Date stop, Date concrete, Double value, Double tlv, String parameter, String unit, String station) {
        this.stationAndParameterAndDate = station + parameter + start.getTime() + stop.getTime();
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateStart = start.getTime();
        this.dateStop = stop.getTime();
        this.dateConcrete = concrete.getTime();
        this.dateStartDate = start;
        this.dateStopDate = stop;
        this.dateConcreteDate = concrete;
        this.value = value;
        this.tlv = tlv;
        this.parameter = Key.create(MeasurementParameter.class, parameter);
        this.parameterString = parameter;
        this.unit = unit;
        new MeasurementParameter(parameter, unit);
        this.station = Key.create(MeasurementStation.class, station);
        this.tlvExceds = this.value > this.tlv;
        this.tlvApproached = this.value > this.tlv * 0.8;

        if (tlvExceds) {
            System.out.println("TLV exceed!!!");
            MeasurementTLVExceedNotification possiblyAlreadyExisiting = ObjectifyService.ofy()
                    .load()
                    .type(MeasurementTLVExceedNotification.class).id(stationAndParameterAndDate).now();

            if (possiblyAlreadyExisiting == null) {
                System.out.println("But notification is not sent yet since  it doesn't exist in the db");
                MeasurementTLVExceedNotification measurementTLVExceedNotification = new MeasurementTLVExceedNotification();
                measurementTLVExceedNotification.dateConcrete = dateConcrete;
                measurementTLVExceedNotification.dateConcreteDate = dateConcreteDate;
                measurementTLVExceedNotification.dateStart = dateStart;
                measurementTLVExceedNotification.dateStop = dateStop;
                measurementTLVExceedNotification.dateStartDate = dateStartDate;
                measurementTLVExceedNotification.dateStopDate = dateStopDate;
                measurementTLVExceedNotification.stationAndParameterAndDate = stationAndParameterAndDate;
                measurementTLVExceedNotification.value = value;
                measurementTLVExceedNotification.tlv = tlv;
                measurementTLVExceedNotification.tlvExceds = tlvExceds;
                measurementTLVExceedNotification.tlvApproached = tlvApproached;
                measurementTLVExceedNotification.unit = unit;
                measurementTLVExceedNotification.latitude = latitude;
                measurementTLVExceedNotification.longitude = longitude;
                measurementTLVExceedNotification.parameterString = parameterString;

                Properties prop = new Properties();
                InputStream input = null;

                try {

                    String filename = "telegram.properties";
                    input = this.getClass().getClassLoader().getResourceAsStream(filename);
                    if (input == null) {
                        System.out.println("Sorry, unable to find " + filename);
                        return;
                    }

                    //load a properties file from class path, inside static method
                    prop.load(input);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                String city = "";
                if (longitude < 49.521669) {
                    city = "Казань";
                } else {
                    if (longitude > 52.099203) {
                        city = "Набережные Челны";
                    } else {
                        city = "Нижнекамск";
                    }
                    ;
                }

//                final Geocoder geocoder = new Geocoder();
//                GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setLocation(new LatLng(new BigDecimal(latitude), new BigDecimal (longitude))).setLanguage("tt").getGeocoderRequest();
//                try {
//                    GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
//                    System.out.println("Geocoder responce got");
//                    if (geocoderResponse.getResults().size()>0) {
//                        List<GeocoderAddressComponent> components=geocoderResponse.getResults().get(0).getAddressComponents();
//                        System.out.println("gac: "+components.toString());
//                        for (GeocoderAddressComponent gac: components)
//                        {
//                            System.out.println("gac: "+gac.toString());
//                            System.out.println("gac: "+gac.getTypes().toString());
//                            if (gac.getTypes().toString().contains("locality"))
//                            {
//                                city=gac.getLongName();
//                                System.out.println("city defined: "+city);
//                            }
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                measurementTLVExceedNotification.city = city;
                System.out.println(city);

                System.out.println("Sending message to @TatPollution telegram channel");
                String TOKEN = prop.getProperty("token");
                long OWNER = Long.parseLong(prop.getProperty("owner"));
                TgBotApi api = new TgBotApi(TOKEN, OWNER);

                //DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, new Locale("ru"));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMMM yyyy HH:mm", new Locale("ru"));
                String formattedStartDate = simpleDateFormat.format(dateStart);
                String formattedStopDate = simpleDateFormat.format(dateStop);
                try {
                    api.sendMessage("@tatpollution", "Превышение предельно допустимого показателя " + tlv + " " + unit + " на станции " + city + ", " + station + " для параметра " + parameter + " по измерениям " + formattedStartDate + "-" + formattedStopDate + " в " + value / tlv + " раз");
                    api.sendLocation("@tatpollution", latitude, longitude, 0, 0, null);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                ObjectifyService.ofy().save().entity(measurementTLVExceedNotification).now();
            } else {
                System.out.println("Notification already exist in db so it will not be sent");
            }

        }


        ObjectifyService.ofy().save().entity(this).now();
    }
}

