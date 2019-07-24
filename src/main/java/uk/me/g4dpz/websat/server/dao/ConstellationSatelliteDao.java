package uk.me.g4dpz.websat.server.dao;

import java.util.List;

import uk.me.g4dpz.websat.server.model.ConstellationSatelliteEntity;

public interface ConstellationSatelliteDao {

	ConstellationSatelliteEntity save(ConstellationSatelliteEntity constellationSatelliteEntity);
	void delete(ConstellationSatelliteEntity constellationSatellite);
	List<ConstellationSatelliteEntity> getByConstellationId(long constellationId);

}
