package uk.me.g4dpz.websat.server.dao;

import java.util.List;

import uk.me.g4dpz.websat.server.model.TleEntity;


public interface TleDao {

    TleEntity get(Long catnum);
    TleEntity save(TleEntity tle);
    void delete(TleEntity tle);
	List<TleEntity> getAll();

}
