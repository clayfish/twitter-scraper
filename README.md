# Twitter Scraper

A scraper to retrieve the conversation in tweets for a particular twitter user.

## Introduction
“Twitter Scraper” (or simply “scraper”) is available under [MIT License](https://github.com/clayfish/twitter-scraper/blob/master/LICENSE). It consists of two steps- Step 1 and Step 2. 
Both these steps are to run one after the other in order. The behaviour is controlled with a properties file, named `application.properties`.

### Step 1

### Step 2

### application.properties

## Prerequisites
Following software are needed to run the built JAR.

1. Oracle JRE 1.8

To build from the source, one needs following pieces of software installed.

1. git client
2. maven 3
3. Oracle JDK 1.8

To install these on an EC2 instance run following commands in order.
``` {bash}
sudo apt-get update
sudo apt-get install git
sudo apt-get install maven
sudo apt-get install python-software-properties
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get install oracle-java8-installer
sudo nano /etc/environment
```
And then append this line at the bottom of the opened file and save it.
`JAVA_HOME=”/usr/lib/jvm/java-8-oracle”`

And then run following command.
``` {bash}
 sudo source /etc/environment
```

Now following command should be working.
``` {bash}
echo $JAVA_HOME
```

And one can check the installed JDK by running following command.
``` {bash}
java -version
```

## Getting the scraper
To get the scraper on EC2 instance (or any other Ubuntu machine), follow the steps below. These steps are needed to run only once.

1. Decide a directory to put the “twitter-scraper” repository. Let’s call this directory `base_directory`. Issue following command to change directory to this base directory. Please replace “base_directory” term with the actual path of this chosen directory.
`cd base_directory`

2. Following steps assume that the commands are being run from the `base_directory`. Issue following command.
`git clone https://github.com/clayfish/twitter-scraper`

3. Now you have clone (source-code) of the scraper on you machine. Notice git command does not need any username/password.

## How to run
It consists of three high-level steps.

1. Update the code
2. Compile the code
3. Run the built JAR file

### Update the code
To update the code with latest changes run following commands.
``` {bash}
cd base_directory/twitter-scraper
git pull
```
### Compile the code
Before compiling the code, please check if `application.properties` is configured as per the needs. Please refer to this section for more information about the configuration. To compile the code run the following commands.
``` {bash}
cd base_directory/twitter-scraper
mvn package
```
These commands will create `base_directory/twitter-scraper/bin/twitter-scraper-0.1.0.one-jar.jar` file.

### Run the built JAR file
To run the built JAR file, you need to run following commands.
``` {bash}
cd base_directory
java -jar twitter-scraper/bin/twitter-scraper-0.1.0.one-jar.jar
```

It will start the scraper and it will start working. To stop the scraper, simply hit `control` + `c` (Mac) or `ctrl` + `c` (Windows/Linux). If you want to exit from the terminal while the scraper still runs, please use following commands instead of the commands written above.
``` {bash}
cd base_directory
java -jar twitter-scraper/bin/twitter-scraper-0.1.0.one-jar.jar &
```

Currently all the logs are directed to terminal hence running it as daemon, doesn’t spare you from the constant logs on the terminal but you can close the terminal without closing the scraper.
