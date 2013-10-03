class MongodbCreateDropGrailsPlugin {
    def version = "1.0"
    def grailsVersion = "2.0 > *"
	def dependsOn = [
		'mongodb': "* > 1.0.0.RC3"
	]
    def pluginExcludes = [
		"grails-app/controllers/**",
		"grails-app/domain/**",
		"grails-app/i18n/**",
		"grails-app/views/**/*",
		"web-app/**"    
	]

    def title = "MongoDB Create-Drop" // Headline display name of the plugin
    def author = "Paul Osborne"
    def authorEmail = "hello@paulosborne.me.uk"
    def description = '''\
Provides Grails applications using MongoDB with the ability to mimic the "create-drop" behaviour available for other databases.
'''

    def documentation = "https://github.com/enrobsop/grails-mongodb-create-drop/wiki"
	def license = "APACHE"
	def organization = [ name: "Paul Osborne", url: "http://www.paulosborne.me.uk/" ]
	def issueManagement = [ system: "GitHub", url: "https://github.com/enrobsop/grails-mongodb-create-drop/issues" ]
	def scm = [ url: "https://github.com/enrobsop/grails-mongodb-create-drop/" ]

}
