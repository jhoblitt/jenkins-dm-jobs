node('jenkins-master') {
  dir('jenkins-dm-jobs') {
    checkout([
      $class: 'GitSCM',
      branches: scm.getBranches(),
      userRemoteConfigs: scm.getUserRemoteConfigs(),
      changelog: false,
      poll: false
    ])
    notify = load 'pipelines/lib/notify.groovy'
    util = load 'pipelines/lib/util.groovy'
  }
}

notify.wrap {
  def image = null
  def hub_repo = 'lsstsqre/mysqldump_to_s3'

  node('docker') {
    stage('checkout') {
      git([
        url: 'https://github.com/lsst-sqre/mysqldump-to-s3',
        branch: 'master'
      ])
    }

    stage('build') {
      image = docker.build("${hub_repo}", '--no-cache .')
    }

    stage('push') {
      docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-sqreadmin') {
        image.push('latest')
      }
    }
  }
} // notify.wrap
