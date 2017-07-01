package com.lab309.time;

public enum TimeUnit {

	MILLISECONDS(1, (short)1000), CENTISECONDS(10, (short)100), SECONDS(1000, (short)60), MINUTES(60000, (short)60), HOURS(3600000, (short)24), DAYS(86400000, (short)7);

	/*ATTRIBUTES*/
	public final double conversionDivisor;
	public final short ceiling;

	/*CONSTRUCTORS*/
	private TimeUnit (double conversionDivisor, short ceiling) {
		this.conversionDivisor = conversionDivisor;
		this.ceiling = ceiling;
	}

}