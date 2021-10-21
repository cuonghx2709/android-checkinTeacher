pipeline {
  agent any
  stages {
    stage('Lint Analysis') {
      steps {
        tool 'Gradle 7.2'
        bat 'gradlew.bat lint'
      }
    }

  }
}