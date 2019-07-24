package uk.me.g4dpz.websat.shared.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;

@XmlRootElement()
public class TrackPosition {


    private String catalogueNumber;
    private int setNumber;
	private double altitude;
	private double latitude;
	private double longitude;
	private double azimuth;
	private double elevation;

    public TrackPosition() {
    }

    public TrackPosition(final String catalogueNumber, final SatPos satPos, final int setNumber) {
        this.catalogueNumber = catalogueNumber;
        this.altitude = satPos.getAltitude();
        this.latitude = satPos.getLatitude() / Satellite.DEG2RAD;
        this.longitude = satPos.getLongitude() / Satellite.DEG2RAD;
        this.azimuth = satPos.getAzimuth() / Satellite.DEG2RAD;
        this.elevation = satPos.getElevation() / Satellite.DEG2RAD;
        this.setNumber = setNumber;
    }

    @XmlAttribute
	public final String getCatalogueNumber() {
        return catalogueNumber;
    }

    @XmlElement(nillable=true)
    public final String getAltitude() {
		return String.format("%5.0f", altitude).trim();
	}

    @XmlElement()
    public final String getLatitude() {
		return String.format("%6.1f", latitude);
	}

    @XmlElement()
    public final String getLongitude() {
		return String.format("%6.1f", longitude);
	}

    @XmlAttribute
	public final String getSetNumber() {
		return Integer.toString(setNumber);
	}

    @XmlElement(nillable=true)
    public final String getAzimuth() {
		return String.format("%6.1f", azimuth);
	}

    @XmlElement(nillable=true)
    public final String getElevation() {
		return String.format("%6.1f", elevation);
	}
}
