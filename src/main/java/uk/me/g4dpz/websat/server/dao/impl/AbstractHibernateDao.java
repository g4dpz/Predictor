package uk.me.g4dpz.websat.server.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public abstract class AbstractHibernateDao {

    @Autowired
    protected SessionFactory sessionFactory;

    public AbstractHibernateDao() {
        super();
    }

    protected Session getSession() {

        return SessionFactoryUtils.getSession(sessionFactory, true);
    }

}
