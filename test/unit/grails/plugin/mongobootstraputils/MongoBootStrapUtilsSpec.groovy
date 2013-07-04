package grails.plugin.mongobootstraputils

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
	def "the correct 'keep pattern' should be used when configured with [#theConfiguredPattern]"() {

		given: "a grailsApplication config with a 'keep pattern'"
			def config = newMongoConfig([keepCollectionsRegex: theConfiguredPattern, databaseName: "myDb"])
		and: "the anticipated default pattern"
		
		when: "creating the bootstrap helper" 
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		then: "the correct pattern is used"
			utils.keepCollectionsRegex == theExpectedPattern
			
		where:
			theConfiguredPattern	| theExpectedPattern
			null					| MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			""						| MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			" "						| MongoBootStrapUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			/system\.users.*/		| /system\.users.*/
			"sys.*"					| "sys.*"
		
	}
	
	def "config validation should work correctly"() {
		
		given: "a grailsApplication config without a databaseName"
			def config = newMongoConfig([:])
			
		when: "creating the bootstrap helper"
			def Utils = new MongoBootStrapUtils(config, dbFactory)
		
		then: "a configuration exception is thrown"
			thrown GrailsConfigurationException
		
	}
	
	def "dropCreate should behave correctly when not in auth mode"() {
		
		given: "no auth mode"
			def config = noAuthModeConfig
		and: "the bootstrap helper class with a mock database"
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		when: "invoking dropCreate"
			utils.dropCreate()
		
		then: "the whole database should be dropped"
			1 * mongo.dropDatabase()

	}
	
	def "dropCreate should behave correctly when in auth mode"() {
		
		given: "auth mode is configured"
			def config	= authModeConfig
		and: "the bootstrap helper class with a mock database"
			def utils = new MongoBootStrapUtils(config, dbFactory)
		and: "some mock collections"
			def categoryCollection = Mock(TestCollection)
			def bookCollection = Mock(TestCollection)
			def authorCollection = Mock(TestCollection)
			
		when: "invoking dropCreate"
			utils.dropCreate()
		
		then: "the user authenticates against the database"
			1 * mongo.authenticate(config.config.grails.mongo.username, {config.config.grails.mongo.password as char[]})
		and: "the all collections for the database are determined"
			1 * mongo.getCollectionNames() >> ["category","book","author","system.indexes","system.users"]
		and: "selected collections are dropped"
			1 * mongo.getCollection("category") >> categoryCollection
			1 * mongo.getCollection("book") >> bookCollection
			1 * mongo.getCollection("author") >> authorCollection
			1 * categoryCollection.drop() 
			1 * bookCollection.drop() 
			1 * authorCollection.drop() 
		and: "the system collections are not used"
			0 * mongo.getCollection("system.indexes")
			0 * mongo.getCollection("system.users")
	}

	def "the keepCollectionsRegex pattern should be applied correctly"() {
		
		given: "a custom collections regex"
			def customRegex = /(category|system.*)/ 
		and: "auth mode is configured"
			def config = newMongoConfig([
				databaseName:			"aDatabase",
				username:				"aUser",
				password:				"aPassword",
				keepCollectionsRegex:	customRegex
			])
		and: "the bootstrap helper class with a mock database"
			def utils = new MongoBootStrapUtils(config, dbFactory)
		and: "some mock collections"
			def bookCollection 		= Mock(TestCollection)
			def authorCollection 	= Mock(TestCollection)
			
		when: "invoking dropCreate"
			utils.dropCreate()
		
		then: "the user authenticates against the database"
			1 * mongo.authenticate(config.config.grails.mongo.username, {config.config.grails.mongo.password as char[]})
		and: "the all collections for the database are determined"
			1 * mongo.getCollectionNames() >> ["category","book","author","system.indexes","system.users"]
		and: "selected collections are dropped"
			1 * mongo.getCollection("book") >> bookCollection
			1 * mongo.getCollection("author") >> authorCollection
			1 * bookCollection.drop()
			1 * authorCollection.drop()
		and: "the 'keep' collections are not dropped"
			0 * mongo.getCollection("category")
			0 * mongo.getCollection("system.indexes")
			0 * mongo.getCollection("system.users")
	}

}