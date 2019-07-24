package uk.me.g4dpz.websat.server.service;

import javax.ws.rs.core.Response;

public interface TrackerService {
	
	Response getTrackPosition(String catalogueNumber);
	Response getObserverRelativePosition(String catalogueNumber,
			String maidenhead, String height);
	Response getSatellitePasses(String catalogueNumber,
			String maidenhead, String height, int hours);
	Response getConstellationPasses(String constellationId,
			String maidenhead, String height, int hours);
	Response getAliases(String catalogueNumber);
	void loadKeps();
	boolean validateAmateurSatelliteName(String string);

}
