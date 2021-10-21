pipeline {
  agent any
  stages {
    stage('Lint Analysis') {
      steps {
        tool 'Gradle 7.2'
        sh '''export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
./gradlew lint'''
      }
    }

  }
}