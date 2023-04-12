# Secure Repository for Confidential Documents

This application represents a secure repository for storing confidential documents. The application allows storing documents for multiple users, with access to a specific document restricted to its owner. The application allows users to log in to the system in two steps, using a digital certificate and a username/password combination. Once logged in, users can download existing documents and upload new ones. Each new document is divided into N segments (N≥4, randomly generated value) and stored in different directories to increase system security and reduce the possibility of document theft.

## Functionality
- User authentication using digital certificates and username/password combination
- Uploading and downloading of confidential documents
- Dividing new documents into N segments (N≥4, randomly generated value) before storing them in the file system, with each segment stored in a different directory to increase system security
- Protection of confidentiality and integrity of each document segment
- Detection of unauthorized changes to stored documents and notification to the user
- Restriction of user certificates to only allow usage for the purposes required by the application
- Issuing of user certificates for a period of 6 months
- Suspension of user certificates after three incorrect login attempts
- Option for users to reactivate suspended certificates or register new accounts

## Requirements
- Public Key Infrastructure (PKI) established before the application starts working
- CA certificate, CRL list, user certificates, and the private key of the currently logged-in user located at an arbitrary location in the file system
- Data in the certificate associated with the corresponding user data

## Technologies
- Programming Language: Java
- Cryptography Libraries: Used openssl commands
