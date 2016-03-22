package com.bm.ejb3data.bo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * PK-Klasse.
 * 
 * @author Fabian
 * 
 */
@Embeddable
public class TestEmbededPK implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "wkn", nullable = false)
	private int wkn;
	@Column(name = "day", nullable = false)
	private short day;
	@Column(name = "framenr", nullable = false)
	private short framenr;

	public int getWkn() {
		return wkn;
	}

	/**
	 * Standardkonstruktor.
	 */
	public TestEmbededPK() {

	}

	/**
	 * Voller Konstruktor mit allen Parametern.
	 * 
	 * @param wkn
	 *            .
	 * @param day
	 *            .
	 * @param framenr
	 *            .
	 */
	public TestEmbededPK(int wkn, short day, short framenr) {
		super();
		this.wkn = wkn;
		this.day = day;
		this.framenr = framenr;
	}

	public void setWkn(int wkn) {
		this.wkn = wkn;
	}

	public short getDay() {
		return day;
	}

	/**
	 * .
	 * 
	 * @param day
	 *            der neue Tag
	 */
	public void setDay(short day) {
		this.day = day;
	}

	public short getFramenr() {
		return framenr;
	}

	/**
	 * .
	 * 
	 * @param framenr
	 *            neue Framenr
	 */
	public void setFramenr(short framenr) {
		this.framenr = framenr;
	}

	/**
	 * Standard equals-Methode.
	 * 
	 * @param obj
	 *            das andere Objekt
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @return .
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean back = false;
		if (obj instanceof TestEmbededPK) {
			final TestEmbededPK otherC = (TestEmbededPK) obj;
			final EqualsBuilder eq = new EqualsBuilder();
			eq.append(this.getWkn(), otherC.getWkn());
			eq.append(this.getDay(), otherC.getDay());
			eq.append(this.getFramenr(), otherC.getFramenr());

			back = eq.isEquals();
		}

		return back;
	}

	/**
	 * HshCode.
	 * 
	 * @return hash code
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder(17, 21);
		builder.append(this.getWkn());
		builder.append(this.getDay());
		builder.append(this.getFramenr());
		return builder.toHashCode();
	}

}