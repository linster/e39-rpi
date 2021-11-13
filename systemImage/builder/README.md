# e39 OS builder

Downloads an rPI base image and modifies it to run the e39 base image.

Build: `docker build . -t e39`
Run: `docker run --privileged --volume /dev:/dev --volume $PWD:/output e39`
