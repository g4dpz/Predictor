package uk.me.g4dpz.websat.shared.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.me.g4dpz.satellite.SatPassTime;

@XmlRootElement()
public class PassDetail {

	private int aosAzimuth;
	private int losAzimuth;
	private Date startTime;
	private Date tcaTime;
	private Date endTime;
	private double maxEl;

	public PassDetail() {
	}

	public PassDetail(final SatPassTime pass) {
		aosAzimuth = pass.getAosAzimuth();
		losAzimuth = pass.getLosAzimuth();
		startTime = pass.getStartTime();
		tcaTime = pass.getTCA();
		endTime = pass.getEndTime();
		maxEl = pass.getMaxEl();
	}

	@XmlElement()
	public final String getAosAzimuth() {
		return Integer.toString(aosAzimuth);
	}

	@XmlElement()
	public final String getLosAzimuth() {
		return Integer.toString(losAzimuth);
	}

	@XmlElement()
	public final Date getStartTime() {
		return startTime;
	}

	@XmlElement()
	public final Date getTcaTime() {
		return tcaTime;
	}

	@XmlElement()
	public final Date getEndTime() {
		return endTime;
	}

	@XmlElement()
	public final String getMaxEl() {
		return String.format("%6.1f", maxEl);
	}

}
