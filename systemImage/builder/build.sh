docker build . -t e39 && \
    docker run --rm --privileged --volume /dev:/dev --volume $PWD:/output e39
