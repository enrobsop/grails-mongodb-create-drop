package grails.plugin.mongobootstraputils

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

import static grails.plugin.mongobootstraputils.DropCreateType.*

class MongoBootStrapUtils {

	static final DEFAULT_KEEP_COLLECTIONS_PATTERN = "system\\..*"
	
	DropCreateType type = none
	
	def keepCollectionsRegex = DEFAULT_KEEP_COLLECTIONS_PATTERN
	def	databaseName
	def	username
	transient def password
	transient def db
	
	MongoBootStrapUtils(grailsApplication, dbFactory = new MongoDbFactory()) {
		def dbConfig			= grailsApplication.config.grails.mongo
		type					= DropCreateType.lookup(dbConfig.dropCreate)
		databaseName			= dbConfig.databaseName
		username				= dbConfig.username 
		password				= dbConfig.password 
		keepCollectionsRegex	= cleanRegexConfig(DropCreateType.getValue(dbConfig.dropCreate)) ?: DEFAULT_KEEP_COLLECTIONS_PATTERN
		validateConfig()
		db = dbFactory.getByName(databaseName)
	}
	
	void dropCreate() {
		if (doAbortBecauseNothingToDo()) return
		authenticate()
		if (type == database) {
			dropDatabase()
		} else {
			drop(collectionsWithNameNotMatching(keepCollectionsRegex))
		}
	}
	
	private boolean doAbortBecauseNothingToDo() {
		boolean isNothingToDo = type == none
		if (isNothingToDo) {
			log.debug "Nothing to do for type='$type'. Aborting dropCreate."
		} 
		isNothingToDo
	}
	
	private def cleanRegexConfig(regex) {
		if (regex?.respondsTo("trim")) {
			return regex.trim()
		}
		regex
	}
	
	private boolean authenticate() {
		boolean isAuthMode = credentialsProvided
		if (isAuthMode) {
			log.debug "Authenticating..."
			db.authenticate(username, password as char[])
		}
		isAuthMode
	}
	
	private def collectionsWithNameNotMatching(regex) {
		def allCollectionNames = db.getCollectionNames()
		log.debug "All collections: $allCollectionNames"
		allCollectionNames.findAll { !it.matches(regex) }
	}
	
	private void dropDatabase() {
		log.debug "Dropping database: $databaseName"
		db.dropDatabase()
	}
	
	private void drop(collectionNames) {
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
		
}