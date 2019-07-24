package uk.me.g4dpz.websat.shared.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class Aliases {
	
	Set<String> aliases = new HashSet<String>();
	
	private String catalogueNumber = null;
	
	public Aliases() {
	}

	public Aliases(final String catalogueNumber, final Set<String> aliases) {
		this.catalogueNumber = catalogueNumber;
		this.aliases = aliases;
	}

	@XmlElement(name="alias", required=true)
	public final Set<String> getAliases() {
		return aliases;
	}

	@XmlAttribute
	public final String getCatalogueNumber() {
		return catalogueNumber;
	}

}
