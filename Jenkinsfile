// TODO: remove the secret and environment variables from this file and use .env files. Also expose the .env files to docker

pipeline {
    agent any

    environment {
        IMAGE_NAME = "portfolio-backend"
    }

    stages {
        stage('Determine Environment') {
            steps {
                script {
                    sh 'git fetch --tags'

                    def tag = sh(script: "git describe --exact-match --tags HEAD || echo ''", returnStdout: true).trim()
                    def branch = env.GIT_BRANCH?.replaceFirst(/^origin\//, '') ?: 'main'

                    echo "Branch: ${branch}"
                    echo "Tag: ${tag}"

                    def environment = 'develop'
                    def version = 'latest'

                    if (tag) {
                        if (tag ==~ /^staging-v\d+\.\d+\.\d+$/) {
                            environment = 'staging'
                            version = tag.replaceFirst(/^staging-v/, '')
                        } else if (tag ==~ /^v\d+\.\d+\.\d+$/) {
                            environment = 'production'
                            version = tag.replaceFirst(/^v/, '')
                        } else {
                            error("Unknown tag format: ${tag}")
                        }
                    } else if (branch == 'main' || branch == 'master') {
                        environment = 'develop'
                    } else {
                        echo "Skipping build: Not a relevant branch or tag"
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.ENVIRONMENT = environment
                    env.VERSION = version
                    env.CONTAINER_NAME = "${IMAGE_NAME}-${environment}"

                    def ports = [develop: '2025', staging: '2026', production: '2027']
                    env.EXPOSED_PORT = ports[environment]

                    echo "Version: ${VERSION}"
                    echo "Exposed port: ${EXPOSED_PORT}"
                    echo "Environment: ${ENVIRONMENT}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}-${ENVIRONMENT}:${VERSION} ."
            }
        }

        stage('Deploy to Docker') {
            steps {
                script {
                    // Match Secret File ID in Jenkins to format: env-file-develop, env-file-staging, env-file-production
                    def envFileCredentialId = "env-file-${ENVIRONMENT}"

                    withCredentials([file(credentialsId: envFileCredentialId, variable: 'ENV_FILE')]) {
                        sh """
                            docker stop ${CONTAINER_NAME} || true
                            docker rm ${CONTAINER_NAME} || true

                            docker run -d \\
                              --env-file $ENV_FILE \\
                              --name ${CONTAINER_NAME} \\
                              -p ${EXPOSED_PORT}:8080 \\
                              ${IMAGE_NAME}-${ENVIRONMENT}:${VERSION}
                        """
                    }
                }
            }
        }
    }
}


