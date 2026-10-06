# ERPRR_backend
Backend for ERP system

## API Documentation (Swagger / OpenAPI)

This project uses **Springdoc OpenAPI** to automatically generate interactive API documentation.

### Accessing Swagger UI

Once the application is running, open your browser and navigate to:

```
http://localhost:8080/swagger-ui/index.html
```

Or, if you have configured a custom path:

```
http://localhost:8080/swagger
```

### OpenAPI Specification

The OpenAPI JSON specification is available at:

```
http://localhost:8080/v3/api-docs
```

or (if configured):

```
http://localhost:8080/api-docs
```
### Development Server 

The latest development version of the backend is deployed to AWS EC2:

#### Backend
URL of the EC2 instance

```
http://ec2-3-7-70-139.ap-south-1.compute.amazonaws.com:8080/swagger-ui/index.html
```

#### Frontend
URL of the S3 instance
```
http://amzn-s3-bucket-frontend-erp-rr.s3-website.ap-south-1.amazonaws.com/
```

URL of the CloudFront distribution
```
https://dbddhdoeu3hqu.cloudfront.net/
```

### Features

- Interactive API documentation
- Execute API requests directly from the browser
- View request and response models
- Automatic documentation of REST endpoints
- OpenAPI 3.0 compliant specification

### Dependency

This project uses the following dependency:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

### Running the Application

1. Start the Spring Boot application.

2. Verify the application is running:

```
http://localhost:8080
```

3. Open the Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

### Notes

- Swagger documentation is generated automatically from the REST controllers.
- API models are generated from request and response DTOs.
- Any changes to controllers are reflected automatically after restarting the application.