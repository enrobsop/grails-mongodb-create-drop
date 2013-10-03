package grails.plugin.mongodbcreatedrop

enum CreateDropType {

	none,
	database,
	collections,
	keep,
	drop

	static CreateDropType lookup(String str) {
		CreateDropType.valueOf(clean(str))
	}

	private static String clean(str) {
		def clean = removeTrailingIfComboName(str)
		clean?.toLowerCase()?.trim() ?: none.name()
	}

	private static String removeTrailingIfComboName(str) {
		str ? str.split(":")[0] : ""
	}

	static String getValue(String str) {
		def firstColonIndex = str?.indexOf(":")
		if (isComboValueWithNonEmptyValue(str?.trim()?.length(), firstColonIndex) ) {
			return str.substring(firstColonIndex + 1)
		}
		null
	}

	static boolean isComboValueWithNonEmptyValue(length = 0, firstColonIndex = 0) {
		firstColonIndex > 0 && length > (firstColonIndex + 1)
	}
}
