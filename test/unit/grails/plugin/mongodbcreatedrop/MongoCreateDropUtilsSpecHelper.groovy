package grails.plugin.mongodbcreatedrop

class MongoCreateDropUtilsSpecHelper {

	def newMongoConfig(options=[:], db=null) {
		def mongoConfig = [
			config: [
				grails: [
					mongo: options
				]
			]
		]

		if (db) {
			setDB(mongoConfig, db)
		}

		mongoConfig
	}

	def curryConfig(options=[:]) {
		def config = [databaseName: "aDatabase"] << options
		newMongoConfig(config)
	}

	def setDB(theConfig, mongo) {

		def mongoDb = [
			getDB: { databaseName -> mongo }
		]

		theConfig.getMainContext = {[
			getBean: { beanName ->
				(beanName == "mongo") ? mongoDb : null
			}
		]}

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

