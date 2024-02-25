cp version_template.conf version.conf
hocon -f version.conf set hmiVersion.sdHash `git log -1 --pretty=format:"%h"`
hocon -f version.conf set hmiVersion.hmiHash `git log -1 --pretty=format:"%h"`

export BUILD_PI="true"

cd ../../
./gradlew clean
./gradlew packageUberJarForCurrentOs
#Make the JAR Runnable from the cli.
zip -d `ls build/compose/jars/*.jar` 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

cd -
unset BUILD_PI

cp ../../build/compose/jars/e39Rpi-linux-x64-1.0.0.jar .
docker build . -t e39 && \
    docker run --rm --privileged --volume /dev:/dev --volume $PWD:/output e39
