package uk.me.g4dpz.websat.server.service.implementation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.InvalidTleException;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatNotFoundException;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;
import uk.me.g4dpz.websat.server.cache.Cache;
import uk.me.g4dpz.websat.server.clock.Clock;
import uk.me.g4dpz.websat.server.dao.ConstellationSatelliteDao;
import uk.me.g4dpz.websat.server.dao.TleDao;
import uk.me.g4dpz.websat.server.model.ConstellationSatelliteEntity;
import uk.me.g4dpz.websat.server.model.TleEntity;
import uk.me.g4dpz.websat.server.service.TrackerService;
import uk.me.g4dpz.websat.shared.model.Aliases;
import uk.me.g4dpz.websat.shared.model.ConstellationPasses;
import uk.me.g4dpz.websat.shared.model.PassDetail;
import uk.me.g4dpz.websat.shared.model.Passes;
import uk.me.g4dpz.websat.shared.model.TrackPosition;

@Service("trackerService")
public class TrackerServiceImpl implements TrackerService {

	private static Cache<String, Satellite> satelliteCache;
	private static ConcurrentMap<String, TLE> allSatElems = new ConcurrentHashMap<String, TLE>();
	private static ConcurrentMap<String, HashSet<String>> allAliases = new ConcurrentHashMap<String, HashSet<String>>();
	private static final String CELESTRAK_AMATEUR = "CELESTRAK_AMATEUR";
	private static final String CELESTRAK_CUBESAT = "CELESTRAK_CUBESAT";
	private static final String AMSAT_NASA_BARE = "AMSAT_NASA_BARE";
	private static final String ELEM_URL_AMSAT_NASA_BARE = "http://www.amsat.org/amsat/ftp/keps/current/nasabare.txt";
	private static final String ELEM_URL_CELESTRAK_AMATEUR = "http://celestrak.com/NORAD/elements/amateur.txt";
	private static final String ELEM_URL_CELESTRAK_CUBESAT = "http://celestrak.com/NORAD/elements/cubesat.txt";

	private static final Logger LOGGER = Logger
			.getLogger(TrackerServiceImpl.class.getName());
	private final Clock clock;
	private final TleDao tleDao;
	private final ConstellationSatelliteDao constellationSatelliteDao;

	public TrackerServiceImpl(Clock clock, TleDao tleDao, ConstellationSatelliteDao constellationSatelliteDao) {
		this.clock = clock;
		this.tleDao = tleDao;
		this.constellationSatelliteDao = constellationSatelliteDao;
		createCache(clock);
		loadKeps();
	}

	@Transactional(readOnly = false)
    @Override
	public void loadKeps() {
		loadKepsFromDatabase();
		loadKepsFromWeb();
	}

	@Override
	@Path("/position/{catalogueNumber}")
	@GET
	@Produces("application/xml")
	public Response getTrackPosition(
			@PathParam("catalogueNumber") String catalogueNumber) {

		TrackPosition trackPosition = null;

		Satellite satellite = null;

		final TLE tle = allSatElems.get(catalogueNumber);

		if (tle != null) {

			satellite = refreshCache(catalogueNumber, tle);

			try {
				satellite.calculateSatelliteVectors(clock.currentDate());
				final SatPos satPos = satellite.calculateSatelliteGroundTrack();
				trackPosition = new TrackPosition(catalogueNumber, satPos, satellite.getTLE().getSetnum());
				
				return Response.status(Status.OK).type(MediaType.TEXT_XML).entity(trackPosition).build();
			}
			catch (Exception e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

		return Response.status(Status.NOT_FOUND).build();
	}
	
	@Override
	@Path("/position/{catalogueNumber}/locator/{maidenhead}/amsl/{height}")
	@GET
	@Produces("application/xml")
	public Response getObserverRelativePosition(
			@PathParam("catalogueNumber") String catalogueNumber, 
			@PathParam("maidenhead") String maidenhead, 
			@PathParam("height") String height) {
		
		TrackPosition trackPosition = null;
		
		try {
			double[] latLong 
				= getLatLongFromLocator(maidenhead.toLowerCase());

			Satellite satellite = null;

			final TLE tle = allSatElems.get(catalogueNumber);

			if (tle != null) {

				satellite = refreshCache(catalogueNumber, tle);

				try {
					satellite.calculateSatelliteVectors(clock.currentDate());
					GroundStationPosition gsPos 
						= new GroundStationPosition(latLong[0], latLong[1], Double.parseDouble(height));
					satellite.calculateSatelliteGroundTrack();
					final SatPos satPos = satellite.calculateSatPosForGroundStation(gsPos);
					trackPosition = new TrackPosition(catalogueNumber, satPos, satellite.getTLE().getSetnum());
					return Response.status(Status.OK).type(MediaType.TEXT_XML).entity(trackPosition).build();
				}
				catch (Exception e) {
					return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				}
			}
		}
		catch (final IllegalArgumentException iae) {
			return Response.status(Status.BAD_REQUEST).build();
		}

		return Response.status(Status.NOT_FOUND).build();
	}

	@Override
	@Path("/satellite/passes/{catalogueNumber}/locator/{maidenhead}/amsl/{height}/future/{hours}")
	@GET
	@Produces("application/xml")
	public Response getSatellitePasses(
			@PathParam("catalogueNumber") String catalogueNumber, 
			@PathParam("maidenhead") String maidenhead, 
			@PathParam("height") String height, 
			@PathParam("hours") int hours) {
		
		Satellite satellite = null;
		
		try {
			
			if (hours <= 0 || hours > 24) {
				throw new IllegalArgumentException();
			}
			
			double[] latLong 
				= getLatLongFromLocator(maidenhead.toLowerCase());

			final TLE tle = allSatElems.get(catalogueNumber);

			if (tle != null) {

				satellite = refreshCache(catalogueNumber, tle);

				try {
					List<PassDetail> passList = calculatePassList(height, hours, satellite, latLong);
					
					return Response.status(Status.OK).type(MediaType.TEXT_XML)
							.entity(new Passes(catalogueNumber, satellite.getTLE().getSetnum(), passList)).build();
				}
				catch (Exception e) {
					return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				}
			}
		}
		catch (final IllegalArgumentException iae) {
			return Response.status(Status.BAD_REQUEST).build();
		}


		return Response.status(Status.NOT_FOUND).build();
	}

	public Satellite refreshCache(String catalogueNumber, final TLE tle) {
		Satellite satellite;
		if (!satelliteCache.containsKey(catalogueNumber)) {
			satellite = SatelliteFactory.createSatellite(tle);
			satelliteCache.put(catalogueNumber, satellite);
		} else {
			satellite = satelliteCache.get(catalogueNumber);
		}
		return satellite;
	}

	@Override
	@Path("/constellation/passes/{constellationId}/locator/{maidenhead}/amsl/{height}/future/{hours}")
	@GET
	@Produces("application/xml")
	@Transactional(readOnly = true)
	public Response getConstellationPasses(
			@PathParam("constellationId") String constellationId, 
			@PathParam("maidenhead") String maidenhead, 
			@PathParam("height") String height, 
			@PathParam("hours") int hours) {
		
		try {
			
			final int id = Integer.parseInt(constellationId);
			
			if (hours <= 0 || hours > 24) {
				throw new IllegalArgumentException();
			}
			
			double[] latLong 
				= getLatLongFromLocator(maidenhead.toLowerCase());
		
			List<ConstellationSatelliteEntity> constellationSatellites = constellationSatelliteDao.getByConstellationId(id);
			
			if (constellationSatellites.size() > 0) {
				
				List<Passes> constellationPasses = new ArrayList<Passes>();
				
				for (ConstellationSatelliteEntity constellationSatellite : constellationSatellites) {

					String catnum = constellationSatellite.getCatnum().toString();
					
					final TLE tle = allSatElems.get(catnum);
					
					if (tle != null) {
					
						Satellite satellite = refreshCache(catnum, tle);
						
						List<PassDetail> passList = calculatePassList(height, hours, satellite, latLong);
						
						Passes constellationPass  = new Passes(catnum, satellite.getTLE().getSetnum(), passList);
						
						constellationPasses.add(constellationPass);
					}
				}
				
				return Response.status(Status.OK).type(MediaType.TEXT_XML)
						.entity(new ConstellationPasses(constellationId, constellationPasses)).build();
			}
		}
		catch  (final NumberFormatException nfe) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		catch (final IllegalArgumentException iae) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (InvalidTleException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (SatNotFoundException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.status(Status.NOT_FOUND).build();
	}
	
	

	@Override
	@Path("/aliases/{catalogueNumber}")
	@GET
	@Produces("application/xml")
	public Response getAliases(
			@PathParam("catalogueNumber") String catalogueNumber) {
		
		HashSet<String> aliases = allAliases.get(catalogueNumber);
		
		if (aliases == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		else {
			return Response.status(Status.OK).type(MediaType.TEXT_XML)
				.entity(new Aliases(catalogueNumber, allAliases.get(catalogueNumber))).build();
		}
	}

	private double[] getLatLongFromLocator(String grid) {
		
		final double[] latLong = new double[2];
		
		if (grid != null && !grid.isEmpty() && (grid.length() == 4 || grid.length() == 6) ) {
			
			int right_field = grid.charAt(0) - 'a';
		    int up_field = grid.charAt(1) - 'a';
		    
		    int right_square = grid.charAt(2) - '0';
		    int up_square = grid.charAt(3) - '0';
		         
		    int right_sub;
		    int up_sub;
		    if (grid.length() > 4)
		    {
		    	right_sub = grid.charAt(4) - 'a';
		    	up_sub = grid.charAt(5) - 'a';
		    }
		    else {   // locate in middle of sub-square
		    	right_sub = 11;
		    	up_sub = 11;
		    }
		    
		    latLong[0] = 10.0*up_field + 1.0*up_square + 2.5*up_sub/60.0 - 90.0;
            latLong[1] = 20.0*right_field + 2.0*right_square + 5.0*right_sub/60.0 - 180.0;
            
		}
		else {
			throw new IllegalArgumentException("Locator was null, empty or incorrect length");
		}
		
		return latLong;
	}

	private static void createCache(final Clock clock) {
		satelliteCache = new Cache<String, Satellite>(clock, 50, 10);
	}

	@Transactional(readOnly = false)
	private boolean loadElemFromNetwork(final String kepSource) {
		boolean success = false;
		URL url;
		try {
			if (AMSAT_NASA_BARE.equals(kepSource)) {
				url = new URL(ELEM_URL_AMSAT_NASA_BARE);
			} else if (CELESTRAK_AMATEUR.equals(kepSource)) {
				url = new URL(ELEM_URL_CELESTRAK_AMATEUR);
			} else if (CELESTRAK_CUBESAT.equals(kepSource)) {
				url = new URL(ELEM_URL_CELESTRAK_CUBESAT);
			} else {
				throw new IllegalArgumentException("Unknown keplerian source["
						+ kepSource + "]");
			}

			final List<TLE> tmpSatElems = TLE.importSat(url.openStream());

			for (TLE tle : tmpSatElems) {

				final String catalogueId = Long.toString(tle.getCatnum());

				TLE storedTle = allSatElems.get(catalogueId);

				if (storedTle == null) { 
					tle.setCreateddate(clock.currentDate());
					tleDao.save(new TleEntity(tle));
					allSatElems.put(catalogueId, tle);
					if (validateAmateurSatelliteName(tle.getName())) {
						constellationSatelliteDao.save( new ConstellationSatelliteEntity(0L, 1L, tle.getCatnum(), true));
					}
				} else if (storedTle.getSetnum() < tle.getSetnum()) {
					tle.setCreateddate(clock.currentDate());
					allSatElems.replace(catalogueId, tle);
				}
				
				final String satelliteName = tle.getName();
				
				HashSet<String> aliases = allAliases.get(satelliteName);
				
				if (aliases == null) {
					aliases = new HashSet<String>();
					aliases.add(satelliteName);
					allAliases.put(catalogueId, aliases);
				}
				else if (!aliases.contains(satelliteName)) {
					aliases.add(satelliteName);
					allAliases.replace(catalogueId, aliases);
				}
			}

			success = true;
			
		} catch (final IOException e) {
			LOGGER.info("Unable to get " + kepSource + " keps.");
			success = false;
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		}
		return success;
	}

	private void loadKepsFromWeb() {
		LOGGER.info("loadCelestrakKeps called");
		LOGGER.info("loadCelestrakKeps " + CELESTRAK_AMATEUR 
				+ (loadElemFromNetwork(CELESTRAK_AMATEUR) ? " succeed" : " failed"));
		LOGGER.info("loadCelestrakKeps called");
		LOGGER.info("loadCelestrakKeps " + CELESTRAK_CUBESAT
				+ (loadElemFromNetwork(CELESTRAK_CUBESAT) ? " succeed" : " failed"));
		LOGGER.info("loadAmsatKeps called");
		LOGGER.info("loadAmsatKeps " + AMSAT_NASA_BARE
				+ (loadElemFromNetwork(AMSAT_NASA_BARE) ? " succeed" : " failed"));
	}

	private void loadKepsFromDatabase() {
		
		List<TleEntity> tles = tleDao.getAll();
		
		for (TleEntity tleEntity : tles) {
			
			TLE tle = new TLE(
					tleEntity.getCatnum(), 
					tleEntity.getName(), 
					tleEntity.getSetnum(), 
					tleEntity.getYear(), 
					tleEntity.getRefepoch(), 
					tleEntity.getIncl(), 
					tleEntity.getRaan(), 
					tleEntity.getEccn(), 
					tleEntity.getArgper(), 
					tleEntity.getMeanan(), 
					tleEntity.getMeanmo(), 
					tleEntity.getDrag(), 
					tleEntity.getNddot6(), 
					tleEntity.getBstar(), 
					tleEntity.getOrbitnum(), 
					tleEntity.getEpoch(), 
					tleEntity.getXndt2o(), 
					tleEntity.getXincl(), 
					tleEntity.getXnodeo(), 
					tleEntity.getEo(), 
					tleEntity.getOmega(), 
					tleEntity.getMo(), 
					tleEntity.getXno(), 
					tleEntity.isDeepspace(), 
					tleEntity.getCreateddate());
			
			final String catalogueId = Long.toString(tle.getCatnum());

			TLE storedTle = allSatElems.get(catalogueId);

			if (storedTle == null) {
				allSatElems.put(catalogueId, tle);
			} else if (storedTle.getSetnum() < tle.getSetnum()) {
				allSatElems.replace(catalogueId, tle);
			}
		}
		
	}
	
	public boolean validateAmateurSatelliteName(final String name) {
		final Pattern pattern = Pattern.compile("[A-Z0-9 (-]*[A-Z][OS]-[0-9][A-Z0-9 )-]*");
        return pattern.matcher(name).matches();
	}

	private List<PassDetail> calculatePassList(String height, int hours, Satellite satellite, double[] latLong)
			throws InvalidTleException, SatNotFoundException {
		
		List<PassDetail> passList = new ArrayList<PassDetail>();
		satellite.calculateSatelliteVectors(clock.currentDate());
		GroundStationPosition gsPos 
			= new GroundStationPosition(latLong[0], latLong[1], Double.parseDouble(height));
		PassPredictor predictor = new PassPredictor(satellite.getTLE(), gsPos);
		List<SatPassTime> passtimes = predictor.getPasses(clock.currentDate(), hours, true);
		
		for (SatPassTime pass : passtimes) {
			passList.add(new PassDetail(pass));
		}
		
		return passList;
	}

}
