pipeline {
  agent any
  stages {
    stage('Lint Analysis') {
      steps {
        tool 'Gradle 7.2'
        sh './gradlew lint'
      }
    }

  }
}