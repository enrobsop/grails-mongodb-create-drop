package grails.plugin.mongobootstraputils

class MongoBootStrapUtilsMixin {

	void dropCreateMongo(grailsApplication) {
		new MongoBootStrapUtils(grailsApplication).dropCreate()
	}
		
}