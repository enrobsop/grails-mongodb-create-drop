package grails.plugin.mongodbcreatedrop

import com.gmongo.GMongo

class MongoDbFactory {

	def getByName(dbHost="localhost",dbName) {
		new GMongo(dbHost).getDB(dbName)
	}
}
