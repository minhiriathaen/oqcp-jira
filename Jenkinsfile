pipeline {
    agent none

    stages {
        stage ('Build & Test') {
            parallel {        
                stage('Build & Test BE') {
                    agent {
                        dockerfile {
                            filename 'Dockerfile-builder'
                            dir 'backend'
                        }
                    }
                    steps {
                        dir ('backend'){
                            sh 'gradle cleanTest test'
                            sh 'gradle sonarqube'
                        }
                    }
                    post {
                        always { junit 'backend/**/test/*.xml' }
                    }
                }
                stage('Build, Test, Validate FE') {
                    stages {
                        stage('Build & Test FE'){
                            agent {
                                dockerfile { 
                                    dir 'web'
                                    args '-u root:root'
                                }
                            }
                            steps {
                                dir('web'){
                                    // be careful with hard coded path, but it helps accelerate build time (see in Dockerfile)
                                    sh 'yarn --cwd /usr/src/app test-ci ; cp -f /usr/src/app/junit.xml ./'
                                    sh 'yarn --cwd /usr/src/app sonar ; cp -rf /usr/src/app/coverage ./'
                                }
                            }
                            post {
                                always { junit 'web/junit.xml' }
                            }
                        }
                        stage('Sonar report FE'){
                            agent { 
                                docker { 
                                    image 'sonarsource/sonar-scanner-cli' 
                                    args '-e SONAR_HOST_URL="https://sonarqube8.minhiriathaen.hu" -e SONAR_LOGIN="b9c6d386555767737f59ab130a3d0393259f2c0d"'
                                }
                            }
                            steps {
                                dir('web'){
                                    sh 'sonar-scanner'
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('Deliver') {
            when {
                branch 'master'
            }
            parallel {       
                stage('Build & Publish BE Docker img') {
                    agent any
                    steps {
                        dir('backend'){
                            script {
                                jiraBackend = docker.build 'oqcp/jira-backend'
                                docker.withRegistry('https://repo.minhiriathaen.hu/repository/docker-private', 'minhiriathaen-repo') {
                                    jiraBackend.push("$BUILD_NUMBER")
                                    jiraBackend.push('latest')
                                }
                            }
                        }
                    }
                }
                stage('Build & Publish FE Docker img') {
                    agent any
                    steps {
                        dir('web'){
                            script {
                                jiraFrontend = docker.build 'oqcp/jira-frontend'
                                docker.withRegistry('https://repo.minhiriathaen.hu/repository/docker-private', 'minhiriathaen-repo') {
                                    jiraFrontend.push("$BUILD_NUMBER")
                                    jiraFrontend.push('latest')
                                }
                            }
                        }
                    }
                }
                stage('Build & Publish Proxy Docker img') {
                    agent any
                    steps {
                        dir('proxy'){
                            script {
                                jiraProxy = docker.build 'oqcp/jira-proxy'
                                docker.withRegistry('https://repo.minhiriathaen.hu/repository/docker-private', 'minhiriathaen-repo') {
                                    jiraProxy.push("$BUILD_NUMBER")
                                    jiraProxy.push('latest')
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('Deploy to Test'){
            when {
                branch 'master'
            }
            agent any
            steps {
                sh 'echo "" >> test.env'
                sh 'echo "TAG=$BUILD_NUMBER" >> test.env'
                script {
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: 'oqcp-test-infra',
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                            remoteDirectory: 'jira',
                                            sourceFiles: 'docker-compose.yml, docker-compose-test.yml, test.env, container.test.env',
                                            execCommand: 
                                                '''
                                                cd jira
                                                docker login -u ci -p HaIK1n22DbIgJrQPWys repo.minhiriathaen.hu && 
                                                docker-compose -f docker-compose.yml -f docker-compose-test.yml --env-file test.env pull &&
                                                docker-compose -f docker-compose.yml -f docker-compose-test.yml --env-file test.env up -d --no-build &&
                                                docker ps --filter "label=com.docker.compose.project=oqcp-jira-test" &&
                                                docker image prune -a -f
                                                '''
                                    )
                                ]
                            )
                        ]
                    )
                }
            }
        }
    }
}