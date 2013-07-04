grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {

	inherits("global")
    log "warn"
	
    repositories {
        grailsCentral()
        mavenCentral()
        mavenLocal()
    }
	
    dependencies {
		test('org.spockframework:spock-grails-support:0.7-groovy-2.0') {
			export = false
		}
	}

    plugins {
		build(':release:2.2.0', ':rest-client-builder:1.0.3') {
		    export = false
		}
		compile ":mongodb:1.2.0"
		test(':spock:0.7') {
		    export = false
			exclude 'spock-grails-support'
		}
    }
}
