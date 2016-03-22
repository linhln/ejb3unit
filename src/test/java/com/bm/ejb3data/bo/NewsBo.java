package com.bm.ejb3data.bo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Test entity bean with annotated fields. Represents market related news
 * object.
 * 
 * @author Daniel Wiese
 * @since 18.09.2005
 */
@Entity
@Table(name = "news")
@NamedQuery(name = "NewsBo.allNews", query = "from com.bm.ejb3data.bo.NewsBo")
public class NewsBo implements Serializable {
	@Transient
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private NewsId primaryKey;

	// fields
	@Column(name = "agentur", nullable = false, length = 50)
	private String agentur;

	@Column(name = "text", nullable = false)
	private String text;

	@Column(name = "tag", nullable = true)
	private Integer tag;

	@Column(name = "aktienliste", nullable = true, length = 250)
	private String aktienliste;

	@Column(name = "quellenID", nullable = true)
	private Integer quellenID;

	/**
	 * Default constructor Constructor. (chapter 2.1 page 17), "The entity class
	 * must have a no-arg constructor. The entity class may have other
	 * constructors as well. The no-arg constructor must be public or
	 * protected."
	 */
	protected NewsBo() {

	}

	/**
	 * Constructor.
	 * 
	 * @param tag
	 *            the day
	 */
	public NewsBo(Integer tag) {
		this.tag = tag;
	}

	/**
	 * Constructor for primary key.
	 * 
	 * @param datumInMillis -
	 *            zeitpunkt der nachricht
	 * @param wkn -
	 *            die wkn zu der die nachricht gehoert
	 * @param ueberschrift -
	 *            die uebershrift
	 */
	public NewsBo(final Long datumInMillis, final int wkn, final String ueberschrift) {
		this.primaryKey = new NewsId();
		this.setDatumInMillis(datumInMillis);
		this.setWkn(wkn);
		this.setUeberschrift(ueberschrift);
	}

	/**
	 * Constructor for required fields.
	 * 
	 * @param datumInMillis -
	 *            zeitpunkt der nachricht
	 * @param wkn -
	 *            die wkn zu der die nachricht gehoert
	 * @param ueberschrift -
	 *            die uebershrift
	 * @param agentur -
	 *            die agentur
	 * @param text -
	 *            der text der nachricht (ev. sehr lang)
	 * 
	 */
	public NewsBo(
			final Long datumInMillis,
			final int wkn,
			final String ueberschrift,
			final String agentur,
			final String text) {
		this.primaryKey = new NewsId();
		this.setDatumInMillis(datumInMillis);
		this.setWkn(wkn);
		this.setUeberschrift(ueberschrift);
		this.setAgentur(agentur);
		this.setText(text);
	}

	/**
	 * Setzt die wkn als integer.
	 * 
	 * @author Daniel Wiese
	 * @since 18.09.2005
	 * @param wkn -
	 *            die wkn als integer
	 */
	public final void setWknInteger(final int wkn) {
		this.setWkn(wkn);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String toString() {
		StringBuilder buf = new StringBuilder(200);
		String laengeText = ((this.getText() != null) ? String.valueOf(this.getText()
				.length()) : "null");
		buf.append(this.getDatumInMillis()).append(": ").append(this.getUeberschrift());
		buf.append(" (WKN=").append(this.getWkn()).append(", Laenge des Textes=");
		buf.append(laengeText).append(")");
		return buf.toString();
	}

	/**
	 * Liefert eine detailiertere toString Methode.
	 * 
	 * @return - eine detailierte toString methode
	 */
	public final String toStringDetail() {
		StringBuilder buf = new StringBuilder(((this.getText() != null) ? this.getText()
				.length() : 200));
		buf.append(this.getDatumInMillis()).append(": ").append(this.getUeberschrift())
				.append("\n");
		buf.append("Agentur: ").append(this.getAgentur()).append(", Quelle: ").append(
				this.getQuellenID()).append("\n");
		buf.append(this.getText());

		return buf.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object other) {
		if (other instanceof NewsBo) {
			final NewsBo otherCast = (NewsBo) other;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.getAgentur(), otherCast.getAgentur());
			builder.append(this.getWkn(), otherCast.getWkn());
			builder.append(this.getDatumInMillis(), otherCast.getDatumInMillis());
			builder.append(this.getText(), otherCast.getText());
			builder.append(this.getUeberschrift(), otherCast.getUeberschrift());
			return builder.isEquals();

		}
		return false;

	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(this.getAgentur());
		builder.append(this.getWkn());
		builder.append(this.getDatumInMillis());
		builder.append(this.getText());
		builder.append(this.getUeberschrift());
		return builder.toHashCode();
	}

	/**
	 * The agency.
	 * 
	 * @return Returns the agentur.
	 */
	public String getAgentur() {
		return this.agentur;
	}

	/**
	 * The agency.
	 * 
	 * @param agentur
	 *            The agentur to set.
	 */
	public void setAgentur(java.lang.String agentur) {
		this.agentur = agentur;
	}

	/**
	 * The stock list.
	 * 
	 * @return Returns the aktienliste.
	 */
	public java.lang.String getAktienliste() {
		return this.aktienliste;
	}

	/**
	 * The stck list.
	 * 
	 * @param aktienliste
	 *            The aktienliste to set.
	 */
	public void setAktienliste(java.lang.String aktienliste) {
		this.aktienliste = aktienliste;
	}

	/**
	 * The millis.
	 * 
	 * @return Returns the datumInMillis.
	 */
	public Long getDatumInMillis() {
		return this.primaryKey.getDatumInMillis();
	}

	/**
	 * The millis.
	 * 
	 * @param datumInMillis
	 *            The datumInMillis to set.
	 */
	public void setDatumInMillis(java.lang.Long datumInMillis) {
		this.primaryKey.setDatumInMillis(datumInMillis);
	}

	/**
	 * The id.
	 * 
	 * @return Returns the quellenID.
	 */
	public java.lang.Integer getQuellenID() {
		return this.quellenID;
	}

	/**
	 * The id.
	 * 
	 * @param quellenID
	 *            The quellenID to set.
	 */
	public void setQuellenID(java.lang.Integer quellenID) {
		this.quellenID = quellenID;
	}

	/**
	 * The day.
	 * 
	 * @return Returns the tag.
	 */
	public java.lang.Integer getTag() {
		return this.tag;
	}

	/**
	 * The tag.
	 * 
	 * @param tag
	 *            The tag to set.
	 */
	public void setTag(java.lang.Integer tag) {
		this.tag = tag;
	}

	/**
	 * The text.
	 * 
	 * @return Returns the text.
	 */
	public java.lang.String getText() {
		return this.text;
	}

	/**
	 * The text.
	 * 
	 * @param text
	 *            The text to set.
	 */
	public void setText(java.lang.String text) {
		this.text = text;
	}

	/**
	 * The title.
	 * 
	 * @return Returns the ueberschrift.
	 */
	public java.lang.String getUeberschrift() {
		return this.primaryKey.getUeberschrift();
	}

	/**
	 * The title.
	 * 
	 * @param ueberschrift
	 *            The ueberschrift to set.
	 */
	public void setUeberschrift(java.lang.String ueberschrift) {
		this.primaryKey.setUeberschrift(ueberschrift);
	}

	/**
	 * The wkn.
	 * 
	 * @return Returns the wkn.
	 */
	public int getWkn() {
		return this.primaryKey.getWkn();
	}

	/**
	 * The wkn.
	 * 
	 * @param wkn
	 *            The wkn to set.
	 */
	public void setWkn(int wkn) {
		this.primaryKey.setWkn(wkn);
	}

}
