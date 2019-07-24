package uk.me.g4dpz.websat.server.clock;

import java.util.Date;

/**
 * Interface for the system clock. Used to allow mocking and testing.
 */
public interface Clock {
    long currentTime();

    Date currentDate();
}
