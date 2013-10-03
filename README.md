Grails MongoDB Create-Drop Plugin [![Build Status](https://travis-ci.org/enrobsop/grails-mongodb-create-drop.png?branch=master)](https://travis-ci.org/enrobsop/grails-mongodb-create-drop)
=================================

This plugin enables Grails applications using MongoDB to mimic the "create-drop" behaviour available for other databases. This is useful during development, testing and integration.

Features:
* Drop and recreate a whole Mongo database at startup.
* Drop and recreate a subset of collections within a database. For example, keep the Mongo users collection between startups.
* Use regex patterns to keep or drop collections.
* Can do nothing! For example, production environment.
* Simplifies the transition between authenticated and non-authenticated Mongo instances. For example, when deploying to a CI server.

Installation
------------

Usage
-----

1. Add `grails.mongo.createDrop` config in `config/Datasource.groovy` (see example below).
2. Invoke from `config/BootStrap.groovy` (see example below). 

Please see the samples below.

### Sample `config/Datasource.groovy`

	grails {
	  mongo {
			host 			= "localhost"
			port 			= 27017
			options {
				autoConnectRetry	= true
				connectTimeout		= 300
			}
		}
	}
	environments {
		development {   // MongoDB instance running without --auth flag.
			grails {
				mongo {
					createDrop		= "database"	// Recreate whole database.
					databaseName	= "mydb-dev"
				}
			}
		}
		test {
			grails {	// MongoDB instance running without --auth flag.
				mongo {
					createDrop		= "database"	// Recreate whole database.
					databaseName	= "mydb-test"
				}
			}
		}
		jenkins {	// MongoDB instance running with --auth flag.
			grails {
				mongo {
					createDrop		= "collections"	// Preserves system.users. Recreates app collections.
					databaseName	= "mydb-test"
					username		= "jsmith"
					password		= "password"
				}
			}
		}
		production {
			grails {
				mongo {	// MongoDB instance with --auth flag.
					createDrop		= "none"		// Do not recreate. Can be omitted because it is the default.
					databaseName	= "mydb-prod"
					username		= "jsmith"
					password		= "s3cr3t"
				}
			}
		}
	}

### Sample `config/Bootstrap.groovy`

    import grails.plugin.mongodbcreatedrop.MongoCreateDropMixin
    import grails.test.mixin.TestMixin
    ...
    @Mixin(MongoCreateDropMixin)
    @TestMixin(MongoCreateDropMixin)
    class BootStrap {
    
		def grailsApplication
    	
		def init = { servletContext ->
			createDropMongo(grailsApplication)
			...
		}
		...	
    }

Settings
--------
### grails.mongo.createDrop:
<table>
<tr><th>createDrop</th>                      <th>Description</th></tr>
<tr><td><tt>none</tt></td>                  <td>Default. Nothing is dropped.</td></tr>
<tr><td><tt>database</tt></td>              <td>Drops the entire database including any configured users. Therefore, it is not suitable for Mongo instances running with the <tt>--auth</tt> flag.</td></tr>
<tr><td><tt>collections</tt></td>           <td>Drops all collections except <tt>system.*</tt> collections. This is useful when Mongo is running with the <tt>--auth</tt> flag. This option is the same as using <tt>keep:system\..*</tt>.</td></tr>
<tr><td><tt>keep:</tt><em>regex</em></td>   <td>Only keeps collections with names matching the defined regex. All other collections are dropped. Example: <tt>keep:(history|system\.users)</tt>.</td></tr>
<tr><td><tt>drop:</tt><em>regex</em></td>   <td>Only drops collections with names matching the defined regex. All other collections are kept. Example: <tt>drop:(book|author|category)</tt>.</td></tr>
</table>
