# AzteGiveaway Bot  

AzteGiveaway Bot is a Discord bot for creating and managing giveaways in Discord servers. It allows administrators to create giveaways, specify the duration, number of winners, and optionally, the   announcement channel. Users can enter giveaways by reacting to the giveaway message with a specified emoji.  

## Features  

- Create giveaways with a unique title, duration, and number of winners.  
- Optionally specify an announcement channel for the giveaway.  
- Users can enter giveaways by reacting with a specified emoji.  
- Each user can enter a giveaway only once.  
- Automatically selects winners when the giveaway ends.  
- Stores giveaway entries and winners.  

## Prerequisites  

- Java Development Kit (JDK) 8 or later  
- Maven  
- A Discord bot token  
- A PostgreSQL database (optional for storing winners, entries and giveaway history)  

## Setup  

### 1. Clone the Repository  

```bash  
git clone https://github.com/your-username/AzteGiveaway.git  
cd AzteGiveaway  
```  

### 2. Cnnfigure the bot:  

Create a config.properties file in the src/main/resources directory with the following content:  
```config
bot.token=YOUR_DISCORD_BOT_TOKEN
```

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
```plaintext
!giveaway create <title> <duration> <number_of_winners> [<channel_id>] 
```  

title: The unique title for the giveaway;  
duration: The duration of the giveaway (e.g., 1h for 1 hour, 30m for 30 minutes, 2d for 2 days) -> use m for minutes, h for hours and d for days;  
number_of_winners: The number of winners for the giveaway;  
channel_id (optional): The ID of the channel where the giveaway will be announced. If not provided, the current channel will be used.  


### Example:  
```plaintext
!giveaway create "Awesome Giveaway" 1h 3 
```
Function above will create a giveaway entitled "Awesome Giveaway" that will choose 3 winners in one hour from creation;  
In case the number of entries is smaller or equal to the number of winners, then those users will win.  
If no users entry the giveaway, a specific message will be sent ("The giveaway has ended, but there were no entries")  

### Reaction Emoji  

Users can enter the giveaway by reacting with the 🎉 emoji to the giveaway message.  

### Actions and errors are logged using SLF4J logger.   









