package com.evan2048.util;

import android.location.Location;

public class GPSdistanceCalculator {
	
	public static double getDistance(double lat1, double lng1, double lat2, double lng2)
	{
		float results[]=new float[3];
		Location.distanceBetween(lat1, lng1, lat2, lng2, results);
		return (double)results[0];
	}
	
	//2014.12.24 add
	/*
	 * Android doc:
	   The computed distance is stored in results[0].if results has length 2 or greater,the initial bearing
	   is stored in result[1].if results has length 3 or greater,the final bearing is stored in results[2].
	 */
	//bearing:north-east-south(return 0~180);south-west-north(return -180~0)
	public static double getDirection(double lat1, double lng1, double lat2, double lng2)
	{
		float results[]=new float[3];
		Location.distanceBetween(lat1, lng1, lat2, lng2, results);
		double direction=(double)results[1];
		if(direction<0)
		{
			direction=360.0+direction;
		}
		return direction;
	}
}

/*
public class GPSdistanceCalculator {

	private final static double EARTH_RADIUS = 6378.137;

	private static double rad(double d)
	{
		return d * Math.PI / 180.0;
	}

	public static double getDistance(double lat1, double lng1, double lat2, double lng2)
	{
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)+ Math.cos(radLat1) * Math.cos(radLat2)* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}
}
*/