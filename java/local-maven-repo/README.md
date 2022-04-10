# How to add or update a lib in this folder ?

Here the procedure to update the jar of [jogamp](https://jogamp.org/): 
- Download the fat jar and the associate sources zip from https://jogamp.org/deployment/archive/rc/, put them in the root of the Makelangelo-software folder

- Cleanup previous lib: 
```shell
git rm -r java/local-maven-repo/org/jogamp/fat
```

- Create the maven folder:
```shell
./mvnw install:install-file -Dfile=jogamp-fat.jar -Dsources=jogamp-fat-java-src.zip -DgroupId=org.jogamp.fat  -DartifactId=jogamp-fat -Dversion=2.4.0-rc-20210111 -Dpackaging=jar -DcreateChecksum=true -DgeneratePom=true -DlocalRepositoryPath=java/local-maven-repo/
```

- Cleanup the downloaded files:
```shell
rm -f jogamp-fat.jar jogamp-fat-java-src.zip
```
