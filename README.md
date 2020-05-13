# Installation

## Prerequisites

Openshift 4+ with admin access
Openshift CLI if you're doing the scripted install (Recommended)

## Namespace

To avoid permission issues, create a dedicated project/namespace to hold all the resources from this demo. This will also make cleanup easier later.

## Dependencies

- Prometheus Operator
- Kafka Operator
- Grafana Operator

Folllow the instructions in the Openshift documentation in order to install the required operators from the cluster's web console: https://docs.openshift.com/container-platform/4.1/applications/operators/olm-adding-operators-to-cluster.html

__NOTE__: When asked about the namespace, choose to install only in the project you created in the previous step.

__NOTE__: AMQ Streams 1.4.1 currently doesn't deploy on Openshift because of a bug in the operator. You can install 1.4.0 by following the simple steps in the AMQ streams doc instead:

https://access.redhat.com/documentation/en-us/red_hat_amq/7.6/html/evaluating_amq_streams_on_openshift/assembly-evaluation-str#proc-install-crds-str

## Preparation

1. Install the Helm CLI by following the instructions in the doc: https://helm.sh/docs/intro/install/
1. Add the demo Helm repo
	- helm repo add msday2020 https://raw.githubusercontent.com/mrethers/msday2020/master/resources/helm/monitor-app
	- helm repo update
1. Install the Openshift CLI if you haven't already. https://docs.openshift.com/container-platform/4.2/cli_reference/openshift_cli/getting-started-cli.html

## Installation

1. Login to your Openshift cluster from a terminal (oc login) and switch to your project (oc project my-project, replace my-project with the project you create in the previous steps)
1. Install the demo
	- helm install --generate-name msday2020/monitor-app
	
## Verification

It might take a little while to install everything because the builds need to download all the maven dependencies. You can check the builds progress in the Builds section of the Openshift web console.

When the services are ready, you should see 3 pods on the Workload/Pods page: thermostat-service, temperature-processor, prometheus-receiver, all in Running state.

In the Networking section of the Openshift console, click Routes and you should see the 3 routes associated with our services.

Check that the services are OK by clicking the links and adding __/actuator/health__ at the end.

You should see a page saying:

```"status": "UP"```

## Grafana

Now we can install our dashboards to visualize the data from Prometheus:

1. Go to the Grafana UI by going to the Networking/Routes section of the Openshift console and find the link to the Grafana cluster service
1. Sign-in as an admin by using the default credentials root/secret
1. Click the + sign in the left menu and choose import
1. Paste the json from https://raw.githubusercontent.com/mrethers/msday2020/master/resources/helm/monitor-app/config/springboot-dashboard.json
1. Click Load
1. Change the name and UID of the dashboard if you see an error

This will create a Spring Boot monitoring dashboard in the Grafana UI. This dashboard is contributed by community users on the Grafana website (ID 10280).

Repeat the import steps with https://raw.githubusercontent.com/mrethers/msday2020/master/resources/helm/monitor-app/config/temperature-dashboard.json

This will install our temperature dashboard to visualize our custom metrics.

## Starting the simulator

The simulator source code is available at https://github.com/mrethers/msday2020.git.

The simulator uses a Spring Boot REST client to send temperatures records to the thermostat service using the /temperature-records endpoint. By default, the client produces a 1 degree increase every second for __pond_1__, which represents a 100% efficiency for demo purposes.

1. Clone the git repository on your local machine
1. Go into the thermostat-simulator directory
1. Run the simulator with Maven: mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dsimulator.url=[thermostat service url in openshift]/temperature-records"

mvn spring-boot:run -Dspring-boot.run.arguments=--simulator.url=http://thermostat-service-awswebinar.apps.demo-student0.egrsolutions.co.za/temperature-records

You should see the temperature records being printed to the console. You can verify that the records are being consumed by going to the pod logs in the Openshift console, for both the thermostat service and temperature processor service.

You can also look at the Temperatures dashboard in the Grafana UI, which should show ~100% efficiency for pond-1.

Now we can run a second siumulator at 100% as well. In a separate terminal:

- Run the simulator with Maven: mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dsimulator.id=pond-2 -Dsimulator.url=[thermostat service url in openshift]/temperature-records"
- Go to Grafana and select pond-2 in the temperatures dashboard at the top
- Notice the efficiency is at ~100%

Now let's simulate a failure on pond-2. To do that we simply change the temperature increase rate to 1 degree every 1.5 seconds by setting the simulator.rate property to 1500 in our simulator.

- Stop the simulator on the second terminal
- Run the simulator with Maven: mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dsimulator.id=pond-2 -Dsimulator.rate=1500 -Dsimulator.url=[thermostat service url in openshift]/temperature-records"

Now the water is heating more slowing than before, which reduces the efficiency to ~66% (1/1.5). This will be reflected almost instantly on the dashboard.

Also you should see an new alert on the Prometheus Alerts page (keep refreshing the page as Prometheus doesn't show real time alerts) The initial status of the alert will say PENDING (yellow) then change to FIRING (red) after a minute. This is because we setup the alert to only fire after a minute of the condition to avoid false alarms. When the alert is firing, notifications will be sent to the chosen destination at the configured interval.

Finally, let's restore the efficiency by stopping the simulator and re-starting it with the normal rate:

- Stop the simulator on the second terminal
- Run the simulator with Maven: mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dsimulator.id=pond-2 -Dsimulator.url=[thermostat service url in openshift]/temperature-records"

The alert status should go back to normal on Prometheus after a little while.
