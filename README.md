# AirSpeed5k parallel records
Start and stop multiple recorders in same action with some Avid AirSpeed 5k.

![Screenshot v0.1](https://raw.githubusercontent.com/hdsdi3g/AS5K-Parallel-Control/gh-pages/images/screenshot.jpg)

Actually limited to :
- 2 channels
- with some french and some english GUI
- Java 8 only & JavaFX

## Usage
Copy+Edit conf directory, add it to the classpath and run.

## Depedencies

- commons-io-2.4.jar
- log4j-1.2.17.jar
- slf4j-api-1.7.10.jar
- slf4j-log4j12-1.7.10.jar
- commons-logging-1.2.jar
- dom4j-1.6.1.jar
- commons-configuration2-2.0.jar
- commons-beanutils-1.9.2.jar
- commons-codec-1.10.jar
- commons-lang3-3.4.jar

## Dev
With Eclipse, don't forget to add
```
<accessrules>
  <accessrule kind="accessible" pattern="javafx/**"/>
</accessrules>
```
To your .classpath file.

## Licence
LGPL 3 for code & CC by SA for icons.
