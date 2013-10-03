package grails.plugin.mongodbcreatedrop

class MongoCreateDropMixin {

	void createDropMongo(grailsApplication) {
		new MongoCreateDropUtils(grailsApplication).createDrop()
	}
}
