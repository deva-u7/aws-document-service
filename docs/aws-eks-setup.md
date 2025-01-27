# Setting Up AWS EKS and Deploying the Application

To set up **Amazon EKS** (Elastic Kubernetes Service) and deploy your application, follow these steps. This guide assumes you have **AWS CLI**, **eksctl**, **kubectl**, and **Helm** installed.

### Prerequisites

Before you begin, ensure that you have the following tools installed:

- **AWS CLI**: Command line tool for interacting with AWS services. [Install AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html).
- **eksctl**: Command line tool for managing EKS clusters. [Install eksctl](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html).
- **kubectl**: Command line tool for interacting with Kubernetes clusters. [Install kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/).
- **Helm**: A package manager for Kubernetes. [Install Helm](https://helm.sh/docs/intro/install/).

### Step 1: Configure AWS CLI

1. Run the following command to configure AWS CLI:

   ```bash
   aws configure
   ```

2. This will prompt you for the following information:
   - **AWS Access Key ID**
   - **AWS Secret Access Key**
   - **Default region name**
   - **Default output format** 

### Step 2: Create an EKS Cluster

1. Create an EKS cluster using **eksctl**:

   ```bash
   eksctl create cluster --name demo-cluster --region ap-south-1 --fargate
   ```

2. This will create a new EKS cluster in the region.

### Step 3: Configure kubectl

1. After the cluster is created, configure **kubectl** to use the context for the new cluster:

   ```bash
   kubectl config use-context iam-root-account@demo-cluster.ap-south-1.eksctl.io
   ```

### Step 4: Configure IAM OIDC Provider

1. Associate the IAM OIDC provider for your EKS cluster:

   ```bash
   eksctl utils associate-iam-oidc-provider --region ap-south-1 --cluster demo-cluster --approve
   ```

### Step 5: Create IAM Policy for AWS Load Balancer Controller

1. Download the IAM policy JSON file for the AWS Load Balancer Controller:

   ```bash
   curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.5.4/docs/install/iam_policy.json
   ```

2. Create the IAM policy:

   ```bash
   aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy --policy-document file://iam_policy.json
   ```

3. Create the IAM service account for the AWS Load Balancer Controller:

   ```bash
   eksctl create iamserviceaccount --cluster=demo-cluster --namespace=kube-system --name=aws-load-balancer-controller --role-name AmazonEKSLoadBalancerControllerRole --attach-policy-arn=arn:aws:iam::[AWS_ACCOUNT_ID]:policy/AWSLoadBalancerControllerIAMPolicy --approve
   ```

### Step 6: Install AWS Load Balancer Controller with Helm

1. Add the Helm repository for AWS EKS charts:

   ```bash
   helm repo add eks https://aws.github.io/eks-charts
   helm repo update eks
   ```

2. Install the AWS Load Balancer Controller in your EKS cluster:

   ```bash
   helm install aws-load-balancer-controller eks/aws-load-balancer-controller -n kube-system --set clusterName=demo-cluster --set serviceAccount.create=false --set serviceAccount.name=aws-load-balancer-controller --set region=ap-south-1 --set vpcId=[VPC_ID]
   ```

### Step 7: Create ECR Repository

1. Create an Amazon Elastic Container Registry (ECR) repository for your Docker images:

   ```bash
   aws ecr create-repository --repository-name my-app-repo --region ap-south-1
   ```
---

## Deployment Steps

After completing the above setup steps, you can deploy your application to the EKS cluster.

### Step 1: Deploy the Application

1. Apply the Kubernetes deployment, service, and ingress resources:

   ```bash
   kubectl apply -f deployment.yaml
   kubectl apply -f service.yaml
   kubectl apply -f ingress.yaml
   ```

### Step 2: Verify the Deployment

1. You can verify that the pods are running using the following command:

   ```bash
   kubectl get pods
   ```

2. To verify that the service is exposed correctly, check the ingress:

   ```bash
   kubectl get ingress
   ```

---

### Notes

- Replace `[AWS_ACCOUNT_ID]` and `[VPC_ID]` with your actual AWS account ID and VPC ID.
- Ensure your **kubectl** context is properly set to interact with your cluster.
- The **AWS CLI** and **eksctl** tools will need appropriate IAM permissions to interact with EKS and other AWS services.

---