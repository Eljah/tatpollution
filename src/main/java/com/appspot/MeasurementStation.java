package com.appspot;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

/**
 * Created by eljah32 on 3/4/2017.
 */
@Cache
@Entity
public class MeasurementStation  implements Serializable {
    @Id
    @Index
    public String stationName;

    MeasurementStation()
    {

    }

    MeasurementStation(String stationName) {
        this.stationName=stationName;
        ObjectifyService.ofy().save().entity(this).now();
    }

}


