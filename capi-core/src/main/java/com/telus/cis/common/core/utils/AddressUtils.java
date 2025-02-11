package com.telus.cis.common.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import lombok.Getter;

public class AddressUtils {
	
	private AddressUtils() {
		// private constructor to override Java's implicit public constructor
		throw new IllegalStateException("Utility classes should not have public constructors.");
	}
	
	public enum ProvinceOrTerritory {
		
		AB("Alberta"),
		BC("British Columbia"),
		MB("Manitoba"),
		NB("New Brunswick"),
		NL("Newfoundland and Labrador"),
		NT("Northwest Territories"),
		NS("Nova Scotia"),
		NU("Nunavut"),
		ON("Ontario"),
		PE("Prince Edward Island"),
		QC("Quebec"),
		SK("Saskatchewan"),
		YT("Yukon");
		
		@Getter
		private String fullName;
		
		private static Map<String, ProvinceOrTerritory> map = new HashMap<>();
		
		static {
			Stream.of(ProvinceOrTerritory.values()).forEach(province -> map.put(province.fullName, province));
		}
		
		ProvinceOrTerritory(String fullName) {
			this.fullName = fullName;
		}
		
		public String getAbbreviation() {
			return this.name();
		}
		
		public static ProvinceOrTerritory getProvinceOrTerritory(String fullName) {
			return map.get(fullName);
		}
		
	}
	
	public enum Country {
		
		CA("Canada", "CAN"),
		US("United States of America", "USA");
		
		@Getter
		private String fullName;
		
		@Getter
		private String code3;
	
		Country(String fullName, String code3) {
			this.fullName = fullName;
			this.code3 = code3;
		}
		
		public String getCode2() {
			return this.name();
		}
		
	}

}