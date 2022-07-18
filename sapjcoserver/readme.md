# Integrate Sap JCOServer with Springboot and Apache Camel 

### The usecase

SAP ABAP RFC call -> SpringBoot+ApacheCamel -> FileSystem

I wrote a  springboot+Apachecamel Application to demonstrate how to receive the ABAP function's result and store it at file system.

### prepare  sap jco connector libs

copy jco jars, .dll (for windows), and .so(for linux) files to lib directory as below shown

![image-jco lib](readme.assets/sapjco.png)
 

### run apache camel springboot application 

For linux
```
export ASHOST=<jco client ashost>
export GWHOST=<jco server gwhost>
```
For windows
```
set ASHOST=<jco client ashost>
set GWHOST=<jco server gwhost>
```
run test
```
mvn -Dtest=sample.camel.SampleJCOServerApplicationTests   test
```
run program
```
mvn spring-boot:run 
```
![image-1](readme.assets/Picture1.png)

 
### execute ABAP function in SAP System
```
Execute Tcode se37 -> STFC_CONNECTION (SAP default Test Function) 
```
![image-2](readme.assets/Picture2.png)

put RFC Target System (it is ...not... as same as progid configured in application.yaml file) and input parameter
![image-3](readme.assets/Picture3.png)

### check result and output 

You will see ABAP function's result at console 
![image-4](readme.assets/Picture4.png)

The result of ABAP function is also stored as file by camel in your local computer where your java program runs 
![image-5](readme.assets/Picture5.png)