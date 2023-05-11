#!groovy
@Library('obos-jenkins@v4')
import no.obos.jenkins.Utils

def CLUSTER = "int"
def CHART = "spring-boot"
def CHART_VERSION = "0"

pipeline {
    agent any

    parameters {
        string(name: "version", defaultValue: "", description: "Version to deploy")
        choice(name: "environment", choices: ["test", "prod"], description: "Environment to deploy to")
        string(name: "namespace", defaultValue: "", description: "Namespace to deploy the service to. Defaults to a predefined value for the environment if not set. If set to 'feature' the branch will be deployd to this namespace. This works for all branches. For example feature branches usually stops the pipeline since they are disallowed in primary namespaces but can be deployd to the 'feature' namespace.")
    }

    stages {

        stage("Call obos-jenkins scripted pipeline") {
            steps {

                script {
                    Utils utils = new Utils()

                    utils.buildBranchOrPR(
                            baseDir: ".",
                            version: params.version,
                            environment: params.environment,
                            cluster: CLUSTER,
                            chartName: CHART,
                            chartVersion: CHART_VERSION,
                            namespace: params.namespace,
                            failOnEqualOrMoreThanNumCriticalVulnerabilities: 10, //TODO: Should change before production
                    )
                }
            }
        }
    }
}