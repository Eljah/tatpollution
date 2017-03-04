package com.appspot;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by eljah32 on 3/4/2017.
 */
@Cache
@Entity
public class Measurement  implements Serializable {
    //@Parent
    Key<MeasurementParameter> parameter;
    @Parent
    Key<MeasurementStation> station;
    @Id
    @Index
    public String stationAndParameterAndDate;
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

    Measurement()
    {

    }

    Measurement(Date start, Date stop, Date concrete, Double value, Double tlv, String parameter, String station )
    {
        this.stationAndParameterAndDate =station+parameter+concrete.getTime();
        this.dateStart=start.getTime();
        this.dateStop=start.getTime();
        this.dateConcrete=concrete.getTime();
        this.dateStartDate=start;
        this.dateStopDate=stop;
        this.dateConcreteDate=concrete;
        this.value=value;
        this.tlv=tlv;
        this.parameter=Key.create(MeasurementParameter.class,parameter);
        this.station=Key.create(MeasurementStation.class,station);
        ObjectifyService.ofy().save().entity(this).now();
    }
}

