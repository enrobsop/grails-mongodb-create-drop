package grails.plugin.mongobootstraputils

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

class MongoBootStrapUtils {

	static final DEFAULT_KEEP_COLLECTIONS_PATTERN = /system.*/
	
	def keepCollectionsRegex = DEFAULT_KEEP_COLLECTIONS_PATTERN
	def	databaseName
	def	username
	transient def password
	transient def db
	
	MongoBootStrapUtils(grailsApplication, dbFactory = new MongoDbFactory()) {
		def dbConfig			= grailsApplication.config.grails.mongo
		databaseName			= dbConfig.databaseName
		username				= dbConfig.username 
		password				= dbConfig.password 
		keepCollectionsRegex	= dbConfig.keepCollectionsRegex?.trim() ?: DEFAULT_KEEP_COLLECTIONS_PATTERN
		validateConfig()
		db = dbFactory.getByName(databaseName)
	}
	
	void dropCreate() {
		log.debug "Mongo credentials provided: ${credentialsProvided}"
		if (credentialsProvided) {
			authenticate()
			dropAll(collectionsWithNameNotMatching(keepCollectionsRegex))
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

	private void validateConfig() {
		failIfDatabaseNameMissing() 
	}
	
	private void failIfDatabaseNameMissing() {
		if (!databaseName) {
			throw new GrailsConfigurationException("'grails.mongo.databaseName' is missing.")
		}
	}
		
	private static boolean isSetButNotBoolean(value) {
		value != null && value?.getClass() != Boolean
	}
		
}