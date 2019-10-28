timestamps {
  currentBuild.displayName = "$branch"+'_'+"$image_version"
    try {
      notifyBuild('STARTED')
      node('build') {
        if (params.build) {
          stage('Build') {
            cleanWs notFailBuild: true
            git credentialsId: 'Main_CI' , branch: "$branch", url: 'git@github.com:company/your-application.git'
            sh "sudo docker build -t your-application:$image_version . -f ./Dockerfile "
            sh "sudo docker tag your-application:$image_version your.registry.com/your-application:$image_version"
            sh "sudo docker push your.registry.com/your-application:$image_version"
          }
        }
      }
      node('master') {
        stage('Deploy') {
          cleanWs notFailBuild: true
          git branch: 'master', url: 'git@github.com:company/infrastructure-ansible.git'
          sh """
             ansible-playbook playbooks/your-application-playbook.yml --diff \
             -e your_application_img=$image_version \
             replicas = $replicas
          """
        }
      }
    } catch (e) {
      currentBuild.result = "FAILED"
      throw e
    } finally {
      notifyBuild(currentBuild.result)
    }
}
def notifyBuild(String buildStatus = 'STARTED') {
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"

  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }
  slackSend (channel: 'jenkins-job', color: colorCode, message: summary, teamDomain: 'company', tokenCredentialId: 'tokenabrakadabra767')
}
