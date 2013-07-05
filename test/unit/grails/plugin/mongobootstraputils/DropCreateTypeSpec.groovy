package grails.plugin.mongobootstraputils

import static grails.plugin.mongobootstraputils.DropCreateType.*

import grails.plugin.spock.UnitSpec
import spock.lang.Unroll

class DropCreateTypeSpec extends UnitSpec {

	@Unroll
	def "correctly lookups a type using a string value of [#str]"() {
		
		expect:
			DropCreateType.lookup(str) == expectedEnumItem
		
		where:		
			str				| expectedEnumItem
			null			| none
			""				| none
			" "				| none
			"None"			| none
			"none"			| none
			"Database"		| database
			" database "	| database
			"Collections"	| collections
			"keep:system.*" | keep
			"keep:" 		| keep
			"keep"	 		| keep
			"drop:system.*" | drop
			"drop:" 		| drop
			"drop"	 		| drop
	}

	@Unroll	
	def "correctly gets the value from a combo string value [#str]"() {
		
		expect:
			DropCreateType.getValue(str) == expectedValue 
		
		where:
			str							| expectedValue
			"keep:system.*"				| "system.*"
			"keep:"						| null
			"keep:  "					| null
			"keep:pattern:with:colon.*"	| "pattern:with:colon.*"
			"keep:system\\.users"		| "system\\.users"
			"keep"						| null
			"drop:system.*"				| "system.*"
			"drop:"						| null
			"drop:  "					| null
			"drop:pattern:with:colon.*"	| "pattern:with:colon.*"
			"drop:system\\.users"		| "system\\.users"
			"drop"						| null
			""							| null
			null						| null
	}
	
}
