package uk.me.g4dpz.websat.server.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "ConstellationSatellite")
public class ConstellationSatelliteEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private Long constellationId;
	private Long catnum;
	boolean active;
	
	/**
	 * @param constellationId
	 * @param catnum
	 */
	public ConstellationSatelliteEntity(final Long id, final Long constellationId, final Long catnum, boolean active) {
		super();
		this.id = id;
		this.constellationId = constellationId;
		this.catnum = catnum;
		this.active = active;
	}

	public ConstellationSatelliteEntity() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public final Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public final void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the constellationId
	 */
	public final Long getConstellationId() {
		return constellationId;
	}

	/**
	 * @param constellationId the constellationId to set
	 */
	public final void setConstellationId(Long constellationId) {
		this.constellationId = constellationId;
	}

	public final Long getCatnum() {
		return catnum;
	}

	public final void setCatnum(final Long catnum) {
		this.catnum = catnum;
	}

	/**
	 * @return the active
	 */
	public final boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public final void setActive(boolean active) {
		this.active = active;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((catnum == null) ? 0 : catnum.hashCode());
		result = prime * result
				+ ((constellationId == null) ? 0 : constellationId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConstellationSatelliteEntity other = (ConstellationSatelliteEntity) obj;
		if (active != other.active) {
			return false;
		}
		if (catnum == null) {
			if (other.catnum != null) {
				return false;
			}
		} else if (!catnum.equals(other.catnum)) {
			return false;
		}
		if (constellationId == null) {
			if (other.constellationId != null) {
				return false;
			}
		} else if (!constellationId.equals(other.constellationId)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
