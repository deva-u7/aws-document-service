# Document Service Application
## Overview
- This service demonstrates how to build and deploy a **Spring Boot application** using **AWS EKS** and **Kubernetes**, while leveraging **LocalStack** for E2E testing of AWS integrations. The application is containerized using Docker, pushed to **Amazon ECR**, and deployed to the **EKS cluster**.

- For comprehensive end-to-end testcases, **LocalStack** is used to simulate AWS services like S3. This approach allows for validating the application behavior without the need for actual AWS resources, ensuring the application can interact seamlessly with simulated AWS environments.

- The project also showcases an automated deployment pipeline using **GitHub Actions** for **CI/CD** and **AWS services** like **ECR** and **EKS**.
## Features
- **File Upload**: Upload files to AWS S3.
- **Metadata Storage**: Save document metadata in an H2 in-memory database.
- **E2E Testing**: Perform E2E tests using LocalStack to simulate S3 interactions.
- **Deploying to AWS EKS**:

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21**
- **Maven**
- **Docker** 

## Configuration

Configure the application using `application.yml` files for different profiles:

1. **S3 Upload Configuration**: In `application.yml`, set the following attributes:
    - `access-key`
    - `secret-key`
    - `bucket-name`
    - `region`

2. **Test Profile**: The Test Profile will automatically generate the above attributes.

## Technologies Used

- Spring Boot
- AWS S3 SDK
- H2 Database
- LocalStack (for S3 integration)
- JUnit
- Maven

---

## Deployment on AWS EKS

### Prerequisites

To follow along with this project, you should have:

- An **AWS Account** with permissions to create ECR repositories and EKS clusters.
- **AWS CLI** installed and configured on your local machine or CI/CD runner.
- **Docker** installed for building the application container.
- **Kubernetes (kubectl)** installed for managing your EKS cluster.

## AWS EKS Setup and Deployment

For detailed setup and deployment instructions, refer to the [AWS EKS Setup Guide](./docs/aws-eks-setup.md).

---

## Architecture

The application is deployed using three primary Kubernetes resources:

1. **Deployment**: Manages the deployment of the Dockerized Spring Boot application, including container image and replica configurations.
2. **Service**: Exposes the application internally within the Kubernetes cluster.
3. **Ingress**: Exposes the application externally through an **Application Load Balancer (ALB)**.

These resources are defined in Kubernetes YAML files for seamless deployment to AWS EKS.

---

## CI/CD Pipeline

### Workflow Steps

1. **Push to `main` Branch**: Triggered whenever code is pushed to the `main` branch.
2. **Build Process**:
    - Code is checked out from GitHub.
    - **JDK 17** is set up.
3. **Docker Build**:
    - The project is packaged into a Docker container.
    - The image is tagged with `latest` and pushed to an **Amazon ECR repository**.
4. **Kubernetes Deployment**:
    - AWS CLI is used to authenticate and configure the environment for deploying to EKS.
    - **kubectl** is configured to interact with the AWS EKS cluster.
    - Kubernetes YAML manifests are applied to deploy the application to EKS.

---

## GitHub Actions Workflow

### Workflows

1. **Build Application Workflow**:
    - Automatically triggered on a push to the `main` branch.
    - Handles code checkout, JDK setup, and Maven build process.

2. **Deploy to Dev Workflow**:
    - Manually triggered via the GitHub Actions UI.
    - Builds the Docker image, pushes it to ECR, and deploys Kubernetes resources to the EKS cluster.

---

## Secrets Configuration

For the CI/CD pipeline to function correctly, set up the following **GitHub Secrets** in your repository:

- **AWS_ACCESS_KEY_ID**: Your AWS access key.
- **AWS_SECRET_ACCESS_KEY**: Your AWS secret key.
- **ECR_REPO**: The URL of your ECR repository.

These secrets are used in the GitHub Actions workflow for authenticating with AWS and interacting with the ECR repository.

---

## How to Deploy

1. **Fork and Clone**:
    - Fork this repository and clone it to your local machine.

2. **Set Up AWS Resources**:
    - Create an **Amazon ECR repository**.
    - Set up an **EKS cluster** via the AWS Console or AWS CLI.

3. **Configure GitHub Secrets**:
    - Add your `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `ECR_REPO` as GitHub secrets.

4. **Push Changes to `main` Branch**:
    - Pushing code to the `main` branch will automatically trigger the **Build Application** workflow.

5. **Manual Trigger for Deployment**:
    - After the application is built, manually trigger the **Deploy to Dev** workflow from the GitHub Actions interface to deploy the application.

---