# TODO
Changes and improvements still to be implemented.

### Image Pull Policy
The image pull policy is currently set to `Always` for all containers. This is suitable for development and testing but in future we should look at `IfNotPresent` to reduce unnecessary image pulls and improve startup times. 

We may need to look into how to periodically refresh images if we switch to `IfNotPresent`.

### Configurable Base URL
The BaseUrl for workshop environments is currently calculated in _helpers.tpl for each chart. And fed into various containers in different places. Ideally we should set this once for all charts using template includes or subcharts. 

Also need to standardise variable names for WORKSHOP_TLD and WORKSHOP_DOMAIN / NAMESPACE. 

### Contrast API Credential Handling
Currently, API creds for contrast are passed as values via the Makefile. Ideally, we should look at using Kubernetes secrets to manage a set of Agent keys and API keys for each namespace, allowing easier configuration across multiple organisations.
