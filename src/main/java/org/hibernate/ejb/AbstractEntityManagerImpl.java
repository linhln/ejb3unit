/*
 * JBoss, the OpenSource EJB server Distributable under LGPL license. See terms of license at
 * gnu.org.
 */
package org.hibernate.ejb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AssertionFailure;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.ejb.transaction.JoinableCMTTransaction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transaction.TransactionFactory;
import org.hibernate.util.JTAHelper;

/**
 * @author <a href="mailto:gavin@hibernate.org">Gavin King</a>
 * @author Emmanuel Bernard
 */
public abstract class AbstractEntityManagerImpl implements HibernateEntityManagerImplementor, Serializable {
	private static Log log = LogFactory.getLog( AbstractEntityManagerImpl.class );

	protected transient TransactionImpl tx = new TransactionImpl( this );
	protected PersistenceContextType persistenceContextType;
	private FlushModeType flushMode = FlushModeType.AUTO;
	private PersistenceUnitTransactionType transactionType;

	protected AbstractEntityManagerImpl(PersistenceContextType type, PersistenceUnitTransactionType transactionType) {
		this.persistenceContextType = type;
		this.transactionType = transactionType;
	}

	protected void postInit() {
		//register in Sync if needed
		if ( PersistenceUnitTransactionType.JTA.equals(transactionType) ) joinTransaction( true );
	}

	public Query createQuery(String ejbqlString) {
		//adjustFlushMode();
		try {
			return new QueryImpl( getSession().createQuery( ejbqlString ), this );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	public Query createNamedQuery(String name) {
		//adjustFlushMode();
		try {
			return new QueryImpl( getSession().getNamedQuery( name ), this );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	public Query createNativeQuery(String sqlString) {
		//adjustFlushMode();
		try {
			SQLQuery q = getSession().createSQLQuery( sqlString );
			return new QueryImpl( q, this );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	public Query createNativeQuery(String sqlString, Class resultClass) {
		//adjustFlushMode();
		try {
			SQLQuery q = getSession().createSQLQuery( sqlString );
			q.addEntity( "alias1", resultClass.getName(), LockMode.READ );
			return new QueryImpl( q, this );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		//adjustFlushMode();
		try {
			SQLQuery q = getSession().createSQLQuery( sqlString );
			q.setResultSetMapping( resultSetMapping );
			return new QueryImpl( q, this );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		//adjustFlushMode();
		try {
			return (T) getSession().load( entityClass, (Serializable) primaryKey );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <A> A find(Class<A> entityClass, Object primaryKey) {
		//adjustFlushMode();
		try {
			return (A) getSession().get( entityClass, (Serializable) primaryKey );
		}
		catch (ObjectNotFoundException e) {
			//should not happen on the entity itself with get
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	private void checkTransactionNeeded() {
		if ( persistenceContextType == PersistenceContextType.TRANSACTION && ! isTransactionInProgress() ) {
			//no need to mark as rollback, no tx in progress
			throw new TransactionRequiredException(
					"no transaction is in progress for a TRANSACTION type persistence context"
			);
		}
	}

	public void persist(Object entity) {
		checkTransactionNeeded();
		//adjustFlushMode();
		try {
			getSession().persist( entity );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage() );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
		}
	}

	@SuppressWarnings("unchecked")
	public <A> A merge(A entity) {
		checkTransactionNeeded();
		//adjustFlushMode();
		try {
			return (A) getSession().merge( entity );
		}
		catch (StaleObjectStateException sse) {
			throw new IllegalArgumentException( sse );
		}
		catch (ObjectDeletedException sse) {
			throw new IllegalArgumentException( sse );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return null;
		}
	}

	public void remove(Object entity) {
		checkTransactionNeeded();
		//adjustFlushMode();
		try {
			getSession().delete( entity );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
		}
	}

	public void refresh(Object entity) {
		checkTransactionNeeded();
		//adjustFlushMode();
		try {
			getSession().refresh( entity );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
		}
	}

	public boolean contains(Object entity) {
		try {
			if ( entity != null
					&& ! ( entity instanceof HibernateProxy )
					&& getSession().getSessionFactory().getClassMetadata( entity.getClass() ) == null ) {
				throw new IllegalArgumentException( "Not an entity:" + entity.getClass() );
			}
			return getSession().contains( entity );
		}
		catch (MappingException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
			return false;
		}
	}

	public void flush() {
		try {
			if ( ! isTransactionInProgress() ) {
				throw new TransactionRequiredException( "no transaction is in progress" );
			}
			//adjustFlushMode();
			getSession().flush();
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
		}
	}

	public abstract Session getSession();

	public EntityTransaction getTransaction() {
		if ( transactionType == PersistenceUnitTransactionType.JTA ) {
			throw new IllegalStateException( "JTA EntityManager cannot access a transactions" );
		}
		return tx;
	}

	public void setFlushMode(FlushModeType flushMode) {
		this.flushMode = flushMode;
		if ( flushMode == FlushModeType.AUTO ) {
			getSession().setFlushMode( FlushMode.AUTO );
		}
		else if ( flushMode == FlushModeType.COMMIT ) {
			getSession().setFlushMode( FlushMode.COMMIT );
		}
		else {
			throw new AssertionFailure( "Unknown FlushModeType: " + flushMode );
		}
	}

	public void clear() {
		//adjustFlushMode();
		try {
			getSession().clear();
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
		}
	}

	public FlushModeType getFlushMode() {
		FlushMode mode = getSession().getFlushMode();
		if ( mode == FlushMode.AUTO ) {
			this.flushMode = FlushModeType.AUTO;
		}
		else if ( mode == FlushMode.COMMIT ) {
			this.flushMode = FlushModeType.COMMIT;
		}
//		else if ( mode == FlushMode.NEVER ) {
//			if ( PersistenceContextType.EXTENDED == persistenceContextType && !isTransactionInProgress() ) {
//				//we are in flushMode none for EXTENDED
//				return flushMode;
//			}
//			else {
//				return null; //TODO exception?
//			}
//		}
		else {
			return null; //TODO exception?
		}
		//otherwise this is an unknown mode for EJB3
		return flushMode;
	}

	public void lock(Object entity, LockModeType lockMode) {
		try {
			if ( ! isTransactionInProgress() ) {
				throw new TransactionRequiredException( "no transaction is in progress" );
			}
			//adjustFlushMode();
			if ( !contains( entity ) ) throw new IllegalArgumentException( "entity not in the persistence context" );
			getSession().lock( entity, getLockMode( lockMode ) );
		}
		catch (HibernateException he) {
			throwPersistenceException( he );
		}
	}

	private LockMode getLockMode(LockModeType lockMode) {
		switch ( lockMode ) {
			case READ:
				return LockMode.UPGRADE; //assuming we are on read-commited and we need to prevent non repeteable read
			case WRITE:
				return LockMode.FORCE;
			default:
				throw new AssertionFailure( "Unknown LockModeType: " + lockMode );
		}
	}

	/**
	 * adjust the flush mode to match the no tx / no flush behavior
	 */
	//remove
	private void adjustFlushMode() {
		Session session = getSession();

		boolean isTransactionActive = isTransactionInProgress();

		if ( isTransactionActive && session.getFlushMode() == FlushMode.NEVER ) {
			log.debug( "Transaction activated, move to FlushMode " + flushMode );
			setFlushMode( flushMode );
		}
		else if ( ! isTransactionActive && session.getFlushMode() != FlushMode.NEVER ) {
			log.debug( "Transaction not active, move to FlushMode NEVER" );
			session.setFlushMode( FlushMode.NEVER );
		}
	}

	public boolean isTransactionInProgress() {
		return ( (SessionImplementor) getSession() ).isTransactionInProgress();
	}

	protected void markAsRollback() {
		if ( tx.isActive() ) {
			tx.setRollbackOnly();
		}
		else {
			if ( PersistenceUnitTransactionType.JTA == transactionType ) {
				TransactionManager transactionManager =
						( (SessionFactoryImplementor) getSession().getSessionFactory() ).getTransactionManager();
				if ( transactionManager == null ) {
					throw new PersistenceException(
							"Using a JTA persistence context wo setting hibernate.transaction.manager_lookup_class"
					);
				}
				try {
					transactionManager.setRollbackOnly();
				}
				catch (SystemException e) {
					throw new PersistenceException( "Unable to set the JTA transaction as RollbackOnly", e );
				}
			}
		}
	}

	public void joinTransaction() {
		joinTransaction( false );
	}

	private void joinTransaction(boolean ignoreNotJoining) {
		//set the joined status
		getSession().isOpen(); //for sync
		if ( transactionType == PersistenceUnitTransactionType.JTA ) {
			try {
				log.debug( "Looking for a JTA transaction to join" );
				final Session session = getSession();
				final Transaction transaction = session.getTransaction();
				if ( transaction != null && transaction instanceof JoinableCMTTransaction ) {
					//can't handle it if not a joinnable transaction
					final JoinableCMTTransaction joinableCMTTransaction = (JoinableCMTTransaction) session.getTransaction();

					if ( joinableCMTTransaction.getStatus() == JoinableCMTTransaction.JoinStatus.JOINED ) {
						log.debug("Transaction already joined");
						return; //no-op
					}
					joinableCMTTransaction.markForJoined();
					session.isOpen(); //register to the Tx
					if ( joinableCMTTransaction.getStatus() == JoinableCMTTransaction.JoinStatus.NOT_JOINED ) {
						if (ignoreNotJoining) {
							log.debug( "No JTA transaction found" );
							return;
						}
						else {
							throw new TransactionRequiredException( "No active JTA transaction on joinTransaction call" );
						}
					}
					else
					if ( joinableCMTTransaction.getStatus() == JoinableCMTTransaction.JoinStatus.MARKED_FOR_JOINED ) {
						throw new AssertionFailure( "Transaction MARKED_FOR_JOINED after isOpen() call" );
					}
					//flush before completion and
					//register clear on rollback
					log.trace("Adding flush() and close() synchronization");
					joinableCMTTransaction.registerSynchronization(
							new Synchronization() {
								public void beforeCompletion() {
									boolean flush = false;
									TransactionFactory.Context ctx = null;
									try {
										ctx = (TransactionFactory.Context) session;
										JoinableCMTTransaction joinable = (JoinableCMTTransaction) session.getTransaction();
										flush = !ctx.isFlushModeNever() &&
												//ctx.isFlushBeforeCompletionEnabled() &&
												//TODO probably make it ! isFlushBeforecompletion()
												!JTAHelper.isRollback( joinable.getTransaction().getStatus() );
									}
									catch (SystemException se) {
										log.error( "could not determine transaction status", se );
										//throwPersistenceException will mark the transaction as rollbacked
										throwPersistenceException(
												new PersistenceException(
														"could not determine transaction status in beforeCompletion()",
														se
												)
										);
									}
									catch (HibernateException he) {
										throwPersistenceException( he );
									}

									try {
										if ( flush ) {
											log.trace( "automatically flushing session" );
											ctx.managedFlush();
										}
										else {
											log.trace( "skipping managed flushing" );
										}
									}
									catch (RuntimeException re) {
										//throwPersistenceException will mark the transaction as rollbacked
										if ( re instanceof HibernateException ) {
											throwPersistenceException( (HibernateException) re );
										}
										else {
											throwPersistenceException( new PersistenceException( re ) );
										}
									}
								}

								public void afterCompletion(int status) {
									try {
										if ( Status.STATUS_ROLLEDBACK == status
												&& transactionType == PersistenceUnitTransactionType.JTA ) {
											if ( session.isOpen() ) {
												session.clear();
											}
										}
										JoinableCMTTransaction joinable = (JoinableCMTTransaction) session.getTransaction();
										joinable.resetStatus();
									}
									catch (HibernateException e) {
										throwPersistenceException( e );
									}
								}
							}
					);
				}
				else {
					log.warn("Cannot join transaction, not a JoinableCMTTransaction");
				}
			}
			catch (HibernateException he) {
				throwPersistenceException( he );
			}
		}
		else {
			if (!ignoreNotJoining) log.warn("Calling joinTransaction() on a non JTA EntityManager");
		}
	}

	/**
	 * returns the underlying session
	 */
	public Object getDelegate() {
		return getSession();
	}

	;

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		tx = new TransactionImpl( this );
	}

	public void throwPersistenceException(PersistenceException e) {
		if ( ! ( e instanceof NoResultException || ( e instanceof NonUniqueResultException ) ) ) markAsRollback();
		throw e;
	}

	public void throwPersistenceException(HibernateException e) {
		if ( e instanceof StaleStateException ) {
			throwPersistenceException( new OptimisticLockException( e ) );
		}
		else if ( e instanceof ConstraintViolationException ) {
			//FIXME this is bad cause ConstraintViolationException happens in other circumstances
			throwPersistenceException( new EntityExistsException( e ) );
		}
		else if ( e instanceof ObjectNotFoundException ) {
			throwPersistenceException( new EntityNotFoundException( e ) );
		}
		else if ( e instanceof org.hibernate.NonUniqueResultException ) {
			throwPersistenceException( new NonUniqueResultException( e ) );
		}
		else if ( e instanceof UnresolvableObjectException ) {
			throwPersistenceException( new EntityNotFoundException( e ) );
		}
		else {
			throwPersistenceException( new PersistenceException( e ) );
		}
	}
}
