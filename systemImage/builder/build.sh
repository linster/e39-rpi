cp version_template.conf version.conf
hocon -f version.conf set hmiVersion.gitHash `git log -1 --pretty=format:"%h"`
cp ../../build/compose/jars/e39Rpi-linux-x64-1.0.0.jar .
docker build . -t e39 && \
    docker run --rm --privileged --volume /dev:/dev --volume $PWD:/output e39
