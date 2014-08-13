package grails.plugin.mongodbcreatedrop

import grails.plugin.spock.UnitSpec
import grails.test.mixin.TestMixin
import spock.lang.Shared
import spock.lang.Unroll

import static grails.plugin.mongodbcreatedrop.CreateDropType.*

@TestMixin(MongoCreateDropUtilsSpecHelper)
class MongoCreateDropUtilsSpec extends UnitSpec {

	@Shared mongo

	def setup() {
		mongo = Mock(TestDB)
	}

	@Unroll
	def "the correct createDrop type is used when configured as [#createDropType]"() {

		given: "a grailsApplication config with createDrop type defined"
			def config = newMongoConfig([createDrop: createDropType, databaseName: "myDb"], mongo)

		when: "creating the bootstrap helper"
			def utils = new MongoCreateDropUtils(config)

		then: "the correct type is used"
			utils.type == theExpectedType

		where:
			createDropType			| theExpectedType
			"none"					| none
			"database"				| database
			"collections"			| collections
			"keep:system\\.users"	| keep
	}

	def "invalid createDropTypes should be handled correctly"() {

		given: "a grailsApplication config with an invalid createDropType"
			def config = newMongoConfig([createDrop: "nonsense", databaseName: "myDb"])

		when: "creating the bootstrap helper"
			new MongoCreateDropUtils(config)

		then: "the correct type is used"
			thrown IllegalArgumentException
	}

	@Unroll
	def "an unspecified createDropType [#unspecifiedType] should be handled correctly"() {

		given: "an unspecified createDrop type in the config"
			def config = newMongoConfig([createDrop: unspecifiedType, databaseName: "myDb"], mongo)

		expect:
			new MongoCreateDropUtils(config).type == none

		where:
			unspecifiedType << [[:], null, [], ""]
	}

	def "the createDrop type defaults correctly"() {

		given: "a minimal grailsApplication config"
			def config = newMongoConfig([databaseName: "myDb"], mongo)

		when: "creating the bootstrap helper"
			def utils = new MongoCreateDropUtils(config)

		then: "the correct type is used"
			utils.type == none
		and: "the correct keep pattern is used"
			utils.collectionsRegex == MongoCreateDropUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
	}

	def "the createDrop type defaults correctly when empty config"() {

		given: "a minimal grailsApplication config"
			def config = newMongoConfig([createDropType: [:], databaseName: "myDb"], mongo)

		when: "creating the bootstrap helper"
			def utils = new MongoCreateDropUtils(config)

		then: "the correct type is used"
			utils.type == none
		and: "the correct keep pattern is used"
			utils.collectionsRegex == MongoCreateDropUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
	}

	@Unroll
	def "the correct 'keep pattern' should be used when configured with [#theConfiguredPattern]"() {

		given: "a grailsApplication config with a 'keep pattern'"
			def config = newMongoConfig([createDrop: "keep:${theConfiguredPattern}", databaseName: "myDb"], mongo)
		and: "the anticipated default pattern"

		when: "creating the bootstrap helper"
			def utils = new MongoCreateDropUtils(config)

		then: "the correct pattern is used"
			utils.collectionsRegex == theExpectedPattern

		where:
			theConfiguredPattern	| theExpectedPattern
			""						| MongoCreateDropUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			" "						| MongoCreateDropUtils.DEFAULT_KEEP_COLLECTIONS_PATTERN
			/system\.users.*/		| "system\\.users.*"
			"sys.*"					| "sys.*"
	}

	def "config validation should work correctly"() {

		given: "a grailsApplication config without a databaseName"
			def config = newMongoConfig([:])

		when: "creating the bootstrap helper"
			new MongoCreateDropUtils(config)

		then: "a configuration exception is thrown"
			thrown IllegalArgumentException
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

		when: "invoking createDrop"
			setDB(theConfig, mongo)
			new MongoCreateDropUtils(theConfig).createDrop()

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
			theConfig 												| nDropDb	| nAuthenticate	| nCollections	| nCategory	| nBook	| nAuthor	| nSysIndexes	| nSyserUser
			curryConfig([createDrop:"database"])					| 1			| 0 			| 0				| 0			| 0		| 0			| 0				| 0
			curryConfig([createDrop:"none"])						| 0			| 0				| 0				| 0			| 0		| 0			| 0				| 0
			curryConfig([createDrop:"collections"])					| 0			| 0				| 1 			| 1			| 1		| 1			| 0				| 0
			curryConfig([createDrop:"keep:system\\.users"])			| 0			| 0				| 1				| 1			| 1		| 1			| 1				| 0
			curryConfig([createDrop:"keep:(system\\.users|book)"])	| 0			| 0				| 1				| 1			| 0		| 1			| 1				| 0
			curryConfig([createDrop:"drop:(book|author)"])			| 0			| 0				| 1				| 0			| 1		| 1			| 0				| 0
	}

	private mockCollectionCalled(name) {
		def collection = Mock(TestCollection)
		mongo.getCollection(name) >> collection
		collection
	}
}
