package com.bm.jndi;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.bm.cfg.Ejb3UnitCfg;
import com.bm.cfg.JndiProperty;
import com.bm.creators.SessionBeanFactory;
import com.bm.introspectors.AbstractIntrospector;
import com.bm.introspectors.SessionBeanIntrospector;

/**
 * This class binds all Objects specified in the Ejb3Unit config to the jndi
 * tree.
 * 
 * @author Daniel
 * 
 */
public class Ejb3UnitJndiBinder {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(Ejb3UnitJndiBinder.class);

	private final InitialContext ctx;

	private final List<JndiProperty> toBind;

	private final Class[] usedEntityBeans;

	public Ejb3UnitJndiBinder(Class... usedEntityBeans) {
		this.usedEntityBeans = usedEntityBeans;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class
					.getName());
			ctx = new InitialContext(env);
			toBind = Ejb3UnitCfg.getJndiBindings();
		} catch (NamingException e) {
			throw new RuntimeException("Can't setup JNDI context for testing");
		}
	}

	public void bind() {
		for (JndiProperty current : toBind) {
			log.debug("Binding (" + current.getClassName() + ") with name ("
					+ current.getJndiName() + ")");
			if (current.isSessionBean()) {
				bindSessionBean(current);
			} else {
				bindPlainClass(current);
			}

		}
	}

	private void bindPlainClass(JndiProperty current) {
		Class<Object> toCreate = loadClass(current);
		try {
			this.ctx
					.bind(current.getJndiName(), toCreate.newInstance());
		} catch (NamingException e) {
			throw new IllegalArgumentException(e);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(
					"Cant instantiate the class ("
							+ current.getClassName()
							+ ") for JNDi context binding");
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(
					"Cant instantiate the class ("
							+ current.getClassName()
							+ ") for JNDi context binding");
		}
	}

	private void bindSessionBean(JndiProperty current) {
		Class<Object> sessionBean = loadClass(current);
		final AbstractIntrospector<Object> intro = new SessionBeanIntrospector<Object>(
				sessionBean);
		final SessionBeanFactory<Object> sf = new SessionBeanFactory<Object>(
				intro, usedEntityBeans);
		try {
			Object createdSessionBean = sf
								.createSessionBeanNoDependencies(sessionBean);
			this.ctx.bind(current.getJndiName(), createdSessionBean);
			sf.injectFields(createdSessionBean);
			sf.executeLifeCycleCreateMethods(createdSessionBean);
		} catch (NamingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<Object> loadClass(JndiProperty current) {
		Class<Object> classToLoad = null;
		try {
			classToLoad = (Class<Object>) Thread.currentThread()
					.getContextClassLoader().loadClass(current.getClassName());
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("The Class ("
					+ current.getClassName()
					+ ") you wold like to bind to JNDI tree can't be found");
		}
		return classToLoad;
	}
}