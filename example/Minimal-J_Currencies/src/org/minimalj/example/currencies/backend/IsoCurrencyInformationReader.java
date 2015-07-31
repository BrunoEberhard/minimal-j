package org.minimalj.example.currencies.backend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.minimalj.example.currencies.model.Currency;
import org.minimalj.example.currencies.model.IsoCurrencyInformation;
import org.minimalj.util.StringUtils;

public class IsoCurrencyInformationReader {

	public static final String _CCY = "Ccy";
	public static final String _CCY_MNR_UNTS = "CcyMnrUnts";
	public static final String _CCY_NBR = "CcyNbr";
	public static final String _CCY_NM = "CcyNm";
	public static final String _CCY_NTRY = "CcyNtry";
	public static final String _CCY_TBL = "CcyTbl";
	public static final String _CTRY_NM = "CtryNm";
	public static final String _HSTRC_CCY_NTRY = "HstrcCcyNtry";
	public static final String _HSTRC_CCY_TBL = "HstrcCcyTbl";
	public static final String _I_S_O_4217 = "ISO_4217";
	public static final String _WTHDRWL_DT = "WthdrwlDt";
	
	private final List<IsoCurrencyInformation> isoCurrencyInformations = new ArrayList<>(300);
	
	public IsoCurrencyInformationReader() {
		InputStream inputStream = this.getClass().getResourceAsStream("list_one.xml");
		try {
			process(inputStream);
		} catch (Exception x) {
			throw new RuntimeException("Read of currencies failed", x);
		}
	}
	
	public List<IsoCurrencyInformation> getIsoCurrencyInformation() {
		return isoCurrencyInformations;
	}
	
	public List<Currency> getCurrencies() {
		List<Currency> currencies = new ArrayList<>(200);
		Set<String> ids = new HashSet<>();
		
		for (IsoCurrencyInformation isoCurrencyInformation : isoCurrencyInformations) {
			String id = isoCurrencyInformation.ccy;
			if (!StringUtils.isBlank(id) && !ids.contains(id)) {
				Currency currency = new Currency();
				currency.id = isoCurrencyInformation.ccy;
				currency.name = isoCurrencyInformation.ccyNm;
				currency.fund = isoCurrencyInformation.fund;
				currency.number = isoCurrencyInformation.ccyNbr;
				currency.minorUnits = isoCurrencyInformation.ccyMnrUnts;
				currency.withdrawalDate = isoCurrencyInformation.wthdrwlDt;
				
				currencies.add(currency);
				ids.add(currency.id);
			}
		}
		return currencies;
	}
	
	
	private void currency(XMLEventReader xml) throws XMLStreamException {
		IsoCurrencyInformation isoCurrencyInformation = new IsoCurrencyInformation();

		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				switch (startName) {
				case _CTRY_NM:
					isoCurrencyInformation.ctryNm = token(xml);
					break;
				case _CCY_NM:
					Attribute isFundAttribute = startElement.getAttributeByName(new QName("IsFund"));
					if (isFundAttribute != null && "true".equals(isFundAttribute.getValue())) {
						isoCurrencyInformation.fund = true;
					}
					isoCurrencyInformation.ccyNm = token(xml);
					break;
				case _CCY:
					isoCurrencyInformation.ccy = token(xml);
					break;
				case _CCY_NBR:
					isoCurrencyInformation.ccyNbr = Integer.valueOf(token(xml));
					break;
				case _CCY_MNR_UNTS:
					String value = token(xml);
					if (StringUtils.equals("N.A.", value)) {
						isoCurrencyInformation.ccyMnrUnts = null;
					} else {
						isoCurrencyInformation.ccyMnrUnts = Integer.valueOf(value);
					}
					break;
				default:
					skip(xml);
				}
			} else if (event.isEndElement()) {
				isoCurrencyInformations.add(isoCurrencyInformation);
				break;
			}
		}
	}
	
	private void currencyTable(XMLEventReader xml) throws XMLStreamException {
		while (xml.hasNext()) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (startName.equals(_CCY_NTRY)) currency(xml);
				else skip(xml);
			}  else if (event.isEndElement()) {
				break;
			}
		}
	}
	
	private void iso4217(XMLEventReader xml) throws XMLStreamException {
		while (xml.hasNext()) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (StringUtils.equals(_CCY_TBL, startName)) currencyTable(xml);
				else skip(xml);
			}  else if (event.isEndElement()) {
				break;
			}
		}
	}

	private void process(InputStream inputStream) throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader xml = inputFactory.createXMLEventReader(inputStream);

		while (xml.hasNext()) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (StringUtils.equals(_I_S_O_4217, startName)) iso4217(xml);
				else skip(xml);
			}  else if (event.isEndElement()) {
				break;
			}
		}
	}
	
	private static String token(XMLEventReader xml) throws XMLStreamException {
		String token = null;
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isCharacters()) {
				token = event.asCharacters().getData().trim();
			} else if (event.isEndElement()) {
				return token;
			} // else skip
		}
	}

	private static void skip(XMLEventReader xml) throws XMLStreamException {
		while (true) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				skip(xml);
			} else if (event.isEndElement()) break;
			// else ignore
		}
	}
	
}