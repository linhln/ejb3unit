package com.bm.ejb3data.bo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Test id class pk.
 * @author Daniel Wiese
 *
 */
@Entity
@IdClass(com.bm.ejb3data.bo.IdClassExampleBoPk.class)
@Table(name = "testidclass")
public class IdClassExampleBo implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "wkn_id", nullable = false)
	private int wkn;
	@Id
	@Column(name = "day_id", nullable = false)
	private short day;
	@Id
	@Column(name = "framenr_id", nullable = false)
	private short framenr;

	@Column(name = "price", nullable = false)
	private float price;

	@Column(name = "transactions", nullable = false)
	private short transactions;

	@Column(name = "volume", nullable = false)
	private int volume;

	/**
	 * Parameterless constructor for JSR 220.
	 */
	public IdClassExampleBo() {
		this.transactions = (short) 1;
	}

	/**
	 * Construktor ohne systemZeit.
	 * 
	 * @param wkn -
	 *            die wkn
	 * @param day -
	 *            det tag (absolut)
	 * @param framenr
	 *            framenr
	 * @param volume -
	 *            die anzehl
	 * @param price -
	 *            der price.
	 * @param transactions
	 *            anzahl Transactionen, die zusammengefasst wurden.
	 */
	public IdClassExampleBo(final int wkn, final short day, final short framenr,
			final int volume, final float price, final short transactions) {
		this.wkn = wkn;
		this.day = day;
		this.framenr = framenr;
		this.volume = volume;
		this.price = price;
		this.transactions = transactions;
	}

	/**
	 * Gibt die Eigenschaft wert zur�ck.
	 * 
	 * @return gibt wert zur�ck.
	 */
	public float getPrice() {
		return this.price;
	}


	/**
	 * Gibt die Eigenschaft anz zur�ck.
	 * 
	 * @return gibt anz zur�ck.
	 */
	public int getVolume() {
		return this.volume;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getWkn() {
		return this.wkn;
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
		if (obj instanceof IdClassExampleBo) {
			final IdClassExampleBo otherC = (IdClassExampleBo) obj;
			final EqualsBuilder eq = new EqualsBuilder();
			eq.append(this.getWkn(), otherC.getWkn());
			eq.append(this.getDay(), otherC.getDay());
			eq.append(this.getFrameNr(), otherC.getFrameNr());

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
		builder.append(this.getFrameNr());
		return builder.toHashCode();
	}

	/**
	 * Setzt die Eigenschaft day.
	 * 
	 * @param day
	 *            ersetzt day.
	 */
	public void setDay(final short day) {
		this.day = day;
	}

	

	public void setPrice(float price) {
		this.price = price;
	}

	/**
	 * Setzt die Eigenschaft volume.
	 * 
	 * @param volume
	 *            ersetzt volume.
	 */
	public void setVolume(final int volume) {
		this.volume = volume;
	}

	/**
	 * Setzt die Eigenschaft wkn.
	 * 
	 * @param wkn
	 *            ersetzt wkn.
	 */
	public void setWkn(final int wkn) {
		this.wkn = wkn;
	}

	public short getTransactions() {
		return transactions;
	}

	public void setTransactions(short transactions) {
		this.transactions = transactions;
	}

	public short getFrameNr() {
		return framenr;
	}

	public short getDay() {
		return getDay();
	}

	/**
	 * {@inheritDoc}
	 */
	public float getWert() {
		return this.price;
	}
}
