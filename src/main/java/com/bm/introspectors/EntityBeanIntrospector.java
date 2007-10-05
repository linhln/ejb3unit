package com.bm.introspectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.log4j.Logger;

import com.bm.utils.AccessType;
import com.bm.utils.AccessTypeFinder;
import com.bm.utils.IdClassInstanceGen;

/**
 * This class inspects all relevant fields of an entity bean and holds the
 * information.
 * 
 * @author Daniel Wiese
 * @author Istvan Devai
 * @param <T> -
 *            the type of the class to inspect
 * @since 07.10.2005
 */
public class EntityBeanIntrospector<T> extends AbstractPersistentClassIntrospector<T>
		implements Introspector<T> {

	static final Logger log = Logger.getLogger(EntityBeanIntrospector.class);

	/** true if the class has an composed pk field. * */
	private boolean hasPKClass = false;

	/** the introspector for the embedded class. * */
	private EmbeddedClassIntrospector<Object> embeddedPKClass = null;

	/** the table name. * */
	private String tableName;

	/** the table name. * */
	private String schemaName;

	private boolean hasSchema = false;

	private Class<?> idClass = null;

	/**
	 * Constroctor with the class to inspect.
	 * 
	 * @param toInspect -
	 *            the class to inspect
	 */
	public EntityBeanIntrospector(Class<T> toInspect) {

		Annotation[] classAnnotations = toInspect.getAnnotations();
		boolean isSessionBean = false;
		boolean isTableNameSpecified = false;
		boolean isAccessTypeField = false;
		Entity entityAnnotation = null;

		// iterate over the annotations
		for (Annotation a : classAnnotations) {
			if (a instanceof Entity) {
				log.debug("The class to introspect " + toInspect.getCanonicalName()
						+ " is an Entity-Bean");
				isSessionBean = true;
				if (AccessTypeFinder.findAccessType(toInspect).equals(AccessType.FIELD)) {
					isAccessTypeField = true;
				}

				entityAnnotation = (Entity) a;

			} else if (a instanceof Table) {
				Table table = (Table) a;
				this.tableName = table.name();
				this.hasSchema = !table.schema().equals("");
				this.schemaName = table.schema();
				isTableNameSpecified = true;
			} else if (a instanceof IdClass) {
				this.idClass = ((IdClass) a).value();
			}
		}

		// check for mandatory conditions
		if (!isSessionBean) {
			throw new RuntimeException("The class " + toInspect.getSimpleName()
					+ " is not a entity bean");
		}

		if (!isTableNameSpecified) {
			this.tableName = this.generateDefautTableName(toInspect, entityAnnotation);
			log.debug("The class " + toInspect.getSimpleName()
					+ " doas not specify a table name! Uning default Name: "
					+ this.tableName);

		}

		if (isAccessTypeField) {
			this.processAccessTypeField(toInspect);
		} else {
			this.processAccessTypeProperty(toInspect);
		}

	}

	/**
	 * Overide the abstract implementation of this method, to handle with
	 * embedded classes
	 * 
	 * @author Daniel Wiese
	 * @since 15.10.2005
	 * @see com.bm.introspectors.AbstractPersistentClassIntrospector#processAccessTypeField(java.lang.Class)
	 */
	@Override
	protected void processAccessTypeField(Class<T> toInspect) {
		// class the super method
		super.processAccessTypeField(toInspect);
		// extract meta information
		Field[] fields = toInspect.getDeclaredFields();
		for (Field aktField : fields) {
			// dont�s introspect fields generated by hibernate
			Annotation[] fieldAnnotations = aktField.getAnnotations();

			// look into the annotations
			for (Annotation a : fieldAnnotations) {
				// set the embedded class, if any
				if (a instanceof EmbeddedId) {
					this.embeddedPKClass = new EmbeddedClassIntrospector<Object>(
							new Property(aktField));
					this.hasPKClass = true;

					// set the akt field information
					final Property aktProperty = new Property(aktField);
					if (this.getPresistentFieldInfo(aktProperty) != null) {
						final PersistentPropertyInfo fi = this
								.getPresistentFieldInfo(aktProperty);
						fi.setEmbeddedClass(true);
						fi.setNullable(false);
					}

					// set the akt pk information> Ebedded classes are not
					// generated
					PrimaryKeyInfo info = new PrimaryKeyInfo(((EmbeddedId) a));
					this.extractGenerator(fieldAnnotations, info);
					this.pkFieldInfo.put(aktProperty, info);
				}
			}
		}
	}

	/**
	 * Returns the pk to delete one entity bean.
	 * 
	 * @param entityBean -
	 *            the entity bean instance
	 * @return - return the pk or the pk class
	 */
	public Object getPrimaryKey(T entityBean) {
		try {
			if (this.hasEmbeddedPKClass()) {
				// return the embedded class instance
				return this.getField(entityBean, this.embeddedPKClass.getAttibuteName());
			} else if (this.getPkFields().size() == 1) {
				// return the single element
				Property toRead = this.getPkFields().iterator().next();
				return this.getField(entityBean, toRead);
			} else if (this.getPkFields().size() > 0 && hasIdClass()) {
				IdClassInstanceGen idClassInstanceGen = new IdClassInstanceGen(this
						.getPkFields(), this.idClass, entityBean);
				return idClassInstanceGen.getIDClassIntance();

			} else {
				throw new RuntimeException(
						"Multiple PK fields detected, use EmbeddedPKClass or IDClass");
			}
		} catch (IllegalAccessException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the tableName.
	 * 
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Returns the tableName.
	 * 
	 * @return Returns the tableName.
	 */
	public String getShemaName() {
		return this.schemaName;
	}

	/**
	 * Returns if a chema name is persent.
	 * 
	 * @return a chema name is persent.
	 */
	public boolean hasSchema() {
		return this.hasSchema;
	}

	/**
	 * Returns the embeddedPKClass.
	 * 
	 * @return Returns the embeddedPKClass.
	 */
	public EmbeddedClassIntrospector<Object> getEmbeddedPKClass() {
		return embeddedPKClass;
	}

	/**
	 * Returns the hasPKClass.
	 * 
	 * @return Returns the hasPKClass.
	 */
	public boolean hasEmbeddedPKClass() {
		return hasPKClass;
	}

	/**
	 * Returns the hasPKClass.
	 * 
	 * @return Returns the hasPKClass.
	 */
	public boolean hasIdClass() {
		return idClass != null;
	}

	/**
	 * If no table name is specifed hgenrate a JSR 220 table name form class
	 * 
	 * @param clazz -
	 *            the clss name
	 * @return - the JSR 220 default table name
	 */
	private String generateDefautTableName(Class clazz, Entity entityAnnotation) {

		if (entityAnnotation != null && !entityAnnotation.name().equals("")) {
			return entityAnnotation.name().toUpperCase();
		}

		String back = clazz.getName();
		if (back.lastIndexOf(".") > 0 && back.lastIndexOf(".") + 1 < back.length()) {
			back = back.substring(back.lastIndexOf(".") + 1, back.length());
			return back.toUpperCase();
		} else {
			return back.toUpperCase();
		}
	}

}
