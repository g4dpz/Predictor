package uk.me.g4dpz.websat.shared.model;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class Passes {
	
	List<PassDetail> passDetails = Collections.emptyList();
	
	private String catalogueNumber = null;
	private int setNum = 0;
	
	public Passes() {
	}

	public Passes(final String catalogueNumber, final int setNum, final List<PassDetail> passDetails) {
		this.catalogueNumber = catalogueNumber;
		this.setNum = setNum;
		this.passDetails = passDetails;
	}

	@XmlElement(name="pass", required=true)
	public final List<PassDetail> getPassDetails() {
		return passDetails;
	}

	@XmlAttribute
	public final String getCatalogueNumber() {
		return catalogueNumber;
	}

	@XmlAttribute
	public final int getSetNum() {
		return setNum;
	}

}
