package com.bm.ejb3data.bo.error;

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
 * Test entity bean with annotated fields. Represents market related news object.
 * 
 * @author Daniel Wiese
 * @since 18.09.2005
 */
@Entity
@Table(name = "newsIncorrectQuery")
@NamedQuery(name = "NewsBoIncorrectQuery.incorrect", query = "from com.ejb3unit.NewsBoIncorrectQuery")
public class NewsBoIncorrectQuery implements Serializable {
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
     * Default constructor Constructor.
     */
    public NewsBoIncorrectQuery() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean equals(Object other) {
        if (other instanceof NewsBoIncorrectQuery) {
            final NewsBoIncorrectQuery otherCast = (NewsBoIncorrectQuery) other;
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
    @Override
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
