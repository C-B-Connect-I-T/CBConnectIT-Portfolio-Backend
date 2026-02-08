pipeline {
    agent any

    environment {
        PROJECT_NAME = "portfolio-backend"
        DOCKER_NETWORK = "infrastructure_app-network"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Determine Environment') {
            steps {
                script {
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
                        version = 'latest'
                    } else {
                        echo "Skipping build: Not a relevant branch or tag"
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.ENVIRONMENT = environment
                    env.VERSION = version
                    env.CONTAINER_NAME = "${PROJECT_NAME}-${environment}"

                    // Port mapping (adjust per project)
                    def ports = [develop: '2025', staging: '2026', production: '2027']
                    env.EXPOSED_PORT = ports[environment]

                    echo "Environment: ${ENVIRONMENT}"
                    echo "Version: ${VERSION}"
                    echo "Container: ${CONTAINER_NAME}"
                    echo "Port: ${EXPOSED_PORT}"
                }
            }
        }

        stage('Run Unit Tests') {
            steps {
                script {
                    sh './gradlew test --no-daemon --stacktrace'
                }
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Run Detekt Analysis') {
            steps {
                script {
                    sh './gradlew detekt --no-daemon'
                }
            }
            post {
                always {
                    recordIssues(
                        tools: [detekt(pattern: '**/build/reports/detekt/detekt.xml')]
                    )
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Now build the image with that file present
                    sh "docker build -t ${PROJECT_NAME}-${ENVIRONMENT}:${VERSION} ."

                    // Tag as latest for the environment
                    sh "docker tag ${PROJECT_NAME}-${ENVIRONMENT}:${VERSION} ${PROJECT_NAME}-${ENVIRONMENT}:latest"
                }
            }
        }

        stage('Stop Old Container') {
            steps {
                script {
                    sh """
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                    """
                }
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    def envFileCredentialId = "env-file-${ENVIRONMENT}"

                    withCredentials([file(credentialsId: envFileCredentialId, variable: 'ENV_FILE')]) {
                        sh """
                            docker run -d \\
                              --env-file \$ENV_FILE \\
                              --name ${CONTAINER_NAME} \\
                              --network ${DOCKER_NETWORK} \\
                              -p ${EXPOSED_PORT}:8080 \\
                              --restart unless-stopped \\
                              ${PROJECT_NAME}-${ENVIRONMENT}:${VERSION}
                        """
                    }
                }
            }
        }

        stage('Cleanup Old Images') {
            steps {
                script {
                    // Keep only the last 3 images per environment
                    sh """
                        docker images ${PROJECT_NAME}-${ENVIRONMENT} --format '{{.ID}} {{.CreatedAt}}' | \\
                        sort -k2 -r | \\
                        tail -n +4 | \\
                        awk '{print \$1}' | \\
                        xargs -r docker rmi || true
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✓ Deployment successful for ${PROJECT_NAME}-${ENVIRONMENT}:${VERSION}"
            echo "Access at: http://localhost:${EXPOSED_PORT}"
        }
        failure {
            echo "✗ Deployment failed for ${PROJECT_NAME}-${ENVIRONMENT}:${VERSION}"
            // TODO: Implement rollback logic
            // sh "docker start ${CONTAINER_NAME}-previous || true"
        }
        always {
            // Clean workspace
            cleanWs()
        }
    }
}

