package grails.plugin.mongobootstraputils

import grails.plugin.spock.UnitSpec
import grails.test.mixin.*

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Unroll

class MongoBootStrapUtilsSpec extends UnitSpec {

	@Shared dbFactory
	@Shared mongo
	
	def setup() {
		dbFactory 	= Mock(MongoDbFactory)
		mongo		= Mock(TestDB)
		dbFactory.getByName(_) >> mongo
	}
	
	def "dropCreate should behave correctly when not in auth mode"() {
		
		given: "no auth mode"
			def config = newMongoConfig([:])
		and: "the bootstrap helper class with a mock database"
			def utils = new MongoBootStrapUtils(config, dbFactory)
			
		when: "invoking dropCreate"
			utils.dropCreate()
		
		then: "the whole database should be dropped"
			1 * mongo.dropDatabase()

	}
	
	@IgnoreRest
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
		
	private def newMongoConfig(options=[:]) {
		[
			config: [
				grails: [
					mongo: options
				]
			]
		]
	}
	
	private def getNoAuthModeConfig() {
		newMongoConfig()
	}
	
	private def getAuthModeConfig() {
		newMongoConfig([username: "aUser", password: "aPassword"])
	}
	
}

class TestDB {
	
	void dropDatabase() {}
	
	void authenticate(username, password) {}
	
	def getCollection(name) { }
	
	def getCollectionNames() { }
	
}

class TestCollection {
	
	def name
	
	void drop() {}
	
}
