#JAVA_HOME=/usr/java/jdk1.6.0_06
#export JAVA_HOME
#export PATH=$JAVA_HOME/bin:$PATH
#echo $JAVA_HOME
#echo $PATH
MAXICODE_FILE_PATH=/home/maxicode
export MAXICODE_FILE_PATH
echo $MAXICODE_FILE_PATH
>log.maxicode
cd /home/maxicode
echo "Generating maxicode for " $1
echo "Generating maxicode for " $2
echo "Generating maxicode for " $3
>textfile.txt
chmod 777 textfile.txt
pwd
ls -ltr
echo $3
echo $3 >> textfile.txt
argument=$#
if [ $argument -eq  2 ]; then
        /usr/java/jdk1.6.0_06/bin/java -cp maxicode.jar:commons-codec-1.7.jar:jai-codec-1.1.3.jar:commons-cli-1.2.jar  MaxiCodeGenerator $1 $2
elif [ $argument -eq  3 ]; then
        /usr/java/jdk1.6.0_06/bin/java -Dfile.encoding=ISO-8859-1 -cp maxicode.jar:commons-codec-1.7.jar:jai-codec-1.1.3.jar:commons-cli-1.2.jar  MaxiCodeGenerator $1 $2 $3
else
        echo "No Parameter "
fi
echo "Maxicode Generation Finish"

