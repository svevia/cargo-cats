# Workshop Project


## Makefile changes
1. Remove the Agent Operator installation from the core Makefile to a seperate step **Partially - use `demo-up / demo-down` once deployed**
2. Migrate Agent Operator installation to Helm! **Done**
3. Move the container build step to a seperate setup step **Partially - use `demo-up / demo-down` once deployed** 
4. Standalone step for deploying the demo app **Done**
1. Deploy app for a specific namespace **Done**
2. 

## Issues
* **FIXED**Agent operator was not using the helm installation, instead installing via manifest 
  * **FIXED** Leads to a bunch of unmanaged resources being created which tripped me up for ages
* **FIXED** When the Agent Operator is installed, it finds all current namespaces and deploys the agentInjectors. If you create a new namespace, then you need to upgrade the Agent Operator using helm for it to add AgentInjectors to new namespaces
* Ingress controller is being applied within each namespace 
  * Best practice is to have ingress controller cluster-wide and just use Ingress resources in each namespace
  * Is this down to the way modSecurity works?  


## Implement a way to reset datafeed to OpenSearch
curl -X POST localhost:5000/clear-tracking



# `simulation-console -> zapproxy.yaml`
### Implemented dynamic domain and namespace in ZAP config
```
http://cargocats.{{ template "simulation-console.baseUrl" . }}
```


### Authentication problem in ZAP
**FIXED UPSTREAM**
Hundereds of errors in the logs like this:
```
713657 [ZAP-ActiveScanner-1] ERROR org.zaproxy.zap.users.User - An error occurred while authenticating:
org.openqa.selenium.SessionNotCreatedException: Could not start a new session. Response code 500. Message: Process unexpectedly closed with status 1 
Host info: host: 'simulation-console-zapproxy-5798cf5bd6-rdndf', ip: '10.1.0.213'
Build info: version: '4.35.0', revision: '1c58e5028b'
System info: os.name: 'Linux', os.arch: 'aarch64', os.version: '6.10.14-linuxkit', java.version: '17.0.16'
Driver info: org.openqa.selenium.firefox.FirefoxDriver
Command: [null, newSession {capabilities=[Capabilities {acceptInsecureCerts: true, browserName: firefox, moz:firefoxOptions: {prefs: {browser.tabs.documentchannel: false, dom.serviceWorkers.enabled: true, network.captive-portal-service.enabled: false, network.proxy.allow_hijacking_localhost: true, network.proxy.http: localhost, network.proxy.http_port: 36447, network.proxy.no_proxies_on: , network.proxy.share_proxy_settings: true, network.proxy.ssl: localhost, network.proxy.ssl_port: 36447, network.proxy.type: 1, remote.active-protocols: 1}, profile: UEsDBBQACAgIAHS/JFsAAAAAAAA...}, webSocketUrl: true}]}]
        at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:114) ~[?:?]
        at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:75) ~[?:?]
        at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:61) ~[?:?]
        at org.openqa.selenium.remote.HttpCommandExecutor.execute(HttpCommandExecutor.java:187) ~[?:?]
        at org.openqa.selenium.remote.service.DriverCommandExecutor.invokeExecute(DriverCommandExecutor.java:216) ~[?:?]
        at org.openqa.selenium.remote.service.DriverCommandExecutor.execute(DriverCommandExecutor.java:174) ~[?:?]
        at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:557) ~[?:?]
        at org.openqa.selenium.remote.RemoteWebDriver.startSession(RemoteWebDriver.java:246) ~[?:?]
        at org.openqa.selenium.remote.RemoteWebDriver.<init>(RemoteWebDriver.java:174) ~[?:?]
        at org.openqa.selenium.firefox.FirefoxDriver.<init>(FirefoxDriver.java:137) ~[?:?]
        at org.openqa.selenium.firefox.FirefoxDriver.<init>(FirefoxDriver.java:132) ~[?:?]
        at org.openqa.selenium.firefox.FirefoxDriver.<init>(FirefoxDriver.java:114) ~[?:?]
        at org.openqa.selenium.firefox.FirefoxDriver.<init>(FirefoxDriver.java:109) ~[?:?]
        at org.openqa.selenium.firefox.FirefoxDriver.<init>(FirefoxDriver.java:94) ~[?:?]
        at org.zaproxy.zap.extension.selenium.ExtensionSelenium.getWebDriverImpl(ExtensionSelenium.java:1175) ~[?:?]
        at org.zaproxy.zap.extension.selenium.ExtensionSelenium.getWebDriver(ExtensionSelenium.java:968) ~[?:?]
        at org.zaproxy.zap.extension.selenium.ExtensionSelenium.getWebDriver(ExtensionSelenium.java:936) ~[?:?]
        at org.zaproxy.zap.extension.selenium.internal.BuiltInSingleWebDriverProvider.getWebDriver(BuiltInSingleWebDriverProvider.java:63) ~[?:?]
        at org.zaproxy.zap.extension.selenium.ExtensionSelenium.getWebDriverImpl(ExtensionSelenium.java:816) ~[?:?]
        at org.zaproxy.zap.extension.selenium.ExtensionSelenium.getWebDriver(ExtensionSelenium.java:616) ~[?:?]
        at org.zaproxy.zap.extension.selenium.ExtensionSelenium.getWebDriver(ExtensionSelenium.java:589) ~[?:?]
        at org.zaproxy.addon.authhelper.BrowserBasedAuthenticationMethodType$BrowserBasedAuthenticationMethod.authenticateImpl(BrowserBasedAuthenticationMethodType.java:335) ~[?:?]
        at org.zaproxy.addon.authhelper.BrowserBasedAuthenticationMethodType$BrowserBasedAuthenticationMethod.authenticate(BrowserBasedAuthenticationMethodType.java:309) ~[?:?]
        at org.zaproxy.zap.users.User.authenticate(User.java:271) ~[zap-2.16.1.jar:2.16.1]
        at org.zaproxy.zap.users.User.processMessageToMatchUser(User.java:170) ~[zap-2.16.1.jar:2.16.1]
        at org.zaproxy.addon.network.internal.client.BaseHttpSender.sendAuthenticated(BaseHttpSender.java:378) ~[?:?]
        at org.zaproxy.addon.network.internal.client.BaseHttpSender.sendNoRedirections(BaseHttpSender.java:351) ~[?:?]
        at org.zaproxy.addon.network.internal.client.BaseHttpSender.send(BaseHttpSender.java:307) ~[?:?]
        at org.zaproxy.addon.network.internal.client.BaseHttpSender.sendAndReceive(BaseHttpSender.java:278) ~[?:?]
        at org.zaproxy.addon.network.internal.client.BaseHttpSender.sendAndReceive(BaseHttpSender.java:234) ~[?:?]
        at org.parosproxy.paros.network.HttpSender.sendImpl(HttpSender.java:536) ~[zap-2.16.1.jar:2.16.1]
        at org.parosproxy.paros.network.HttpSender.sendAndReceive(HttpSender.java:368) ~[zap-2.16.1.jar:2.16.1]
        at org.parosproxy.paros.core.scanner.AbstractPlugin.sendAndReceive(AbstractPlugin.java:328) ~[zap-2.16.1.jar:2.16.1]
        at org.parosproxy.paros.core.scanner.AbstractPlugin.sendAndReceive(AbstractPlugin.java:265) ~[zap-2.16.1.jar:2.16.1]
        at org.zaproxy.zap.extension.ascanrules.SqlInjectionMsSqlTimingScanRule.lambda$scan$0(SqlInjectionMsSqlTimingScanRule.java:253) ~[?:?]
        at org.zaproxy.addon.commonlib.timing.TimingUtils.sendRequestAndTestConfidence(TimingUtils.java:125) [commonlib-release-1.35.0.zap:?]
        at org.zaproxy.addon.commonlib.timing.TimingUtils.checkTimingDependence(TimingUtils.java:91) [commonlib-release-1.35.0.zap:?]
        at org.zaproxy.zap.extension.ascanrules.SqlInjectionMsSqlTimingScanRule.scan(SqlInjectionMsSqlTimingScanRule.java:259) [ascanrules-release-73.zap:?]
        at org.parosproxy.paros.core.scanner.AbstractAppParamPlugin.scan(AbstractAppParamPlugin.java:216) [zap-2.16.1.jar:2.16.1]
        at org.parosproxy.paros.core.scanner.AbstractAppParamPlugin.scan(AbstractAppParamPlugin.java:132) [zap-2.16.1.jar:2.16.1]
        at org.parosproxy.paros.core.scanner.AbstractAppParamPlugin.scan(AbstractAppParamPlugin.java:92) [zap-2.16.1.jar:2.16.1]
        at org.parosproxy.paros.core.scanner.AbstractPlugin.run(AbstractPlugin.java:391) [zap-2.16.1.jar:2.16.1]
        at java.base/java.lang.Thread.run(Thread.java:840) [?:?]
713658 [ZAP-ActiveScanner-1] INFO  org.zaproxy.zap.users.User - Authentication failed for user: admin
713680 [ZAP-ActiveScanner-1] INFO  org.zaproxy.zap.users.User - Authenticating user: admin
```
The problem was that the authentication user password line in the ZAP config was wrong. Password was set to 'admin' instead of 'password123'.
