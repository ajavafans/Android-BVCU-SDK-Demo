package com.example.myapplication1;

import com.baidu.location.BDLocation;

public class GlobalTool {
	private final static double a = 6378245.0;
	private final static double ee = 0.00669342162296594323;
	private static boolean bTaiWanInChina = true;

	private static boolean outOfChina(BDLocation bdLocation) {
		double lat = bdLocation.getLatitude();
		double lon = bdLocation.getLongitude();

		if(IsInsideChina(bdLocation)) {
			return false;
		}
		else {
			if (bTaiWanInChina && (119.962 < lon && lon < 121.750) && (21.586 < lat && lat < 25.463))
				return false;
			return true;
		}
	}

	private final static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
	
	public static BDLocation BAIDU_to_WGS84(BDLocation bdLocation) {
		if (outOfChina(bdLocation)) {
			return bdLocation;
		}
		double x = bdLocation.getLongitude() - 0.0065;
		double y = bdLocation.getLatitude() - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		bdLocation.setLongitude(z * Math.cos(theta));
		bdLocation.setLatitude(z * Math.sin(theta));
		return GCJ02_to_WGS84(bdLocation);
	}

	private static BDLocation GCJ02_to_WGS84(BDLocation bdLocation) {
		if (outOfChina(bdLocation)) {
			return bdLocation;
		}
		BDLocation tmpLocation = new BDLocation();
		tmpLocation.setLatitude(bdLocation.getLatitude());
		tmpLocation.setLongitude(bdLocation.getLongitude());
		BDLocation tmpLatLng = WGS84_to_GCJ02(tmpLocation);
		double tmpLat = 2 * bdLocation.getLatitude() - tmpLatLng.getLatitude();
		double tmpLng = 2 * bdLocation.getLongitude()
				- tmpLatLng.getLongitude();
		for (int i = 0; i < 0; ++i) {
			tmpLocation.setLatitude(bdLocation.getLatitude());
			tmpLocation.setLongitude(bdLocation.getLongitude());
			tmpLatLng = WGS84_to_GCJ02(tmpLocation);
			tmpLat = 2 * tmpLat - tmpLatLng.getLatitude();
			tmpLng = 2 * tmpLng - tmpLatLng.getLongitude();
		}
		bdLocation.setLatitude(tmpLat);
		bdLocation.setLongitude(tmpLng);
		return bdLocation;
	}

	private static BDLocation WGS84_to_GCJ02(BDLocation bdLocation) {
		if (outOfChina(bdLocation)) {
			return bdLocation;
		}
		double dLat = transformLat(bdLocation.getLongitude() - 105.0,
				bdLocation.getLatitude() - 35.0);
		double dLon = transformLon(bdLocation.getLongitude() - 105.0,
				bdLocation.getLatitude() - 35.0);
		double radLat = bdLocation.getLatitude() / 180.0 * Math.PI;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0)
				/ ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
		bdLocation.setLatitude(bdLocation.getLatitude() + dLat);
		bdLocation.setLongitude(bdLocation.getLongitude() + dLon);
		return bdLocation;
	}

	private static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x
				* Math.PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0
				* Math.PI)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y
				* Math.PI / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x
				* Math.PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0
				* Math.PI)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x
				/ 30.0 * Math.PI)) * 2.0 / 3.0;
		return ret;
	}

	private static Rectangle[] region = new Rectangle[]
			{
					new Rectangle(49.220400, 079.446200, 42.889900, 096.330000),
					new Rectangle(54.141500, 109.687200, 39.374200, 135.000200),
					new Rectangle(42.889900, 073.124600, 29.529700, 124.143255),
					new Rectangle(29.529700, 082.968400, 26.718600, 097.035200),
					new Rectangle(29.529700, 097.025300, 20.414096, 124.367395),
					new Rectangle(20.414096, 107.975793, 17.871542, 111.744104),

			};
	private static Rectangle[] exclude = new Rectangle[]
			{
					new Rectangle(25.398623, 119.921265, 21.785006, 122.497559),
					new Rectangle(22.284000, 101.865200, 20.098800, 106.665000),
					new Rectangle(21.542200, 106.452500, 20.487800, 108.051000),
					new Rectangle(55.817500, 109.032300, 50.325700, 119.127000),
					new Rectangle(55.817500, 127.456800, 49.557400, 137.022700),
					new Rectangle(44.892200, 131.266200, 42.569200, 137.022700),
			};

	private static boolean IsInsideChina(BDLocation pos) {
		for (int i = 0; i < region.length; i++) {
			if (InRectangle(region[i], pos)) {
				for (int j = 0; j < exclude.length; j++) {
					if (InRectangle(exclude[j], pos)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private static boolean InRectangle(Rectangle rect, BDLocation pos) {
		return rect.West <= pos.getLongitude() && rect.East >= pos.getLongitude() && rect.North >= pos.getLatitude() && rect.South <= pos.getLatitude();
	}

	static class Rectangle {
		public double West;
		public double North;
		public double East;
		public double South;

		public Rectangle(double latitude1, double longitude1, double latitude2, double longitude2) {
			this.West = Math.min(longitude1, longitude2);
			this.North = Math.max(latitude1, latitude2);
			this.East = Math.max(longitude1, longitude2);
			this.South = Math.min(latitude1, latitude2);
		}
	}
}
