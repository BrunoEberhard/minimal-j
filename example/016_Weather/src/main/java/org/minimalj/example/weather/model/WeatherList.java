
package org.minimalj.example.weather.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class WeatherList {

    public Integer dt;
    public Main main;
    public java.util.List<WeatherDescription> weather = new ArrayList<WeatherDescription>();
    public Clouds clouds;
    public Wind wind;
    public Rain rain;
    public Sys_ sys;
    public LocalDateTime dtTxt;

}
