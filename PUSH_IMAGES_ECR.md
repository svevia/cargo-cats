# Push new service images: 

With an SE ADMIN user configured for AWS SSO, run the following commands: 

```bash 
aws sso login

aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 771960604435.dkr.ecr.eu-west-1.amazonaws.com

# Example: 
cd services/console-ui
docker buildx build --push \
    --platform linux/amd64,linux/arm64 \
    --tag 771960604435.dkr.ecr.eu-west-1.amazonaws.com/workshop-images/console-ui:latest .
```

### Note
ECR does not allow the repository creation for new images just by pushing the image. You must create the repository first in the AWS Console or using the AWS CLI.

Please follow the naming convention for the image repositories: `workshop-images/<service-name>`. For example, for the `console-ui` service, the repository name should be `workshop-images/console-ui`.
