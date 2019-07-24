package uk.me.g4dpz.websat.shared.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class ConstellationPasses {
	
	List<Passes> passes = new ArrayList<Passes>();
	
	private String constellationId;
	
	public ConstellationPasses() {
	}

	public ConstellationPasses(final String constellationId, final List<Passes> passes) {
		this.constellationId = constellationId;
		this.passes = passes;
	}

	@XmlElement(name="passes", required=true)
	public final List<Passes> getPassess() {
		return passes;
	}

	@XmlAttribute
	public final String getConstellationId() {
		return constellationId;
	}

}
