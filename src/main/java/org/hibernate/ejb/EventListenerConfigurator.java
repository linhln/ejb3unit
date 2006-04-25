//$Id: EventListenerConfigurator.java,v 1.1 2006/04/17 12:11:08 daniel_wiese Exp $
package org.hibernate.ejb;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.hibernate.HibernateException;
import org.hibernate.ejb.event.CallbackHandlerConsumer;
import org.hibernate.ejb.event.EJB3DeleteEventListener;
import org.hibernate.ejb.event.EJB3FlushEntityEventListener;
import org.hibernate.ejb.event.EJB3MergeEventListener;
import org.hibernate.ejb.event.EJB3PersistEventListener;
import org.hibernate.ejb.event.EJB3PostDeleteEventListener;
import org.hibernate.ejb.event.EJB3PostInsertEventListener;
import org.hibernate.ejb.event.EJB3PostLoadEventListener;
import org.hibernate.ejb.event.EJB3PostUpdateEventListener;
import org.hibernate.ejb.event.EJB3SaveEventListener;
import org.hibernate.ejb.event.EJB3SaveOrUpdateEventListener;
import org.hibernate.ejb.event.EntityCallbackHandler;
import org.hibernate.ejb.event.EJB3AutoFlushEventListener;
import org.hibernate.ejb.event.EJB3FlushEventListener;
import org.hibernate.ejb.event.EJB3PersistOnFlushEventListener;
import org.hibernate.event.AutoFlushEventListener;
import org.hibernate.event.DeleteEventListener;
import org.hibernate.event.EventListeners;
import org.hibernate.event.FlushEntityEventListener;
import org.hibernate.event.FlushEventListener;
import org.hibernate.event.MergeEventListener;
import org.hibernate.event.PersistEventListener;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.secure.JACCPreDeleteEventListener;
import org.hibernate.secure.JACCPreInsertEventListener;
import org.hibernate.secure.JACCPreLoadEventListener;
import org.hibernate.secure.JACCPreUpdateEventListener;
import org.hibernate.secure.JACCSecurityListener;
import org.hibernate.validator.event.ValidateEventListener;

/**
 * @author Emmanuel Bernard
 */
public class EventListenerConfigurator {
	private static final Object[] READER_METHOD_ARGS = new Object[0];

	private Properties properties;
	private Ejb3Configuration configuration;
	private boolean isValidator;
	private boolean isSecurity;
	private String jaccContextID;

	public EventListenerConfigurator(Ejb3Configuration configuration) {
		this.configuration = configuration;
		ValidateEventListener validateEventListener = new ValidateEventListener();
		EventListeners listenerConfig = configuration.getEventListeners();

		//Action event
		//EJB3-specific ops listeners
		listenerConfig.setFlushEventListeners( new FlushEventListener[]{EJB3FlushEventListener.INSTANCE} );
		//EJB3-specific ops listeners
		listenerConfig.setAutoFlushEventListeners( new AutoFlushEventListener[]{EJB3AutoFlushEventListener.INSTANCE} );
		listenerConfig.setDeleteEventListeners( new DeleteEventListener[]{new EJB3DeleteEventListener()} );
		listenerConfig.setFlushEntityEventListeners(
				new FlushEntityEventListener[]{new EJB3FlushEntityEventListener()}
		);
		listenerConfig.setMergeEventListeners( new MergeEventListener[]{new EJB3MergeEventListener()} );
		listenerConfig.setPersistEventListeners( new PersistEventListener[]{new EJB3PersistEventListener()} );
		listenerConfig.setPersistOnFlushEventListeners( new PersistEventListener[]{new EJB3PersistOnFlushEventListener()} );
		listenerConfig.setSaveEventListeners( new SaveOrUpdateEventListener[]{new EJB3SaveEventListener()} );
		listenerConfig.setSaveOrUpdateEventListeners(
				new SaveOrUpdateEventListener[]{new EJB3SaveOrUpdateEventListener()}
		);

		//Pre events
		listenerConfig.setPreInsertEventListeners(
				new PreInsertEventListener[]{
						new JACCPreInsertEventListener(),
						validateEventListener
				}
		);
		listenerConfig.setPreUpdateEventListeners(
				new PreUpdateEventListener[]{
						new JACCPreUpdateEventListener(),
						validateEventListener
				}
		);
		listenerConfig.setPreDeleteEventListeners(
				new PreDeleteEventListener[]{
						new JACCPreDeleteEventListener()
				}
		);
		listenerConfig.setPreLoadEventListeners(
				new PreLoadEventListener[]{
						new JACCPreLoadEventListener()
				}
		);

		//post events
		listenerConfig.setPostDeleteEventListeners(
				new PostDeleteEventListener[]{new EJB3PostDeleteEventListener()}
		);
		listenerConfig.setPostInsertEventListeners(
				new PostInsertEventListener[]{new EJB3PostInsertEventListener()}
		);
		listenerConfig.setPostLoadEventListeners(
				new PostLoadEventListener[]{new EJB3PostLoadEventListener()}
		);
		listenerConfig.setPostUpdateEventListeners(
				new PostUpdateEventListener[]{new EJB3PostUpdateEventListener()}
		);
	}

	public void setValidator(boolean validator) {
		isValidator = validator;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
		if ( properties.containsKey( HibernatePersistence.JACC_ENABLED ) ) {
			isSecurity = true;
		}
		if ( properties.containsKey( HibernatePersistence.JACC_CONTEXT_ID ) ) {
			jaccContextID = properties.getProperty( HibernatePersistence.JACC_CONTEXT_ID );
		}
		//override events if needed
		Enumeration<?> enumeration = properties.propertyNames();
		while ( enumeration.hasMoreElements() ) {
			String name = (String) enumeration.nextElement();
			if ( name.startsWith( HibernatePersistence.EVENT_LISTENER_PREFIX ) ) {
				String type = name.substring( HibernatePersistence.EVENT_LISTENER_PREFIX.length() + 1 );
				StringTokenizer st = new StringTokenizer( properties.getProperty( name ), " ,", false );
				List<String> listeners = new ArrayList<String>();
				while ( st.hasMoreElements() ) {
					listeners.add( (String) st.nextElement() );
				}
				configuration.setListeners( type, listeners.toArray( new String[ listeners.size() ] ) );
			}
		}
		;
	}

	public void configure() {
		//TODO exclude pure hbm file classes?
		//TODO move it to each event listener initialize()?
		EntityCallbackHandler callbackHandler = new EntityCallbackHandler();
		configuration.buildMappings(); //needed to get all the classes
		Iterator classes = configuration.getClassMappings();
		while ( classes.hasNext() ) {
			PersistentClass clazz = (PersistentClass) classes.next();
			callbackHandler.add( clazz.getMappedClass() );
		}

		EventListeners listenerConfig = configuration.getEventListeners();

		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo( listenerConfig.getClass(), Object.class );
			PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
			try {
				for ( int i = 0, max = pds.length; i < max ; i++ ) {
					final Object listeners = pds[i].getReadMethod().invoke( listenerConfig, READER_METHOD_ARGS );
					if ( listeners == null ) {
						throw new HibernateException( "Listener [" + pds[i].getName() + "] was null" );
					}
					if ( listeners instanceof Object[] ) {
						int securityListenersNbr = 0;
						Object[] listenersArray = (Object[]) listeners;
						for ( Object listener : listenersArray ) {
							if ( listener != null && listener instanceof CallbackHandlerConsumer ) {
								( (CallbackHandlerConsumer) listener ).setCallbackHandler( callbackHandler );
							}
							if ( listener != null && listener instanceof JACCSecurityListener ) {
								if ( !isSecurity ) {
									securityListenersNbr++;
								}
							}
						}
						if ( !isSecurity ) {
							Class clazz = pds[i].getReadMethod().getReturnType().getComponentType();
							Object newArray = Array.newInstance( clazz, listenersArray.length - securityListenersNbr );
							int index = 0;
							for ( Object listener : listenersArray ) {
								if ( ! ( listener != null && listener instanceof JACCSecurityListener ) ) {
									Array.set( newArray, index++, listener );
								}
							}
							pds[i].getWriteMethod().invoke( listenerConfig, newArray );
						}
					}
				}
			}
			catch (HibernateException e) {
				throw e;
			}
			catch (Throwable t) {
				throw new HibernateException( "Unable to validate listener config", t );
			}
		}
		catch (Exception t) {
			throw new HibernateException( "Unable to copy listeners", t );
		}
		finally {
			if ( beanInfo != null ) {
				// release the jdk internal caches everytime to ensure this
				// plays nicely with destroyable class-loaders
				Introspector.flushFromCaches( getClass() );
			}
		}
	}
}
