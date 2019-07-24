package uk.me.g4dpz.websat.server.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import uk.me.g4dpz.websat.server.dao.ConstellationSatelliteDao;
import uk.me.g4dpz.websat.server.model.ConstellationSatelliteEntity;

public class ConstellationSatelliteDaoImpl  extends AbstractHibernateDao implements ConstellationSatelliteDao {

	public ConstellationSatelliteDaoImpl() {
	}

	@Override
    public ConstellationSatelliteEntity save(ConstellationSatelliteEntity constellationSatellite) {
        getSession().saveOrUpdate(constellationSatellite);
        return constellationSatellite;
	}

	@Override
    public void delete(ConstellationSatelliteEntity constellationSatellite) {
        getSession().delete(constellationSatellite);
	}

	@SuppressWarnings("unchecked")
	@Override
    public List<ConstellationSatelliteEntity> getByConstellationId(final long constellationId) {
		final Query query = getSession().createQuery(
                "from ConstellationSatelliteEntity cse where cse.constellationId = :constellationId");
        query.setLong("constellationId", constellationId);
        return query.list();
	}
}
