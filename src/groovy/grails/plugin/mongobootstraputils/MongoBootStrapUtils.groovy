package grails.plugin.mongobootstraputils

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

class MongoBootStrapUtils {

	transient def db
	
	def	authMode
	def	databaseName
	def	username
	
	transient def password
	
	MongoBootStrapUtils(grailsApplication, dbFactory = new MongoDbFactory()) {
		def dbConfig	= grailsApplication.config.grails.mongo
		authMode		= dbConfig.authMode ?: false
		databaseName	= dbConfig.databaseName
		username		= dbConfig.username 
		password		= dbConfig.password 
		failIfInvalidConfig()
		db 				= dbFactory.getByName(databaseName)
	}
	
	void dropCreate() {
		log.debug "Mongo running in 'auth' mode: ${authMode}"
		if (authMode) {
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

	private void failIfInvalidConfig() {
		failIfAuthModeInvalid()
		failIfMissingCredentials()
	}
		
	private void failIfAuthModeInvalid() {
		if (isSetButNotBoolean(authMode)) {
			throw new GrailsConfigurationException("'grails.mongo.authMode' must be boolean. ${authMode.getClass()}")
		}
	}
	
	private void failIfMissingCredentials() {
		if (authMode && !(username && password)) {
			throw new GrailsConfigurationException("Username and password must be provided when using 'auth' mode.")
		}
	}

	private static boolean isSetButNotBoolean(value) {
		value != null && value?.getClass() != Boolean
	}
		
}