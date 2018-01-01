pipeline 
{
    agent any

    triggers
    {
        cron('H H(0-5) * * *')
        pollSCM('@hourly')
    }

    options
    {
        buildDiscarder(logRotator(numToKeepStr:'10'))
        disableConcurrentBuilds()
        skipStagesAfterUnstable()
        timeout(time: 1, unit: 'HOURS')
    }

    stages
    {
        stage('Compile')
        {
            steps
            {
                sh 'mvn clean compile test-compile'
            }
        }
  
        stage('Unit-Tests')
        {
            steps
            {
                sh 'mvn surefire:test'
            }
        }
  
        stage('Integration-Tests')
        {
            steps
            {
                sh 'mvn failsafe:integration-test'
            }
        }
        
        stage('Release')
        {
            when { branch "master" }
        
            steps
            {
                sh 'mvn clean deploy -Dmaven.test.skip=true'
            }
        }
    }
}