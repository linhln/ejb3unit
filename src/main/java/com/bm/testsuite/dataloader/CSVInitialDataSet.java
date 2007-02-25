package com.bm.testsuite.dataloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.bm.cfg.Ejb3UnitCfg;
import com.bm.introspectors.EmbeddedClassIntrospector;
import com.bm.introspectors.EntityBeanIntrospector;
import com.bm.introspectors.PersistentPropertyInfo;
import com.bm.introspectors.Property;
import com.bm.utils.BasicDataSource;
import com.bm.utils.Ejb3Utils;
import com.bm.utils.SQLUtils;
import com.bm.utils.csv.CSVParser;

/**
 * This class creates initial data from a comma separated file.
 * 
 * @param <T>
 *            the type of the entity bean (mapping the table)
 * 
 * @author Daniel Wiese
 * @since 17.04.2006
 */
public class CSVInitialDataSet<T> implements InitialDataSet {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(CSVInitialDataSet.class);

	private EntityBeanIntrospector<T> introspector;

	private String[] propertyMapping;

	private Property[] propertyInfo;

	private String insertSQLString;

	private File file;

	/**
	 * Constructor.
	 * 
	 * @param entityBeanClass -
	 *            the corresponding enetity bean class
	 * @param propertyMapping -
	 *            a string array whith the meaning the first column of the cvs
	 *            file belongs to the property with the name
	 *            <code>propertyMapping[0]</code>
	 * @param isCompressed -
	 *            true if compressed (zip)
	 * @param csvFileName -
	 *            the name of the cvs file
	 */
	public CSVInitialDataSet(Class<T> entityBeanClass, String csvFileName,
			boolean isCompressed, String... propertyMapping) {
		initialize(entityBeanClass, propertyMapping);
		if (isCompressed) {
			try {
				URL compressedFile = Thread.currentThread()
						.getContextClassLoader().getResource(csvFileName);
				if (compressedFile == null) {
					throw new IllegalArgumentException(
							"Can�t find the CVS file named (" + csvFileName
									+ ")");
				}
				File tempDir = Ejb3Utils.getTempDirectory();
				InputStream input = new FileInputStream(compressedFile
						.getFile());
				List<File> extracted = Ejb3Utils.unjar(input, tempDir);
				input.close();
				if (extracted.isEmpty()) {
					throw new IllegalArgumentException("The copressed file "
							+ csvFileName + " was empty");
				} else if (extracted.size() != 1) {
					throw new IllegalArgumentException("The copressed file "
							+ csvFileName + " must contain exactly ONE file");
				}

				file = extracted.get(0);
			} catch (FileNotFoundException e) {
				log.error("The file " + csvFileName + " was not found", e);
				throw new IllegalArgumentException("The file was not found");
			} catch (IOException e) {
				log.error("The file " + csvFileName + " could not be accessed",
						e);
				throw new IllegalArgumentException(
						"The file could not be accessed");
			}

		}

	}

	/**
	 * Constructor.
	 * 
	 * @param entityBeanClass -
	 *            the corresponding enetity bean class
	 * @param propertyMapping -
	 *            a string array whith the meaning the first column of the cvs
	 *            file belongs to the property with the name
	 *            <code>propertyMapping[0]</code>
	 * @param csvFileName -
	 *            the name of the cvs file
	 */
	public CSVInitialDataSet(Class<T> entityBeanClass, String csvFileName,
			String... propertyMapping) {
		initialize(entityBeanClass, propertyMapping);
		final URL tmp = Thread.currentThread().getContextClassLoader()
				.getResource(csvFileName);
		if (tmp == null) {
			throw new IllegalArgumentException(
					"Can�t find the CVS file named (" + csvFileName + ")");
		}

		file = new File(tmp.getFile());

	}

	/**
	 * @author Daniel Wiese
	 * @since 29.06.2006
	 * @param entityBeanClass
	 * @param propertyMapping
	 */
	private void initialize(Class<T> entityBeanClass, String... propertyMapping) {
		this.introspector = new EntityBeanIntrospector<T>(entityBeanClass);
		// init configuration if not yet done
		if (!Ejb3UnitCfg.isInitialized()) {
			List<Class<? extends Object>> usedBeans = new ArrayList<Class<? extends Object>>();
			usedBeans.add(entityBeanClass);
			Ejb3UnitCfg.addEntytiesToTest(usedBeans);
		}
		this.propertyMapping = propertyMapping;
		this.propertyInfo = new Property[propertyMapping.length];
		this.insertSQLString = this.buildInsertSQL();
	}

	private String buildInsertSQL() {
		StringBuilder insertSQL = new StringBuilder();
		StringBuilder questionMarks = new StringBuilder();
		insertSQL.append("INSERT INTO ").append(
				this.introspector.getTableName()).append(" (");
		int counter = -1;
		for (String stringProperty : this.propertyMapping) {
			final Property property = this.getProperty(stringProperty);
			// persistent field info
			final PersistentPropertyInfo info = this
					.getPersistentFieldInfo(stringProperty);

			insertSQL.append(info.getDbName());
			questionMarks.append("?");
			counter++;
			// store the property
			this.propertyInfo[counter] = property;
			if (counter + 1 < this.propertyMapping.length) {
				insertSQL.append(", ");
				questionMarks.append(", ");
			}
		}
		insertSQL.append(") ").append("VALUES (").append(
				questionMarks.toString()).append(")");

		return insertSQL.toString();
	}

	@SuppressWarnings("unchecked")
	private Property getProperty(String property) {
		Property info = null;
		if (this.introspector.hasPKClass()) {
			final EmbeddedClassIntrospector pkintro = this.introspector
					.getEmbeddedPKClass();
			final List<Property> pkFields = pkintro.getPersitentFields();

			for (Property current : pkFields) {
				if (current.getName().equals(property)) {
					info = current;
					break;
				}
			}
		}
		if (info == null) {
			for (Property current : this.introspector.getPersitentFields()) {
				if (current.getName().equals(property)) {
					info = current;
					break;
				}
			}

			if (info == null) {
				throw new IllegalArgumentException("The property (" + property
						+ ") is not a persistent field");
			}
		}

		return info;
	}

	@SuppressWarnings("unchecked")
	private PersistentPropertyInfo getPersistentFieldInfo(String property) {
		PersistentPropertyInfo info = null;
		if (this.introspector.hasPKClass()) {
			final EmbeddedClassIntrospector pkintro = this.introspector
					.getEmbeddedPKClass();
			final List<Property> pkFields = pkintro.getPersitentFields();

			for (Property current : pkFields) {
				if (current.getName().equals(property)) {
					info = pkintro.getPresistentFieldInfo(current);
					break;
				}
			}
		}
		if (info == null) {
			for (Property current : this.introspector.getPersitentFields()) {
				if (current.getName().equals(property)) {
					info = this.introspector.getPresistentFieldInfo(current);
					break;
				}
			}

			if (info == null) {
				throw new IllegalArgumentException("The property (" + property
						+ ") is not a persistent field");
			}
		}

		return info;
	}

	/**
	 * Creates the data.
	 * 
	 * @author Daniel Wiese
	 * @since 17.04.2006
	 * @see com.bm.testsuite.dataloader.InitialDataSet#create()
	 */
	public void create() {
		BasicDataSource ds = new BasicDataSource(Ejb3UnitCfg.getConfiguration());
		Connection con = null;
		PreparedStatement prep = null;
		try {
			con = ds.getConnection();
			prep = con.prepareStatement(this.insertSQLString);
			final CSVParser parser = new CSVParser(new FileInputStream(file));
			parser.setCommentStart("#;!");
			parser.setEscapes("nrtf", "\n\r\t\f");
			String value;
			int count = 0;
			int lastLineNumber = parser.lastLineNumber();
			while ((value = parser.nextValue()) != null) {
				if (parser.lastLineNumber() != lastLineNumber) {
					// we have a new line
					lastLineNumber = parser.lastLineNumber();
					count = 0;
				}

				// insert only if neccessary (ignore not requiered fields)
				if (count < this.propertyInfo.length) {
					this.setPreparedStatement(count + 1, prep,
							this.propertyInfo[count], value);
					count++;
				}

				// execute sql
				if (count == this.propertyInfo.length) {
					prep.execute();
				}

			}

			parser.close();
		} catch (FileNotFoundException e) {
			new RuntimeException(e);
		} catch (IOException e) {
			new RuntimeException(e);
		} catch (SQLException e) {
			new RuntimeException(e);
		} finally {
			SQLUtils.cleanup(con, prep);
		}
	}

	/**
	 * Deletes the data.
	 * 
	 * @param ds -
	 *            the datasource.
	 * @author Daniel Wiese
	 * @since 17.04.2006
	 * @see com.bm.testsuite.dataloader.InitialDataSet#cleanup(javax.sql.DataSource)
	 */
	public void cleanup(DataSource ds) {
		StringBuilder deleteSQL = new StringBuilder();
		deleteSQL.append("DELETE FROM ").append(
				this.introspector.getTableName());

		Connection con = null;
		PreparedStatement prep = null;
		try {
			con = ds.getConnection();
			prep = con.prepareStatement(deleteSQL.toString());
			prep.execute();
		} catch (SQLException e) {
			new RuntimeException(e);
		} finally {
			SQLUtils.cleanup(con, prep);
		}

	}

	/**
	 * Sets the value (using the right type) in the prepared statement.
	 * 
	 * @param index -
	 *            the index inside the premared statement
	 * @param statement -
	 *            the prepared statement itself
	 * @param prop -
	 *            the property representing the type
	 * @param value -
	 *            the value to be set
	 * @throws SQLException -
	 *             in error case
	 */
	private void setPreparedStatement(int index, PreparedStatement statement,
			Property prop, String value) throws SQLException {
		// convert to nonprimitive if primitive
		Class type = Ejb3Utils.getNonPrimitiveType(prop.getType());

		if (type.equals(String.class)) {
			statement.setString(index, value);
		} else if (type.equals(Integer.class)) {
			statement.setInt(index, ((value.equals("")) ? 0 : Integer
					.valueOf(value)));
		} else if (type.equals(Long.class)) {
			statement.setLong(index, ((value.equals("")) ? 0 : Long
					.valueOf(value)));
		} else if (type.equals(Boolean.class)) {
			final boolean result = (value != null && value.equals("True") ? true
					: false);
			statement.setBoolean(index, result);
		} else if (type.equals(Short.class)) {
			statement.setShort(index, ((value.equals("")) ? 0 : Short
					.valueOf(value)));
		} else if (type.equals(Byte.class)) {
			statement.setByte(index, Byte.valueOf(value));
		} else if (type.equals(Character.class)) {
			statement.setString(index, String.valueOf(value));
		} else if (type.equals(Date.class)) {
			try {
				statement.setDate(index, new java.sql.Date(SimpleDateFormat
						.getDateInstance().parse(value).getTime()));
			} catch (ParseException e) {
				throw new IllegalArgumentException("Illegral date format ("
						+ value + ")");
			}
		} else if (type.equals(Double.class)) {
			statement.setDouble(index, ((value.equals("")) ? 0 : Double
					.valueOf(value)));
		} else if (type.equals(Float.class)) {
			statement.setFloat(index, ((value.equals("")) ? 0 : Float
					.valueOf(value)));
		}
	}

}