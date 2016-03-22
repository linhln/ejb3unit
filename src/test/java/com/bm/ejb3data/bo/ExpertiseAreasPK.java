package com.bm.ejb3data.bo;

import java.io.Serializable;

/**
 * Id class.
 * 
 * @author Daniel Wiese
 * 
 */
public class ExpertiseAreasPK implements Serializable {

	private static final long serialVersionUID = 1L;

	Long prodId;

	Long userId;

	public ExpertiseAreasPK() {
	}

	public ExpertiseAreasPK(Long prodId, Long userId) {
		this.prodId = prodId;
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((prodId == null) ? 0 : prodId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpertiseAreasPK other = (ExpertiseAreasPK) obj;
		if (prodId == null) {
			if (other.prodId != null)
				return false;
		} else if (!prodId.equals(other.prodId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}
