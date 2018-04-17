package com.appspot;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by eljah32 on 4/16/2018.
 */

@Cache
@Entity
public class MeasurementTLVExceedNotification implements Serializable {

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
    public String city;
    @Index
    public Float latitude;
    @Index
    public Float longitude;
    @Index
    public String unit;


    MeasurementTLVExceedNotification()
    {}

}
