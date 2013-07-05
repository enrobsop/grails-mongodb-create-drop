package grails.plugin.mongobootstraputils

import static grails.plugin.mongobootstraputils.DropCreateType.*
import grails.plugin.spock.UnitSpec
import grails.test.mixin.*

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

import spock.lang.Shared
import spock.lang.Unroll

@TestMixin(MongoBootStrapUtilsSpecHelper)
class MongoBootStrapUtilsSpec extends UnitSpec {

	@Shared dbFactory
	@Shared mongo
	
	def setup() {
		dbFactory 	= Mock(MongoDbFactory)
		mongo		= Mock(TestDB)
		dbFactory.getByName(_) >> mongo
	}
	
	@Unroll
	def "the correct dropCreate type is used when configured as [#dropCreateType]"() {
		
		given: "a grailsApplication config with dropCreate type defined"
			def config = newMongoConfig([dropCreate: dropCreateType, databaseName: "myDb"])
			
		when: "creating the bootstrap helper"
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		then: "the correct type is used"
			utils.type == theExpectedType
		
		where:
			dropCreateType			| theExpectedType
			"none"					| none
			"database"				| database
			"collections"			| collections
			"keep:system\\.users"	| keep
	}
	
	def "the dropCreate type defaults correctly"() {
		
		given: "a minimal grailsApplication config"
			def config = newMongoConfig([databaseName: "myDb"])
			
		when: "creating the bootstrap helper"
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		then: "the correct type is used"
			utils.type == none
		and: "the correct keep pattern is used"
			utils.keepCollectionsRegex == MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN

	}

	def "the dropCreate type defaults correctly when empty config"() {
		
		given: "a minimal grailsApplication config"
			def config = newMongoConfig([dropCreateType: [:], databaseName: "myDb"])
			
		when: "creating the bootstrap helper"
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		then: "the correct type is used"
			utils.type == none
		and: "the correct keep pattern is used"
			utils.keepCollectionsRegex == MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
		
	}


	@Unroll
	def "the correct 'keep pattern' should be used when configured with [#theConfiguredPattern]"() {

		given: "a grailsApplication config with a 'keep pattern'"
			def config = newMongoConfig([dropCreate: "keep:${theConfiguredPattern}", databaseName: "myDb"])
		and: "the anticipated default pattern"
		
		when: "creating the bootstrap helper" 
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		then: "the correct pattern is used"
			utils.keepCollectionsRegex == theExpectedPattern
			
		where:
			theConfiguredPattern	| theExpectedPattern
			""						| MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			" "						| MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			/system\.users.*/		| "system\\.users.*"
			"sys.*"					| "sys.*"
		
	}
	
	def "config validation should work correctly"() {
		
		given: "a grailsApplication config without a databaseName"
			def config = newMongoConfig([:])
			
		when: "creating the bootstrap helper"
			new MongoBootStrapUtils(config, dbFactory)
		
		then: "a configuration exception is thrown"
			thrown GrailsConfigurationException
		
	}
	
	@Unroll
	def "dropping collections should work correctly for #theConfig"() {
		
		given: "some collections"
			def categoryCollection		= mockCollectionCalled("category")
			def bookCollection			= mockCollectionCalled("book")
			def authorCollection		= mockCollectionCalled("author")
			def sysIndexesCollection	= mockCollectionCalled("system.indexes")
			def sysUsersCollection		= mockCollectionCalled("system.users")
			def collectionNames			= ["category","book","author","system.indexes","system.users"]

		when: "invoking dropCreate"
			new MongoBootStrapUtils(theConfig, dbFactory).dropCreate()
		
		then: "dropDatabase is called correctly"
			nDropDb * mongo.dropDatabase()
		and: "authenticate is called correctly"
			nAuthenticate * mongo.authenticate(_,_)
		and: "getCollectionNames is called correctly"
			nCollections * mongo.getCollectionNames() >> collectionNames
		and: "each collection is dropped correctly"
			nCategory	* categoryCollection.drop()
			nBook		* bookCollection.drop()
			nAuthor 	* authorCollection.drop()
			nSysIndexes	* sysIndexesCollection.drop()
			nSyserUser 	* sysUsersCollection.drop()
			
		where:
			theConfig 														| nDropDb	| nAuthenticate	| nCollections	| nCategory	| nBook	| nAuthor	| nSysIndexes	| nSyserUser
			getNoAuthModeConfig([dropCreate:"database"])					| 1			| 0 			| 0				| 0			| 0		| 0			| 0				| 0
			getNoAuthModeConfig([dropCreate:"none"])						| 0			| 0				| 0				| 0			| 0		| 0			| 0				| 0
			getNoAuthModeConfig([dropCreate:"collections"])					| 0			| 0				| 1 			| 1			| 1		| 1			| 0				| 0
			getNoAuthModeConfig([dropCreate:"keep:system\\.users"])			| 0			| 0				| 1				| 1			| 1		| 1			| 1				| 0
			getNoAuthModeConfig([dropCreate:"keep:(system\\.users|book)"])	| 0			| 0				| 1				| 1			| 0		| 1			| 1				| 0
			getAuthModeConfig([dropCreate:"database"])						| 1			| 1				| 0				| 0			| 0		| 0			| 0				| 0
			getAuthModeConfig([dropCreate:"none"])							| 0			| 0				| 0				| 0			| 0		| 0			| 0				| 0
			getAuthModeConfig([dropCreate:"collections"])					| 0			| 1				| 1				| 1			| 1		| 1			| 0				| 0
			getAuthModeConfig([dropCreate:"keep:system\\.users"])			| 0			| 1				| 1				| 1			| 1		| 1			| 1				| 0
	}

	private def mockCollectionCalled(name) {
		def collection = Mock(TestCollection)
		mongo.getCollection(name) >> collection
		collection
	}
	
}