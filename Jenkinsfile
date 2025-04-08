pipeline {
    agent any

//     environment {
//         IMAGE_NAME = "portfolio-frontend"
//     }
//
    stages {
        stage('Determine Environment') {
            steps {
                script {
                    sh 'git fetch --tags'

                    def tag = sh(script: "git describe --exact-match --tags HEAD || echo ''", returnStdout: true).trim()
                    def branch = env.GIT_BRANCH?.replaceFirst(/^origin\//, '') ?: 'develop'

                    echo "Branch: ${branch}"
                    echo "Tag: ${tag}"

                    def environment = 'develop'
                    def version = 'latest'

                    if (tag) {
                        if (tag ==~ ~/^staging-v\d+\.\d+\.\d+$/) {
                            environment = 'staging'
                            version = tag.replaceFirst(/^staging-v/, '')
                        } else if (tag ==~ ~/^v\d+\.\d+\.\d+$/) {
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
//                     env.CONTAINER_NAME = "${IMAGE_NAME}-${environment}"

                    env.EXPOSED_PORT = environment == 'production' ? '2027' :
                                       environment == 'staging' ? '2026' : '2025'

                    echo "Version: ${VERSION}"
                    echo "Exposed port: ${EXPOSED_PORT}"
                    echo "Environment: ${ENVIRONMENT}"
                }
            }
        }

        stage('Stop existing containers') {
            steps {
                sh 'docker-compose down || true'
            }
        }

        stage('Build and Start containers') {
            steps {
                sh 'docker-compose up -d --build'
            }
        }
    }
}


