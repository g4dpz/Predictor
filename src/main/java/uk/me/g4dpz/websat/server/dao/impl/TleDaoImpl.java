package uk.me.g4dpz.websat.server.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import uk.me.g4dpz.websat.server.dao.TleDao;
import uk.me.g4dpz.websat.server.model.TleEntity;

public class TleDaoImpl  extends AbstractHibernateDao implements TleDao {

	public TleDaoImpl() {
	}

	@Override
    public TleEntity get(Long catnum) {
        return (TleEntity)getSession().get(TleEntity.class, catnum);
	}

	@Override
    public TleEntity save(TleEntity tle) {
        getSession().saveOrUpdate(tle);
        return tle;
	}

	@Override
    public void delete(TleEntity tle) {
        getSession().delete(tle);
	}

	@SuppressWarnings("unchecked")
	@Override
    @Transactional(readOnly = true)
	public List<TleEntity> getAll() {
        final Query query = getSession()
                .createQuery("from TleEntity tle");
        final List<TleEntity> list = query.list();
        return list;
	}
}
