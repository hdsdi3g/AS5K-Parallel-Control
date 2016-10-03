# AirSpeed5k parallel records
Start and stop multiple recorders in same action with some Avid AirSpeed 5k. It use the socket API: it don't need a special cable, just a simple network link.

![Screenshot v0.1](https://raw.githubusercontent.com/hdsdi3g/AS5K-Parallel-Control/gh-pages/images/screenshot.jpg)

Actually limited:
- with some french and some english GUI
- Java 8 only & JavaFX

3 modes :
- 2 recorders (main + backup), ID backup will be the main ID + 1.
- 1 recorder, the same fonctions (CUE/REC/STOP) with only one channel. Can be used with 2 recorder mode in another instance.
- 3 recorders (A + B + C)

## Usage
Copy+Edit conf directory, add it to the classpath with the dependencies and run.

Add 1, 2 or 3 in param for start with mode 1, 2 or 3. 2 is default.

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
