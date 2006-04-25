//$Id: EntityManagerFactoryImpl.java,v 1.1 2006/04/17 12:11:08 daniel_wiese Exp $
package org.hibernate.ejb;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.SessionFactory;

/**
 * @author Gavin King
 * @author Emmanuel Bernard
 */
public class EntityManagerFactoryImpl implements HibernateEntityManagerFactory {

	private SessionFactory sessionFactory;
	private PersistenceUnitTransactionType transactionType;
	private boolean discardOnClose;

	public EntityManagerFactoryImpl(
			SessionFactory sessionFactory,
			PersistenceUnitTransactionType transactionType,
			boolean discardOnClose
	) {
		this.sessionFactory = sessionFactory;
		this.transactionType = transactionType;
		this.discardOnClose = discardOnClose;
	}

	public EntityManager createEntityManager() {
		return createEntityManager( null );
	}

	public EntityManager createEntityManager(Map map) {
		//TODO support discardOnClose, persistencecontexttype?, interceptor,
		return new EntityManagerImpl(
				sessionFactory, PersistenceContextType.EXTENDED, transactionType, discardOnClose
		);
	}

	public void close() {
		sessionFactory.close();
	}

	public boolean isOpen() {
		return ! sessionFactory.isClosed();
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
