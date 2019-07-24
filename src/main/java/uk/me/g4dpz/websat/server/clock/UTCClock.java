package uk.me.g4dpz.websat.server.clock;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Default implementation of the {@link Clock} that uses the UTC clock.
 */
public class UTCClock implements Clock {
	
	private static final SimpleTimeZone TZ = new SimpleTimeZone(0, "UTC");
	
    public UTCClock() {
        super();
    }

    @Override
    public Date currentDate() {
        return Calendar.getInstance(TZ).getTime();
    }

    @Override
    public long currentTime() {
        return Calendar.getInstance(TZ).getTime().getTime();
    }
}
