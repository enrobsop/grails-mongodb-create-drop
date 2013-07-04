class MongoBootstrapUtilsGrailsPlugin {
    def version = "0.1.SNAPSHOT"
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

    def title = "Mongo BootStrap Utils" // Headline display name of the plugin
    def author = "Paul Osborne"
    def authorEmail = "hello@paulosborne.me.uk"
    def description = '''\
Provides BootStrap utilities to simulate database dropCreate behaviour when working with MongoDB. 
'''

    def documentation = "https://github.com/enrobsop/grails-mongo-bootstrap-utils/wiki"
	def license = "APACHE"
	def organization = [ name: "Morley Computing", url: "http://www.morley-computing.co.uk/" ]
	def issueManagement = [ system: "GitHub", url: "https://github.com/enrobsop/grails-mongo-bootstrap-utils/issues" ]
	def scm = [ url: "https://github.com/enrobsop/grails-mongo-bootstrap-utils/" ]

}
