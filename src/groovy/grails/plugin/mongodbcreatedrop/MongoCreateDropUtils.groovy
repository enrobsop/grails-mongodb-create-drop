package grails.plugin.mongodbcreatedrop

import static grails.plugin.mongodbcreatedrop.CreateDropType.*

class MongoCreateDropUtils {

	static final String DEFAULT_KEEP_COLLECTIONS_PATTERN = "system\\..*"

	CreateDropType type		= none
	def collectionsRegex	= DEFAULT_KEEP_COLLECTIONS_PATTERN
	def	databaseName
	transient db

	/**
	 * @deprecated dbFactory is no longer used, use {@link #MongoCreateDropUtils(grailsApplication)}.
	 * // TODO Remove this constructor.
	 */
	MongoCreateDropUtils(grailsApplication, dbFactory) {
		this(grailsApplication)
	}

	MongoCreateDropUtils(grailsApplication) {
		def dbConfig		= grailsApplication.config.grails.mongo

		type				= getTypeFrom(dbConfig)
		collectionsRegex	= getRegexFrom(dbConfig)
        databaseName		= dbConfig.databaseName

        validateConfig()

        def mongo = grailsApplication.getMainContext().getBean("mongo")
        db = mongo.getDB(databaseName)
	}

	void createDrop() {
		if (doAbortBecauseNothingToDo()) return
		if (type == database) {
			dropDatabase()
		} else if (type == drop) {
			dropAll(collectionsWithNameMatching())
		} else {
			dropAll(collectionsWithNameNotMatching())
		}
	}

	private boolean doAbortBecauseNothingToDo() {
		boolean isNothingToDo = type == none
		if (isNothingToDo) {
			log.debug "Nothing to do for type='$type'. Aborting createDrop."
		}
		isNothingToDo
	}

	private getTypeFrom(config) {
		try {
			def configType = config?.createDrop ?: "none"
			return CreateDropType.lookup(configType)
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid value for createDrop: $config.createDrop")
		}
	}

	private getRegexFrom(config) {
		cleanRegexConfig(CreateDropType.getValue(config?.createDrop?.toString())) ?: DEFAULT_KEEP_COLLECTIONS_PATTERN
	}

	private cleanRegexConfig(regex) {
		if (regex?.respondsTo("trim")) {
			return regex.trim()
		}
		regex
	}

	private collectionsWithNameNotMatching() {
		findCollectionNamesWhere() {!it.matches(collectionsRegex) }
	}

	private collectionsWithNameMatching() {
		findCollectionNamesWhere() { it.matches(collectionsRegex) }
	}

	private findCollectionNamesWhere(condition) {
		def allCollectionNames = db.getCollectionNames()
		log.debug "All collections: $allCollectionNames"
		allCollectionNames.findAll { condition(it) }
	}

	private void dropDatabase() {
		log.debug "Dropping database: $databaseName"
		db.dropDatabase()
	}

	private void dropAll(collectionNames) {
		collectionNames.each {
			log.debug "Dropping collection: $it"
			db.getCollection(it).drop()
		}
	}

	private void validateConfig() {
		failIfDatabaseNameMissing()
	}

	private void failIfDatabaseNameMissing() {
		if (!databaseName) {
			throw new IllegalArgumentException("'grails.mongo.databaseName' is missing.")
		}
	}
}
