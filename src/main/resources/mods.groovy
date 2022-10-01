ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[43,)'

    license = 'MIT'
    issueTrackerUrl = 'https://github.com/MatyrobbrtMods/SimpleMiners/issues'

    mod {
        modId = 'simpleminers'
        displayName = 'Simple Miners'

        version = this.version

        description = 'A mod about mining out of thin ai.r'
        authors = ['Matyrobbrt']

        // logoFile = 'simplegui.png'

        dependencies {
            forge = "[${this.forgeVersion},)"
            minecraft = this.minecraftVersionRange

            mod('jei') {
                mandatory = false
                versionRange = "[${this.buildProperties['jei_version']})"
            }
        }
    }
}