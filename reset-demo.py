import requests
import json
import os
import subprocess

from dotenv import load_dotenv

load_dotenv()

# Configuration
BASE_URL = "https://eval.contrastsecurity.com"
ORG_ID = os.getenv('ORG_ID')
API_KEY = os.getenv('API_KEY')
AUTH_TOKEN = os.getenv('AUTH_TOKEN')  # typically "Authorization: Bearer <token>"

APP_PREFIX = os.getenv('CONTRAST__UNIQ__NAME')

HEADERS = {
    "API-Key": API_KEY,
    "Authorization": AUTH_TOKEN,
    "Accept": "application/json",
    "Content-Type": "application/json"
}

# Step 1: Get list of applications
POST_URL = f"{BASE_URL}/Contrast/api/ng/{ORG_ID}/applications/filter?offset=0&limit=25&expand=license%2Ccompliance_policy%2Cskip_links%2Ctechnologies%2Ctrace_severity_breakdown%2Cmetadata%2Cscores&sort=appName"
POST_BODY = {
    "quickFilter": "ALL",
    "filterTechs": [],
    "filterLanguages": [],
    "filterTags": [],
    "scoreLetterGrades": [],
    "filterServers": [],
    "filterCompliance": [],
    "filterVulnSeverities": [],
    "environment": [],
    "appImportances": [],
    "filterText": APP_PREFIX,
    "metadataFilters": []
}
response = requests.post(POST_URL, headers=HEADERS, data=json.dumps(POST_BODY))

if response.status_code != 200:
    print(f"Failed to fetch applications: {response.status_code} - {response.text}")
    exit(1)

apps = response.json().get("applications", [])

# Step 2: For each app_id, call DELETE endpoint
for app in apps:
    app_id = app.get("app_id")
    if not app_id:
        continue

    delete_url = f"{BASE_URL}/api/ns-ui/v1/organizations/{ORG_ID}/issues?applicationId={app_id}"
    del_response = requests.delete(delete_url, headers=HEADERS)

    if del_response.status_code == 202:
        print(f"Issues successfully deleted for app {app_id}")
    else:
        print(f"Failed to delete issues for app {app_id}: {del_response.status_code} - {del_response.text}")



# Step 3 : make redeploy the application 
print("Redeploying the application...")
subprocess.run("make redeploy", shell=True)
