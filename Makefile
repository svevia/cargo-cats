.SILENT:

ifneq (,$(wildcard ./.env))
    include .env
    export
endif

# Default application namespace if not specified
NAMESPACE ?= default

# Set the TLD for DNS resolution in Kubernetes, or set to localhost for local docker
# TLD=localhost
TLD=workshop.contrastdemo.com

ifeq ($(NAMESPACE),default)
    # If the namespace is default, set the domain to localhost
    NAMESPACE_DOMAIN=$(TLD)
    NAMESPACE_DOMAIN_ESCAPED=$(TLD)
else
    # If the namespace is not default, set the domain to the namespace
    NAMESPACE_DOMAIN=$(NAMESPACE).$(TLD)
	# Also set the CONTRAST__UNIQ__NAME to the name of the namespace
	CONTRAST__UNIQ__NAME=$(NAMESPACE)
	# TODO: Add a way to dynamically handle the Namespace Domain
	# Periods (.) need to be escaped with a double backslash for aliasHost
	NAMESPACE_DOMAIN_ESCAPED=$(NAMESPACE)\\.workshop\\.contrastdemo\\.com
endif

download-helm-dependencies:
	@echo "Downloading Helm chart dependencies..."
	@cd contrast-cargo-cats && helm dependency update
	@echo "Helm chart dependencies downloaded successfully."


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

check-version-tag:
ifndef TAG
	echo "TAG is not set. Please specify a TAG version in the format TAG=v1"
	exit 1
endif

buildx-dataservice: check-version-tag
	@echo "Building dataservice..."
	cd services/dataservice && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/dataservice:$(TAG) .

buildx-webhookservice: check-version-tag
	@echo "Building webhookservice..."
	cd services/webhookservice && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/webhookservice:$(TAG) .

buildx-frontgateservice: check-version-tag
	@echo "Building frontgateservice..."
	cd services/frontgateservice && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/frontgateservice:$(TAG) .

buildx-console-ui: check-version-tag
	@echo "Building console-ui..."
	cd services/console-ui && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/console-ui:$(TAG) .

buildx-exploit-server: check-version-tag
	@echo "Building exploit-server..."
	cd services/exploit-server && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/exploit-server:$(TAG) .

buildx-imageservice: check-version-tag
	@echo "Building imageservice..."
	cd services/imageservice && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/imageservice:$(TAG) .

buildx-labelservice: check-version-tag
	@echo "Building labelservice..."
	cd services/labelservice && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/labelservice:$(TAG) .

buildx-docservice: check-version-tag
	@echo "Building docservice..."
	cd services/docservice && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/docservice:$(TAG) .

buildx-contrastdatacollector: check-version-tag
	@echo "Building contrastdatacollector..."
	cd services/contrastdatacollector && \
	docker buildx build --push \
		--platform linux/amd64,linux/arm64 \
		--tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/contrastdatacollector:$(TAG) .

aws-eks-auth:
	@echo "Authenticating with AWS EKS..."
	aws sso login
	aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 771960604435.dkr.ecr.eu-west-1.amazonaws.com
	@echo "AWS EKS authentication complete."

buildx-containers: aws-eks-auth check-version-tag buildx-dataservice buildx-webhookservice buildx-frontgateservice buildx-console-ui buildx-exploit-server buildx-imageservice buildx-labelservice buildx-docservice buildx-contrastdatacollector
	@echo "\nBuilding x-platform images complete."


# TODO: Use the CONTRAST_UNIQ_NAME for both the application name and the namespace 
run-helm: 
	echo ""
	@echo "Deploying cargo-cats to namespace: $(NAMESPACE)..."
	helm upgrade --install contrast-cargo-cats ./contrast-cargo-cats --cleanup-on-fail \
		--namespace $(NAMESPACE) --create-namespace \
		--set contrast.uniqName=$(CONTRAST__UNIQ__NAME)

deploy-simulation-console: validate-env-vars
	@echo "Waiting for ingress controller to be ready..."
	@until kubectl get deployment --namespace kube-system ingress-nginx-controller -o jsonpath='{.status.readyReplicas}' 2>/dev/null | grep -q "1"; do \
		echo "Waiting for ingress controller..."; \
		sleep 5; \
	done
	@echo "Getting ingress controller IP..."
# 	$(eval INGRESS_IP := $(shell kubectl get service ingress-nginx-controller -n kube-system -o jsonpath='{.spec.clusterIP}' 2>/dev/null))
	@echo "Ingress controller IP: $(INGRESS_IP)"
	@echo "Deploying simulation console..."
	helm upgrade --install simulation-console ./simulation-console --cleanup-on-fail \
		--namespace $(NAMESPACE) \
		--create-namespace \
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


# 		--set-string aliashost.cargocats\\.$(NAMESPACE_DOMAIN_ESCAPED)=$(INGRESS_IP) \
	
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
	echo "OpenSearch Dashboard: http://opensearch.$(TLD)"
	echo "  Username: admin"
	echo "  Password: Contrast@123!"
	echo ""
	echo "Contrast UI: $(contrast_url)"
	echo "==================================================================\n"
	echo ""


update-builds: download-helm-dependencies build-containers
	@echo "\nUpdated docker builds and helm dependencies to latest. You can now deploy the application using 'make demo-up'."

demo-up: run-helm deploy-simulation-console print-deployment 
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

install: update-builds demo-up


uninstall: demo-down
	@echo "Deleting the Contrast Agent Operator..."
	kubectl delete namespace contrast-agent-operator;

redeploy: uninstall deploy
	@echo "Redeployment complete!"
