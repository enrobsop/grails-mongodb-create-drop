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
	
	def getNoAuthModeConfig() {
		newMongoConfig([databaseName: "aDatabase"])
	}
	
	def getAuthModeConfig() {
		newMongoConfig([databaseName: "aDatabase", username: "aUser", password: "aPassword"])
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

