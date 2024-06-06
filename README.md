# AzteGiveaway Bot  

AzteGiveaway Bot is a Discord bot designed to create and manage giveaways in Discord servers. This bot allows administrators to create giveaways with specified durations, number of winners, and optional announcement channels. Users can enter giveaways by reacting to the giveaway message with a specified emoji.

## Features  

- Create Giveaways: Administrators can create giveaways with a unique title, duration, and number of winners.
- Planning Giveaways: Giveaways can also be planned to start in the future.
- Custom Announcement Channel: Optionally specify an announcement channel for the giveaway.  
- User Participation: Users can enter giveaways by reacting with a specified emoji.  
- Single Entry: Each user can enter a giveaway only once.  
- Automatic Winner Selection: Winners are automatically selected when the giveaway ends.  
- Database Integration: Store giveaway entries, winners, and preferences in a PostgreSQL database.  
- Localization: Supports multiple languages.    

## Prerequisites  

- Java Development Kit (JDK) 8 or later  
- Maven  
- A Discord bot token  
- A MySQL database (use Azure Data Sutdio to view the tables; more on that below)

## Setup  

### 1. Clone the Repository  

```bash  
git clone https://github.com/your-username/AzteGiveaway.git  
cd AzteGiveaway  
```  

### 2. Cnnfigure the bot:  

Create an application.properties file in the src/main/resources directory with the following content:  
```config
bot.token=YOUR_DISCORD_BOT_TOKEN
```
Application.properties is required to configure properties for Spring Framework as well. 

### 3. Build the Project:  

Use Maven to build the project:  
```mvn
mvn clean install
```

### 4. Run the Bot  

Run the bot using the following command:  
```mvn
mvn exec:java -Dexec.mainClass="org.example.Main"
```
or by running main  


### Supported Commands:  

#### Giveaway Commands 

##### Create Giveaway

```plaintext
 /giveaway create <title> <prize> <duration> <number_of_winners> [<channel_id>]
```  

title: The unique title for the giveaway;  
prize: The prize for the giveaway;  
duration: The duration of the giveaway (e.g., 1h for 1 hour, 30m for 30 minutes, 2d for 2 days) -> use m for minutes, h for hours and d for days;  
number_of_winners: The number of winners for the giveaway;  
channel_id (optional): The ID of the channel where the giveaway will be announced. If not provided, the current channel will be used.  


###### Example:  

```plaintext
/giveaway create title: Awesome Giveaway prize: Awesome Prize duration: 1h number_of_winners: 3 
```

Function above will create a giveaway entitled "Awesome Giveaway" that will choose 3 winners in one hour from creation;  
In case the number of entries is smaller or equal to the number of winners, then those users will win.  
If no users entry the giveaway, a specific message will be sent ("The giveaway has ended, but there were no entries")  

##### Plan Giveaway

```plaintext
 /giveaway plan <start_time> <title> <prize> <duration> <number_of_winners> [<channel_id>]
```

start_time: The time that needs to pass until the creation of the giveaway;  
title: The unique title for the giveaway;  
prize: The prize for the giveaway;  
duration: The duration of the giveaway (e.g., 1h for 1 hour, 30m for 30 minutes, 2d for 2 days) -> use m for minutes, h for hours and d for days;  
number_of_winners: The number of winners for the giveaway;  
channel_id (optional): The ID of the channel where the giveaway will be announced. If not provided, the current channel will be used.  

###### Example: 

```plaintext
 /giveaway plan start_time: plan 2d  title: Awesome Giveaway prize: Awesome Prize duration: 1h number_of_winners: 3 
```  

###### !Attention 

Creating a planned giveaway will store that giveaway in the database, so the title used for that giveaway cannot be reused until it's deleted.  


##### Roll Giveaway

This command is used to roll a giveaway instantly, without waiting for it's completion.  

```plaintext
 /giveaway roll <title> 
```

title: The unique title for the giveaway;  

###### Example: 

```plaintext
 /giveaway roll title: Awesome Giveaway
```  

###### !Attention 

This command can only be used once / giveaway. Use command reroll to choose new winners for a certain giveaway.

##### Reroll Giveaway

```plaintext
 /giveaway reroll <title> [<number_of_winners>]  
```

title: The unique title for the giveaway;  
number_of_winners:The number of winners for the giveaway; You can optionally choose to reroll more winners for the giveaway; 

The above command is used to reroll the winners for a certain giveaway. It will rerol as many times as you use the command. 

###### Example: 

```plaintext
 /giveaway reroll title: Awesome Giveaway number_of_winners: 1
```  

###### !Attention 

If the number of entries is equal or smaller than the new number of winners selected, then those users will become the new winners;  
If the number of entries left is 0, then the previously rolled winners will remain the winners.  

##### Delete Giveaway

```plaintext
 /giveaway delete <title>
```

title: The unique title for the giveaway;  

The above command deletes a giveaway from the database. The title is still kept in the database of the winners;  
Command is useful in case a new giveaway with the same title needs to be created;  

###### Example: 

```plaintext
 /giveaway delete title: Awesome Giveaway
```

##### Get Winners

```plaintext
 /giveaway winners [<giveaway_title>] [<giveaway_message_id>]
```

giveaway_title: The unique title for the giveaway;  
giveaway_message_id: The message id of the embed created when the giveaway was posted;  

The above command has two ways of being used: 
  - with no arguments -> it retrieves all ever winners and send a paginated embed with all the winners;
  - with both arguments -> it retrieves the winners for that specific giveaway;

###### Example: 

```plaintext
 /giveaway winners giveaway_title: Awesome Giveaway giveaway_message_id: 1248296087190765642
```

###### !Attention 

Either both or no arguments is accepted for this command. If only one argument is used, then the command won't work;  


#### Preference Command

#### Set Language

```plaintext
 /set language language: Romanian/English 
```

language: The supported language the bot can be set to;  

The above command can be used to set the Bot to send messages either in Romanian or English  
It uses default choices: Romanian and English as these are the only supported languages as of now (06-Jun-2024)  

###### Example: 

```plaintext
 /set language language:Romanian
```

### Logging

Actions and errors are logged using the SLF4J logger.  

### Testing

Unit Testing is done for the services.  

### Cloud deployment

The Bot is deployed to Google Cloud Storage.  
Files used for deployment: 
  -Dockerfile
  -docker-compose.yaml
  -secret.yaml
  -service.yaml
  -deployment.yaml

#### Dockerfile

The Dockerfile defines a multi-stage build process for a Java application using Maven. 

##### Stage 1  

```dockerfile
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package
```
1. FROM maven:3.8.4-openjdk-17 AS build:  
  - This line specifies the base image to use for the build stage. It uses a Maven image with JDK 17.  
  - The AS build part names this stage "build" for later reference.  
    
2. WORKDIR /app:  
- This sets the working directory inside the container to /app.  

3. COPY pom.xml .:
     - This copies the pom.xml file from your local machine to the working directory in the container.
       
4. COPY src ./src:
   - This runs the Maven command to clean and package the application. This command will compile the application, run tests, and package it into a JAR file.
  
5. RUN mvn clean package:
    - This runs the Maven command to clean and package the application. This command will compile the application, run tests, and package it into a JAR file.


##### Stage 2  

```dockerfile
FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar aztegiveaway.jar
ENTRYPOINT ["java", "-jar", "aztegiveaway.jar"]
```

1. FROM openjdk:17-jdk:
   - This specifies the base image for the final stage. It uses the JDK 17 image from OpenJDK.

2. WORKDIR /app:
   - This sets the working directory inside the container to /app.
  
3. COPY --from=build /app/target/*.jar aztegiveaway.jar:
   - This copies the JAR file generated in the build stage (/app/target/*.jar) to the working directory of the final image, renaming it to aztegiveaway.jar.
   - The --from=build part tells Docker to copy the JAR file from the "build" stage.
  
4. ENTRYPOINT ["java", "-jar", "aztegiveaway.jar"]:
   - This sets the command to run when the container starts. It runs the JAR file using the java -jar command.  

#### docker-compose.yaml  

The docker-compose file is used to set up the services: 
  - mySQL database service -> ('mysql-db')  
  - AzteGiveaway Service (the bot) -> (aztegiveaway)

### General Steps to upload to Google Cloud

1. Set Up Google Cloud SDK
   - Make sure you have the Google Cloud SDK installed and configured:  
   ```bash  
    gcloud init  
    ```  
   
2. Create a Google Cloud Artifact Registry:  
   - Create an Artifact Registry repository to store your Docker images  
   - depends on your_project_id, artifact_repository_id  
     
3. Authenticate Docker with Google Cloud
   - Configure Docker to use the gcloud command-line tool to authenticate requests to Artifact Registry
   ```bash  
    gcloud auth configure-docker your-region-docker.pkg.dev  
   ```  
4. Build the Docker Image
   - Build your Docker image using the Dockerfile:
   ```bash  
   docker build -t YOUR_REGION-docker.pkg.dev/YOUR_PROJECT_ID/YOUR_PROJECT_REPO/aztegiveaway:latest .  
   ```

5. Push the Docker Image to Artifact Registry
   - Push the Docker image to your newly created Artifact Registry repository:
   ```bash
   docker push YOUR_REGION-docker.pkg.dev/YOUR_PROJECT_ID/YOUR_PROJECT_REPO/aztegiveaway:latest
   ```

6. Deploy to Google Kubernetes Engine (GKE)
   - If you haven't already, create a GKE cluster: (only once)
   - Can be done from the Google console as well (https://console.cloud.google.com)
     ```bash
     gcloud container clusters create aztegiveaway-cluster --zone us-central1-c --num-nodes=3
     ```

7. Deploy the Application to GKE
   ```bash
   kubectl apply -f secret.yaml
   kubectl apply -f deployment.yaml
   kubectl apply -f service.yaml
   ```






