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

# Scripted Install

## Preparation

1. Install the Helm CLI by following the instructions in the doc: https://helm.sh/docs/intro/install/
1. Add the demo Helm repo
	- helm repo add helm-pure-mc https://raw.githubusercontent.com/2vcps/helm-pure-mc/master/
	- helm repo update
1. Install the Openshift CLI if you haven't already. https://docs.openshift.com/container-platform/4.2/cli_reference/openshift_cli/getting-started-cli.html

## Installation

1. Login to your Openshift cluster from a terminal (oc login) and switch to your project (oc project my-project, replace my-project with the project you create in the previous steps)
1. Install the demo
	- helm install helm-pure-mc/minecraft

# Manual Installation

## AMQ Streams/Kafka

1. In the installed operators, choose AMQ Streams
1. In the Overview screen, click the Kafka/+Create Instance link
1. Leave everything default and create the cluster
1. You should see a new cluster created called "my-cluster"
1. In the my-cluster/Resources screen, you should see 3 pods for your kafka nodes, and 3 for the Zookeeper nodes. The cluster is ready when they all say "Running"

## Thermostat Service

1. Switch to the Developer Perspective
1. Click +Add menu
1. Use the following options
    - __Git Repo URL__: https://github.com/mrethers/msday2020.git
    - __Git Advanced Options__:
      - __Context Dir__: /thermostat-service
    - __Builder Image__: Java
    - __Builder Image Version__: 11 (Red Hat OpenJDK 11)
    - __Application Name__: temperature-monitoring
    - __Name__: thermostat-service
    - Change the Deployment Config by clicking the link at the bottom, and add an environment variable SPRING_PROFILES_ACTIVE: kubernetes

## Temperature Processor

1. Switch to the Developer Perspective
1. Click +Add menu
1. Use the following options
    - __Git Repo URL__: https://github.com/mrethers/msday2020.git
    - __Git Advanced Options__:
      - __Context Dir__: /temperature-processor
    - __Builder Image__: Java
    - __Builder Image Version__: 11 (Red Hat OpenJDK 11)
    - __Application Name__: temperature-monitoring
    - __Name__: thermostat-processor
    - Change the Deployment Config by clicking the link at the bottom, and add an environment variable SPRING_PROFILES_ACTIVE: kubernetes

In the Openshift console, go back to the Administrator perspective

If everything went well, there should be two new pods running in the Workloads/Pods section:

- thermostat-service-...
- temperature-processor-...

In the Networking section, click Routes and you should see the 2 routes associated with these services. Check that the services are OK by clicking the links and adding __/actuator/health__ at the end.

You should see a page saying:

```"status": "UP"```

## Prometheus

Switch back to the Administrator view in the web console.

First we create a service monitor. This tells Prometheus which services to monitor in our namespace by scanning the services tagged with a specific label:

1. In the web console Administrator perspective, go to Operators/Installed Operators
1. Click on Prometheus Operator
1. In the Overview tab, click the Service Monitor/+Create Instance link
1. Replace the YAML with the content from https://raw.githubusercontent.com/mrethers/msday2020/master/resources/service-monitor.yml
1. Change the namespace to where you deployed the services above
1. Click Create

Now let's add an alerting rule to monitor our efficiency metric:

1. Click on the Prometheus Rule tab
1. Click Create Prometheus Rule
1. Again, replace with the content from https://raw.githubusercontent.com/mrethers/msday2020/master/resources/prometheus-rule.yml
1. Change the namespace to where you deployed the services above
1. Click Create

Alertmanager is a Prometheus service that manages alert notifications. Upon receiving an alter, a message will be sent to a Java bridge that will in turn forward the event to a Kafka topic (default: prometheus-alerts).

Let's first install the prometheus webhook service:

1. Switch to the Developer Perspective
1. Click +Add menu
1. Use the following options
    - __Git Repo URL__: https://github.com/mrethers/msday2020.git
    - __Git Advanced Options__:
      - __Context Dir__: /prometheus-receiver
    - __Builder Image__: Java
    - __Builder Image Version__: 11 (Red Hat OpenJDK 11)
    - __Application Name__: temperature-monitoring
    - __Name__: temperature-receiver
    - Change the Deployment Config by clicking the link at the bottom, and add an environment variable SPRING_PROFILES_ACTIVE: kubernetes
    
Then we need to create a secret that contains the Alertmanager notifications config:

1. Switch back to the Administrator perspective.
1. In the console, navigate to the Workloads > Secrets page
1. Choose Create > From YAML
1. Paste the content of alertmanager-secret.yml

Then deploy the Alertmanager cluster:

1. Go back to the Operators/Installed Operators section in the console
1. Click the Prometheus Operator
1. Click Create AlertManager
1. Paste the content from alertmanager-instance.yml
1. Go to Networking/Route and create a route for the AlertManager like we did for Prometheus. Use port 9093 to expose the UI.

Finally we deploy the actual Prometheus instance:

1. Click on the Prometheus tab
1. Click Create Prometheus
1. Again, replace with the content from https://raw.githubusercontent.com/mrethers/msday2020/master/resources/prometheus.yml
1. Change the namespace to where you deployed the services above
1. Click Create

By default, the Prometheus operator doesn't expose the UI to the outside world, but for demo purposes, we'll add a route so we can see what our setup produces. This is not recommended in production environments.

1. Expose Prometheus by going to the Networking/Routes section of the main menu
1. Click Create Route
1. Enter Name: prometheus-operated
1. Service: prometheus-operated
1. Target Port: 9090 -> web(TCP)
1. On the next screen, click the Location link to see your prometheus dashboard

In order for Prometheus to discover our microservices, we will need to label the corresponding Openshift services with __k8s-app: prometheus__. This is what we configured in our __Service Monitor__ resource.

1. Go to Networking/Services
1. Choose thermostat-service
1. In the YAML tab, add a label in the metadata/labels section -> k8s-app: prometheus
1. Save
1. Repeat for the thermostat-processor service

If everything goes well, you should see the two services UP in the Prometheus Status/Targets page

## Grafana:

1. Go back to the Operators/Installed Operators section in the console
1. Click the Grafana Operator
1. Click Create Grafana
1. Paste the content from grafana.yml
1. Go to Networking/Route and create a route for Grafana like we did for Prometheus

## Grafana Dashboards:

First we need to tell Grafana about our Prometheus cluster. We use a GrafanaDatasource custom resource for that:

1. Go back to the Operators/Installed Operators section in the console
1. Click the Grafana Operator
1. Click Create GrafanaDatasource
1. Paste the content from grafana-datasource.yml

Now we can install our dashboards to visualize the data from Prometheus:

1. Go back to the Operators/Installed Operators section in the console
1. Click the Grafana Operator
1. Click Create GrafanaDashboard
1. Paste the content from grafana-springboot-dashboard.yml

This will create a Spring Boot monitoring dashboard in the Grafana UI. This dashboard is contributed by community users on the Grafana website (ID 10280).

1. Go back to the Operators/Installed Operators section in the console
1. Click the Grafana Operator
1. Click Create GrafanaDashboard
1. Paste the content from grafana-temperature-dashboard.yml

This will install our temperature dashboard to visualize our custom metrics.

Now you can go to the Grafana UI by going to the Networking/Routes section of the Openshift console and find the link to the Grafana cluster service.

You can sign-in as an admin by using the default credentials root/secret, but this is not required to see the dashboards.

## Starting the simulator

The simulator code is available at https://github.com/mrethers/msday2020.git.

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
