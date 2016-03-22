package com.bm.cfg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;

import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.H2Dialect;
import org.h2.Driver;

/**
 * This class reads and holds the ejb3unit configuration.
 *
 * @author Daniel Wiese
 */
public final class Ejb3UnitCfg {

    /**
     * Config file name. *
     */
    public static final String EJB3UNIT_PROPERTIES_NAME = "ejb3unit.properties";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_LOAD_PERSISTENMCE_XML = "ejb3unit.loadPersistenceXML";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_PERSISTENCE_UNIT_NAME = "ejb3unit.persistenceUnit.name";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_AUTOMATIC_SCHEMA_UPDATE = "ejb3unit.schema.update";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_CONNECTION_DRIVER_CLASS = "ejb3unit.connection.driver_class";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_CONNECTION_PASSWORD = "ejb3unit.connection.password";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_CONNECTION_URL = "ejb3unit.connection.url";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_CONNECTION_USERNAME = "ejb3unit.connection.username";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_IN_MEMORY_TEST = "ejb3unit.inMemoryTest";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_SHOW_SQL = "ejb3unit.show_sql";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_SQL_DIALECT = "ejb3unit.dialect";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_SESSION_CONTEXT_CLASS = "ejb3unit.sessionContext.class";

    /**
     * Konfiguration key. *
     */
    public static final String KEY_MOCKING_PROVIDER = "ejb3unit.mocking.provider";

    private static final String DEFAULT_SESSION_CONTEXT_CLASS = "com.bm.utils.substitues.FakedSessionContext";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(Ejb3UnitCfg.class);

    /**
     * Konfiguration key. *
     */
    private static Ejb3UnitCfg singelton = null;

    public static final String JMOCK_VALUE = "jmock";
    public static final String MOCKITO_VALUE = "mockito";
    public static final String EASYMOCK_VALUE = "easymock";
    
    private static final String[] VALID_MOCKING_PROVIDERS = {JMOCK_VALUE, MOCKITO_VALUE, EASYMOCK_VALUE};
    private static final String DEFAULT_MOCKING_PROVIDER = JMOCK_VALUE;

    private final Properties config;

    private boolean inMemory = true;

    private Ejb3UnitCfg() {
        try {
            final InputStream inStr = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(EJB3UNIT_PROPERTIES_NAME);
            config = new Properties();
            if (inStr != null) {
                config.load(inStr);

            } else {
                // run the system in memory if no config if found
                System.setProperty(KEY_IN_MEMORY_TEST, "true");
            }
            if (config.getProperty(KEY_SESSION_CONTEXT_CLASS) == null ||
                    "".equals(config.getProperty(KEY_SESSION_CONTEXT_CLASS).trim())) {
                this.setProperty(config, KEY_SESSION_CONTEXT_CLASS, DEFAULT_SESSION_CONTEXT_CLASS);
                log.debug("Loading default SessionContext class: " + DEFAULT_SESSION_CONTEXT_CLASS);
            }

            if (!Arrays.asList(VALID_MOCKING_PROVIDERS).contains(config.getProperty(KEY_MOCKING_PROVIDER))) {
                this.setProperty(config, KEY_MOCKING_PROVIDER, DEFAULT_MOCKING_PROVIDER);
                log.debug("Loading default mocking framework: " + DEFAULT_MOCKING_PROVIDER);
            }

        } catch (Exception e) {
            // propagete the exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the hibernate configuration settings which will be converted form
     * ejb3configaration settings.
     *
     * @return hibernate entity manager Ejb3Configuration
     */
    public Configuration getEJB3Configuration() {
    	Configuration cfg = new Configuration();
        // tranform the ejb3unit configuration to the hibernate
        // configuration
        final Properties prop = cfg.getProperties();
        if (Boolean.valueOf(this.config.getProperty(KEY_IN_MEMORY_TEST, "true"))) {
            // configuration for in memory db
            this.inMemory = true;
            this
                    .setProperty(prop, "hibernate.connection.url",
                            "jdbc:h2:mem:ejb3unit");
            this.setProperty(prop, "hibernate.connection.driver_class", Driver.class
                    .getName());
            this.setProperty(prop, "hibernate.connection.username", "sa");
            this.setProperty(prop, "hibernate.connection.password", "");
            this.setProperty(prop, "hibernate.dialect", H2Dialect.class.getName());
            this.setProperty(prop, "hibernate.hbm2ddl.auto", "create-drop");
        } else {
            this.inMemory = false;
            this.setProperty(prop, "hibernate.connection.url", this.config
                    .getProperty(KEY_CONNECTION_URL));
            this.setProperty(prop, "hibernate.connection.driver_class", this.config
                    .getProperty(KEY_CONNECTION_DRIVER_CLASS));
            this.setProperty(prop, "hibernate.connection.username", this.config
                    .getProperty(KEY_CONNECTION_USERNAME));
            this.setProperty(prop, "hibernate.connection.password", this.config
                    .getProperty(KEY_CONNECTION_PASSWORD));
            this.setProperty(prop, "hibernate.dialect", this.config
                    .getProperty(KEY_SQL_DIALECT));
            this.setProperty(prop, "hibernate.hbm2ddl.auto", this.config
                    .getProperty(KEY_AUTOMATIC_SCHEMA_UPDATE));
        }
        this.setProperty(prop, "hibernate.cache.provider_class", NoCachingRegionFactory.class
                .getName());
        this.setProperty(prop, "hibernate.show_sql", this.config
                .getProperty(KEY_SHOW_SQL));
        this.setProperty(prop, "hibernate.cache.use_second_level_cache", "false");
        this.setProperty(prop, "hibernate.cache.use_query_cache", "false");
        // static properties
//        this.setProperty(prop, "hibernate.transaction.factory_class",
//                JDBCTransactionFactory.class.getName());
        return cfg;
    }

    /**
     * Returns the schema gen script.
     *
     * @param dialect for dialect
     * @return the ddl
     */
    public String getSchemaGenScript(DBDialect dialect) {
//        String[] generateSchemaCreationScript = this.getEJB3Configuration()
//                .getHibernateConfiguration().generateSchemaCreationScript(
//                        dialect.getDialect());
        final StringBuilder sb = new StringBuilder();
//        for (String ddl : generateSchemaCreationScript) {
//            sb.append(ddl).append("\n");
//        }
//        System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * Retruns a value of a config key.
     *
     * @param key -
     *            the key
     * @return - a value of an config key
     * @author Daniel Wiese
     * @since 08.11.2005
     */
    public String getValue(String key) {
        return this.config.getProperty(key);
    }

    /**
     * True is the persistence xml should be loaded
     */
    public boolean isLoadPeristenceXML() {
        final String isPersistenceXML = this.config
                .getProperty(KEY_LOAD_PERSISTENMCE_XML);
        return (isPersistenceXML != null && isPersistenceXML.equalsIgnoreCase("true"));
    }

    /**
     * Returns the inMemory.
     *
     * @return Returns the inMemory.
     */
    public boolean isInMemory() {
        return inMemory;
    }

    /**
     * Allowing overriding of properties programatically
     *
     * @param key -
     * @param value -
     */
    public void setProperty(String key, String value) {
        this.setProperty(config, key, value);
    }

    /**
     * This helper method will ignore null properties (keys or values)
     *
     * @param prop  -
     *              the propertiy file
     * @param key   -
     *              the key
     * @param value -
     *              the value
     */
    private void setProperty(Properties prop, String key, String value) {
        if (key != null && value != null) {
            prop.setProperty(key, value);
        }
    }

    /**
     * Creates / returns a singelton instance of the configuration.
     *
     * @return - a singelton instance
     */
    public static synchronized Ejb3UnitCfg getConfiguration() {
        if (singelton == null) {
            singelton = new Ejb3UnitCfg();
        }

        return singelton;
    }

    /**
     * Liefert die properties der Jndi rules.
     *
     * @return - die sell rules
     * @since 06.07.2006
     */
    public static List<JndiProperty> getJndiBindings() {
        return getNestedProperty("ejb3unit_jndi", JndiProperty.class);
    }

    /**
     * Checks if the configuration was initialized.
     *
     * @return - a singelton instance
     */
    public static boolean isInitialized() {
        return (singelton != null);
    }

    /**
     * Liest ein NestedProperty Objekte ein.
     *
     * @param key    -
     *               ConfigKeys - die keys um im Property-File einen Wert
     *               auszulesen
     * @param toRead -
     *               die klasse die eingelesen werden soll
     * @return - eine Liste mit gelesenen NestedProperty Objekten.
     * @author Daniel Wiese
     * @since 04.12.2005
     */
    private static <T extends NestedProperty> List<T> getNestedProperty(String key,
                                                                        Class<T> toRead) {
        try {
            final List<T> back = new ArrayList<T>();
            boolean continueRead = true;
            int counter = 0;
            StringBuilder sb = null;
            while (continueRead) {
                T currentInstance = toRead.newInstance();

                counter++;
                sb = new StringBuilder();
                sb.append(key).append(".").append(counter);

                for (String currentInnerValue : currentInstance.innerValues()) {
                    StringBuilder innerValue = new StringBuilder(sb);
                    innerValue.append(".").append(currentInnerValue);
                    final String value = getConfiguration().getValue(
                            innerValue.toString());
                    if (value != null) {
                        currentInstance.setValue(currentInnerValue, value);
                    } else {
                        continueRead = false;
                        break;
                    }
                }

                if (continueRead) {
                    back.add(currentInstance);
                }
            }

            return back;
        } catch (InstantiationException e) {
            log.error("Can't instantiate class: " + toRead);
			throw new IllegalArgumentException("Can�t instantiate class: " + toRead);
		} catch (IllegalAccessException e) {
			log.error("Can't instantiate class: " + toRead);
			throw new IllegalArgumentException("Can�t instantiate class: " + toRead);
		}
	}

}
