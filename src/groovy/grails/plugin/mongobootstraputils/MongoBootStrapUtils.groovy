package grails.plugin.mongobootstraputils

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

class MongoBootStrapUtils {

	transient def db
	
	def	databaseName
	def	username
	transient def password
	
	MongoBootStrapUtils(grailsApplication, dbFactory = new MongoDbFactory()) {
		def dbConfig	= grailsApplication.config.grails.mongo
		databaseName	= dbConfig.databaseName
		username		= dbConfig.username 
		password		= dbConfig.password 
		failIfInvalidConfig()
		db 				= dbFactory.getByName(databaseName)
	}
	
	void dropCreate() {
		log.debug "Mongo credentials provided: ${credentialsProvided}"
		if (credentialsProvided) {
			authenticate()
			dropAll(collectionsWithNameNotMatching(/system.*/))
		} else {
			log.debug "Dropping database: $databaseName"
			db.dropDatabase()
		}
	}
	
	void authenticate() {
		log.debug "Authenticating..."
		db.authenticate(username, password as char[])
	}
	
	def collectionsWithNameNotMatching(regex) {
		def allCollectionNames = db.getCollectionNames()
		log.debug "All collections: $allCollectionNames"
		allCollectionNames.findAll { !it.matches(regex) }			
	}
	
	void dropAll(collectionNames) {
		collectionNames.each {
			log.debug "Dropping collection: $it"
			db.getCollection(it).drop()
		}
	}
	
	private boolean isCredentialsProvided() {
		username || password
	}

	private void failIfInvalidConfig() {
		failIfDatabaseNameMissing() 
	}
	
	private void failIfDatabaseNameMissing() {
		if (!databaseName) {
			//throw 
		}
	}
		
	private static boolean isSetButNotBoolean(value) {
		value != null && value?.getClass() != Boolean
	}
		
}