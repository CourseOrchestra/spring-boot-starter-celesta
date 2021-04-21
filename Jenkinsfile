node {
    def server = Artifactory.server 'ART'
    def rtMaven = Artifactory.newMavenBuild()
    def buildInfo
    def oldWarnings

    stage ('Clone') {
        checkout scm
    }

    stage ('Artifactory configuration') {
        rtMaven.tool = 'M3' 
        rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
        rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
        buildInfo = Artifactory.newBuildInfo()
        buildInfo.env.capture = true
    }


    try{
        stage ('Exec Maven') {
            rtMaven.run pom: 'pom.xml', goals: 'clean install -P dev', buildInfo: buildInfo
        }
    } finally {
        junit '**/surefire-reports/**/*.xml'
        recordIssues tool: checkStyle(pattern: '**/target/checkstyle-result.xml')
        recordIssues tool: spotBugs(pattern: '**/target/spotbugsXml.xml')
		publishHTML (target: [
          allowMissing: true,
          alwaysLinkToLastBuild: true,
          keepAll: true,
          reportDir: 'target/site/jacoco',
          reportFiles: 'index.html',
          reportName: "JaCoCo report"
       ])
    }

    if (env.BRANCH_NAME == 'dev') {
        stage ('Publish build info') {
            server.publishBuildInfo buildInfo
        }
    }
}
