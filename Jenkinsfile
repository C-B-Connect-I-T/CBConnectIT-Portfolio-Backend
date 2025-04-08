pipeline {
    agent any

    environment {
        IMAGE_NAME = "portfolio-frontend"
    }

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
                    env.CONTAINER_NAME = "${IMAGE_NAME}-${environment}"

                    env.EXPOSED_PORT = environment == 'production' ? '2027' :
                                       environment == 'staging' ? '2026' : '2025'

                    env.ADMIN_SEED_PASSWORD = "Test1234+@"
                    env.database-url="jdbc:mysql://0.0.0.0:3307/cbconnectitportfoliodev"
                    env.database-password="password"
                    env.database-username="christiano"
                    env.JWT_SECRET="My-very-secret-jwt-secret"

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
                    sh "docker stop ${CONTAINER_NAME} || true"
                    sh "docker rm ${CONTAINER_NAME} || true"
                    sh "docker run -d --name ${CONTAINER_NAME} -p ${EXPOSED_PORT}:8080 ${IMAGE_NAME}-${ENVIRONMENT}:${VERSION}"
                    sh "docker system prune -f"
                }
            }
        }
    }
}


