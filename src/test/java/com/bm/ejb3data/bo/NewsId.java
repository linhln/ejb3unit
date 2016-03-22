package com.bm.ejb3data.bo;
/**
	 * The PK Class from News - wird benutzt, da es sich um einen
	 * zusammengesetzten Key handelt.
	 * 
	 * @author Daniel
	 * 
	 */
	@Embeddable
	public class NewsId implements Serializable {

		private static final long serialVersionUID = 1L;

		/**
		 * PK Comonent 1: Uberschrift der Meldung.
		 */
		@Column(name = "ueberschrift", nullable = false, length = 250)
		private String ueberschrift;

		/**
		 * PK Comonent 2: WKN der Meldung.
		 */
		@Column(name = "wkn", nullable = false)
		private int wkn;

		/**
		 * PK Comonent 3: Datum der Meldung.
		 */
		@Column(name = "datum", nullable = false)
		private long datumInMillis;

		/**
		 * Returns the datumInMillis.
		 * 
		 * @return Returns the datumInMillis.
		 */
		public long getDatumInMillis() {
			return this.datumInMillis;
		}

		/**
		 * Sets the datumInMillis.
		 * 
		 * @param datumInMillis
		 *            The datumInMillis to set.
		 */
		public void setDatumInMillis(long datumInMillis) {
			this.datumInMillis = datumInMillis;
		}

		/**
		 * Returns the ueberschrift.
		 * 
		 * @return Returns the ueberschrift.
		 */
		public String getUeberschrift() {
			return this.ueberschrift;
		}

		/**
		 * Sets the ueberschrift.
		 * 
		 * @param ueberschrift
		 *            The ueberschrift to set.
		 */
		public void setUeberschrift(String ueberschrift) {
			this.ueberschrift = ueberschrift;
		}

		/**
		 * Returns the wkn.
		 * 
		 * @return Returns the wkn.
		 */
		public int getWkn() {
			return this.wkn;
		}

		/**
		 * Sets the wkn.
		 * 
		 * @param wkn
		 *            The wkn to set.
		 */
		public void setWkn(int wkn) {
			this.wkn = wkn;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object other) {
			if (other != null && other instanceof NewsId) {
				final NewsId otherC = (NewsId) other;
				final EqualsBuilder eq = new EqualsBuilder();
				eq.append(otherC.datumInMillis, this.datumInMillis);
				eq.append(otherC.ueberschrift, this.ueberschrift);
				eq.append(otherC.wkn, this.wkn);
				return eq.isEquals();
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final HashCodeBuilder hb = new HashCodeBuilder(17, 21);
			hb.append(ueberschrift);
			hb.append(wkn);
			hb.append(datumInMillis);
			return hb.toHashCode();
		}

	}