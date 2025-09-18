.SILENT:

ifneq (,$(wildcard ./.env))
    include .env
    export
endif

# Default application namespace if not specified
NAMESPACE ?= default
ifeq ($(NAMESPACE),default)
    # If the namespace is default, set the domain to localhost
    NAMESPACE_DOMAIN=localhost
    NAMESPACE_DOMAIN_ESCAPED=localhost
else
    # If the namespace is not default, set the domain to the namespace
    NAMESPACE_DOMAIN=$(NAMESPACE).localhost
	# Also set the CONTRAST__UNIQ__NAME to the name of the namespace
	CONTRAST__UNIQ__NAME=$(NAMESPACE)
	NAMESPACE_DOMAIN_ESCAPED=$(NAMESPACE)\\.localhost
endif

download-helm-dependencies:
	@echo "Downloading Helm chart dependencies..."
	@cd contrast-cargo-cats && helm dependency update
	@echo "Helm chart dependencies downloaded successfully."

deploy-contrast:
	@echo "\nDeploying Contrast Agent Operator..."
# 	kubectl apply -f https://github.com/Contrast-Security-OSS/agent-operator/releases/latest/download/install-prod.yaml
	helm upgrade --install --namespace contrast-agent-operator --create-namespace -f contrast-agent-operator.yaml contrast-agent-operator contrast/contrast-agent-operator 
	@echo "\nSetting Contrast Agent Operator Token..."
	kubectl -n contrast-agent-operator delete secret default-agent-connection-secret --ignore-not-found
	kubectl -n contrast-agent-operator create secret generic default-agent-connection-secret --from-literal=token=$(CONTRAST__AGENT__TOKEN)
	echo ""

setup-opensearch:
	@echo "\nSetting up OpenSearch at http://opensearch.$(NAMESPACE_DOMAIN)..."
	@until curl --insecure -s -o /dev/null -w "%{http_code}" http://opensearch.$(NAMESPACE_DOMAIN) | grep -q "302"; do \
        echo "Waiting for OpenSearch..."; \
        sleep 5; \
    done

	curl --insecure  -X POST -H "Content-Type: multipart/form-data" -H "osd-xsrf: osd-fetch" "http://opensearch.$(NAMESPACE_DOMAIN)/api/saved_objects/_import?overwrite=true" -u admin:Contrast@123! --form file='@contrast-cargo-cats/opesearch_savedobjects.ndjson'
	curl --insecure  -X POST -H 'Content-Type: application/json' -H 'osd-xsrf: osd-fetch' "http://opensearch.$(NAMESPACE_DOMAIN)/api/opensearch-dashboards/settings" -u admin:Contrast@123! --data-raw '{"changes":{"defaultRoute":"/app/dashboards#/"}}'
	sleep 5;
	echo "OpenSearch setup complete."

validate-env-vars:
	@echo "Validating environment variables..."
	@if [ -z "$(CONTRAST__AGENT__TOKEN)" ]; then \
		echo "Error: CONTRAST__AGENT__TOKEN is not set in .env file"; \
		exit 1; \
	fi
	@if [ -z "$(CONTRAST__UNIQ__NAME)" ]; then \
		echo "Error: CONTRAST__UNIQ__NAME is not set in .env file"; \
		exit 1; \
	fi
	@if [ -z "$(CONTRAST__API__KEY)" ]; then \
		echo "Warning: CONTRAST__API__KEY is not set in .env file (optional for ADR data fetching and delete functionality)"; \
	fi
	@if [ -z "$(CONTRAST__API__AUTHORIZATION)" ]; then \
		echo "Warning: CONTRAST__API__AUTHORIZATION is not set in .env file (optional for ADR data fetching and delete functionality)"; \
	fi
	@echo "Required environment variables are set."

build-dataservice:
	@echo "Building dataservice..."
	cd services/dataservice && \
	docker build -t dataservice:latest  .

build-webhookservice:
	@echo "Building webhookservice..."
	cd services/webhookservice && \
	docker build -t webhookservice:latest  .

build-frontgateservice:
	@echo "Building frontgateservice..."
	cd services/frontgateservice && \
	docker build -t frontgateservice:latest  .

build-console-ui:
	@echo "Building console-ui..."
	cd services/console-ui && \
	docker build -t console-ui:latest  .

build-exploit-server:
	@echo "Building exploit-server..."
	cd services/exploit-server && \
	docker build -t exploit-server:latest  .

build-imageservice:
	@echo "Building imageservice..."
	cd services/imageservice && \
	docker build -t imageservice:latest  .

build-labelservice:
	@echo "Building labelservice..."
	cd services/labelservice && \
	docker build -t labelservice:latest  .

build-docservice:
	@echo "Building docservice..."
	cd services/docservice && \
	docker build -t docservice:latest  .

build-contrastdatacollector:
	@echo "Building contrastdatacollector..."
	cd services/contrastdatacollector && \
	docker build -t contrastdatacollector:latest  .

build-containers: build-dataservice build-webhookservice build-frontgateservice build-console-ui build-exploit-server build-imageservice build-labelservice build-docservice build-contrastdatacollector
	@echo "\nBuilding containers complete."

# TODO: Use the CONTRAST_UNIQ_NAME for both the application name and the namespace 
run-helm: 
	echo ""
	@echo "Deploying cargo-cats to namespace: $(NAMESPACE)..."
	helm upgrade --install contrast-cargo-cats ./contrast-cargo-cats --cleanup-on-fail \
		--namespace $(NAMESPACE) --create-namespace \
		--set contrast.uniqName=$(CONTRAST__UNIQ__NAME)

deploy-simulation-console: build-console-ui build-contrastdatacollector
	@echo "Waiting for ingress controller to be ready..."
	@until kubectl get deployment contrast-cargo-cats-ingress-nginx-controller -n $(NAMESPACE) -o jsonpath='{.status.readyReplicas}' 2>/dev/null | grep -q "1"; do \
		echo "Waiting for ingress controller..."; \
		sleep 5; \
	done
	@echo "Getting ingress controller IP..."
	$(eval INGRESS_IP := $(shell kubectl get service contrast-cargo-cats-ingress-nginx-controller -n $(NAMESPACE) -o jsonpath='{.spec.clusterIP}' 2>/dev/null))
	@echo "Ingress controller IP: $(INGRESS_IP)"
	@echo "Deploying simulation console..."
	helm upgrade --install simulation-console ./simulation-console --cleanup-on-fail \
		--namespace $(NAMESPACE) \
		--create-namespace \
		--set-string aliashost.cargocats\\.$(NAMESPACE_DOMAIN_ESCAPED)=$(INGRESS_IP) \
		--set contrastdatacollector.contrastUniqName=$(CONTRAST__UNIQ__NAME) \
		--set contrastdatacollector.contrastApiToken=$(CONTRAST__AGENT__TOKEN) \
		--set contrastdatacollector.contrastApiKey=$(CONTRAST__API__KEY) \
		--set contrastdatacollector.contrastApiAuthorization=$(CONTRAST__API__AUTHORIZATION) \
		--set consoleui.contrastApiToken=$(CONTRAST__AGENT__TOKEN) \
		--set consoleui.contrastUniqName=$(CONTRAST__UNIQ__NAME) \
		--set consoleui.contrastApiKey=$(CONTRAST__API__KEY) \
		--set consoleui.contrastApiAuthorization=$(CONTRAST__API__AUTHORIZATION)
		--set consoleui.workshopNamespace=$(NAMESPACE) \
	echo ""
	
print-deployment:
	$(eval contrast_url := $(shell echo "$(CONTRAST__AGENT__TOKEN)" | base64 --decode | grep -o '"url"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"\(.*\)"/\1/' | sed 's/-agents//g'))
	echo "\n\nDeployment complete!"
	echo "=================================================================="
	echo "Note: It may take a few minutes for the deployment to be fully ready."
	echo "==================================================================\n"
	echo ""
	echo "Simulation Console: http://console.$(NAMESPACE_DOMAIN)"
	echo ""
	echo "Vuln App: http://cargocats.$(NAMESPACE_DOMAIN)"
	echo "  Username: admin"
	echo "  Password: password123"
	echo ""
	echo "OpenSearch Dashboard: http://opensearch.$(NAMESPACE_DOMAIN)"
	echo "  Username: admin"
	echo "  Password: Contrast@123!"
	echo ""
	echo "Contrast UI: $(contrast_url)"
	echo "==================================================================\n"
	echo ""

deploy: validate-env-vars deploy-contrast download-helm-dependencies build-containers run-helm setup-opensearch deploy-simulation-console print-deployment

prepare-environment: validate-env-vars deploy-contrast 
	@echo "\nOperator deployment complete! You can now deploy the application using 'make deploy-simulation-console'."

prepare-infrastructure: validate-env-vars
	@echo "\nPreparing infrastructure..."
	helm upgrade --install infrastructure ./infrastructure --cleanup-on-fail
	@echo "\nSetting Contrast Agent Operator Token..."
	kubectl -n contrast-agent-operator delete secret default-agent-connection-secret --ignore-not-found
	kubectl -n contrast-agent-operator create secret generic default-agent-connection-secret --from-literal=token=$(CONTRAST__AGENT__TOKEN)
	echo ""
	@echo "\nInfrastructure deployment complete! You can now deploy the application using 'make demo-up'."

update-builds: download-helm-dependencies build-containers
	@echo "\nUpdated docker builds and helm dependencies to latest. You can now deploy the application using 'make demo-up'."

create-namespace:
	@echo "Creating namespace for new workshop user: $(NAMESPACE)..."
	@if [ "$(NAMESPACE)" != "default" ]; then \
		kubectl create namespace $(NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -; \
		echo "Namespace $(NAMESPACE) created or already exists."; \
	else \
		echo "Using default namespace. No need to create."; \
	fi
	@echo ""

	@echo "Labelling the namespace for AgentInjectors..."
	kubectl label namespace/$(NAMESPACE) agents.contrastsecurity.com/agent-injectors=true;

demo-up: run-helm setup-opensearch deploy-simulation-console print-deployment
# 	@echo "\nDemo deployment complete! You can now access the application."

demo-down: 
	@echo "Deleting the Contrast CargoCats deployment..."
	@if [ "$(NAMESPACE)" != "default" ]; then \
		helm uninstall --namespace $(NAMESPACE) contrast-cargo-cats; helm uninstall --namespace $(NAMESPACE) simulation-console; \
	else \
		echo "No custom namespace to delete. Deleting resources separately..."; \
		helm uninstall contrast-cargo-cats; helm uninstall simulation-console; \
	fi
	@echo "\nDemo deployment tear down complete!"

uninstall: demo-down
	@echo "Deleting the Contrast Agent Operator..."
	kubectl delete namespace contrast-agent-operator;

redeploy: uninstall deploy
	@echo "Redeployment complete!"
