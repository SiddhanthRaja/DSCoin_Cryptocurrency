# javac DSCoinPackage/*.java HelperClasses/*.java DSCoinPackage/DriverCode.java
javac DSCoinPackage/*.java HelperClasses/*.java
java DSCoinPackage/DriverCode > output.txt
if !(diff -w -q output.txt outputhonest.txt)
    then
        echo "Error in DSCoin_Honest"
        exit
    fi
echo "DSCoin_Honest is correct"

java DSCoinPackage/DriverCodeMalicious > output.txt
if !(diff -w -q output.txt outputmalicious.txt)
    then
        echo "Error in DSCoin_Malicious"
        exit
    fi
echo "DSCoin_Malicious is correct"
rm output.txt