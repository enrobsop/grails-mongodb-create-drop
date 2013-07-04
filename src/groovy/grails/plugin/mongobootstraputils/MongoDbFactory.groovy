package grails.plugin.mongobootstraputils

import com.gmongo.GMongo

class MongoDbFactory {

	def getByName(dbName) {
		new GMongo().getDB(dbName)
	}
	
}
