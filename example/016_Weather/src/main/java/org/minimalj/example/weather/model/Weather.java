
package org.minimalj.example.weather.model;

import java.util.ArrayList;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Weather {

    public City city;
    public String cod;
    public Double message;
    public Integer cnt;
    public java.util.List<org.minimalj.example.weather.model.WeatherList> list = new ArrayList<org.minimalj.example.weather.model.WeatherList>();

}
