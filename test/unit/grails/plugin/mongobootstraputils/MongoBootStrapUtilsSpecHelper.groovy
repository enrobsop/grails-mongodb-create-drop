package grails.plugin.mongobootstraputils

class MongoBootStrapUtilsSpecHelper {

	def newMongoConfig(options=[:]) {
		[
			config: [
				grails: [
					mongo: options
				]
			]
		]
	}
	
	def getNoAuthModeConfig(options=[:]) {
		def config = [databaseName: "aDatabase"] << options
		newMongoConfig(config)
	}
	
	def getAuthModeConfig(options=[:]) {
		def config = [databaseName: "aDatabase", username: "aUser", password: "aPassword"] << options
		newMongoConfig(config)
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

