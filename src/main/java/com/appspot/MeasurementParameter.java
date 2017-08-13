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
public class MeasurementParameter implements Serializable {
    @Id
    @Index
    public String parameterName;

    @Index
    public String parameterNameSearch;
    public String unit;

    MeasurementParameter()
    {

    }

    MeasurementParameter(String parameterName, String unit) {
        this.parameterName=parameterName;
        this.parameterNameSearch=parameterName;
        this.unit=unit;
        ObjectifyService.ofy().save().entity(this).now();
    }

    MeasurementParameter(String parameterName) {
        this.parameterName=parameterName;
        this.parameterNameSearch=parameterName;
        this.unit=ObjectifyService.ofy().load().entity(this).now().unit;

    }


}
